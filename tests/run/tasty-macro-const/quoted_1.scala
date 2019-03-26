import scala.quoted._
import scala.tasty._

object Macros {

  inline def natConst(x: Int): Int = ${ natConstImpl('x) }

  def natConstImpl(x: Expr[Int])(implicit reflection: Reflection): Expr[Int] = {
    import reflection._
    val xTree: Term = x.unseal
    xTree match {
      case Term.Inlined(_, _, Term.Literal(Constant.Int(n))) =>
        if (n <= 0)
          throw new QuoteError("Parameter must be natural number")
        xTree.seal[Int]
      case _ =>
        throw new QuoteError("Parameter must be a known constant")
    }
  }

}
