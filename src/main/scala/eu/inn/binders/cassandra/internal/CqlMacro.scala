package eu.inn.binders.cassandra.internal

import scala.language.reflectiveCalls
import scala.reflect.macros.Context
import language.experimental.macros
import eu.inn.binders.naming.Converter
import scala.concurrent.{Future, ExecutionContext}
import eu.inn.binders.cassandra.Statement

object CqlMacro {
  def cql[C <: Converter : c.WeakTypeTag]
  (c: Context)
  (args: c.Expr[Any]*)
  (sessionQueryCache: c.Expr[eu.inn.binders.cassandra.SessionQueryCache[C]]): c.Expr[Statement[C]] = {
    import c.universe._

    // Extract and format CQL query string from StringContext (which is this)
    val queryString = c.prefix.tree match {
      case Apply(_, List(Apply(_, stringParts))) =>
        stringParts.map(_.asInstanceOf[Literal].value.value).mkString("?")
      case _ => c.abort(c.enclosingPosition, "Invalid CQL!")
    }

    val queryTerm = newTermName(c.fresh("$qry"))
    val queryVal = ValDef(Modifiers(), queryTerm, TypeTree(),
      reify(
        sessionQueryCache.splice.createQuery(
          c.Expr[String](Literal(Constant(queryString))).splice
        )
      ).tree
    )

    val stmtTerm = newTermName(c.fresh("$stmt"))
    val stmtVal = ValDef(Modifiers(), stmtTerm, TypeTree(),
      Select(Ident(queryTerm), newTermName("createStatement"))
    )

    val bindArgsCall: List[c.Tree] = if (!args.isEmpty) {
      List(Apply(Select(Ident(stmtTerm), newTermName("bindArgs")), args.map(_.tree).toList))
    } else {
      Nil
    }

    val block = Block(
      List(queryVal, stmtVal) ++ bindArgsCall,
      Ident(stmtTerm)
    )

    c.Expr[Statement[C]](block)
  }

  def one[S: c.WeakTypeTag, O: c.WeakTypeTag]
  (c: Context)
  (executor: c.Expr[ExecutionContext]): c.Expr[Future[O]] = {
    import c.universe._

    val apply = Apply(
      Select(
        // rows.unbindOne[O]
        TypeApply(
          Select(Ident(newTermName("rows")), newTermName("unbindOne")),
          List(Ident(weakTypeOf[O].typeSymbol))
        ),
        newTermName("getOrElse")
      ),
      throwNoRows[O](c)
    )

    val tree = executeAndMap[S, O](c)(apply)
    c.Expr[Future[O]](tree)
  }

  def oneOption[S: c.WeakTypeTag, O: c.WeakTypeTag]
  (c: Context)
  (executor: c.Expr[ExecutionContext]): c.Expr[Future[Option[O]]] = {
    import c.universe._

    // rows.unbindOne[O]
    val apply =
      TypeApply(
        Select(Ident(newTermName("rows")), newTermName("unbindOne")),
        List(Ident(weakTypeOf[O].typeSymbol))
      )

    val tree = executeAndMap[S, O](c)(apply)
    c.Expr[Future[Option[O]]](tree)
  }

  def all[S: c.WeakTypeTag, O: c.WeakTypeTag]
  (c: Context)
  (executor: c.Expr[ExecutionContext]): c.Expr[Future[Iterator[O]]] = {
    import c.universe._

    // rows.unbindAll[O]
    val apply =
      TypeApply(
        Select(Ident(newTermName("rows")), newTermName("unbindAll")),
        List(Ident(weakTypeOf[O].typeSymbol))
      )

    val tree = executeAndMap[S, O](c)(apply)
    c.Expr[Future[Iterator[O]]](tree)
  }

  private def executeAndMap[S: c.WeakTypeTag, O: c.WeakTypeTag](c: Context)(map: c.universe.Tree): c.universe.Tree = {
    import c.universe._

    val stmtTerm = newTermName(c.fresh("$stmt"))
    val stmtVal = ValDef(Modifiers(), stmtTerm, TypeTree(), Select(c.prefix.tree, newTermName("stmt")))

    val mapCall = Apply(
      //stmt.execute.map(
      Select(Select(Ident(stmtTerm), newTermName("execute")), newTermName("map")),
      List(
        Function(
          // rows =>
          List(ValDef(Modifiers(Flag.PARAM), newTermName("rows"), TypeTree(), EmptyTree)),
          map
        )
      )
    )

    val vals = List(stmtVal)
    val block = Block(vals, mapCall)
    // println(block)
    block
  }

  private def throwNoRows[O: c.WeakTypeTag](c: Context): List[c.Tree] = {
    import c.universe._
    List(Throw(Apply(Select(New(
      Select(
        Select(Select(Select(Ident(newTermName("eu")), newTermName("inn")), newTermName("binders")), newTermName("cassandra")),
        newTypeName("NoRowsSelectedException")
      )
    ),
      nme.CONSTRUCTOR),
      List(Literal(Constant(weakTypeOf[O].typeSymbol.fullName)))
    )))
  }
}
