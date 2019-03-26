import scala.quoted._

import scala.quoted.Toolbox.Default._

object Test {

  def main(args: Array[String]): Unit = {
    val ack3 = ackermann(3).run
    println(ack3(1))
    println(ack3(2))
    println(ack3(3))
    println(ack3(4))
  }

  def ackermann(m: Int): Expr[Int => Int] = {
    if (m == 0) '{ n => n + 1 }
    else '{ n =>
      def `ackermann(m-1)`(n: Int): Int = ${ackermann(m - 1)('n)} // Expr[Int => Int] applied to Expr[Int]
      def `ackermann(m)`(n: Int): Int =
        if (n == 0) `ackermann(m-1)`(1) else `ackermann(m-1)`(`ackermann(m)`(n - 1))
      `ackermann(m)`(n)
    }
  }

}
