package com.github.pshirshov.izumi.distage.roles.impl

import java.io.File

import com.github.pshirshov.izumi.distage.roles.launcher.ConfigWriter.WriteReference
import com.github.pshirshov.izumi.distage.roles.launcher.RoleArgs
import com.github.pshirshov.izumi.logstage.api.{IzLogger, Log}
import scopt.OptionParser

case class ScoptLauncherArgs(
                           configFile: Option[File] = None
                           , writeReference: Option[WriteReference] = None
                           , dummyStorage: Option[Boolean] = Some(false)
                           , rootLogLevel: Log.Level = Log.Level.Info
                           , jsonLogging: Option[Boolean] = Some(false)
                           , dumpContext: Option[Boolean] = Some(false)
                           , roles: List[RoleArgs] = List.empty
                         )

// TODO: this stuff needs to be refactored, we can't keep WriteReference here
object ScoptLauncherArgs {

  lazy val parser: OptionParser[ScoptLauncherArgs] = new OptionParser[ScoptLauncherArgs]("izumi-launcher") {
     head("tg-launcher", "TODO: manifest version")
     help("help")

     opt[Unit]("logs-json").abbr("lj")
       .text("turn on json logging")
       .action { (_, c) =>
         c.copy(jsonLogging = Some(true))
       }

    opt[Unit]("dump-graph").abbr("dg")
      .text("dump object graph")
      .action { (_, c) =>
        c.copy(dumpContext = Some(true))
      }

     opt[Unit]("dummy-storage").abbr("ds")
       .text("use in-memory dummy storages instead of production ones")
       .action { (_, c) =>
         c.copy(dummyStorage = Some(true))
       }

     opt[Log.Level]("root-log-level").abbr("rll")
       .text("Root logging level")
       .valueName("log level")
       .action { (v, c) =>
         c.copy(rootLogLevel = v)
       }

     opt[File]('c', "config")
       .text("configuration file")
       .valueName("<common config file>")
       .action {
         case (x, c) =>
           c.copy(configFile = Some(x))
       }

     opt[Unit]("write-reference").abbr("wr").action((_, c) =>
       c.copy(writeReference = Some(WriteReference()))).text("dump reference config in HOCON format")
       .children(
         opt[Unit]("json").abbr("js").action((_, c) =>
           c.copy(writeReference = Some(WriteReference(asJson = true)))
         ).text("dump reference config in json format"),
         opt[Boolean]("include-common").abbr("ic").action((b, c) =>
           c.copy(writeReference = Some(WriteReference(includeCommon = b)))
         ).text("include common part in role configs"),
         opt[Boolean]("use-launcher-version").abbr("lv").action((b, c) =>
           c.copy(writeReference = Some(WriteReference(useLauncherVersion = b)))
         ).text("use launcher version instead of role version"),
         opt[String]("targetDir").abbr("d").action((dir, c) =>
           c.copy(writeReference = Some(WriteReference(targetDir = dir)))
         ).text("folder to store reference configs, ./config by default")
       )

     arg[String]("<role>...").unbounded().optional()
       .text("roles to enable")
       .action {
         (a, c) =>
           c.copy(roles = c.roles :+ RoleArgs(a, None))
       }
       .children(
         opt[String]("role-id").abbr("i")
           .text("role id to enable (legacy option, just put role name as argument)")
           .action { case (a, c) =>
             c.copy(roles = c.roles.init :+ c.roles.last.copy(name = a))
           },

         opt[File]("role-config").abbr("rc").optional()
           .text("config file for role, it will override the common config file")
           .action { case (f, c) =>
             c.copy(roles = c.roles.init :+ c.roles.last.copy(configFile = Some(f)))
           },
       )
   }

  implicit lazy val logLevelRead: scopt.Read[Log.Level] = scopt.Read.reads[Log.Level] {
    v =>
      IzLogger.parseLevel(v)
  }
}


