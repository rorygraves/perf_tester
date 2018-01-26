import akka.AkkaBuild._

initialize := {
  // Load system properties from a file to make configuration from Jenkins easier
  loadSystemProperties("project/akka-build.properties")
  initialize.value
}

serverConnectionType := ConnectionType.Tcp
serverAuthentication := Set(ServerAuthentication.Token)

akka.AkkaBuild.buildSettings

