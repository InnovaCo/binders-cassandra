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

  def isNull = false

  def iterator() : Iterator[FieldDeserializer] = row.getColumnDefinitions.map { column =>
    new FieldDeserializer(column.getName)
  }.toIterator

  class FieldDeserializer(val name: String) extends eu.inn.binders.core.Deserializer[C] {
    def fieldName: Option[String] = Some(name)

    def iterator(): Iterator[eu.inn.binders.core.Deserializer[C]] = ???

    protected def checkNotNull() = if (row.isNull(name)) throw new ColumnValueIsNullException(name)

    def isNull: Boolean = row.isNull(name)

    def readString(): String = {
      checkNotNull()
      row.getString(name)
    }

    def readInt(): Int = {
      checkNotNull()
      row.getInt(name)
    }

    def readLong(): Long = {
      checkNotNull()
      row.getLong(name)
    }

    def readDate(): java.util.Date = {
      checkNotNull()
      row.getDate(name)
    }

    def readBoolean(): Boolean = {
      checkNotNull()
      row.getBool(name)
    }

    def readFloat(): Float = {
      checkNotNull()
      row.getFloat(name)
    }

    def readDouble(): Double = {
      checkNotNull()
      row.getDouble(name)
    }

    def readBytes(): ByteBuffer = {
      checkNotNull()
      row.getBytes(name)
    }

    def readBigInteger(): BigInteger = {
      checkNotNull()
      row.getVarint(name)
    }

    def readBigDecimal(): BigDecimal = {
      checkNotNull()
      row.getDecimal(name)
    }

    def readUUID(): UUID = {
      checkNotNull()
      row.getUUID(name)
    }

    def readInetAddress(): InetAddress = {
      checkNotNull()
      row.getInet(name)
    }

    // collections implementation isn't not very efficient at the moment
    def readList[T: ClassTag](): List[T] = row.getList(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toList

    def readSet[T: ClassTag](): Set[T] = row.getSet(name, classTag[T].runtimeClass).map(x => x.asInstanceOf[T]).toSet

    def readMap[K: ClassTag, V: ClassTag](): Map[K, V] =
      row.getMap(name, classTag[K].runtimeClass, classTag[V].runtimeClass).map(kv => (kv._1.asInstanceOf[K], kv._2.asInstanceOf[V])).toMap
  }
}
