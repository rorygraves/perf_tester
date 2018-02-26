import akka.AkkaBuild._

initialize := {
  // Load system properties from a file to make configuration from Jenkins easier
  loadSystemProperties("project/akka-build.properties")
  initialize.value
}

akka.AkkaBuild.buildSettings
Global / serverConnectionType := ConnectionType.Tcp
Global / serverAuthentication := Set(ServerAuthentication.Token)

lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  actor 
)

lazy val root = Project(
  id = "akka",
  base = file(".")
).aggregate(aggregatedProjects: _*)

lazy val actor = akkaModule("akka-actor")

def akkaModule(name: String): Project =
  Project(id = name, base = file(name))
    .settings(akka.AkkaBuild.buildSettings)
