import com.datastax.driver.core.{DataType, ColumnDefinitions}
import org.hamcrest.{Description, BaseMatcher}
import org.mockito.{ArgumentMatcher, Matchers}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

trait CustomMockers extends MockitoSugar {

  def cols(cs: String*) : ColumnDefinitions = {
    val dClass = classOf[ColumnDefinitions.Definition]
    val dCtr = dClass.getDeclaredConstructor(classOf[String], classOf[String], classOf[String], classOf[DataType])
    dCtr.setAccessible(true)

    val list = cs.map { c =>
      val d = dCtr.newInstance("test-keyspace", "test-table", c, DataType.ascii)
      d.asInstanceOf[ColumnDefinitions.Definition]
    }

    val cdClass = classOf[ColumnDefinitions]
    val cdCtr = cdClass.getDeclaredConstructor(classOf[Array[ColumnDefinitions.Definition]])
    cdCtr.setAccessible(true)
    val cd = cdCtr.newInstance(list.toArray)
    cd.asInstanceOf[ColumnDefinitions]
  }

  def row(cs: String*) = {
    val mr = mock[com.datastax.driver.core.Row]
    when(mr.getColumnDefinitions).thenReturn(cols(cs :_*))
    mr
  }

  def stmt(cs: String*) = {
    val mr = mock[com.datastax.driver.core.BoundStatement]
    val mp = mock[com.datastax.driver.core.PreparedStatement]
    when(mp.getVariables).thenReturn(cols(cs:_*))
    when(mr.preparedStatement()).thenReturn(mp)
    mr
  }

  def argMatch[T](f: T ⇒ Any) = argThat(new ArgumentMatcher[T] {

    override def matches(arg: scala.Any): Boolean = {
      f(arg.asInstanceOf[T])
      true
    }
  })
}
