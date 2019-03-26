object `implicit-match-ambiguous-bind` {
  case class Box[T](value: T)
  implicit val ibox: Box[Int] = Box(0)
  implicit val sbox: Box[String] = Box("")
  inline def unbox = implicit match {
    case b: Box[t] => b.value // error
  }
  val unboxed = unbox
}
