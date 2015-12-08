package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._

import com.datastax.driver.core.{Statement, BatchStatement, BoundStatement, Session}

import eu.inn.binders.naming.Converter

class BatchStatementWrapper[C <: Converter : TypeTag](session: Session, val batchType: BatchStatement.Type, statements: Statement *)
  extends StatementWrapper[C, BatchStatement](session, new BatchStatement(batchType)) {
  import scala.collection.JavaConversions._

  statement.addAll(statements)

  override protected def queryString(): String = {
    s"""
       BEGIN $batchType BATCH

       APPLY BATCH
     """
  }
}
