package eu.inn.binders.cassandra.internal

import scala.language.reflectiveCalls
import scala.reflect.macros.Context
import language.experimental.macros
import eu.inn.binders.naming.Converter

object CqlMacro {
  def cql[C <: Converter : c.WeakTypeTag]
  (c: Context)
  (args: c.Expr[Any]*)
  (sessionQueryCache: c.Expr[eu.inn.binders.cassandra.SessionQueryCache[C]]): c.Expr[Any] = {
    import c.universe._

    // Extract and format CQL query string from StringContext (which is this)
    val queryString = c.prefix.tree match {
      case Apply(_, List(Apply(_, stringParts))) =>
        stringParts.map(_.asInstanceOf[Literal].value.value).mkString("?")
      case _ => c.abort(c.enclosingPosition, "Invalid CQL!")
    }

    val queryTerm = newTermName(c.fresh("$qry"))
    val createQueryCall = reify(
      sessionQueryCache.splice.createQuery(
        c.Expr[String](Literal(Constant(queryString))).splice
      )
    ).tree
    val queryVal = ValDef(Modifiers(), queryTerm, TypeTree(), createQueryCall)
    val stmtTerm = newTermName(c.fresh("$stmt"))
    val createStmtCall = Apply(Select(Ident(queryTerm), newTermName("createStatement")), List())
    val stmtVal = ValDef(Modifiers(), stmtTerm, TypeTree(), createStmtCall)

    val bindArgsCall: List[c.Tree] = if (!args.isEmpty) {
      List(Apply(Select(Ident(stmtTerm), newTermName("bindArgs")), args.map(_.tree).toList))
    } else {
      Nil
    }

    val block = Block(
      List(queryVal, stmtVal) ++ bindArgsCall,
      Ident(stmtTerm)
    )

    c.Expr[Any](block)
  }
}
