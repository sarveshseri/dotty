class Low
object Low {
  implied low for Low
}
class Medium extends Low
object Medium {
  implied medium for Medium
}
class High extends Medium
object High {
  implied high for High
}

class Foo[T](val i: Int)
object Foo {
  def apply[T] given (fooT: Foo[T]): Int = fooT.i

  implied foo[T]    given Low for Foo[T](0)
  implied foobar[T] given Low for Foo[Bar[T]](1)
  implied foobarbaz given Low for Foo[Bar[Baz]](2)
}
class Bar[T]
object Bar {
  implied foobar[T] given Medium for Foo[Bar[T]](3)
  implied foobarbaz given Medium for Foo[Bar[Baz]](4)
}
class Baz
object Baz {
  implied baz given High for Foo[Bar[Baz]](5)
}

object Test extends App {
  assert(Foo[Int] == 0)
  assert(Foo[Bar[Int]] == 3)
  assert(Foo[Bar[Baz]] == 5)
}