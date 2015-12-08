package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._

import com.datastax.driver.core._

import eu.inn.binders.naming.Converter

class BatchStatementWrapper[C <: Converter : TypeTag](session: Session, val batchType: BatchStatement.Type, statements: Statement *)
  extends StatementWrapper[C, BatchStatement](session, new BatchStatement(batchType)) {
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
