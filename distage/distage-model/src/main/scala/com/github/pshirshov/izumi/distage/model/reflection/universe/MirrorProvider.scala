package com.github.pshirshov.izumi.distage.model.reflection.universe

import com.github.pshirshov.izumi.distage.model
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse._

trait MirrorProvider {
  def runtimeClass(tpe: SafeType): Class[_]
  def runtimeClass(tpe: TypeNative): Class[_]
  def mirror: u.Mirror
}

object MirrorProvider {
  object Impl extends MirrorProvider {

    override val mirror: model.reflection.universe.RuntimeDIUniverse.u.Mirror = scala.reflect.runtime.currentMirror

    override def runtimeClass(tpe: SafeType): Class[_] = {
      runtimeClass(tpe.tpe)
    }

    override def runtimeClass(tpe: TypeNative): Class[_] = {
      mirror.runtimeClass(tpe.erasure)
    }
  }

}
