package com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s.fixtures

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Timer}
import com.github.pshirshov.izumi.functional.bio.BIORunner
import com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s.Http4sRuntime
import com.github.pshirshov.izumi.logstage.api.routing.{ConfigurableLogRouter, StaticLogRouter}
import com.github.pshirshov.izumi.logstage.api.{IzLogger, Log}
import io.circe.Printer
import scalaz.zio

import scala.concurrent.ExecutionContext.Implicits.global

object RT {
  final val logger = makeLogger()
  final val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)
  final val handler = BIORunner.DefaultHandler.Custom(message => zio.IO.sync(logger.warn(s"Fiber failed: $message")))

  implicit val contextShift: ContextShift[cats.effect.IO] = IO.contextShift(global)
  implicit val timer: Timer[cats.effect.IO] = IO.timer(global)
  implicit val BIOR: BIORunner[zio.IO] = BIORunner.createZIO(Executors.newWorkStealingPool(), handler)


  final val rt = new Http4sRuntime[zio.IO, cats.effect.IO, DummyRequestContext, String, Unit](global)

  private def makeLogger(): IzLogger = {
    val router = ConfigurableLogRouter(Log.Level.Info, levels = Map(
      "org.http4s" -> Log.Level.Warn
      , "org.http4s.server.blaze" -> Log.Level.Error
      , "org.http4s.blaze.channel.nio1" -> Log.Level.Crit
      , "com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s" -> Log.Level.Trace
    ))

    val out = IzLogger(router)
    StaticLogRouter.instance.setup(router)
    out
  }

}
