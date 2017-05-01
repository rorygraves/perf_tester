import akka.{ AkkaBuild, Dependencies, Version }

AkkaBuild.defaultSettings
Dependencies.actor
Version.versionSettings

enablePlugins(spray.boilerplate.BoilerplatePlugin)
