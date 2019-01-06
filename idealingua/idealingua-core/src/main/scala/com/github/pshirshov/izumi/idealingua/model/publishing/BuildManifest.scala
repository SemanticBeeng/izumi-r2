package com.github.pshirshov.izumi.idealingua.model.publishing

import com.github.pshirshov.izumi.idealingua.model.publishing.BuildManifest._

trait BuildManifest {
  def common: Common

  def dependencies: List[ManifestDependency]
}

case class ProjectVersion(version: String, release: Boolean, buildId: String)

object ProjectVersion {
  def default = ProjectVersion("0.0.1", release = false, "UNKNOWNBUILD")
}

object BuildManifest {

  case class Common(
                     name: String,
                     group: String,
                     tags: List[String],
                     description: String,
                     releaseNotes: String,
                     publisher: Publisher,
                     version: ProjectVersion,
                     licenses: List[License],
                     website: MFUrl,
                     copyright: String,
                     izumiVersion: String,
                   )

  object Common {
    final val default = Common(
      name = "idealingua-project",
      group = "com.mycompany.generated",
      tags = List.empty,
      description = "Generated by Izumi IDL Compiler",
      releaseNotes = "",
      publisher = Publisher("MyCompany", "com.my.company"),
      version = ProjectVersion.default,
      licenses = List(License("MIT", MFUrl("https://opensource.org/licenses/MIT"))),
      website = MFUrl("http://project.website"),
      copyright = "Copyright (C) Test Inc.",
      izumiVersion = "0.0.0-SNAPSHOT"
    )
  }

  final case class ManifestDependency(module: String, version: String)

  final case class License(name: String, url: MFUrl)

  final case class MFUrl(url: String)
}
