import eu.inn.binders.naming.PlainConverter
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{UUID, Date}
import org.scalatest.{FlatSpec, Matchers}
import eu.inn.binders._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock

class TestStatementSpec extends FlatSpec with Matchers {

  val (yesterday, now) = {
    import java.util._
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.add(Calendar.DATE, -11)
    (cal.getTime(), new Date())
  }

  case class TestInt(i1: Int, i2: Option[Int], i3: Option[Int])

  "Row " should " bind int fields " in {
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val s = mock[com.datastax.driver.core.Session]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestInt(10, Some(20), None))

    verify(cr).setInt("i1", 10)
    verify(cr).setInt("i2", 20)
    verify(cr).setBytesUnsafe("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind int parameters " in {
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val s = mock[com.datastax.driver.core.Session]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, 10)
    br.bindParameter(1, Some(20))
    br.bindParameter(2, None.asInstanceOf[Option[Int]])

    verify(cr).setInt(0, 10)
    verify(cr).setInt(1, 20)
    verify(cr).setBytesUnsafe(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestLong(i1: Long, i2: Option[Long], i3: Option[Long])

  "Row " should " bind long fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestLong(10l, Some(20l), None))

    verify(cr).setLong("i1", 10)
    verify(cr).setLong("i2", 20)
    verify(cr).setBytesUnsafe("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind long parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, 10l)
    br.bindParameter(1, Some(20l))
    br.bindParameter(2, None.asInstanceOf[Option[Long]])

    verify(cr).setLong(0, 10l)
    verify(cr).setLong(1, 20l)
    verify(cr).setBytesUnsafe(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestString(i1: String, i2: Option[String], i3: Option[String], i4: Option[String])

  "Row " should " bind string fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestString("10", Some("20"), None, Some(null)))

    verify(cr).setString("i1", "10")
    verify(cr).setString("i2", "20")
    verify(cr).setString("i3", null)
    verify(cr).setString("i4", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind string parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, "10")
    br.bindParameter(1, Some("20"))
    br.bindParameter(2, None.asInstanceOf[Option[String]])
    br.bindParameter(3, Some(null.asInstanceOf[String]))

    verify(cr).setString(0, "10")
    verify(cr).setString(1, "20")
    verify(cr).setString(2, null)
    verify(cr).setString(3, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestDate(i1: Date, i2: Option[Date], i3: Option[Date])

  "Row " should " bind date fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestDate(yesterday, Some(now), None))

    verify(cr).setDate("i1", yesterday)
    verify(cr).setDate("i2", now)
    verify(cr).setDate("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind date parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, yesterday)
    br.bindParameter(1, now)
    br.bindParameter(2, null.asInstanceOf[Date])

    verify(cr).setDate(0, yesterday)
    verify(cr).setDate(1, now)
    verify(cr).setDate(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestBoolean(i1: Boolean, i2: Option[Boolean], i3: Option[Boolean])

  "Row " should " bind boolean fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestBoolean(true, Some(false), None))

    verify(cr).setBool("i1", true)
    verify(cr).setBool("i2", false)
    verify(cr).setBytesUnsafe("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind boolean parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, true)
    br.bindParameter(1, false)
    br.bindParameter(2, None.asInstanceOf[Option[Boolean]])

    verify(cr).setBool(0, true)
    verify(cr).setBool(1, false)
    verify(cr).setBytesUnsafe(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestFloat(i1: Float, i2: Option[Float], i3: Option[Float])

  "Row " should " nbind float fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestFloat(1.0f, Some(2.0f), None))

    verify(cr).setFloat("i1", 1.0f)
    verify(cr).setFloat("i2", 2.0f)
    verify(cr).setBytesUnsafe("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind float parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, 1.0f)
    br.bindParameter(1, 2.0f)
    br.bindParameter(2, None.asInstanceOf[Option[Float]])

    verify(cr).setFloat(0, 1.0f)
    verify(cr).setFloat(1, 2.0f)
    verify(cr).setBytesUnsafe(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestDouble(i1: Double, i2: Option[Double], i3: Option[Double])

  "Row " should " bind double fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestDouble(1.0, Some(2.0), None))

    verify(cr).setDouble("i1", 1.0)
    verify(cr).setDouble("i2", 2.0)
    verify(cr).setBytesUnsafe("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind double parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, 1.0)
    br.bindParameter(1, 2.0)
    br.bindParameter(2, None.asInstanceOf[Option[Double]])

    verify(cr).setDouble(0, 1.0)
    verify(cr).setDouble(1, 2.0)
    verify(cr).setBytesUnsafe(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestBytes(i1: ByteBuffer, i2: Option[ByteBuffer], i3: Option[ByteBuffer])

  "Row " should " bind ByteBuffer fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestBytes(ByteBuffer.wrap(Array[Byte](1, 2, 3)), Some(ByteBuffer.wrap(Array[Byte](5, 6, 7))), None))

    verify(cr).setBytes("i1", ByteBuffer.wrap(Array[Byte](1, 2, 3)))
    verify(cr).setBytes("i2", ByteBuffer.wrap(Array[Byte](5, 6, 7)))
    verify(cr).setBytes("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind ByteBuffer parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, ByteBuffer.wrap(Array[Byte](1, 2, 3)))
    br.bindParameter(1, Some(ByteBuffer.wrap(Array[Byte](5, 6, 7))))
    br.bindParameter(2, null.asInstanceOf[ByteBuffer])

    verify(cr).setBytes(0, ByteBuffer.wrap(Array[Byte](1, 2, 3)))
    verify(cr).setBytes(1, ByteBuffer.wrap(Array[Byte](5, 6, 7)))
    verify(cr).setBytes(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestBigInteger(i1: BigInteger, i2: Option[BigInteger], i3: Option[BigInteger])

  "Row " should " unbind BigInteger fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestBigInteger(new BigInteger("123"), Some(new BigInteger("567")), None))

    verify(cr).setVarint("i1", new BigInteger("123"))
    verify(cr).setVarint("i2", new BigInteger("567"))
    verify(cr).setVarint("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind BigInteger parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, new BigInteger("123"))
    br.bindParameter(1, new BigInteger("567"))
    br.bindParameter(2, null.asInstanceOf[BigInteger])

    verify(cr).setVarint(0, new BigInteger("123"))
    verify(cr).setVarint(1, new BigInteger("567"))
    verify(cr).setVarint(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestBigDecimal(i1: BigDecimal, i2: Option[BigDecimal], i3: Option[BigDecimal])

  "Row " should " bind BigDecimal fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestBigDecimal(BigDecimal("123"), Some(BigDecimal("567")), None))

    verify(cr).setDecimal("i1", BigDecimal("123").bigDecimal)
    verify(cr).setDecimal("i2", BigDecimal("567").bigDecimal)
    verify(cr).setDecimal("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind BigDecimal parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, BigDecimal("123"))
    br.bindParameter(1, BigDecimal("567"))
    br.bindParameter(2, None.asInstanceOf[Option[BigDecimal]])

    verify(cr).setDecimal(0, BigDecimal("123").bigDecimal)
    verify(cr).setDecimal(1, BigDecimal("567").bigDecimal)
    verify(cr).setDecimal(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestUUID(i1: UUID, i2: Option[UUID], i3: Option[UUID])

  "Row " should " bind UUID fields " in {
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestUUID(uuid1, Some(uuid2), None))

    verify(cr).setUUID("i1", uuid1)
    verify(cr).setUUID("i2", uuid2)
    verify(cr).setUUID("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind UUID parameters " in {
    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, uuid1)
    br.bindParameter(1, uuid2)
    br.bindParameter(2, None.asInstanceOf[Option[UUID]])

    verify(cr).setUUID(0, uuid1)
    verify(cr).setUUID(1, uuid2)
    verify(cr).setUUID(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestInetAddress(i1: InetAddress, i2: Option[InetAddress], i3: Option[InetAddress])

  "Row " should " bind InetAddress fields " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestInetAddress(InetAddress.getLocalHost, Some(InetAddress.getLoopbackAddress), None))

    verify(cr).setInet("i1", InetAddress.getLocalHost)
    verify(cr).setInet("i2", InetAddress.getLoopbackAddress)
    verify(cr).setInet("i3", null)
    verifyNoMoreInteractions(cr)
  }

  "Row " should " bind InetAddress parameters " in {
    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bindParameter(0, InetAddress.getLocalHost)
    br.bindParameter(1, Some(InetAddress.getLoopbackAddress))
    br.bindParameter(2, None.asInstanceOf[Option[InetAddress]])

    verify(cr).setInet(0, InetAddress.getLocalHost)
    verify(cr).setInet(1, InetAddress.getLoopbackAddress)
    verify(cr).setInet(2, null)
    verifyNoMoreInteractions(cr)
  }

  case class TestList(i1: List[Int], i2: List[String], i3: List[Date])

  "Row " should " bind list fields " in {
    import scala.collection.JavaConversions._

    val lst1 = List(1, 2, 3)
    val lst2 = List("1", "2", "3")
    val lst3 = List(yesterday, now)

    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)

    br.bind(TestList(lst1, lst2, lst3))

    verify(cr).setList("i1", lst1)
    verify(cr).setList("i2", lst2)
    verify(cr).setList("i3", lst3)
    verifyNoMoreInteractions(cr)
  }

  case class TestSet(i1: Set[Int], i2: Set[String], i3: Set[Date])

  "Row " should " unbind set fields " in {
    import scala.collection.JavaConversions._

    val set1 = Set(1, 2, 3)
    val set2 = Set("1", "2", "3")
    val set3 = Set(yesterday, now)

    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestSet(set1, set2, set3))

    verify(cr).setSet("i1", set1)
    verify(cr).setSet("i2", set2)
    verify(cr).setSet("i3", set3)
    verifyNoMoreInteractions(cr)
  }

  case class TestMap(i1: Map[Int, String], i2: Map[Long, Date])

  "Row " should " unbind map fields " in {
    import scala.collection.JavaConversions._

    val map1 = Map(1 -> "11", 2 -> "22")
    val map2 = Map(0l -> yesterday, 1l -> now)

    val s = mock[com.datastax.driver.core.Session]
    val cr = mock[com.datastax.driver.core.BoundStatement]
    val br = new eu.inn.binders.cassandra.Statement[PlainConverter](s, cr)
    br.bind(TestMap(map1, map2))

    verify(cr).setMap("i1", map1)
    verify(cr).setMap("i2", map2)
    verifyNoMoreInteractions(cr)
  }
}