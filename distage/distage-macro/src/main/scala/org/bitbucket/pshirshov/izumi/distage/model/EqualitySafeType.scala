package org.bitbucket.pshirshov.izumi.distage.model

import org.bitbucket.pshirshov.izumi.distage.Tag
import org.bitbucket.pshirshov.izumi.distage.TypeNative

import scala.reflect.runtime.universe.typeTag

case class EqualitySafeType(tpe: TypeNative) {

  override def toString: String = tpe.toString

  override def hashCode(): Int = tpe.toString.hashCode

  override def equals(obj: scala.Any): Boolean = obj match {
    case EqualitySafeType(otherSymbol) =>
      tpe =:= otherSymbol
    case _ =>
      false
  }
}

object EqualitySafeType {
  def get[T:Tag] = EqualitySafeType(typeTag[T].tpe)
}