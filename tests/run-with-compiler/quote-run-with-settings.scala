
import java.nio.file.{Files, Paths}

import scala.quoted.Toolbox.Default._
import scala.quoted.Toolbox

import scala.quoted._

object Test {
  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make
    val expr = '{
      val a = 3
      println("foo")
      2 + a
    }
    println(expr.show)
    println(expr.run)
    println()

    val outDir = Paths.get("out/out-quoted-1")
    val classFile = outDir.resolve("Quoted.class")

    Files.deleteIfExists(classFile)

    {
      implicit val settings = Toolbox.Settings.make(outDir = Some(outDir.toString))
      implicit val toolbox2: scala.quoted.Toolbox = scala.quoted.Toolbox.make
      println(expr.run)
      assert(Files.exists(classFile))
    }
  }
}
