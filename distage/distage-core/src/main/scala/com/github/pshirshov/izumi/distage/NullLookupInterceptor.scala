package com.github.pshirshov.izumi.distage

import com.github.pshirshov.izumi.distage.model.DIKey
import com.github.pshirshov.izumi.fundamentals.reflection._

class NullLookupInterceptor extends LookupInterceptor {
  def interceptLookup[T:Tag](key: DIKey, context: Locator): Option[TypedRef[T]] = None
}

object NullLookupInterceptor {
  final val instance = new NullLookupInterceptor()
}