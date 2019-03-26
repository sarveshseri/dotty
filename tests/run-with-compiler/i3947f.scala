
import scala.quoted._
import scala.quoted.Toolbox.Default._

object Test {

  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make

    def test[T: Type](clazz: java.lang.Class[T]): Unit = {
      val lclazz = clazz.toExpr
      val name = '{ ($lclazz).getCanonicalName }
      println()
      println(name.show)
      println(name.run)
    }

    // class Array[Object]
    test(classOf[Array[Any]])
    test(classOf[Array[AnyVal]])
    test(classOf[Array[AnyRef]])
    test(classOf[Array[Object]])
  }

}

class Foo
