package dotty.tools
package backend
package jvm

import java.io.{ DataOutputStream, FileOutputStream, IOException, OutputStream, File => JFile }
import dotty.tools.io._
import java.util.jar.Attributes.Name
import scala.language.postfixOps

/** Can't output a file due to the state of the file system. */
class FileConflictException(msg: String, val file: AbstractFile) extends IOException(msg)

/** For the last mile: turning generated bytecode in memory into
 *  something you can use.  Has implementations for writing to class
 *  files, jars, and disassembled/javap output.
 */
trait BytecodeWriters {
  val int: BackendInterface
  import int._

  /**
   * @param clsName cls.getName
   */
  def getFile(base: AbstractFile, clsName: String, suffix: String): AbstractFile = {
    def ensureDirectory(dir: AbstractFile): AbstractFile =
      if (dir.isDirectory) dir
      else throw new FileConflictException(s"${base.path}/$clsName$suffix: ${dir.path} is not a directory", dir)
    var dir = base
    val pathParts = clsName.split("[./]").toList
    for (part <- pathParts.init) dir = ensureDirectory(dir) subdirectoryNamed part
    ensureDirectory(dir) fileNamed pathParts.last + suffix
  }
  def getFile(sym: Symbol, clsName: String, suffix: String): AbstractFile =
    getFile(sym.outputDirectory, clsName, suffix)

  def factoryNonJarBytecodeWriter(): BytecodeWriter = {
    val emitAsmp  = int.emitAsmp
    val doDump    = int.dumpClasses
    (emitAsmp.isDefined, doDump.isDefined) match {
      case (false, false) => new ClassBytecodeWriter { }
      case (false, true ) => new ClassBytecodeWriter with DumpBytecodeWriter { }
      case (true,  false) => new ClassBytecodeWriter with AsmpBytecodeWriter
      case (true,  true ) => new ClassBytecodeWriter with AsmpBytecodeWriter with DumpBytecodeWriter { }
    }
  }

  trait BytecodeWriter {
    def writeClass(label: String, jclassName: String, jclassBytes: Array[Byte], outfile: AbstractFile): Unit
    def close(): Unit = ()
  }

  class DirectToJarfileWriter(jfile: JFile) extends BytecodeWriter {
    val jarMainAttrs = mainClass.map(nm => List(Name.MAIN_CLASS -> nm)).getOrElse(Nil)

    val writer = new Jar(jfile).jarWriter(jarMainAttrs: _*)

    def writeClass(label: String, jclassName: String, jclassBytes: Array[Byte], outfile: AbstractFile): Unit = {
      assert(outfile == null,
             "The outfile formal param is there just because ClassBytecodeWriter overrides this method and uses it.")
      val path = jclassName + ".class"
      val out  = writer.newOutputStream(path)

      try out.write(jclassBytes, 0, jclassBytes.length)
      finally out.flush()

      informProgress("added " + label + path + " to jar")
    }
    override def close() = writer.close()
  }

  /*
   * The ASM textual representation for bytecode overcomes disadvantages of javap ouput in three areas:
   *    (a) pickle dingbats undecipherable to the naked eye;
   *    (b) two constant pools, while having identical contents, are displayed differently due to physical layout.
   *    (c) stack maps (classfile version 50 and up) are displayed in encoded form by javap,
   *        their expansion by ASM is more readable.
   *
   * */
  trait AsmpBytecodeWriter extends BytecodeWriter {
    import scala.tools.asm

    private val baseDir = Directory(int.emitAsmp.get).createDirectory()

    private def emitAsmp(jclassBytes: Array[Byte], asmpFile: dotty.tools.io.File): Unit = {
      val pw = asmpFile.printWriter()
      try {
        val cnode = new asm.tree.ClassNode()
        val cr    = new asm.ClassReader(jclassBytes)
        cr.accept(cnode, 0)
        val trace = new scala.tools.asm.util.TraceClassVisitor(new java.io.PrintWriter(new java.io.StringWriter()))
        cnode.accept(trace)
        trace.p.print(pw)
      }
      finally pw.close()
    }

    abstract override def writeClass(label: String, jclassName: String, jclassBytes: Array[Byte], outfile: AbstractFile): Unit = {
      super.writeClass(label, jclassName, jclassBytes, outfile)

      val segments = jclassName.split("[./]")
      val asmpFile = segments.foldLeft(baseDir: Path)(_ / _) changeExtension "asmp" toFile;

      asmpFile.parent.createDirectory()
      emitAsmp(jclassBytes, asmpFile)
    }
  }

  trait ClassBytecodeWriter extends BytecodeWriter {
    def writeClass(label: String, jclassName: String, jclassBytes: Array[Byte], outfile: AbstractFile): Unit = {
      assert(outfile != null,
             "Precisely this override requires its invoker to hand out a non-null AbstractFile.")
      val outstream = new DataOutputStream(outfile.bufferedOutput)

      try outstream.write(jclassBytes, 0, jclassBytes.length)
      finally outstream.close()
      informProgress("wrote '" + label + "' to " + outfile)
    }
  }

  trait DumpBytecodeWriter extends BytecodeWriter {
    val baseDir = Directory(int.dumpClasses.get).createDirectory()

    abstract override def writeClass(label: String, jclassName: String, jclassBytes: Array[Byte], outfile: AbstractFile): Unit = {
      super.writeClass(label, jclassName, jclassBytes, outfile)

      val pathName = jclassName
      val dumpFile = pathName.split("[./]").foldLeft(baseDir: Path) (_ / _) changeExtension "class" toFile;
      dumpFile.parent.createDirectory()
      val outstream = new DataOutputStream(new FileOutputStream(dumpFile.path))

      try outstream.write(jclassBytes, 0, jclassBytes.length)
      finally outstream.close()
    }
  }
}
