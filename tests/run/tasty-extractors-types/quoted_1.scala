import scala.quoted._

import scala.tasty._

object Macros {

  implicit inline def printType[T]: Unit = ${ impl('[T]) }

  def impl[T](x: Type[T])(implicit reflect: Reflection): Expr[Unit] = {
    import reflect._

    val tree = x.unseal
    '{
      println(${tree.show.toExpr})
      println(${tree.tpe.show.toExpr})
      println()
    }
  }
}
