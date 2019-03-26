import scala.quoted.Toolbox.Default._
import scala.quoted._
object Test {
  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make

    val x: Expr[Int] = '{3}

    val f4: Expr[Int => Int] = '{
      inlineLambda
    }
    println(f4(x).run)
    println(f4(x).show)
  }

  inline def inlineLambda <: Int => Int = x => x + x
}