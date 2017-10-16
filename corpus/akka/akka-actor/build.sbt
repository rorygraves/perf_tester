import akka.{ AkkaBuild, Dependencies, Version }

AkkaBuild.defaultSettings
Dependencies.actor
Version.versionSettings
unmanagedSourceDirectories in Compile += {
  val ver = scalaVersion.value.take(4)
  (scalaSource in Compile).value.getParentFile / s"scala-$ver"
}

