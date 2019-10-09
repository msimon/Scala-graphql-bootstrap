package com.dm.macros

import language.experimental.macros
import scala.reflect.macros.whitebox._

object UtilsMacro {
  def isPrimitiveType(c: Context)(tpeSym: c.universe.Symbol) : Boolean = {
    import c.universe._

    tpeSym match {
      case t if t == typeOf[Boolean].typeSymbol => true
      case t if t == typeOf[Short].typeSymbol => true
      case t if t == typeOf[Int].typeSymbol => true
      case t if t == typeOf[Long].typeSymbol => true
      case t if t == typeOf[Float].typeSymbol => true
      case t if t == typeOf[Double].typeSymbol => true
      case t if t == typeOf[Char].typeSymbol => true
      case t if t == typeOf[String].typeSymbol => true
      // Had to hack my way around if user specify `: String` since symbole is not java.lang.String
      case t if t.fullName == "scala.Predef.String" => true

      case _ => false
    }
  }


  def generateParamsMapImpl(c: Context)(params: c.Tree*): c.Tree = {
    import c.universe._

    def wrongTypeException(tpe: c.universe.Type) = throw new Exception(s"generateParamsMapImpl only accept *variable* of primitive type or primitive option type. $tpe not accepted.")

    // will return a List of tree representing tuple (String, Option[_])
    val l = params.foldLeft(List[c.Tree]()) {
      case (acc, tree) => {
        val Ident(TermName(name)) = tree

        tree.tpe match {
          case TypeRef(_, tpeSym, tailTpe) if tpeSym == typeOf[Option[_]].typeSymbol && tailTpe.length > 0 => {
            tailTpe(0) match {
              case TypeRef(_, tpeSym2, List()) if isPrimitiveType(c)(tpeSym2) => q"""($name, $tree)"""::acc
              case tpe => wrongTypeException(tpe)
            }
          }
          case TypeRef(_, tpeSym, List()) if isPrimitiveType(c)(tpeSym) => q"""($name, Some($tree))"""::acc
          case tpe => wrongTypeException(tpe)
        }
      }
    }

    val parameterMapTree = q"""
      List(..$l).foldLeft(scala.collection.immutable.Map[String, String] ()) {
        case (map, (k, vOpt)) => {
          vOpt match {
            case Some(v) => map + (k -> v.toString)
            case None => map
          }
        }
      }
    """

    return parameterMapTree
  }
}

object Utils {
  // This is a very simple macro that takes parameters and create a Map[String, String](paramName -> paramValue)
  def generateParamsMap(params: Any*) : Map[String, String] = macro UtilsMacro.generateParamsMapImpl
}
