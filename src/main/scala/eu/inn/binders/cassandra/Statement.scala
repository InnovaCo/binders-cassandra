package eu.inn.binders.cassandra

import com.datastax.driver.core.BoundStatement
import java.util.{UUID, Date}
import java.nio.ByteBuffer
import java.math.BigInteger
import java.net.InetAddress
import scala.reflect.ClassTag


class Statement(val boundStatement : BoundStatement) extends eu.inn.binders.core.Statement {

  import scala.collection.JavaConversions._
  def hasParameter(parameterName: String): Boolean = boundStatement.preparedStatement().getVariables.contains(parameterName)

  def setString(index: Int, value: String) = boundStatement.setString(index, value)
  def setNullableString(index: Int, value: Option[String]) = if (value.isDefined)
      boundStatement.setString(index, value.get)
    else
      boundStatement.setString(index, null)

  def setString(name: String, value: String) = boundStatement.setString(name, value)
  def setNullableString(name: String, value: Option[String]) = if (value.isDefined)
      boundStatement.setString(name, value.get)
    else
      boundStatement.setString(name, null)

  def setInt(index: Int, value: Int) = boundStatement.setInt(index, value)
  def setInt(name: String, value: Int) = boundStatement.setInt(name, value)
  def setIntNullable(index: Int, value: Option[Int]) = if (value.isDefined)
      boundStatement.setInt(index, value.get)
    else
      boundStatement.setBytesUnsafe(index, null)

  def setIntNullable(name: String, value: Option[Int]) = if (value.isDefined)
      boundStatement.setInt(name, value.get)
    else
      boundStatement.setBytesUnsafe(name, null)

  def setLong(index: Int, value: Long) = boundStatement.setLong(index, value)
  def setLong(name: String, value: Long) = boundStatement.setLong(name, value)
  def setLongNullable(index: Int, value: Option[Long]) = if (value.isDefined)
    boundStatement.setLong(index, value.get)
  else
    boundStatement.setBytesUnsafe(index, null)
  def setLongNullable(name: String, value: Option[Long]) = if (value.isDefined)
    boundStatement.setLong(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def setDate(index: Int, value: Date) = boundStatement.setDate(index, value)
  def setDate(name: String, value: Date) = boundStatement.setDate(name, value)

  def setDateNullable(index: Int, value: Option[Date]) = boundStatement.setDate(index, value.orNull)
  def setDateNullable(name: String, value: Option[Date]) = boundStatement.setDate(name, value.orNull)

  def setBoolean(index: Int, value: Boolean) = boundStatement.setBool(index, value)
  def setBoolean(name: String, value: Boolean) = boundStatement.setBool(name, value)
  def setBooleanNullable(index: Int, value: Option[Boolean]) = if (value.isDefined)
    boundStatement.setBool(index, value.get)
  else
    boundStatement.setBytesUnsafe(index, null)
  def setBooleanNullable(name: String, value: Option[Boolean]) = if (value.isDefined)
    boundStatement.setBool(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def setFloat(index: Int, value: Float) = boundStatement.setFloat(index, value)
  def setFloat(name: String, value: Float) = boundStatement.setFloat(name, value)
  def setFloatNullable(index: Int, value: Option[Float]) = if (value.isDefined)
    boundStatement.setFloat(index, value.get)
  else
    boundStatement.setBytesUnsafe(index, null)
  def setFloatNullable(name: String, value: Option[Float]) = if (value.isDefined)
    boundStatement.setFloat(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def setDouble(index: Int, value: Double) = boundStatement.setDouble(index, value)
  def setDouble(name: String, value: Double) = boundStatement.setDouble(name, value)
  def setDoubleNullable(index: Int, value: Option[Double]) = if (value.isDefined)
    boundStatement.setDouble(index, value.get)
  else
    boundStatement.setBytesUnsafe(index, null)

  def setDoubleNullable(name: String, value: Option[Double]) = if (value.isDefined)
    boundStatement.setDouble(name, value.get)
  else
    boundStatement.setBytesUnsafe(name, null)

  def setBytes(index: Int, value: ByteBuffer) = boundStatement.setBytes(index, value)
  def setBytes(name: String, value: ByteBuffer) = boundStatement.setBytes(name, value)
  def setBytesNullable(index: Int, value: Option[ByteBuffer]) = boundStatement.setBytes(index, value.orNull)
  def setBytesNullable(name: String, value: Option[ByteBuffer]) = boundStatement.setBytes(name, value.orNull)

  def setBigInteger(index: Int, value: BigInteger) = boundStatement.setVarint(index, value)
  def setBigInteger(name: String, value: BigInteger) = boundStatement.setVarint(name, value)
  def setBigIntegerNullable(index: Int, value: Option[BigInteger]) = boundStatement.setVarint(index, value.orNull)
  def setBigIntegerNullable(name: String, value: Option[BigInteger]) = boundStatement.setVarint(name, value.orNull)

  def setBigDecimal(index: Int, value: BigDecimal) = boundStatement.setDecimal(index, value.bigDecimal)
  def setBigDecimal(name: String, value: BigDecimal) = boundStatement.setDecimal(name, value.bigDecimal)
  def setBigDecimalNullable(index: Int, value: Option[BigDecimal]) =  if (value.isDefined)
    boundStatement.setDecimal(index, value.get.bigDecimal)
  else
    boundStatement.setDecimal(index, null)
  def setBigDecimalNullable(name: String, value: Option[BigDecimal]) =  if (value.isDefined)
    boundStatement.setDecimal(name, value.get.bigDecimal)
  else
    boundStatement.setDecimal(name, null)

  def setUUID(index: Int, value: UUID) = boundStatement.setUUID(index, value)
  def setUUID(name: String, value: UUID) = boundStatement.setUUID(name, value)
  def setUUIDNullable(index: Int, value: Option[UUID]) = boundStatement.setUUID(index, value.orNull)
  def setUUIDNullable(name: String, value: Option[UUID]) = boundStatement.setUUID(name, value.orNull)

  def setInetAddress(index: Int, value: InetAddress) = boundStatement.setInet(index, value)
  def setInetAddress(name: String, value: InetAddress) = boundStatement.setInet(name, value)
  def setInetAddressNullable(index: Int, value: Option[InetAddress]) = boundStatement.setInet(index, value.orNull)
  def setInetAddressNullable(name: String, value: Option[InetAddress]) = boundStatement.setInet(name, value.orNull)

  def setList[T: ClassTag](index: Int, value: List[T]) = boundStatement.setList(index, value)
  def setList[T: ClassTag](name: String, value: List[T]) = boundStatement.setList(name, value)

  def setSet[T: ClassTag](index: Int, value: Set[T]) = boundStatement.setSet(index, value)
  def setSet[T: ClassTag](name: String, value: Set[T]) = boundStatement.setSet(name, value)

  def setMap[K: ClassTag, V: ClassTag](index: Int, value: Map[K,V]) = boundStatement.setMap(index, value)
  def setMap[K: ClassTag, V: ClassTag](name: String, value: Map[K,V]) = boundStatement.setMap(name, value)
}
