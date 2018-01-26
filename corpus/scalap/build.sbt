name := "scalap"

version := "0.1"

scalaVersion := "2.12.1"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.12.1"

serverConnectionType := ConnectionType.Tcp
serverAuthentication := Set(ServerAuthentication.Token)
