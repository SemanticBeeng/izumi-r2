package com.github.pshirshov.izumi.distage.roles.launcher

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.github.pshirshov.izumi.distage.app.DIAppStartupContext
import com.github.pshirshov.izumi.distage.config.ResolvedConfig
import com.github.pshirshov.izumi.distage.model.definition.Id
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.WiringOp
import com.github.pshirshov.izumi.distage.roles._
import com.github.pshirshov.izumi.distage.roles.launcher.ConfigWriter.WriteReference
import com.github.pshirshov.izumi.fundamentals.platform.language.Quirks
import com.github.pshirshov.izumi.fundamentals.platform.resources.{ArtifactVersion, IzManifest}
import com.github.pshirshov.izumi.logstage.api.IzLogger
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}

import scala.reflect.ClassTag
import scala.util._


object ConfigWriter extends RoleDescriptor {

  case class WriteReference(
                             asJson: Boolean = false,
                             targetDir: String = "config",
                             includeCommon: Boolean = true,
                             useLauncherVersion: Boolean = true,
                           )

  override final val id = "configwriter"
}

case class ConfigurableComponent(componentId: String, version: Option[ArtifactVersion], parent: Option[Config] = None)

@RoleId(ConfigWriter.id)
abstract class AbstractConfigWriter[LAUNCHER: ClassTag]
(
  logger: IzLogger,
  launcherVersion: ArtifactVersion@Id("launcher-version"),
  roleInfo: RolesInfo,
  config: WriteReference,
  context: DIAppStartupContext,
)
  extends RoleService
    with RoleTask {

  override def start(): Unit = {
    writeReferenceConfig()
  }

  def writeReferenceConfig(): Unit = {
    val configPath = Paths.get(config.targetDir).toFile
    logger.info(s"Config ${configPath.getAbsolutePath -> "directory to use"}...")

    if (!configPath.exists())
      configPath.mkdir()

    val maybeVersion = IzManifest.manifest[LAUNCHER]().map(IzManifest.read).map(_.version)
    logger.info(s"Going to process ${roleInfo.availableRoleBindings.size -> "roles"}")

    val commonComponent = ConfigurableComponent("common", maybeVersion)
    val commonConfig = buildConfig(ConfigurableComponent("common", maybeVersion))
    if (!config.includeCommon) {
      writeConfig(commonComponent, None, commonConfig)
    }

    Quirks.discard(for {
      binding <- roleInfo.availableRoleBindings
      component = ConfigurableComponent(binding.name, binding.source.map(_.version))
      cfg = buildConfig(component.copy(parent = Some(commonConfig)))
    } yield {
      val version = if (config.useLauncherVersion) {
        Some(ArtifactVersion(launcherVersion.version))
      } else {
        component.version
      }
      val versionedComponent = component.copy(version = version)

      writeConfig(versionedComponent, None, cfg)

      minimizeConfig(binding)
        .foreach {
          cfg =>
            writeConfig(versionedComponent, Some("minimized"), cfg)
        }
    })
  }

  private def minimizeConfig(binding: RoleBinding): Option[Config] = {
    val bindingKey = binding.binding.key
    val replanned = context.startupContext.startup(Array("--root-log-level", "crit", binding.name))
    val newPlan = replanned.plan

    if (newPlan.steps.exists(_.target == bindingKey)) {
      newPlan
        .filter[ResolvedConfig]
        .collect {
          case op: WiringOp.ReferenceInstance =>
            op.wiring.instance
        }
        .collectFirst {
          case r: ResolvedConfig =>
            r.minimized()
        }
    } else {
      logger.warn(s"$bindingKey is not in the refined plan")
      None
    }
  }

  private def buildConfig(cmp: ConfigurableComponent): Config = {
    val referenceConfig = s"${cmp.componentId}-reference.conf"
    logger.info(s"[${cmp.componentId}] Resolving $referenceConfig... with ${config.includeCommon -> "shared sections"}")

    val reference = (if (config.includeCommon) {
      cmp.parent
        .map(ConfigFactory.parseResourcesAnySyntax(referenceConfig).withFallback)
        .getOrElse(ConfigFactory.parseResourcesAnySyntax(referenceConfig))
    } else {
      ConfigFactory.parseResourcesAnySyntax(referenceConfig)
    }).resolve()

    if (reference.isEmpty) {
      logger.warn(s"[${cmp.componentId}] Reference config is empty. Sounds stupid.")
    }

    val resolved = ConfigFactory.defaultOverrides()
      .withFallback(reference)
      .resolve()

    val filtered = cleanupEffectiveAppConfig(resolved, reference)
    filtered.checkValid(reference)
    filtered
  }

  private def writeConfig(cmp: ConfigurableComponent, suffix: Option[String], typesafeConfig: Config) = {
    val fileName = referenceFileName(cmp.componentId, cmp.version, config.asJson, suffix)
    val target = Paths.get(config.targetDir, fileName)

    Try {
      val cfg = render(typesafeConfig, config.asJson)
      val bytes = cfg.getBytes(StandardCharsets.UTF_8)
      Files.write(target, bytes)
      logger.info(s"[${cmp.componentId}] Reference config saved -> $target (${bytes.size} bytes)")
    }.recover {
      case e: Throwable => logger.error(s"[${cmp.componentId -> "component id" -> null}] Can't write reference config to $target, ${e.getMessage -> "message"}")
    }
  }

  // TODO: sdk?
  private final def cleanupEffectiveAppConfig(effectiveAppConfig: Config, reference: Config): Config = {
    import scala.collection.JavaConverters._

    ConfigFactory.parseMap(effectiveAppConfig.root().unwrapped().asScala.filterKeys(reference.hasPath).asJava)
  }

  private def render(cfg: Config, asJson: Boolean) = {
    cfg.root().render(configRenderOptions.setJson(asJson))
  }

  private def referenceFileName(service: String, version: Option[ArtifactVersion], asJson: Boolean, suffix: Option[String]): String = {
    val extension = if (asJson) {
      "json"
    } else {
      "conf"
    }

    val vstr = version.map(_.version).getOrElse("0.0.0-UNKNOWN")
    suffix match {
      case Some(value) =>
        s"$service-$value-$vstr.$extension"
      case None =>
        s"$service-$vstr.$extension"

    }
  }

  private val configRenderOptions = ConfigRenderOptions.defaults.setOriginComments(false).setComments(false)
}
