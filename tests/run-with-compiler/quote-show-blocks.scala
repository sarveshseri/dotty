import scala.quoted._

object Test {
  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make

    def a(n: Int, x: Expr[Unit]): Expr[Unit] =
      if (n == 0) x
      else a(n - 1, '{ println(${n.toExpr}); $x })

    println(a(5, '{}).show)


    def b(n: Int, x: Expr[Unit]): Expr[Unit] =
      if (n == 0) x
      else b(n - 1, '{ $x; println(${n.toExpr}) })

    println(b(5, '{}).show)
  }

}
