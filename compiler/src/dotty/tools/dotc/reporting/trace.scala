package dotty.tools
package dotc
package reporting

import core.Contexts.Context
import config.Config
import config.Printers
import core.Mode

object trace {

  @forceInline
  def onDebug[TD](question: => String)(op: => TD)(implicit ctx: Context): TD =
    conditionally(ctx.settings.YdebugTrace.value, question, false)(op)

  @forceInline
  def conditionally[TC](cond: Boolean, question: => String, show: Boolean)(op: => TC)(implicit ctx: Context): TC =
    if (Config.tracingEnabled) {
      def op1 = op
      if (cond) apply[TC](question, Printers.default, show)(op1)
      else op1
    } else op

  @forceInline
  def apply[T](question: => String, printer: Printers.Printer, showOp: Any => String)(op: => T)(implicit ctx: Context): T =
    if (Config.tracingEnabled) {
      def op1 = op
      if (printer.eq(config.Printers.noPrinter)) op1
      else doTrace[T](question, printer, showOp)(op1)
    }
    else op

  @forceInline
  def apply[T](question: => String, printer: Printers.Printer, show: Boolean)(op: => T)(implicit ctx: Context): T =
    if (Config.tracingEnabled) {
      def op1 = op
      if (printer.eq(config.Printers.noPrinter)) op1
      else doTrace[T](question, printer, if (show) showShowable(_) else alwaysToString)(op1)
    }
    else op

  @forceInline
  def apply[T](question: => String, printer: Printers.Printer)(op: => T)(implicit ctx: Context): T =
    apply[T](question, printer, false)(op)

  @forceInline
  def apply[T](question: => String, show: Boolean)(op: => T)(implicit ctx: Context): T =
    apply[T](question, Printers.default, show)(op)

  @forceInline
  def apply[T](question: => String)(op: => T)(implicit ctx: Context): T =
    apply[T](question, Printers.default, false)(op)

  private def showShowable(x: Any)(implicit ctx: Context) = x match {
    case x: printing.Showable => x.show
    case _ => String.valueOf(x)
  }

  private val alwaysToString = (x: Any) => String.valueOf(x)

  private def doTrace[T](question: => String,
                         printer: Printers.Printer = Printers.default,
                         showOp: Any => String = alwaysToString)
                        (op: => T)(implicit ctx: Context): T = {
    // Avoid evaluating question multiple time, since each evaluation
    // may cause some extra logging output.
    lazy val q: String = question
    apply[T](s"==> $q?", (res: Any) => s"<== $q = ${showOp(res)}")(op)
  }

  def apply[T](leading: => String, trailing: Any => String)(op: => T)(implicit ctx: Context): T =
    if (ctx.mode.is(Mode.Printing)) op
    else {
      var finalized = false
      var logctx = ctx
      while (logctx.reporter.isInstanceOf[StoreReporter]) logctx = logctx.outer
      def finalize(result: Any, note: String) =
        if (!finalized) {
          ctx.base.indent -= 1
          logctx.log(s"${ctx.base.indentTab * ctx.base.indent}${trailing(result)}$note")
          finalized = true
        }
    try {
      logctx.log(s"${ctx.base.indentTab * ctx.base.indent}$leading")
      ctx.base.indent += 1
      val res = op
      finalize(res, "")
      res
    } catch {
      case ex: Throwable =>
        finalize("<missing>", s" (with exception $ex)")
        throw ex
    }
  }
}