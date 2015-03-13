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
      logger.trace(boundStatement.preparedStatement.getQueryString.trim)
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

  def writeNull() = boundStatement.setToNull(nextIndex())

  def writeString(value: String) = boundStatement.setString(nextIndex(), value)
  def writeInt(value: Int) = boundStatement.setInt(nextIndex(), value)
  def writeLong(value: Long) = boundStatement.setLong(nextIndex(), value)
  def writeDate(value: Date) = boundStatement.setDate(nextIndex(), value)
  def writeBoolean(value: Boolean) = boundStatement.setBool(nextIndex(), value)
  def writeFloat(value: Float) = boundStatement.setFloat(nextIndex(), value)
  def writeDouble(value: Double) = boundStatement.setDouble(nextIndex(), value)
  def writeBytes(value: ByteBuffer) = boundStatement.setBytes(nextIndex(), value)
  def writeBigInteger(value: BigInteger) = boundStatement.setVarint(nextIndex(), value)
  def writeBigDecimal(value: BigDecimal) = boundStatement.setDecimal(nextIndex(), value.bigDecimal)
  def writeUUID(value: UUID) = boundStatement.setUUID(nextIndex(), value)
  def writeInetAddress(value: InetAddress) = boundStatement.setInet(nextIndex(), value)
  def writeList[T: ClassTag](value: List[T]) = boundStatement.setList(nextIndex(), value)
  def writeSet[T: ClassTag](value: Set[T]) = boundStatement.setSet(nextIndex(), value)
  def writeMap[K: ClassTag, V: ClassTag](value: Map[K, V]) = boundStatement.setMap(nextIndex(), value)

  class StatementFieldSerializer(val name: String) extends eu.inn.binders.core.Serializer[C] {
    def fieldName: Option[String] = Some(name)

    def getFieldSerializer(fieldName: String): Option[StatementFieldSerializer] = None

    def writeNull() = boundStatement.setToNull(name)
    def writeString(value: String) = boundStatement.setString(name, value)
    def writeInt(value: Int) = boundStatement.setInt(name, value)
    def writeLong(value: Long) = boundStatement.setLong(name, value)
    def writeDate(value: Date) = boundStatement.setDate(name, value)
    def writeBoolean(value: Boolean) = boundStatement.setBool(name, value)
    def writeFloat(value: Float) = boundStatement.setFloat(name, value)
    def writeDouble(value: Double) = boundStatement.setDouble(name, value)
    def writeBytes(value: ByteBuffer) = boundStatement.setBytes(name, value)
    def writeBigInteger(value: BigInteger) = boundStatement.setVarint(name, value)
    def writeBigDecimal(value: BigDecimal) = boundStatement.setDecimal(name, value.bigDecimal)
    def writeUUID(value: UUID) = boundStatement.setUUID(name, value)
    def writeInetAddress(value: InetAddress) = boundStatement.setInet(name, value)
    def writeList[T: ClassTag](value: List[T]) = boundStatement.setList(name, value)
    def writeSet[T: ClassTag](value: Set[T]) = boundStatement.setSet(name, value)
    def writeMap[K: ClassTag, V: ClassTag](value: Map[K, V]) = boundStatement.setMap(name, value)
  }
}
