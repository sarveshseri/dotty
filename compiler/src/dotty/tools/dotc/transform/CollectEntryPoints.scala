package dotty.tools.dotc.transform

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core._
import Symbols._
import dotty.tools.dotc.transform.MegaPhase._
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import Types._
import Decorators._
import StdNames._
import dotty.tools.dotc.util.SourcePosition
import dotty.tools.dotc.config.JavaPlatform

class CollectEntryPoints extends MiniPhase {

  /** perform context-dependant initialization */
  override def prepareForUnit(tree: tpd.Tree)(implicit ctx: Context): Context = {
    entryPoints = collection.immutable.TreeSet.empty[Symbol](new SymbolOrdering())
    assert(ctx.platform.isInstanceOf[JavaPlatform], "Java platform specific phase")
    ctx
  }

  private[this] var entryPoints: Set[Symbol] = _

  def getEntryPoints: List[Symbol] = entryPoints.toList

  override def phaseName: String = "collectEntryPoints"
  override def transformDefDef(tree: tpd.DefDef)(implicit ctx: Context): tpd.Tree = {
    if (tree.symbol.owner.isClass && isJavaEntryPoint(tree.symbol)) {
      // collecting symbols for entry points here (as opposed to GenBCode where they are used)
      // has the advantage of saving an additional pass over all ClassDefs.
      entryPoints += tree.symbol
    }
    tree
  }

  def isJavaEntryPoint(sym: Symbol)(implicit ctx: Context): Boolean = {
    def fail(msg: String, pos: SourcePosition = sym.sourcePos) = {
      ctx.warning(
        i"""${sym.name} has a main method with parameter type Array[String], but ${sym.fullName} will not be a runnable program.
           |Reason: $msg""", sym.sourcePos
        // TODO: make this next claim true, if possible
        //   by generating valid main methods as static in module classes
        //   not sure what the jvm allows here
        // + "  You can still run the program by calling it as " + javaName(sym) + " instead."
      )
      false
    }
    def failNoForwarder(msg: String) = {
      fail(s"$msg, which means no static forwarder can be generated.\n")
    }
    val possibles = if (sym.flags is Flags.Module) (sym.info nonPrivateMember nme.main).alternatives else Nil
    val hasApproximate = possibles exists {
      m =>
        m.info match {
          case MethodTpe(_, p :: Nil, _) =>
            p.typeSymbol == defn.ArrayClass
          case _ => false
        }
    }
    def precise(implicit ctx: Context) = {
      val companion = sym.companionClass //sym.asClass.linkedClassOfClass
      if (ctx.platform.hasMainMethod(companion))
        failNoForwarder("companion contains its own main method")
      else if (companion.exists && companion.info.member(nme.main).exists)
      // this is only because forwarders aren't smart enough yet
        failNoForwarder("companion contains its own main method (implementation restriction: no main is allowed, regardless of signature)")
      else if (companion.flags is Flags.Trait)
        failNoForwarder("companion is a trait")
      // Now either succeed, or issue some additional warnings for things which look like
      // attempts to be java main methods.
      else (possibles exists (x => ctx.platform.isMainMethod(x.symbol))) || {
        possibles exists {
          m =>
            m.symbol.info match {
              case t: PolyType =>
                fail("main methods cannot be generic.")
              case t: MethodType =>
                if (t.resultType :: t.paramInfos exists (_.typeSymbol.isAbstractType))
                  fail("main methods cannot refer to type parameters or abstract types.", m.symbol.sourcePos)
                else
                  ctx.platform.isMainMethod(m.symbol) || fail("main method must have exact signature (Array[String])Unit", m.symbol.sourcePos)
              case tp =>
                fail(s"don't know what this is: $tp", m.symbol.sourcePos)
            }
        }
      }
    }

  // At this point it's a module with a main-looking method, so either succeed or warn that it isn't.
  hasApproximate && precise(ctx.withPhase(ctx.erasurePhase))
    // Before erasure so we can identify generic mains.


}

}

class SymbolOrdering(implicit ctx: Context) extends Ordering[Symbol] {
  override def compare(x: Symbol, y: Symbol): Int = {
    x.fullName.toString.compareTo(y.fullName.toString)
  }
}
