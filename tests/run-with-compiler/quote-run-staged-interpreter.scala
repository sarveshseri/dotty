import scala.quoted._

import scala.quoted.Toolbox.Default._

enum Exp {
  case Num(n: Int)
  case Plus(e1: Exp, e2: Exp)
  case Var(x: String)
  case Let(x: String, e: Exp, in: Exp)
}

object Test {
  import Exp._

  def compile(e: Exp, env: Map[String, Expr[Int]], keepLets: Boolean): Expr[Int] = {
    def compileImpl(e: Exp, env: Map[String, Expr[Int]]): Expr[Int] = e match {
      case Num(n) => n.toExpr
      case Plus(e1, e2) => '{${compileImpl(e1, env)} + ${compileImpl(e2, env)}}
      case Var(x) => env(x)
      case Let(x, e, body) =>
        if (keepLets)
          '{ val y = ${compileImpl(e, env)}; ${compileImpl(body, env + (x -> 'y)) } }
        else
          compileImpl(body, env + (x -> compileImpl(e, env)))
    }
    compileImpl(e, env)
  }


  def main(args: Array[String]): Unit = {
    val exp = Plus(Plus(Num(2), Var("x")), Num(4))
    val letExp = Let("x", Num(3), exp)

    val res1 = '{ (x: Int) => ${compile(exp, Map("x" -> 'x), false)} }


    println(res1.show)

    val fn = res1.run
    println(fn(0))
    println(fn(2))
    println(fn(3))

    println("---")

    val res2 = compile(letExp, Map(), false)
    println(res2.show)
    println(res2.run)

    println("---")

    val res3 = compile(letExp, Map(), true)
    println(res3.show)
    println(res3.run)
  }
}
