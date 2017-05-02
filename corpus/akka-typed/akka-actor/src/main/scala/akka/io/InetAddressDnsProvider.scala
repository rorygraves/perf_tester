package akka.io

class InetAddressDnsProvider extends DnsProvider {
  override def cache: Dns = new SimpleDnsCache()
  override def actorClass: Class[InetAddressDnsResolver] = classOf[InetAddressDnsResolver]
  override def managerClass: Class[SimpleDnsManager] = classOf[SimpleDnsManager]
}
