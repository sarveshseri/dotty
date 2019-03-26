object Test {
  implied A {
    def (x: Int) |+| (y: Int) = x + y
  }
  implied B {
    def (x: Int) |+| (y: String) = x + y.length
  }
  assert((1 |+| 2) == 3)  // error ambiguous

  locally {
    import B.|+|
    assert((1 |+| "2") == 2)  // OK
  }
}