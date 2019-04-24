package org.perftester.process.child

import java.io.FileDescriptor
import java.net.InetAddress
import java.security.Permission

object SecMan extends SecurityManager {
  var exit = false

  override def checkExit(status: Int): Unit = {
    if (!exit) throw new DontExit
  }

  override def checkExec(cmd: String): Unit = ()

  override def checkAwtEventQueueAccess(): Unit = ()

  override def checkPrintJobAccess(): Unit = ()

  override def checkMulticast(maddr: InetAddress): Unit = ()

  override def checkMulticast(maddr: InetAddress, ttl: Byte): Unit = ()

  override def checkPermission(perm: Permission): Unit = ()

  override def checkPermission(perm: Permission, context: scala.Any): Unit = ()

  override def checkAccept(host: String, port: Int): Unit = ()

  override def checkSetFactory(): Unit = ()

  override def checkLink(lib: String): Unit = ()

  override def checkWrite(fd: FileDescriptor): Unit = ()

  override def checkWrite(file: String): Unit = ()

  override def checkPropertyAccess(key: String): Unit = ()

  override def checkSecurityAccess(target: String): Unit = ()

  override def checkListen(port: Int): Unit = ()

  override def checkAccess(t: Thread): Unit = ()

  override def checkAccess(g: ThreadGroup): Unit = ()

  override def checkDelete(file: String): Unit = ()

  override def checkCreateClassLoader(): Unit = ()

  override def checkPackageDefinition(pkg: String): Unit = ()

  override def checkConnect(host: String, port: Int): Unit = ()

  override def checkConnect(host: String, port: Int, context: scala.Any): Unit = ()

  override def checkPackageAccess(pkg: String): Unit = ()

  override def checkPropertiesAccess(): Unit = ()

  override def checkSystemClipboardAccess(): Unit = ()

  override def checkRead(fd: FileDescriptor): Unit = ()

  override def checkRead(file: String): Unit = ()

  override def checkRead(file: String, context: scala.Any): Unit = ()
}
