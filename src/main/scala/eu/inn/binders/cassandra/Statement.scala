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

  def hasField(fieldName: String): Boolean = boundStatement.preparedStatement().getVariables.contains(fieldName)

  def addString(value: String) = boundStatement.setString(nextIndex(), value)

  def addNullableString(value: Option[String]) = if (value.isDefined)
    boundStatement.setString(nextIndex(), value.get)
  else
    boundStatement.setString(nextIndex(), null)

  def setString(name: String, value: String) = boundStatement.setString(name, value)

  def setNullableString(name: String, value: Option[String]) = if (value.isDefined)
    boundStatement.setString(name, value.get)
  else
    boundStatement.setString(name, null)

  def addInt(value: Int) = boundStatement.setInt(nextIndex(), value)

  def addIntNullable(value: Option[Int]) = if (value.isDefined)
    boundStatement.setInt(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def setInt(name: String, value: Int) = boundStatement.setInt(name, value)

  def setIntNullable(name: String, value: Option[Int]) = if (value.isDefined)
    boundStatement.setInt(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def addLong(value: Long) = boundStatement.setLong(nextIndex(), value)

  def addLongNullable(value: Option[Long]) = if (value.isDefined)
    boundStatement.setLong(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def setLong(name: String, value: Long) = boundStatement.setLong(name, value)

  def setLongNullable(name: String, value: Option[Long]) = if (value.isDefined)
    boundStatement.setLong(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def addDate(value: Date) = boundStatement.setDate(nextIndex(), value)

  def setDate(name: String, value: Date) = boundStatement.setDate(name, value)

  def addDateNullable(value: Option[Date]) = boundStatement.setDate(nextIndex(), value.orNull)

  def setDateNullable(name: String, value: Option[Date]) = boundStatement.setDate(name, value.orNull)

  def addBoolean(value: Boolean) = boundStatement.setBool(nextIndex(), value)

  def setBoolean(name: String, value: Boolean) = boundStatement.setBool(name, value)

  def addBooleanNullable(value: Option[Boolean]) = if (value.isDefined)
    boundStatement.setBool(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def setBooleanNullable(name: String, value: Option[Boolean]) = if (value.isDefined)
    boundStatement.setBool(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def addFloat(value: Float) = boundStatement.setFloat(nextIndex(), value)

  def setFloat(name: String, value: Float) = boundStatement.setFloat(name, value)

  def addFloatNullable(value: Option[Float]) = if (value.isDefined)
    boundStatement.setFloat(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def setFloatNullable(name: String, value: Option[Float]) = if (value.isDefined)
    boundStatement.setFloat(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def addDouble(value: Double) = boundStatement.setDouble(nextIndex(), value)

  def setDouble(name: String, value: Double) = boundStatement.setDouble(name, value)

  def addDoubleNullable(value: Option[Double]) = if (value.isDefined)
    boundStatement.setDouble(nextIndex(), value.get)
  else
    boundStatement.setBytesUnsafe(nextIndex(), null)

  def setDoubleNullable(name: String, value: Option[Double]) = if (value.isDefined)
    boundStatement.setDouble(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def addBytes(value: ByteBuffer) = boundStatement.setBytes(nextIndex(), value)

  def setBytes(name: String, value: ByteBuffer) = boundStatement.setBytes(name, value)

  def addBytesNullable(value: Option[ByteBuffer]) = boundStatement.setBytes(nextIndex(), value.orNull)

  def setBytesNullable(name: String, value: Option[ByteBuffer]) = boundStatement.setBytes(name, value.orNull)

  def addBigInteger(value: BigInteger) = boundStatement.setVarint(nextIndex(), value)

  def setBigInteger(name: String, value: BigInteger) = boundStatement.setVarint(name, value)

  def addBigIntegerNullable(value: Option[BigInteger]) = boundStatement.setVarint(nextIndex(), value.orNull)

  def setBigIntegerNullable(name: String, value: Option[BigInteger]) = boundStatement.setVarint(name, value.orNull)

  def addBigDecimal(value: BigDecimal) = boundStatement.setDecimal(nextIndex(), value.bigDecimal)

  def setBigDecimal(name: String, value: BigDecimal) = boundStatement.setDecimal(name, value.bigDecimal)

  def addBigDecimalNullable(value: Option[BigDecimal]) = if (value.isDefined)
    boundStatement.setDecimal(nextIndex(), value.get.bigDecimal)
  else
    boundStatement.setDecimal(nextIndex(), null)

  def setBigDecimalNullable(name: String, value: Option[BigDecimal]) = if (value.isDefined)
    boundStatement.setDecimal(name, value.get.bigDecimal)
  else
    boundStatement.setDecimal(name, null)

  def addUUID(value: UUID) = boundStatement.setUUID(nextIndex(), value)

  def setUUID(name: String, value: UUID) = boundStatement.setUUID(name, value)

  def addUUIDNullable(value: Option[UUID]) = boundStatement.setUUID(nextIndex(), value.orNull)

  def setUUIDNullable(name: String, value: Option[UUID]) = boundStatement.setUUID(name, value.orNull)

  def addInetAddress(value: InetAddress) = boundStatement.setInet(nextIndex(), value)

  def setInetAddress(name: String, value: InetAddress) = boundStatement.setInet(name, value)

  def addInetAddressNullable(value: Option[InetAddress]) = boundStatement.setInet(nextIndex(), value.orNull)

  def setInetAddressNullable(name: String, value: Option[InetAddress]) = boundStatement.setInet(name, value.orNull)

  def addList[T: ClassTag](value: List[T]) = boundStatement.setList(nextIndex(), value)

  def setList[T: ClassTag](name: String, value: List[T]) = boundStatement.setList(name, value)

  def addSet[T: ClassTag](value: Set[T]) = boundStatement.setSet(nextIndex(), value)

  def setSet[T: ClassTag](name: String, value: Set[T]) = boundStatement.setSet(name, value)

  def addMap[K: ClassTag, V: ClassTag](value: Map[K, V]) = boundStatement.setMap(nextIndex(), value)

  def setMap[K: ClassTag, V: ClassTag](name: String, value: Map[K, V]) = boundStatement.setMap(name, value)
}
