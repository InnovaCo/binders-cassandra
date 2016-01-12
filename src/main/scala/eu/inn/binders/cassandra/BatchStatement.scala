package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._

import com.datastax.driver.core.{BatchStatement ⇒ DriverBatchStatement, Statement ⇒ DriverStatement, _}

import eu.inn.binders.naming.Converter

class BatchStatement[C <: Converter : TypeTag](session: Session, val batchType: DriverBatchStatement.Type, statements: DriverStatement *)
  extends AbstractStatement[C, DriverBatchStatement](session, new DriverBatchStatement(batchType)) {
  import scala.collection.JavaConversions._

  statement.addAll(statements)

  override protected def queryString(): String = {
    val allQueryStrings = statement.getStatements.map {
      case b: BoundStatement ⇒ b.preparedStatement().getQueryString
      case s: SimpleStatement ⇒ s.getQueryString
    }
    s"""
       BEGIN $batchType BATCH
        ${allQueryStrings.mkString(";\n")}
       APPLY BATCH
     """
  }
}
