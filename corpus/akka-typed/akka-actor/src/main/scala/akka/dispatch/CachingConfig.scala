/**
 * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.dispatch

import java.time.Duration
import java.util.Map
import java.{lang, util}
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import com.typesafe.config._

import scala.util.{Failure, Success, Try}

/**
 * INTERNAL API
 */
private[akka] object CachingConfig {
  val emptyConfig: Config = ConfigFactory.empty()

  sealed abstract trait PathEntry {
    val valid: Boolean
    val exists: Boolean
    val config: Config
  }
  final case class ValuePathEntry(valid: Boolean, exists: Boolean, config: Config = emptyConfig) extends PathEntry
  final case class StringPathEntry(valid: Boolean, exists: Boolean, config: Config, value: String) extends PathEntry

  val invalidPathEntry = ValuePathEntry(false, true)
  val nonExistingPathEntry = ValuePathEntry(true, false)
  val emptyPathEntry = ValuePathEntry(true, true)
}

/**
 * INTERNAL API
 *
 * A CachingConfig is a Config that wraps another Config and is used to cache path lookup and string
 * retrieval, which we happen to do a lot in some critical paths of the actor creation and mailbox
 * selection code.
 *
 * All other Config operations are delegated to the wrapped Config.
 */
private[akka] class CachingConfig(_config: Config) extends Config {

  import CachingConfig._

  private val (config: Config, entryMap: ConcurrentHashMap[String, PathEntry]) = _config match {
    case cc: CachingConfig ⇒ (cc.config, cc.entryMap)
    case _ ⇒ (_config, new ConcurrentHashMap[String, PathEntry])
  }

  private def getPathEntry(path: String): PathEntry = entryMap.get(path) match {
    case null ⇒
      val ne = Try { config.hasPath(path) } match {
        case Failure(e) ⇒ invalidPathEntry
        case Success(false) ⇒ nonExistingPathEntry
        case _ ⇒
          Try { config.getValue(path) } match {
            case Failure(e) ⇒
              emptyPathEntry
            case Success(v) ⇒
              if (v.valueType() == ConfigValueType.STRING)
                StringPathEntry(true, true, v.atKey("cached"), v.unwrapped().asInstanceOf[String])
              else
                ValuePathEntry(true, true, v.atKey("cached"))
          }
      }

      entryMap.putIfAbsent(path, ne) match {
        case null ⇒ ne
        case e ⇒ e
      }

    case e ⇒ e
  }

  def checkValid(reference: Config, restrictToPaths: String*) {
    config.checkValid(reference, restrictToPaths: _*)
  }

  def root(): ConfigObject = config.root()

  def origin(): ConfigOrigin = config.origin()

  def withFallback(other: ConfigMergeable) = new CachingConfig(config.withFallback(other))

  def resolve(): CachingConfig = resolve(ConfigResolveOptions.defaults())

  def resolve(options: ConfigResolveOptions): CachingConfig = {
    val resolved = config.resolve(options)
    if (resolved eq config) this
    else new CachingConfig(resolved)
  }

  def hasPath(path: String): Boolean = {
    val entry = getPathEntry(path)
    if (entry.valid)
      entry.exists
    else // run the real code to get proper exceptions
      config.hasPath(path)
  }

  def hasPathOrNull(path: String): Boolean = config.hasPathOrNull(path)

  def isEmpty: Boolean = config.isEmpty

  def entrySet(): util.Set[Map.Entry[String, ConfigValue]] = config.entrySet()

  def getBoolean(path: String): Boolean = config.getBoolean(path)

  def getNumber(path: String): Number = config.getNumber(path)

  def getInt(path: String): Int = config.getInt(path)

  def getLong(path: String): Long = config.getLong(path)

  def getDouble(path: String): Double = config.getDouble(path)

  def getString(path: String): String = {
    getPathEntry(path) match {
      case StringPathEntry(_, _, _, string) ⇒
        string
      case e ⇒ e.config.getString("cached")
    }
  }

  def getObject(path: String): ConfigObject = config.getObject(path)

  def getConfig(path: String): Config = config.getConfig(path)

  def getAnyRef(path: String): AnyRef = config.getAnyRef(path)

  def getValue(path: String): ConfigValue = config.getValue(path)

  def getBytes(path: String): lang.Long = config.getBytes(path)

  def getMilliseconds(path: String): lang.Long = config.getDuration(path, TimeUnit.MILLISECONDS)

  def getNanoseconds(path: String): lang.Long = config.getDuration(path, TimeUnit.NANOSECONDS)

  def getList(path: String): ConfigList = config.getList(path)

  def getBooleanList(path: String): util.List[lang.Boolean] = config.getBooleanList(path)

  def getNumberList(path: String): util.List[Number] = config.getNumberList(path)

  def getIntList(path: String): util.List[Integer] = config.getIntList(path)

  def getLongList(path: String): util.List[lang.Long] = config.getLongList(path)

  def getDoubleList(path: String): util.List[lang.Double] = config.getDoubleList(path)

  def getStringList(path: String): util.List[String] = config.getStringList(path)

  def getObjectList(path: String): util.List[_ <: ConfigObject] = config.getObjectList(path)

  def getConfigList(path: String): util.List[_ <: Config] = config.getConfigList(path)

  def getAnyRefList(path: String): util.List[_] = config.getAnyRefList(path)

  def getBytesList(path: String): util.List[lang.Long] = config.getBytesList(path)

  def getMillisecondsList(path: String): util.List[lang.Long] = config.getDurationList(path, TimeUnit.MILLISECONDS)

  def getNanosecondsList(path: String): util.List[lang.Long] = config.getDurationList(path, TimeUnit.NANOSECONDS)

  def withOnlyPath(path: String) = new CachingConfig(config.withOnlyPath(path))

  def withoutPath(path: String) = new CachingConfig(config.withoutPath(path))

  def atPath(path: String) = new CachingConfig(config.atPath(path))

  def atKey(key: String) = new CachingConfig(config.atKey(key))

  def withValue(path: String, value: ConfigValue) = new CachingConfig(config.withValue(path, value))

  def getDuration(path: String, unit: TimeUnit): Long = config.getDuration(path, unit)

  def getDurationList(path: String, unit: TimeUnit): util.List[lang.Long] = config.getDurationList(path, unit)

  def getDuration(path: String): java.time.Duration = config.getDuration(path)

  def getDurationList(path: String): util.List[Duration] = config.getDurationList(path)

  def getIsNull(path: String): Boolean = config.getIsNull(path)

  def getMemorySize(path: String): ConfigMemorySize = config.getMemorySize(path)

  def getMemorySizeList(path: String): util.List[ConfigMemorySize] = config.getMemorySizeList(path)

  def isResolved(): Boolean = config.isResolved()

  def resolveWith(source: Config, options: ConfigResolveOptions): Config = config.resolveWith(source, options)

  def resolveWith(source: Config): Config = config.resolveWith(source)

  override def getEnumList[T <: Enum[T]](enumClass: Class[T], path: String): util.List[T] = config.getEnumList(enumClass, path)

  override def getEnum[T <: Enum[T]](enumClass: Class[T], path: String): T = config.getEnum(enumClass, path)

}

