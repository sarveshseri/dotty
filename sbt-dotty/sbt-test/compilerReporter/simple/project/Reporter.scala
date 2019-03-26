import sbt._
import Keys._
import KeyRanks.DTask

object Reporter {
  import xsbti.{Reporter, Problem, Position, Severity}

  lazy val check = TaskKey[Unit]("check", "make sure compilation info are forwared to sbt")

  // compilerReporter is marked private in sbt
  lazy val compilerReporter = TaskKey[xsbti.Reporter]("compilerReporter", "Experimental hook to listen (or send) compilation failure messages.", DTask)

  lazy val reporter =
    new xsbti.Reporter {
      private val buffer = collection.mutable.ArrayBuffer.empty[Problem]
      def reset(): Unit = buffer.clear()
      def hasErrors: Boolean = buffer.exists(_.severity == Severity.Error)
      def hasWarnings: Boolean = buffer.exists(_.severity == Severity.Warn)
      def printSummary(): Unit = println(problems.mkString(System.lineSeparator))
      def problems: Array[Problem] = buffer.toArray
      def log(problem: Problem): Unit = buffer.append(problem)
      def comment(pos: xsbti.Position, msg: String): Unit = ()
    }

  lazy val checkSettings = Seq(
    compilerReporter in (Compile, compile) := reporter,
    check := (compile in Compile).failure.map(_ => {
      val problems = reporter.problems
      println(problems.toList)
      assert(problems.size == 1)

      // Assert disabled because we don't currently pass positions to sbt
      // See https://github.com/lampepfl/dotty/pull/2107
      // assert(problems.forall(_.position.offset.isDefined))

      assert(problems.count(_.severity == Severity.Error) == 1) // not found: er1,
    }).value
  )
}
