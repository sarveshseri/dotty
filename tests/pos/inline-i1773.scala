object Test {
  implicit class Foo(sc: StringContext) {
    object q {
      inline def unapply(arg: Any): Option[(Any, Any)] =
        Some((sc.parts(0), sc.parts(1)))
    }
  }

  def main(args: Array[String]): Unit = {
    val q"class $name extends $parent" = new Object // now ok, was an error that "method unapply is used"
    println(name)
    println(parent)
  }
}
