import eu.inn.binders.naming.PlainConverter
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{UUID, Date}
import org.scalatest.{FlatSpec, Matchers}
import eu.inn.binders._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock


class TestRowSpec extends FlatSpec with Matchers {

  val (yesterday, now) = {
    import java.util._
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.add(Calendar.DATE, -11)
    (cal.getTime(), new Date())
  }

  case class TestInt(i1: Int, i2: Option[Int], i3: Option[Int])

  "Row " should " unbind int fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getInt("i1")).thenReturn(10)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getInt("i2")).thenReturn(20)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getInt("i3")).thenReturn(0)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestInt]
    assert(t == TestInt(10, Some(20), None))
  }

  case class TestLong(i1: Long, i2: Option[Long], i3: Option[Long])

  "Row " should " unbind long fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getLong("i1")).thenReturn(10)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getLong("i2")).thenReturn(20)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getLong("i3")).thenReturn(0)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestLong]
    assert(t == TestLong(10, Some(20), None))
  }

  case class TestString(i1: String, i2: Option[String], i3: Option[String], i4: Option[String])

  "Row " should " unbind string fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getString("i1")).thenReturn("10")
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getString("i2")).thenReturn("20")
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getString("i3")).thenReturn("0")
    when(cr.isNull("i4")).thenReturn(false)
    when(cr.getString("i4")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestString]
    assert(t == TestString("10", Some("20"), None, None))
  }

  case class TestDate(i1: Date, i2: Option[Date], i3: Option[Date])

  "Row " should " unbind date fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getDate("i1")).thenReturn(yesterday)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getDate("i2")).thenReturn(now)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getDate("i3")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestDate]
    assert(t == TestDate(yesterday, Some(now), None))
  }

  case class TestBoolean(i1: Boolean, i2: Option[Boolean], i3: Option[Boolean])

  "Row " should " unbind boolean fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getBool("i1")).thenReturn(true)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getBool("i2")).thenReturn(false)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getBool("i3")).thenReturn(false)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestBoolean]
    assert(t == TestBoolean(true, Some(false), None))
  }

  case class TestFloat(i1: Float, i2: Option[Float], i3: Option[Float])

  "Row " should " unbind float fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getFloat("i1")).thenReturn(1.0f)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getFloat("i2")).thenReturn(2.0f)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getFloat("i3")).thenReturn(0)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestFloat]
    assert(t == TestFloat(1.0f, Some(2.0f), None))
  }

  case class TestDouble(i1: Double, i2: Option[Double], i3: Option[Double])

  "Row " should " unbind double fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getDouble("i1")).thenReturn(1.0)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getDouble("i2")).thenReturn(2.0)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getDouble("i3")).thenReturn(0)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestDouble]
    assert(t == TestDouble(1.0, Some(2.0), None))
  }

  case class TestBytes(i1: ByteBuffer, i2: Option[ByteBuffer], i3: Option[ByteBuffer])

  "Row " should " unbind ByteBuffer fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getBytes("i1")).thenReturn(ByteBuffer.wrap(Array[Byte](1, 2, 3)))
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getBytes("i2")).thenReturn(ByteBuffer.wrap(Array[Byte](5, 6, 7)))
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getBytes("i3")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestBytes]
    assert(t == TestBytes(ByteBuffer.wrap(Array[Byte](1, 2, 3)), Some(ByteBuffer.wrap(Array[Byte](5, 6, 7))), None))
  }

  case class TestBigInteger(i1: BigInteger, i2: Option[BigInteger], i3: Option[BigInteger])

  "Row " should " unbind BigInteger fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getVarint("i1")).thenReturn(new BigInteger("123"))
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getVarint("i2")).thenReturn(new BigInteger("567"))
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getVarint("i3")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestBigInteger]
    assert(t == TestBigInteger(new BigInteger("123"), Some(new BigInteger("567")), None))
  }

  case class TestBigDecimal(i1: BigDecimal, i2: Option[BigDecimal], i3: Option[BigDecimal])

  "Row " should " unbind BigDecimal fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getDecimal("i1")).thenReturn(BigDecimal("123").bigDecimal)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getDecimal("i2")).thenReturn(BigDecimal("567").bigDecimal)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getDecimal("i3")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestBigDecimal]
    assert(t == TestBigDecimal(BigDecimal("123"), Some(BigDecimal("567")), None))
  }

  case class TestUUID(i1: UUID, i2: Option[UUID], i3: Option[UUID])

  "Row " should " unbind UUID fields " in {
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getUUID("i1")).thenReturn(uuid1)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getUUID("i2")).thenReturn(uuid2)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getUUID("i3")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestUUID]
    assert(t == TestUUID(uuid1, Some(uuid2), None))
  }

  case class TestInetAddress(i1: InetAddress, i2: Option[InetAddress], i3: Option[InetAddress])

  "Row " should " unbind InetAddress fields " in {
    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getInet("i1")).thenReturn(InetAddress.getLocalHost)
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getInet("i2")).thenReturn(InetAddress.getLoopbackAddress)
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getInet("i3")).thenReturn(null)

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestInetAddress]
    assert(t == TestInetAddress(InetAddress.getLocalHost, Some(InetAddress.getLoopbackAddress), None))
  }

  case class TestList(i1: List[Int], i2: List[String], i3: List[Date])

  "Row " should " unbind list fields " in {
    import scala.collection.JavaConversions._

    val cr = mock[com.datastax.driver.core.Row]
    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)

    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getList[Int]("i1", classOf[Int])).thenReturn(List(1, 2, 3))
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getList[String]("i2", classOf[String])).thenReturn(List("1", "2", "3"))
    when(cr.isNull("i3")).thenReturn(true)
    when(cr.getList[Date]("i3", classOf[Date])).thenReturn(List(yesterday, now))

    val t = br.unbind[TestList]
    assert(t == TestList(List(1, 2, 3), List("1", "2", "3"), List(yesterday, now)))
  }

  case class TestSet(i1: Set[Int], i2: Set[String], i3: Set[Date])

  "Row " should " unbind set fields " in {
    import scala.collection.JavaConversions._

    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getSet[Int]("i1", classOf[Int])).thenReturn(Set(1, 2, 3))
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getSet[String]("i2", classOf[String])).thenReturn(Set("1", "2", "3"))
    when(cr.isNull("i3")).thenReturn(false)
    when(cr.getSet[Date]("i3", classOf[Date])).thenReturn(Set(yesterday, now))

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestSet]
    assert(t == TestSet(Set(1, 2, 3), Set("1", "2", "3"), Set(yesterday, now)))
  }

  case class TestMap(i1: Map[Int, String], i2: Map[Long, Date])

  "Row " should " unbind map fields " in {
    import scala.collection.JavaConversions._

    val cr = mock[com.datastax.driver.core.Row]
    when(cr.isNull("i1")).thenReturn(false)
    when(cr.getMap[Int, String]("i1", classOf[Int], classOf[String])).thenReturn(Map(1 -> "11", 2 -> "22"))
    when(cr.isNull("i2")).thenReturn(false)
    when(cr.getMap[Long, Date]("i2", classOf[Long], classOf[Date])).thenReturn(Map(0l -> yesterday, 1l -> now))

    val br = new eu.inn.binders.cassandra.Row[PlainConverter](cr)
    val t = br.unbind[TestMap]
    assert(t == TestMap(Map(1 -> "11", 2 -> "22"), Map(0l -> yesterday, 1l -> now)))
  }

}
