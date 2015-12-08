import com.datastax.driver.core.{Statement, ResultSetFuture, BatchStatement}
import org.mockito.ArgumentMatcher
import org.scalatest.{BeforeAndAfter, Matchers, FlatSpec}

import eu.inn.binders.naming.PlainConverter
import eu.inn.binders.cassandra._
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.collection.JavaConversions._

class TestBatchStatementWrapperSpec extends FlatSpec with Matchers with CustomMockers with BeforeAndAfter {

  val session = mock[com.datastax.driver.core.Session]

  before {
    org.mockito.Mockito.reset(session)
  }

  "BatchStatementWrapper" should "create and fill BatchStatement from plain statements" in {
    val statements = Seq(stmt("s1"), stmt("s2"))
    val br = new eu.inn.binders.cassandra.BatchStatementWrapper[PlainConverter](session, BatchStatement.Type.LOGGED, statements: _*)
    when(session.executeAsync(any(classOf[BatchStatement]))).thenReturn(mock[ResultSetFuture])
    br.execute()

    verify(session).executeAsync(argMatch[BatchStatement] { batchStatement ⇒
      assert(batchStatement.size() == statements.size)
      assert(batchStatement.getStatements.containsAll(statements))
    }
    )
  }


  "BatchStatementWrapper" should "flatten and add statements from BatchStatement" in {
    val statements = Seq(stmt("s1"), stmt("s2"))
    val statementsInBatch = Seq(stmt("b1"), stmt("b2"))

    val batchStatement = new BatchStatement(BatchStatement.Type.LOGGED)
    batchStatement.addAll(statementsInBatch)

    val br = new eu.inn.binders.cassandra.BatchStatementWrapper[PlainConverter](session, BatchStatement.Type.LOGGED, batchStatement +: statements : _*)
    when(session.executeAsync(any(classOf[BatchStatement]))).thenReturn(mock[ResultSetFuture])
    br.execute()

    verify(session).executeAsync(argMatch[BatchStatement] { batchStatement ⇒
        val expectedStatements = statements ++ statementsInBatch
        assert(batchStatement.size() == expectedStatements.size)
        assert(batchStatement.getStatements.containsAll(expectedStatements))
      }
    )
  }

}