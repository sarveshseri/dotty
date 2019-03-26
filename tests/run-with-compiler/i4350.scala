import scala.quoted.Toolbox.Default._

import scala.quoted.Type

class Foo[T: Type] {
  def q = '{(null: Any).asInstanceOf[T]}
}

object Test {
  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make
    println((new Foo[Object]).q.show)
    println((new Foo[String]).q.show)
  }
}
