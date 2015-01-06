package eu.inn.binders.cassandra

import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.UUID
import scala.reflect.runtime.universe._

import eu.inn.binders.naming.Converter


class Row[C <: Converter : TypeTag](val row: com.datastax.driver.core.Row) extends eu.inn.binders.core.Deserializer[C] {
  import scala.reflect._
  import scala.collection.JavaConversions._

  def fieldName: Option[String] = None

  def iterator() : Iterator[FieldDeserializer] = row.getColumnDefinitions.map { column =>
    new FieldDeserializer(column.getName)
  }.toIterator

  class FieldDeserializer(val name: String) extends eu.inn.binders.core.Deserializer[C] {
    def fieldName: Option[String] = Some(name)

    def iterator(): Iterator[eu.inn.binders.core.Deserializer[C]] = ???

    def checkNotNull() = if (row.isNull(name)) throw new ColumnValueIsNullException(name)

    def getString(): String = {
      checkNotNull()
      row.getString(name)
    }

    def getStringNullable(): Option[String] = if (row.isNull(name)) None else Option(row.getString(name))

    def getInt(): Int = {
      checkNotNull()
      row.getInt(name)
    }

    def getIntNullable(): Option[Int] = if (row.isNull(name)) None else Some(row.getInt(name))

    def getLong(): Long = {
      checkNotNull()
      row.getLong(name)
    }

    def getLongNullable(): Option[Long] = if (row.isNull(name)) None else Some(row.getLong(name))

    def getDate(): java.util.Date = {
      checkNotNull()
      row.getDate(name)
    }

    def getDateNullable(): Option[java.util.Date] = if (row.isNull(name)) None else Option(row.getDate(name))

    def getBoolean(): Boolean = {
      checkNotNull()
      row.getBool(name)
    }

    def getBooleanNullable(): Option[Boolean] = if (row.isNull(name)) None else Some(row.getBool(name))

    def getFloat(): Float = {
      checkNotNull()
      row.getFloat(name)
    }

    def getFloatNullable(): Option[Float] = if (row.isNull(name)) None else Some(row.getFloat(name))

    def getDouble(): Double = {
      checkNotNull()
      row.getDouble(name)
    }

    def getDoubleNullable(): Option[Double] = if (row.isNull(name)) None else Some(row.getDouble(name))

    def getBytes(): ByteBuffer = {
      checkNotNull()
      row.getBytes(name)
    }

    def getBytesNullable(): Option[ByteBuffer] = if (row.isNull(name)) None else Option(row.getBytes(name))

    def getBigInteger(): BigInteger = {
      checkNotNull()
      row.getVarint(name)
    }

    def getBigIntegerNullable(): Option[BigInteger] = if (row.isNull(name)) None else Option(row.getVarint(name))

    def getBigDecimal(): BigDecimal = {
      checkNotNull()
      row.getDecimal(name)
    }

    def getBigDecimalNullable(): Option[BigDecimal] = if (row.isNull(name)) None else Option(row.getDecimal(name))

    def getUUID(): UUID = {
      checkNotNull()
      row.getUUID(name)
    }

    def getUUIDNullable(): Option[UUID] = if (row.isNull(name)) None else Option(row.getUUID(name))

    def getInetAddress(): InetAddress = {
      checkNotNull()
      row.getInet(name)
    }

    def getInetAddressNullable(): Option[InetAddress] = if (row.isNull(name)) None else Option(row.getInet(name))

    // collections implementation isn't not very efficient at the moment
    def getList[T: ClassTag](): List[T] = row.getList(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toList

    def getSet[T: ClassTag](): Set[T] = row.getSet(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toSet

    def getMap[K: ClassTag, V: ClassTag](): Map[K, V] =
      row.getMap(name, classTag[K].runtimeClass, classTag[V].runtimeClass).map(kv => (kv._1.asInstanceOf[K], kv._2.asInstanceOf[V])).toMap
  }
}
