package com.github.pshirshov.izumi.logstage.api

import com.github.pshirshov.izumi.logstage.api.Log.{CustomContext, Entry}
import com.github.pshirshov.izumi.logstage.api.logger.LogRouter

/** Logger that forwards entries to [[LogRouter]] */
trait RoutingLogger extends AbstractLogger {

  protected def router: LogRouter
  protected def customContext: CustomContext

  @inline override final def acceptable(loggerId: Log.LoggerId, logLevel: Log.Level): Boolean = {
    router.acceptable(loggerId, logLevel)
  }

  /** Log irrespective of minimum log level */
  @inline override final def unsafeLog(entry: Log.Entry): Unit = {
    val entryWithCtx = addCustomCtx(entry, customContext)
    router.log(entryWithCtx)
  }

  @inline private[this] def addCustomCtx(entry: Log.Entry, ctx: CustomContext): Entry = {
    if (ctx.values.isEmpty) {
      entry
    } else {
      entry.copy(context = entry.context.copy(customContext = entry.context.customContext + ctx))
    }
  }

}
