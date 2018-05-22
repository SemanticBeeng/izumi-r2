package com.github.pshirshov.izumi.distage.model.provisioning.strategies

import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.ImportDependency
import com.github.pshirshov.izumi.distage.model.provisioning.{OpResult, ProvisioningKeyProvider}

trait ImportStrategy {
  def importDependency(context: ProvisioningKeyProvider, op: ImportDependency): Seq[OpResult]

}
