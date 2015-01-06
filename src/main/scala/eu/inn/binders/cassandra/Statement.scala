package eu.inn.binders.cassandra

import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{UUID, Date}
import scala.concurrent.{Promise, Future}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

import com.datastax.driver.core.{ResultSet, Session, BoundStatement}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import org.slf4j.LoggerFactory

import eu.inn.binders.naming.Converter

class Statement[C <: Converter : TypeTag](val session: Session, val boundStatement: BoundStatement)
  extends eu.inn.binders.core.Serializer[C] {
  import scala.collection.JavaConversions._

  protected val logger = LoggerFactory.getLogger(getClass)
  protected var argIndex = 0
  protected def nextIndex() = {
    val prev = argIndex
    argIndex += 1
    prev
  }

  def execute(): Future[Rows[C]] = {
    if (logger.isTraceEnabled) {
      logger.trace(boundStatement.preparedStatement.getQueryString)
    }

    val promise = Promise[Rows[C]]()
    Futures.addCallback(session.executeAsync(boundStatement), new FutureConverter(promise))
    promise.future
  }

  private class FutureConverter(promise: Promise[Rows[C]]) extends FutureCallback[ResultSet] {
    override def onFailure(t: Throwable) {
      promise.failure(t)
    }

    override def onSuccess(result: ResultSet) {
      promise.success(new Rows(result))
    }
  }

  def fieldName: Option[String] = None

  def getFieldSerializer(name: String): Option[StatementFieldSerializer] = {
    if (boundStatement.preparedStatement().getVariables.contains(name))
      Some(new StatementFieldSerializer(name))
    else
      None
  }

  def writeString(value: String) = boundStatement.setString(nextIndex(), value)

  def writeStringNullable(value: Option[String]) = if (value.isDefined)
    boundStatement.setString(nextIndex(), value.get)
  else
    boundStatement.setString(nextIndex(), null)

  def writeInt(value: Int) = boundStatement.setInt(nextIndex(), value)

  def writeIntNullable(value: Option[Int]) = if (value.isDefined)
    boundStatement.setInt(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def writeLong(value: Long) = boundStatement.setLong(nextIndex(), value)

  def writeLongNullable(value: Option[Long]) = if (value.isDefined)
    boundStatement.setLong(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def writeDate(value: Date) = boundStatement.setDate(nextIndex(), value)

  def writeDateNullable(value: Option[Date]) = boundStatement.setDate(nextIndex(), value.orNull)

  def writeBoolean(value: Boolean) = boundStatement.setBool(nextIndex(), value)

  def writeBooleanNullable(value: Option[Boolean]) = if (value.isDefined)
    boundStatement.setBool(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def writeFloat(value: Float) = boundStatement.setFloat(nextIndex(), value)

  def writeFloatNullable(value: Option[Float]) = if (value.isDefined)
    boundStatement.setFloat(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def writeDouble(value: Double) = boundStatement.setDouble(nextIndex(), value)

  def writeDoubleNullable(value: Option[Double]) = if (value.isDefined)
    boundStatement.setDouble(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def writeBytes(value: ByteBuffer) = boundStatement.setBytes(nextIndex(), value)

  def writeBytesNullable(value: Option[ByteBuffer]) = boundStatement.setBytes(nextIndex(), value.orNull)

  def writeBigInteger(value: BigInteger) = boundStatement.setVarint(nextIndex(), value)

  def writeBigIntegerNullable(value: Option[BigInteger]) = boundStatement.setVarint(nextIndex(), value.orNull)

  def writeBigDecimal(value: BigDecimal) = boundStatement.setDecimal(nextIndex(), value.bigDecimal)

  def writeBigDecimalNullable(value: Option[BigDecimal]) = if (value.isDefined)
    boundStatement.setDecimal(nextIndex(), value.get.bigDecimal)
  else
    boundStatement.setDecimal(nextIndex(), null)

  def writeUUID(value: UUID) = boundStatement.setUUID(nextIndex(), value)

  def writeUUIDNullable(value: Option[UUID]) = boundStatement.setUUID(nextIndex(), value.orNull)

  def writeInetAddress(value: InetAddress) = boundStatement.setInet(nextIndex(), value)

  def writeInetAddressNullable(value: Option[InetAddress]) = boundStatement.setInet(nextIndex(), value.orNull)

  def writeList[T: ClassTag](value: List[T]) = boundStatement.setList(nextIndex(), value)

  def writeSet[T: ClassTag](value: Set[T]) = boundStatement.setSet(nextIndex(), value)

  def writeMap[K: ClassTag, V: ClassTag](value: Map[K, V]) = boundStatement.setMap(nextIndex(), value)

  class StatementFieldSerializer(val name: String) extends eu.inn.binders.core.Serializer[C] {
    def fieldName: Option[String] = Some(name)

    def getFieldSerializer(fieldName: String): Option[StatementFieldSerializer] = None
    
    def writeString(value: String) = boundStatement.setString(name, value)

    def writeStringNullable(value: Option[String]) = if (value.isDefined)
      boundStatement.setString(name, value.get)
    else
      boundStatement.setString(name, null)

    def writeInt(value: Int) = boundStatement.setInt(name, value)

    def writeIntNullable(value: Option[Int]) = if (value.isDefined)
      boundStatement.setInt(name, value.get)
    else
      boundStatement.setBytesUnsafe(name, null)

    def writeLong(value: Long) = boundStatement.setLong(name, value)

    def writeLongNullable(value: Option[Long]) = if (value.isDefined)
      boundStatement.setLong(name, value.get)
    else
      boundStatement.setBytesUnsafe(name, null)

    def writeDate(value: Date) = boundStatement.setDate(name, value)

    def writeDateNullable(value: Option[Date]) = boundStatement.setDate(name, value.orNull)

    def writeBoolean(value: Boolean) = boundStatement.setBool(name, value)

    def writeBooleanNullable(value: Option[Boolean]) = if (value.isDefined)
      boundStatement.setBool(name, value.get)
    else
      boundStatement.setBytesUnsafe(name, null)

    def writeFloat(value: Float) = boundStatement.setFloat(name, value)

    def writeFloatNullable(value: Option[Float]) = if (value.isDefined)
      boundStatement.setFloat(name, value.get)
    else
      boundStatement.setBytesUnsafe(name, null)

    def writeDouble(value: Double) = boundStatement.setDouble(name, value)

    def writeDoubleNullable(value: Option[Double]) = if (value.isDefined)
      boundStatement.setDouble(name, value.get)
    else
      boundStatement.setBytesUnsafe(name, null)

    def writeBytes(value: ByteBuffer) = boundStatement.setBytes(name, value)

    def writeBytesNullable(value: Option[ByteBuffer]) = boundStatement.setBytes(name, value.orNull)

    def writeBigInteger(value: BigInteger) = boundStatement.setVarint(name, value)

    def writeBigIntegerNullable(value: Option[BigInteger]) = boundStatement.setVarint(name, value.orNull)

    def writeBigDecimal(value: BigDecimal) = boundStatement.setDecimal(name, value.bigDecimal)

    def writeBigDecimalNullable(value: Option[BigDecimal]) = if (value.isDefined)
      boundStatement.setDecimal(name, value.get.bigDecimal)
    else
      boundStatement.setDecimal(name, null)

    def writeUUID(value: UUID) = boundStatement.setUUID(name, value)

    def writeUUIDNullable(value: Option[UUID]) = boundStatement.setUUID(name, value.orNull)

    def writeInetAddress(value: InetAddress) = boundStatement.setInet(name, value)

    def writeInetAddressNullable(value: Option[InetAddress]) = boundStatement.setInet(name, value.orNull)

    def writeList[T: ClassTag](value: List[T]) = boundStatement.setList(name, value)

    def writeSet[T: ClassTag](value: Set[T]) = boundStatement.setSet(name, value)
  
    def writeMap[K: ClassTag, V: ClassTag](value: Map[K, V]) = boundStatement.setMap(name, value)
  }
}
