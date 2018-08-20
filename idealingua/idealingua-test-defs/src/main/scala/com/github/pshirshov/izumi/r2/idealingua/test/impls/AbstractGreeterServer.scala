package com.github.pshirshov.izumi.r2.idealingua.test.impls

import cats.MonadError
import cats.data.EitherT
import com.github.pshirshov.izumi.idealingua.runtime.rpc._
import com.github.pshirshov.izumi.r2.idealingua.test.generated._
import scalaz.zio.IO
import IRTResultTransZio._

import scala.language.higherKinds

abstract class AbstractGreeterServer1[R[_, _] : IRTResult, C]
  extends GreeterServiceServer[R, C] {

  override val R: IRTResult[R] = implicitly[IRTResult[R]]

  import R._

  override def greet(ctx: C, name: String, surname: String): Just[String] = just {
    s"Hi, $name $surname!"
  }

  override def sayhi(ctx: C): Just[String] = just {
    "Hi!"
  }

  override def alternative(ctx: C): Or[Long, String] = choice {
    Right("value")
  }

  override def nothing(ctx: C): Or[Nothing, String] = just {
    ""
  }
}

object AbstractGreeterServer1 {

  class Impl[R[_, _] : IRTResult, C] extends AbstractGreeterServer1[R, C]

}

//trait AbstractGreeterServer2[R[_, _], C]
//  extends GreeterServiceServer[R, C] {
//  import R._
//
//  protected implicit def ME[E]: MonadError[Or[E, ?], E]
//
//  override def greet(ctx: C, name: String, surname: String): Just[String] = just {
//    s"Hi, $name $surname!"
//  }
//
//  override def sayhi(ctx: C): Just[String] = just {
//    "Hi!"
//  }
//
//  override def alternative(ctx: C): Or[Long, String] = choice {
//    /*
//    ME[Long].raiseError(45)
//    ME[Long].pure("test")
//    */
//    Right("value")
//  }
//
//  override def nothing(ctx: C): Or[Nothing, String] = just {
//    ""
//  }
//}
//
//object AbstractGreeterServer2 {
//
//  class Impl[C] extends AbstractGreeterServer2[EitherT[cats.effect.IO, ?, ?], C] {
//
//    override def stop[V](v: => Throwable): Just[V] = ME[Nothing].point(throw v)
//
//    protected def ME[E]: MonadError[Or[E, ?], E] = implicitly
//
//    def choice[E, V](v: => Either[E, V]): Or[E, V] = v match {
//      case Right(r) =>
//        ME[E].pure(r)
//
//      case Left(l) =>
//        ME[E].raiseError(l)
//    }
//
//    def just[V](v: => V): Just[V] = ME[Nothing].pure(v)
//  }
//
//}
