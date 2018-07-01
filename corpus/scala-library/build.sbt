name := "scala-library"

version := "1.0"

scalaVersion := "2.13.0-M1"
scalaHome := Some(file("/Path/To/Build"))

scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint", "-deprecation")
javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-XDignore.symbol.file", "-Xlint:deprecation")

Global / serverConnectionType := ConnectionType.Tcp
Global / serverAuthentication := Set(ServerAuthentication.Token)
