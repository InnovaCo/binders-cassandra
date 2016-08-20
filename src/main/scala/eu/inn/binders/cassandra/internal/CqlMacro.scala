package eu.inn.binders.cassandra.internal

import scala.concurrent.{Future, ExecutionContext}
import scala.language.experimental.macros
import scala.language.reflectiveCalls
import scala.reflect.macros.Context

import eu.inn.binders.cassandra.{IfApplied, DynamicQuery, Statement}
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

    val queryTerm = newTermName(c.fresh("$qry"))
    val queryVal = ValDef(Modifiers(), queryTerm, TypeTree(),
      reify(
        sessionQueryCache.splice.createQuery(
          c.Expr[String](queryCode._1).splice
        )
      ).tree
    )

    val stmtTerm = newTermName(c.fresh("$stmt"))
    val stmtVal = ValDef(Modifiers(), stmtTerm, TypeTree(),
      Select(Ident(queryTerm), newTermName("createStatement"))
    )

    val staticArgs = args.filterNot(_.actualType <:< typeOf[DynamicQuery])
    val bindArgsCall: List[c.Tree] = if (staticArgs.nonEmpty) {
      List(Apply(Select(Ident(stmtTerm), newTermName("bindArgs")), staticArgs.map(_.tree).toList))
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
    val tpe = weakTypeOf[O].typeSymbol
    val tree = q"""{
      val t = ${c.prefix.tree}
      t.stmt.execute.map { rows =>
        rows.unbind[Seq[$tpe]].headOption.getOrElse {
          throw new NoRowsSelectedException(${tpe.fullName})
        }
      }
    }"""
    // println(tree)
    c.Expr[Future[O]](tree)
  }

  def oneApplied[S: c.WeakTypeTag, O: c.WeakTypeTag]
  (c: Context)
  (executor: c.Expr[ExecutionContext]): c.Expr[Future[IfApplied[O]]] = {
    import c.universe._
    val tpe = weakTypeOf[O].typeSymbol
    val tree = q"""{
      val t = ${c.prefix.tree}
      t.stmt.execute.map { rows =>
        eu.inn.binders.cassandra.internal.Helpers.checkIfApplied[$tpe](
          rows,
          ${tpe.fullName},
          () => rows.unbind[Seq[$tpe]].headOption
        )
      }
    }"""
    // println(tree)
    c.Expr[Future[IfApplied[O]]](tree)
  }

  def oneOption[S: c.WeakTypeTag, O: c.WeakTypeTag]
  (c: Context)
    (executor: c.Expr[ExecutionContext]): c.Expr[Future[Option[O]]] = {
    import c.universe._

    val tree = q"""{
      val t = ${c.prefix.tree}
      t.stmt.execute.map(rows => rows.unbind[Seq[${weakTypeOf[O].typeSymbol}]].headOption)
    }"""
    // println(tree)
    c.Expr[Future[Option[O]]](tree)
  }

  def all[S: c.WeakTypeTag, O: c.WeakTypeTag]
  (c: Context)
    (executor: c.Expr[ExecutionContext]): c.Expr[Future[Iterator[O]]] = {
    import c.universe._

    val tree = q"""{
      val t = ${c.prefix.tree}
      t.stmt.execute.map(rows => rows.unbind[Iterator[${weakTypeOf[O].typeSymbol}]])
    }"""
    // println(tree)
    c.Expr[Future[Iterator[O]]](tree)
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

    val sbTerm = newTermName(c.fresh("$sb"))
    val sbVal = ValDef(Modifiers(), sbTerm, TypeTree(),
      Apply(Select(New(
        Select(
          Select(Select(Ident(newTermName("scala")), newTermName("collection")), newTermName("mutable")),
          newTypeName("StringBuilder")
        )
      ),
        nme.CONSTRUCTOR),
        List()
      )
    )

    val argsIterator = args.iterator
    val appendCalls =
      strings.flatMap { s =>
        if (argsIterator.hasNext) {
          val stringContextArg = argsIterator.next()
          if (stringContextArg.actualType <:< typeOf[DynamicQuery]) {
            List(
              Apply(Select(Ident(sbTerm), newTermName("append")), List(Literal(Constant(s)))),
              Apply(Select(Ident(sbTerm), newTermName("append")), List(
                Select(stringContextArg.tree, newTermName("getDynamicQuery"))
              ))
            )
          }
          else {
            List(
              Apply(Select(Ident(sbTerm), newTermName("append")), List(Literal(Constant(s)))),
              Apply(Select(Ident(sbTerm), newTermName("append")), List(Literal(Constant("?"))))
            )
          }
        }
        else {
          List(Apply(Select(Ident(sbTerm), newTermName("append")), List(Literal(Constant(s)))))
        }
      }

    (Apply(Select(Ident(sbTerm), newTermName("toString")), List()), List(sbVal) ++ appendCalls)
  }

  private def getStaticQueryCode[C <: Converter : c.WeakTypeTag]
  (c: Context)
  (strings: List[String], args: Seq[c.type#Expr[Any]]) : (c.Tree, List[c.Tree]) = {
    import c.universe._

    val queryString = strings.mkString("?")
    (Literal(Constant(queryString)), List())
  }
}
