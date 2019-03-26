trait Assoc[T] {
  type U
  def foo(t: T): U
}

trait Link[T, A]

case class Foo(i: Int)
object Foo {
  println(s"Foo companion")

  erased implicit val barLink: Link[Foo, FooAssoc.type] = null
}

implicit object FooAssoc extends Assoc[Foo] {
  println(s"FooAssoc")

  type U = Int
  def foo(t: Foo): Int = t.i
}

inline def link[T] = implicit match {
  case _: Link[T, s] =>
    implicit match {
      case stuff: s => stuff
    }
}

object Test {
  println(s"Test")

  def main(args: Array[String]): Unit = {
    val foo = Foo(23)
    println(s"foo: $foo")

    val assoc = link[Foo]
    val res: Int = assoc.foo(foo)
    println(s"assoc: ${res}")
  }
}
