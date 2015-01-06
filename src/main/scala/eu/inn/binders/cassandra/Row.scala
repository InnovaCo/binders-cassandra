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

    def readString(): String = {
      checkNotNull()
      row.getString(name)
    }

    def readStringNullable(): Option[String] = if (row.isNull(name)) None else Option(row.getString(name))

    def readInt(): Int = {
      checkNotNull()
      row.getInt(name)
    }

    def readIntNullable(): Option[Int] = if (row.isNull(name)) None else Some(row.getInt(name))

    def readLong(): Long = {
      checkNotNull()
      row.getLong(name)
    }

    def readLongNullable(): Option[Long] = if (row.isNull(name)) None else Some(row.getLong(name))

    def readDate(): java.util.Date = {
      checkNotNull()
      row.getDate(name)
    }

    def readDateNullable(): Option[java.util.Date] = if (row.isNull(name)) None else Option(row.getDate(name))

    def readBoolean(): Boolean = {
      checkNotNull()
      row.getBool(name)
    }

    def readBooleanNullable(): Option[Boolean] = if (row.isNull(name)) None else Some(row.getBool(name))

    def readFloat(): Float = {
      checkNotNull()
      row.getFloat(name)
    }

    def readFloatNullable(): Option[Float] = if (row.isNull(name)) None else Some(row.getFloat(name))

    def readDouble(): Double = {
      checkNotNull()
      row.getDouble(name)
    }

    def readDoubleNullable(): Option[Double] = if (row.isNull(name)) None else Some(row.getDouble(name))

    def readBytes(): ByteBuffer = {
      checkNotNull()
      row.getBytes(name)
    }

    def readBytesNullable(): Option[ByteBuffer] = if (row.isNull(name)) None else Option(row.getBytes(name))

    def readBigInteger(): BigInteger = {
      checkNotNull()
      row.getVarint(name)
    }

    def readBigIntegerNullable(): Option[BigInteger] = if (row.isNull(name)) None else Option(row.getVarint(name))

    def readBigDecimal(): BigDecimal = {
      checkNotNull()
      row.getDecimal(name)
    }

    def readBigDecimalNullable(): Option[BigDecimal] = if (row.isNull(name)) None else Option(row.getDecimal(name))

    def readUUID(): UUID = {
      checkNotNull()
      row.getUUID(name)
    }

    def readUUIDNullable(): Option[UUID] = if (row.isNull(name)) None else Option(row.getUUID(name))

    def readInetAddress(): InetAddress = {
      checkNotNull()
      row.getInet(name)
    }

    def readInetAddressNullable(): Option[InetAddress] = if (row.isNull(name)) None else Option(row.getInet(name))

    // collections implementation isn't not very efficient at the moment
    def readList[T: ClassTag](): List[T] = row.getList(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toList

    def readSet[T: ClassTag](): Set[T] = row.getSet(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toSet

    def readMap[K: ClassTag, V: ClassTag](): Map[K, V] =
      row.getMap(name, classTag[K].runtimeClass, classTag[V].runtimeClass).map(kv => (kv._1.asInstanceOf[K], kv._2.asInstanceOf[V])).toMap
  }
}
