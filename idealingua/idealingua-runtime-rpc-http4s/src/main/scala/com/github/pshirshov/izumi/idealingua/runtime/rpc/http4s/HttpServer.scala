package com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s

import java.time.ZonedDateTime
import java.util.concurrent.RejectedExecutionException

import _root_.io.circe.parser._
import cats.implicits._
import com.github.pshirshov.izumi.functional.bio.BIO._
import com.github.pshirshov.izumi.functional.bio.BIOExit
import com.github.pshirshov.izumi.functional.bio.BIOExit.{Error, Success, Termination}
import com.github.pshirshov.izumi.fundamentals.platform.language.Quirks
import com.github.pshirshov.izumi.fundamentals.platform.time.IzTime
import com.github.pshirshov.izumi.idealingua.runtime.rpc
import com.github.pshirshov.izumi.idealingua.runtime.rpc.{IRTClientMultiplexor, RPCPacketKind, _}
import com.github.pshirshov.izumi.logstage.api.IzLogger
import io.circe
import io.circe.{Json, Printer}
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Binary, Close, Pong, Text}

class HttpServer[C <: Http4sContext](val c: C#IMPL[C]
                                     , val muxer: IRTServerMultiplexor[C#BiIO, C#RequestContext]
                                     , val codec: IRTClientMultiplexor[C#BiIO]
                                     , val contextProvider: AuthMiddleware[C#CatsIO, C#RequestContext]
                                     , val wsContextProvider: WsContextProvider[C#BiIO, C#RequestContext, C#ClientId]
                                     , val wsSessionStorage: WsSessionsStorage[C#BiIO, C#ClientId, C#RequestContext]
                                     , val listeners: Seq[WsSessionListener[C#ClientId]]
                                     , logger: IzLogger
                                     , printer: Printer
                                    ) {

  import c._

  protected val dsl: Http4sDsl[CatsIO] = c.dsl

  import dsl._

  protected def loggingMiddle(service: HttpRoutes[CatsIO]): HttpRoutes[CatsIO] = cats.data.Kleisli {
    req: Request[CatsIO] =>
      logger.trace(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: initiated")

      try {
        service(req).map {
          case Status.Successful(resp) =>
            logger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: success, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            resp
          case resp if resp.attributes.get(org.http4s.server.websocket.websocketKey[CatsIO]).isDefined =>
            logger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: websocket request")
            resp
          case resp =>
            logger.info(s"${req.method.name -> "method"} ${req.pathInfo -> "uri"}: rejection, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            resp
        }
      } catch {
        case cause: Throwable =>
          logger.error(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: failure, $cause")
          throw cause
      }
  }

  def service: HttpRoutes[CatsIO] = {
    val svc = AuthedService(handler())
    val aservice: HttpRoutes[CatsIO] = contextProvider(svc)
    loggingMiddle(aservice)
  }

  protected def handler(): PartialFunction[AuthedRequest[CatsIO, RequestContext], CatsIO[Response[CatsIO]]] = {
    case request@GET -> Root / "ws" as ctx =>
      val result = setupWs(request, ctx)
      result

    case request@GET -> Root / service / method as ctx =>
      val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
      run(new HttpRequestContext(request, ctx), body = "{}", methodId)

    case request@POST -> Root / service / method as ctx =>
      val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
      request.req.decode[String] {
        body =>
          run(new HttpRequestContext(request, ctx), body, methodId)
      }
  }

  protected def handleWsClose(context: WebsocketClientContext[C#BiIO, C#ClientId, C#RequestContext]): Unit = {
    logger.debug(s"${context -> null}: Websocket client disconnected")
    context.finish()
  }

  protected def onWsOpened(): Unit = {
  }

  protected def onWsUpdate(maybeNewId: Option[C#ClientId], old: WsClientId[ClientId]): Unit = {
  }

  protected def onWsClosed(): Unit = {
  }

  protected def setupWs(request: AuthedRequest[CatsIO, RequestContext], initialContext: RequestContext): CatsIO[Response[CatsIO]] = {
    val context = new WebsocketClientContextImpl[C](c, request, initialContext, listeners, wsSessionStorage, logger) {

      override def onWsSessionOpened(): Unit = {
          onWsOpened()
          super.onWsSessionOpened()
      }

      override def onWsClientIdUpdate(maybeNewId: Option[C#ClientId], oldId: WsClientId[C#ClientId]): Unit = {
        onWsUpdate(maybeNewId, oldId)
        super.onWsClientIdUpdate(maybeNewId, oldId)
      }

      override def onWsSessionClosed(): Unit = {
        onWsClosed()
        super.onWsSessionClosed()
      }
    }
    context.start()
    logger.debug(s"${context -> null}: Websocket client connected")

    context.queue.flatMap { q =>
      val d = q.dequeue.through {
        stream =>
          stream
            .evalMap(handleWsMessage(context))
            .collect({ case Some(v) => WebSocketFrame.Text(v) })
      }
      val e = q.enqueue
      WebSocketBuilder[CatsIO].build(
        d.merge(context.outStream).merge(context.pingStream)
        , e
        , onClose = CIO.delay(handleWsClose(context)))
    }
  }

  protected def handleWsMessage(context: WebsocketClientContextImpl[C], requestTime: ZonedDateTime = IzTime.utcNow): WebSocketFrame => CatsIO[Option[String]] = {
    case Text(msg, _) =>
      val ioresponse = makeResponse(context, msg)
      CIO.async {
        cb =>
          BIORunner.unsafeRunAsyncAsEither(ioresponse) {
            result =>
              cb(Right(handleResult(context, result)))
          }
      }

    case Close(_) =>
      CIO.point(None)

    case v: Binary =>
      CIO.point(Some(handleWsError(context, List.empty, Some(v.toString.take(100) + "..."), "badframe")))

    case _: Pong =>
      onHeartbeat(requestTime).map(_ => None)
  }

  def onHeartbeat(requestTime: ZonedDateTime): C#CatsIO[Unit] = CIO.point {
    Quirks.discard(requestTime)
  }

  protected def handleResult(context: WebsocketClientContextImpl[C], result: BIOExit[Throwable, Option[RpcPacket]]): Option[String] = {
    result match {
      case Success(v) =>
        v.map(_.asJson).map(printer.pretty)

      case Error(error) =>
        Some(handleWsError(context, List(error), None, "failure"))

      case Termination(cause, _) =>
        Some(handleWsError(context, List(cause), None, "termination"))
    }
  }

  protected def makeResponse(context: WebsocketClientContextImpl[C], message: String): BiIO[Throwable, Option[RpcPacket]] = {
    for {
      parsed <- BIO.fromEither(parse(message))
      unmarshalled <- BIO.fromEither(parsed.as[RpcPacket])
      id <- BIO.syncThrowable(wsContextProvider.toId(context.initialContext, context.id, unmarshalled))
      _ <- BIO.syncThrowable(context.updateId(id))
      response <- respond(context, unmarshalled).sandboxWith {
        _.redeem(
          {
            case Left(exception :: otherIssues) =>
              logger.error(s"${context -> null}: WS processing terminated, $message, $exception, $otherIssues")
              BIO.point(Some(rpc.RpcPacket.rpcFail(unmarshalled.id, exception.getMessage)))
            case Left(Nil) =>
              BIO.terminate(new IllegalStateException())
            case Right(exception) =>
              logger.error(s"${context -> null}: WS processing failed, $message, $exception")
              BIO.point(Some(rpc.RpcPacket.rpcFail(unmarshalled.id, exception.getMessage)))
          }, {
            succ => BIO.point(succ)
          }
        )
      }
    } yield {
      response
    }
  }

  protected def respond(context: WebsocketClientContextImpl[C], input: RpcPacket): BiIO[Throwable, Option[RpcPacket]] = {
    input match {
      case RpcPacket(RPCPacketKind.RpcRequest, None, _, _, _, _, _) =>
        val (newId, response) = wsContextProvider.handleEmptyBodyPacket(context.id, context.initialContext, input)
        BIO.syncThrowable(context.updateId(newId)) *> response

      case RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
        val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
        for {
          userCtx <- BIO.syncThrowable(wsContextProvider.toContext(context.id, context.initialContext, input))
          _ <- BIO.point(logger.debug(s"${context -> null}: $id, $userCtx"))
          result <- muxer.doInvoke(data, userCtx, methodId)
          asBio <- result match {
            case None =>
              BIO.fail(new IRTMissingHandlerException(s"${context -> null}: No rpc handler for $methodId", input))
            case Some(resp) =>
              BIO.point(Some(rpc.RpcPacket.rpcResponse(id, resp)))
          }
        } yield {
          asBio
        }

      case RpcPacket(RPCPacketKind.BuzzResponse, Some(data), _, id, _, _, _) =>
        context.requestState.handleResponse(id, data).as(None)

      case RpcPacket(RPCPacketKind.BuzzFailure, Some(data), _, Some(id), _, _, _) =>
        context.requestState.respond(id, RawResponse.BadRawResponse())
        BIO.fail(new IRTGenericFailure(s"Buzzer has returned failure: $data"))

      case k =>
        BIO.fail(new IRTMissingHandlerException(s"Can't handle $k", k))
    }
  }

  protected def handleWsError(context: WebsocketClientContextImpl[C], causes: List[Throwable], data: Option[String], kind: String): String = {
    causes.headOption match {
      case Some(cause) =>
        logger.error(s"${context -> null}: WS Execution failed, $kind, $data, $cause")
        printer.pretty(rpc.RpcPacket.rpcCritical(data.getOrElse(cause.getMessage), kind).asJson)

      case None =>
        logger.error(s"${context -> null}: WS Execution failed, $kind, $data")
        printer.pretty(rpc.RpcPacket.rpcCritical("?", kind).asJson)
    }
  }


  protected def run(context: HttpRequestContext[CatsIO, RequestContext], body: String, method: IRTMethodId): CatsIO[Response[CatsIO]] = {
    val ioR = for {
      parsed <- BIO.fromEither(parse(body))
      maybeResult <- muxer.doInvoke(parsed, context.context, method)
    } yield {
      maybeResult
    }

    CIO.async[CatsIO[Response[CatsIO]]] {
      cb =>
        BIORunner.unsafeRunAsyncAsEither(ioR) {
          result =>
            cb(Right(handleResult(context, method, result)))
        }
    }
      .flatten
  }

  private def handleResult(context: HttpRequestContext[CatsIO, RequestContext], method: IRTMethodId, result: BIOExit[Throwable, Option[Json]]): CatsIO[Response[CatsIO]] = {
    result match {
      case Success(v) =>
        v match {
          case Some(value) =>
            dsl.Ok(printer.pretty(value))
          case None =>
            logger.warn(s"${context -> null}: No service handler for $method")
            dsl.NotFound()
        }

      case Error(error: circe.Error) =>
        logger.info(s"${context -> null}: Parsing failure while handling $method: $error")
        dsl.BadRequest()

      case Error(error: IRTDecodingException) =>
        logger.info(s"${context -> null}: Parsing failure while handling $method: $error")
        dsl.BadRequest()

      case Error(error) =>
        logger.info(s"${context -> null}: Unexpected failure while handling $method: $error")
        dsl.InternalServerError()

      case Termination(_, (cause: IRTHttpFailureException) :: _) =>
        logger.debug(s"${context -> null}: Request rejected, $method, ${context.request}, $cause")
        CIO.pure(Response(status = cause.status))

      case Termination(_, (cause: RejectedExecutionException) :: _) =>
        logger.warn(s"${context -> null}: Not enough capacity to handle $method: $cause")
        dsl.TooManyRequests()

      case Termination(cause, _) =>
        logger.error(s"${context -> null}: Execution failed, termination, $method, ${context.request}, $cause")
        dsl.InternalServerError()
    }
  }

}
