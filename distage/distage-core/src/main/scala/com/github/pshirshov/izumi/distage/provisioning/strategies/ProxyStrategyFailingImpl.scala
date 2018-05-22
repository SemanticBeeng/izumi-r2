package com.github.pshirshov.izumi.distage.provisioning.strategies

import com.github.pshirshov.izumi.distage.model.exceptions.DIException
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.ProxyOp
import com.github.pshirshov.izumi.distage.model.provisioning.strategies.ProxyStrategy
import com.github.pshirshov.izumi.distage.model.provisioning.{OpResult, OperationExecutor, ProvisioningKeyProvider}
import com.github.pshirshov.izumi.fundamentals.platform.language.Quirks

class ProxyStrategyFailingImpl extends ProxyStrategy {
  override def initProxy(context: ProvisioningKeyProvider, executor: OperationExecutor, initProxy: ProxyOp.InitProxy): Seq[OpResult] = {
    Quirks.discard(context, executor)
    throw new DIException(s"ProxyStrategyFailingImpl does not support proxies, failed op: $initProxy", null)
  }

  override def makeProxy(context: ProvisioningKeyProvider, makeProxy: ProxyOp.MakeProxy): Seq[OpResult] = {
    Quirks.discard(context)
    throw new DIException(s"ProxyStrategyFailingImpl does not support proxies, failed op: $makeProxy", null)
  }
}
