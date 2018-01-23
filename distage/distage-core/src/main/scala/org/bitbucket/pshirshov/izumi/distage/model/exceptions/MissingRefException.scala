package org.bitbucket.pshirshov.izumi.distage.model.exceptions

import org.bitbucket.pshirshov.izumi.distage.model.DIKey
import org.bitbucket.pshirshov.izumi.distage.model.plan.RefTable

class MissingRefException(message: String, val missing: Set[DIKey], val reftable: Option[RefTable]) extends DIException(message, null)