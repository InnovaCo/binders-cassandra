package eu.inn.binders.cassandra.internal

import scala.concurrent.{Future, ExecutionContext}
import scala.language.experimental.macros
import scala.language.reflectiveCalls
import scala.reflect.macros.Context

import eu.inn.binders.cassandra.{DynamicQuery, Statement}
import eu.inn.binders.naming.Converter


object CqlMacro {
  def cql[C <: Converter : c.WeakTypeTag]
  (c: Context)
    (args: c.Expr[Any]*)
    (sessionQueryCache: c.Expr[eu.inn.binders.cassandra.SessionQueryCache[C]]): c.Expr[Statement[C]] = {
    import c.universe._

    // Extract and format CQL query string from StringContext (which is this)
    val strings: List[String] = c.prefix.tree match {
      case Apply(_, List(Apply(_, stringParts))) =>
        stringParts.map(_.asInstanceOf[Literal].value.value.toString)
      case _ => c.abort(c.enclosingPosition, "Invalid CQL!")
    }

    // query code (static/dynamic)
    val queryCode = getQueryCode[C](c)(strings, args)

    val queryTerm = TermName(c.freshName("$qry"))
    val queryVal = ValDef(Modifiers(), queryTerm, TypeTree(),
      reify(
        sessionQueryCache.splice.createQuery(
          c.Expr[String](queryCode._1).splice
        )
      ).tree
    )

    val stmtTerm = TermName(c.freshName("$stmt"))
    val stmtVal = ValDef(Modifiers(), stmtTerm, TypeTree(),
      Select(Ident(queryTerm), TermName("createStatement"))
    )

    val staticArgs = args.filterNot(_.actualType <:< typeOf[DynamicQuery])
    val bindArgsCall: List[c.Tree] = if (staticArgs.nonEmpty) {
      List(Apply(Select(Ident(stmtTerm), TermName("bindArgs")), staticArgs.map(_.tree).toList))
    } else {
      Nil
    }

    val block = Block(
      queryCode._2 ++ List(queryVal, stmtVal) ++ bindArgsCall,
      Ident(stmtTerm)
    )

    //println(block)
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
        Select(Ident(TermName("rows")), TermName("unbindOne")),
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
        Select(Ident(TermName("rows")), TermName("unbindAll")),
        List(Ident(weakTypeOf[O].typeSymbol))
      )

    val tree = executeAndMap[S, O](c)(apply)
    c.Expr[Future[Iterator[O]]](tree)
  }

  private def executeAndMap[S: c.WeakTypeTag, O: c.WeakTypeTag](c: Context)(map: c.universe.Tree): c.universe.Tree = {
    import c.universe._

    val stmtTerm = TermName(c.freshName("$stmt"))
    val stmtVal = ValDef(Modifiers(), stmtTerm, TypeTree(), Select(c.prefix.tree, TermName("stmt")))

    val mapCall = Apply(
      //stmt.execute.map(
      Select(Select(Ident(stmtTerm), TermName("execute")), newTermName("map")),
      List(
        Function(
          // rows =>
          List(ValDef(Modifiers(Flag.PARAM), TermName("rows"), TypeTree(), EmptyTree)),
          map
        )
      )
    )

    val vals = List(stmtVal)
    val block = Block(vals, mapCall)
    // println(block)
    block
  }

  private def getQueryCode[C <: Converter : c.WeakTypeTag]
  (c: Context)
  (strings: List[String], args: Seq[c.type#Expr[Any]]) : (c.Tree, List[c.Tree]) = {
    import c.universe._

    if (args.exists(_.actualType <:< typeOf[DynamicQuery])) {
      getDynamicQueryCode[C](c)(strings,args)
    }else {
      getStaticQueryCode[C](c)(strings,args)
    }
  }

  private def getDynamicQueryCode[C <: Converter : c.WeakTypeTag]
  (c: Context)
  (strings: List[String], args: Seq[c.type#Expr[Any]]) : (c.Tree, List[c.Tree]) = {
    import c.universe._

    val sbTerm = TermName(c.freshName("$sb"))
    val sbVal = ValDef(Modifiers(), sbTerm, TypeTree(),
      Apply(Select(New(
        Select(
          Select(Select(Ident(TermName("scala")), TermName("collection")), TermName("mutable")),
          TypeName("StringBuilder")
        )
      ),
        termNames.CONSTRUCTOR),
        List()
      )
    )

    val argsIterator = args.iterator
    val appendCalls =
      strings.map { s =>
        if (argsIterator.hasNext) {
          val stringContextArg = argsIterator.next()
          if (stringContextArg.actualType <:< typeOf[DynamicQuery]) {
            List(
              Apply(Select(Ident(sbTerm), TermName("append")), List(Literal(Constant(s)))),
              Apply(Select(Ident(sbTerm), TermName("append")), List(
                Select(stringContextArg.tree, TermName("getDynamicQuery"))
              ))
            )
          }
          else {
            List(
              Apply(Select(Ident(sbTerm), TermName("append")), List(Literal(Constant(s)))),
              Apply(Select(Ident(sbTerm), TermName("append")), List(Literal(Constant("?"))))
            )
          }
        }
        else {
          List(Apply(Select(Ident(sbTerm), TermName("append")), List(Literal(Constant(s)))))
        }
      }.flatten

    (Apply(Select(Ident(sbTerm), TermName("toString")), List()), List(sbVal) ++ appendCalls)
  }

  private def getStaticQueryCode[C <: Converter : c.WeakTypeTag]
  (c: Context)
  (strings: List[String], args: Seq[c.type#Expr[Any]]) : (c.Tree, List[c.Tree]) = {
    import c.universe._

    val queryString = strings.mkString("?")
    (Literal(Constant(queryString)), List())
  }

  private def throwNoRows[O: c.WeakTypeTag](c: Context): List[c.Tree] = {
    import c.universe._
    List(Throw(Apply(Select(New(
      Select(
        Select(Select(Select(Ident(TermName("eu")), TermName("inn")), TermName("binders")), TermName("cassandra")),
        TypeName("NoRowsSelectedException")
      )
    ),
      termNames.CONSTRUCTOR),
      List(Literal(Constant(weakTypeOf[O].typeSymbol.fullName)))
    )))
  }
}
