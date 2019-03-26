package enums

enum List[+T] {
  case Cons[T](x: T, xs: List[T]) // error: missing extends
  case Snoc[U](xs: List[U], x: U) // error: missing extends
}

enum E1[T] {
  case C // error: cannot determine type argument
}

enum E2[+T, +U >: T] {
  case C // error: cannot determine type argument
}

enum E3[-T <: Ordered[T]] {
  case C // error: cannot determine type argument
}

enum E4 {
  case C
}

case class C4() extends E4 // error: cannot extend enum
case object O4 extends E4 // error: cannot extend enum

enum Option[+T] derives Eql {
  case Some(x: T)
  case None
}

object Test {

  class Unrelated

  val x: Option[Int] = Option.Some(1)
  x == new Unrelated // error: cannot compare

}

