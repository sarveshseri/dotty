
import quoted._
import scala.quoted.Toolbox.Default._

object Test {
  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make

    val q = f(g(Type.IntTag))
    println(q.run)
    println(q.show)
  }

  def f(t: Type[List[Int]]): Expr[Int] = '{
    def ff: Int = {
      val a: $t = {
        type T = $t
        val b: T = 3 :: Nil
        b
      }
      a.head
    }
    ff
  }

  def g[T](a: Type[T]): Type[List[T]] = '[List[$a]]
}
