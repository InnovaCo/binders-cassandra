package eu.inn.binders.cassandra

import scala.None
import java.nio.ByteBuffer
import java.math.BigInteger
import java.util.UUID
import java.net.InetAddress
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._

class Row[C <: Converter : TypeTag](row: com.datastax.driver.core.Row) extends eu.inn.binders.core.Row {
  type nameConverterType = C

  import scala.reflect._
  import scala.collection.JavaConversions._

  def hasField(name: String): Boolean = row.getColumnDefinitions.contains(name)

  def checkNotNull(name: String) = if (row.isNull(name)) throw new ColumnValueIsNullException(name)

  def getString(name: String): String = {
    checkNotNull(name);
    row.getString(name)
  }

  def getStringNullable(name: String): Option[String] = if (row.isNull(name)) None else Option(row.getString(name))

  def getInt(name: String): Int = {
    checkNotNull(name);
    row.getInt(name)
  }

  def getIntNullable(name: String): Option[Int] = if (row.isNull(name)) None else Some(row.getInt(name))

  def getLong(name: String): Long = {
    checkNotNull(name);
    row.getLong(name)
  }

  def getLongNullable(name: String): Option[Long] = if (row.isNull(name)) None else Some(row.getLong(name))

  def getDate(name: String): java.util.Date = {
    checkNotNull(name);
    row.getDate(name)
  }

  def getDateNullable(name: String): Option[java.util.Date] = if (row.isNull(name)) None else Option(row.getDate(name))

  def getBoolean(name: String): Boolean = {
    checkNotNull(name);
    row.getBool(name)
  }

  def getBooleanNullable(name: String): Option[Boolean] = if (row.isNull(name)) None else Some(row.getBool(name))

  def getFloat(name: String): Float = {
    checkNotNull(name);
    row.getFloat(name)
  }

  def getFloatNullable(name: String): Option[Float] = if (row.isNull(name)) None else Some(row.getFloat(name))

  def getDouble(name: String): Double = {
    checkNotNull(name);
    row.getDouble(name)
  }

  def getDoubleNullable(name: String): Option[Double] = if (row.isNull(name)) None else Some(row.getDouble(name))

  def getBytes(name: String): ByteBuffer = {
    checkNotNull(name);
    row.getBytes(name)
  }

  def getBytesNullable(name: String): Option[ByteBuffer] = if (row.isNull(name)) None else Option(row.getBytes(name))

  def getBigInteger(name: String): BigInteger = {
    checkNotNull(name);
    row.getVarint(name)
  }

  def getBigIntegerNullable(name: String): Option[BigInteger] = if (row.isNull(name)) None else Option(row.getVarint(name))

  def getBigDecimal(name: String): BigDecimal = {
    checkNotNull(name);
    row.getDecimal(name)
  }

  def getBigDecimalNullable(name: String): Option[BigDecimal] = if (row.isNull(name)) None else Option(row.getDecimal(name))

  def getUUID(name: String): UUID = {
    checkNotNull(name);
    row.getUUID(name)
  }

  def getUUIDNullable(name: String): Option[UUID] = if (row.isNull(name)) None else Option(row.getUUID(name))

  def getInetAddress(name: String): InetAddress = {
    checkNotNull(name);
    row.getInet(name)
  }

  def getInetAddressNullable(name: String): Option[InetAddress] = if (row.isNull(name)) None else Option(row.getInet(name))

  // collections implementation isn't not very efficient at the moment
  def getList[T: ClassTag](name: String): List[T] = row.getList(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toList

  def getSet[T: ClassTag](name: String): Set[T] = row.getSet(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toSet

  def getMap[K: ClassTag, V: ClassTag](name: String): Map[K, V] =
    row.getMap(name, classTag[K].runtimeClass, classTag[V].runtimeClass).map(kv => (kv._1.asInstanceOf[K], kv._2.asInstanceOf[V])).toMap
}
