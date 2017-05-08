package dotty
package tools
package dotc

import org.junit.{ Test, BeforeClass, AfterClass }

import java.nio.file._
import java.util.stream.{ Stream => JStream }
import scala.collection.JavaConverters._
import scala.util.matching.Regex
import scala.concurrent.duration._

import vulpix.{ ParallelTesting, SummaryReport, SummaryReporting, TestConfiguration }


class CompilationTests extends ParallelTesting {
  import TestConfiguration._
  import CompilationTests._

  // Test suite configuration --------------------------------------------------

  def maxDuration = 30.seconds
  def numberOfSlaves = 5
  def safeMode = Properties.testsSafeMode
  def isInteractive = SummaryReport.isInteractive
  def testFilter = Properties.testsFilter

  // Positive tests ------------------------------------------------------------

  @Test def compilePos: Unit = {
    compileList("compileStdLib", StdLibSources.whitelisted, scala2Mode.and("-migration", "-Yno-inline")) +
    compileDir("../collection-strawman/src/main", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/ast", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/config", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/core", allowDeepSubtypes) +
    compileDir("../compiler/src/dotty/tools/dotc/transform", allowDeepSubtypes) +
    compileDir("../compiler/src/dotty/tools/dotc/parsing", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/printing", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/reporting", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/typer", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/util", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/io", defaultOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/core", noCheckOptions ++ classPath) +
    compileFile("../tests/pos/nullarify.scala", defaultOptions.and("-Ycheck:nullarify")) +
    compileFile("../tests/pos-scala2/rewrites.scala", scala2Mode.and("-rewrite")).copyToTarget() +
    compileFile("../tests/pos-special/t8146a.scala", allowDeepSubtypes) +
    compileFile("../tests/pos-special/utf8encoded.scala", explicitUTF8) +
    compileFile("../tests/pos-special/utf16encoded.scala", explicitUTF16) +
    compileList(
      "compileMixed",
      List(
        "../tests/pos/B.scala",
        "../scala2-library/src/library/scala/collection/immutable/Seq.scala",
        "../scala2-library/src/library/scala/collection/parallel/ParSeq.scala",
        "../scala2-library/src/library/scala/package.scala",
        "../scala2-library/src/library/scala/collection/GenSeqLike.scala",
        "../scala2-library/src/library/scala/collection/SeqLike.scala",
        "../scala2-library/src/library/scala/collection/generic/GenSeqFactory.scala"
      ),
      defaultOptions
    ) +
    compileFilesInDir("../tests/pos-special/spec-t5545", defaultOptions) +
    compileFile("../scala2-library/src/library/scala/collection/immutable/IndexedSeq.scala", defaultOptions) +
    compileFile("../scala2-library/src/library/scala/collection/parallel/mutable/ParSetLike.scala", defaultOptions) +
    compileList(
      "parSetSubset",
      List(
       "../scala2-library/src/library/scala/collection/parallel/mutable/ParSetLike.scala",
       "../scala2-library/src/library/scala/collection/parallel/mutable/ParSet.scala",
       "../scala2-library/src/library/scala/collection/mutable/SetLike.scala"
      ),
      scala2Mode
    ) +
    compileFilesInDir("../tests/new", defaultOptions) +
    compileFilesInDir("../tests/pos-scala2", scala2Mode) +
    compileFilesInDir("../tests/pos", defaultOptions) +
    compileFile(
      // succeeds despite -Xfatal-warnings because of -nowarn
      "../tests/neg/customArgs/xfatalWarnings.scala",
      defaultOptions.and("-nowarn", "-Xfatal-warnings")
    )
  }.checkCompile()

  @Test def posTwice: Unit = {
    compileFile("../tests/pos/Labels.scala", defaultOptions) +
    compileFilesInDir("../tests/pos-java-interop", defaultOptions) +
    compileFile("../tests/pos/t2168.scala", defaultOptions) +
    compileFile("../tests/pos/erasure.scala", defaultOptions) +
    compileFile("../tests/pos/Coder.scala", defaultOptions) +
    compileFile("../tests/pos/blockescapes.scala", defaultOptions) +
    compileFile("../tests/pos/collections.scala", defaultOptions) +
    compileFile("../tests/pos/functions1.scala", defaultOptions) +
    compileFile("../tests/pos/implicits1.scala", defaultOptions) +
    compileFile("../tests/pos/inferred.scala", defaultOptions) +
    compileFile("../tests/pos/Patterns.scala", defaultOptions) +
    compileFile("../tests/pos/selftypes.scala", defaultOptions) +
    compileFile("../tests/pos/varargs.scala", defaultOptions) +
    compileFile("../tests/pos/vararg-pattern.scala", defaultOptions) +
    compileFile("../tests/pos/opassign.scala", defaultOptions) +
    compileFile("../tests/pos/typedapply.scala", defaultOptions) +
    compileFile("../tests/pos/nameddefaults.scala", defaultOptions) +
    compileFile("../tests/pos/desugar.scala", defaultOptions) +
    compileFile("../tests/pos/sigs.scala", defaultOptions) +
    compileFile("../tests/pos/typers.scala", defaultOptions) +
    compileDir("../tests/pos/typedIdents", defaultOptions) +
    compileFile("../tests/pos/assignments.scala", defaultOptions) +
    compileFile("../tests/pos/packageobject.scala", defaultOptions) +
    compileFile("../tests/pos/overloaded.scala", defaultOptions) +
    compileFile("../tests/pos/overrides.scala", defaultOptions) +
    compileDir("../tests/pos/java-override", defaultOptions) +
    compileFile("../tests/pos/templateParents.scala", defaultOptions) +
    compileFile("../tests/pos/overloadedAccess.scala", defaultOptions) +
    compileFile("../tests/pos/approximateUnion.scala", defaultOptions) +
    compileFilesInDir("../tests/pos/tailcall", defaultOptions) +
    compileShallowFilesInDir("../tests/pos/pos_valueclasses", defaultOptions) +
    compileFile("../tests/pos/subtyping.scala", defaultOptions) +
    compileFile("../tests/pos/i0239.scala", defaultOptions) +
    compileFile("../tests/pos/anonClassSubtyping.scala", defaultOptions) +
    compileFile("../tests/pos/extmethods.scala", defaultOptions) +
    compileFile("../tests/pos/companions.scala", defaultOptions) +
    compileList(
      "testNonCyclic",
      List(
        "../compiler/src/dotty/tools/dotc/CompilationUnit.scala",
        "../compiler/src/dotty/tools/dotc/core/Types.scala",
        "../compiler/src/dotty/tools/dotc/ast/Trees.scala"
      ),
      defaultOptions.and("-Xprompt")
    ) +
    compileList(
      "testIssue34",
      List(
        "../compiler/src/dotty/tools/dotc/config/Properties.scala",
        "../compiler/src/dotty/tools/dotc/config/PathResolver.scala"
      ),
      defaultOptions.and("-Xprompt")
    )
  }.times(2).checkCompile()

  // Negative tests ------------------------------------------------------------

  @Test def compileNeg: Unit = {
    compileShallowFilesInDir("../tests/neg", defaultOptions) +
    compileFile("../tests/neg/customArgs/typers.scala", allowDoubleBindings) +
    compileFile("../tests/neg/customArgs/overrideClass.scala", scala2Mode) +
    compileFile("../tests/neg/customArgs/autoTuplingTest.scala", defaultOptions.and("-language:noAutoTupling")) +
    compileFile("../tests/neg/customArgs/i1050.scala", defaultOptions.and("-strict")) +
    compileFile("../tests/neg/customArgs/i1240.scala", allowDoubleBindings) +
    compileFile("../tests/neg/customArgs/i2002.scala", allowDoubleBindings) +
    compileFile("../tests/neg/customArgs/nopredef.scala", defaultOptions.and("-Yno-predef")) +
    compileFile("../tests/neg/customArgs/noimports.scala", defaultOptions.and("-Yno-imports")) +
    compileFile("../tests/neg/customArgs/noimports2.scala", defaultOptions.and("-Yno-imports")) +
    compileFile("../tests/neg/customArgs/overloadsOnAbstractTypes.scala", allowDoubleBindings) +
    compileFile("../tests/neg/customArgs/xfatalWarnings.scala", defaultOptions.and("-Xfatal-warnings")) +
    compileFile("../tests/neg/tailcall/t1672b.scala", defaultOptions) +
    compileFile("../tests/neg/tailcall/t3275.scala", defaultOptions) +
    compileFile("../tests/neg/tailcall/t6574.scala", defaultOptions) +
    compileFile("../tests/neg/tailcall/tailrec.scala", defaultOptions) +
    compileFile("../tests/neg/tailcall/tailrec-2.scala", defaultOptions) +
    compileFile("../tests/neg/tailcall/tailrec-3.scala", defaultOptions) +
    compileDir("../tests/neg/typedIdents", defaultOptions)
  }.checkExpectedErrors()

  // Run tests -----------------------------------------------------------------

  @Test def runAll: Unit =
    compileFilesInDir("../tests/run", defaultOptions).checkRuns()

  // Pickling Tests ------------------------------------------------------------
  //
  // Pickling tests are very memory intensive and as such need to be run with a
  // lower level of concurrency as to not kill their running VMs

  @Test def testPickling: Unit = {
    compileDir("../compiler/src/dotty/tools", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc", picklingOptions) +
    compileFilesInDir("../tests/new", picklingOptions) +
    compileFilesInDir("../tests/pickling", picklingOptions) +
    compileDir("../library/src/dotty/runtime", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/backend/jvm", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/ast", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/core", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/config", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/parsing", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/printing", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/repl", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/rewrite", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/transform", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/typer", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/util", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/io", picklingOptions) +
    compileFile("../tests/pos/pickleinf.scala", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/core/classfile", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/core/tasty", picklingOptions) +
    compileDir("../compiler/src/dotty/tools/dotc/core/unpickleScala2", picklingOptions)
  }.limitThreads(4).checkCompile()

  /** The purpose of this test is two-fold, being able to compile dotty
   *  bootstrapped, and making sure that TASTY can link against a compiled
   *  version of Dotty
   */
  @Test def tastyBootstrap: Unit = {
    val opt = Array(
      "-classpath",
      // compile with bootstrapped library on cp:
      defaultOutputDir + "lib/src/:" +
      // as well as bootstrapped compiler:
      defaultOutputDir + "dotty1/dotty1/:" +
      Jars.dottyInterfaces
    )

    def lib =
      compileDir("../library/src",
        allowDeepSubtypes.and("-Ycheck-reentrant", "-strict", "-priorityclasspath", defaultOutputDir))

    def dotty1 = {
      compileList(
        "dotty1",
        compilerSources ++ backendSources ++ backendJvmSources,
        opt)
    }

    def dotty2 =
      compileShallowFilesInDir("../compiler/src/dotty", opt)

    {
      lib.keepOutput :: dotty1.keepOutput :: {
        dotty2 +
        compileShallowFilesInDir("../compiler/src/dotty/tools", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/ast", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/config", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/parsing", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/printing", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/repl", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/reporting", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/rewrite", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/transform", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/typer", opt) +
        compileShallowFilesInDir("../compiler/src/dotty/tools/dotc/util", opt) +
        compileList("shallow-backend", backendSources, opt) +
        compileList("shallow-backend-jvm", backendJvmSources, opt)
      } :: Nil
    }.map(_.checkCompile()).foreach(_.delete())
  }

  /** Add a `z` so that they run last. TODO: Only run them selectively? */
  @Test def zBytecodeIdempotency: Unit = {
    var failed = 0
    var total = 0
    val blacklisted = Set(
      // Bridges on collections in different order. Second one in scala2 order.
      "pos/Map/scala/collection/immutable/Map",
      "pos/Map/scala/collection/immutable/AbstractMap",
      "pos/t1203a/NodeSeq",
      "pos/i2345/Whatever"
    )
    def checkIdempotency(): Unit = {
      val groupedBytecodeFiles: List[(Path, Path, Path, Path)] = {
        val bytecodeFiles = {
          def bytecodeFiles(paths: JStream[Path]): List[Path] = {
            def isBytecode(file: String) = file.endsWith(".class") || file.endsWith(".tasty")
            paths.iterator.asScala.filter(path => isBytecode(path.toString)).toList
          }
          val compilerDir1 = Paths.get("../out/idempotency1")
          val compilerDir2 = Paths.get("../out/idempotency2")
          bytecodeFiles(Files.walk(compilerDir1)) ++ bytecodeFiles(Files.walk(compilerDir2))
        }
        val groups = bytecodeFiles.groupBy(f => f.toString.substring("../out/idempotencyN/".length, f.toString.length - 6))
        groups.filterNot(x => blacklisted(x._1)).valuesIterator.flatMap { g =>
          def pred(f: Path, i: Int, isTasty: Boolean) =
            f.toString.contains("idempotency" + i) && f.toString.endsWith(if (isTasty) ".tasty" else ".class")
          val class1 = g.find(f => pred(f, 1, isTasty = false))
          val class2 = g.find(f => pred(f, 2, isTasty = false))
          val tasty1 = g.find(f => pred(f, 1, isTasty = true))
          val tasty2 = g.find(f => pred(f, 2, isTasty = true))
          assert(class1.isDefined, "Could not find class in idempotency1 for " + class2)
          assert(class2.isDefined, "Could not find class in idempotency2 for " + class1)
          if (tasty1.isEmpty || tasty2.isEmpty) Nil
          else List(Tuple4(class1.get, tasty1.get, class2.get, tasty2.get))
        }.toList
      }

      for ((class1, tasty1, class2, tasty2) <- groupedBytecodeFiles) {
        total += 1
        val bytes1 = Files.readAllBytes(class1)
        val bytes2 = Files.readAllBytes(class2)
        if (!java.util.Arrays.equals(bytes1, bytes2)) {
          failed += 1
          val tastyBytes1 = Files.readAllBytes(tasty1)
          val tastyBytes2 = Files.readAllBytes(tasty2)
          if (java.util.Arrays.equals(tastyBytes1, tastyBytes2))
            println(s"Idempotency test failed between $class1 and $class1 (same tasty)")
          else
            println(s"Idempotency test failed between $tasty1 and $tasty2")
          /* Dump bytes to console, could be useful if issue only appears in CI.
           * Create the .class locally with Files.write(path, Array[Byte](...)) with the printed array
           */
          // println(bytes1.mkString("Array[Byte](", ",", ")"))
          // println(bytes2.mkString("Array[Byte](", ",", ")"))
        }
      }
    }

    val opt = defaultOptions.and("-YemitTasty")

    def idempotency1() = {
      compileList("dotty1", compilerSources ++ backendSources ++ backendJvmSources, opt) +
      compileFilesInDir("../tests/pos", opt)
    }
    def idempotency2() = {
      compileList("dotty1", compilerSources ++ backendSources ++ backendJvmSources, opt) +
      compileFilesInDir("../tests/pos", opt)
    }

    val tests = (idempotency1() + idempotency2()).keepOutput.checkCompile()

    assert(new java.io.File("../out/idempotency1/").exists)
    assert(new java.io.File("../out/idempotency2/").exists)

    val t0 = System.currentTimeMillis()
    checkIdempotency()
    println(s"checked bytecode idempotency (${(System.currentTimeMillis() - t0) / 1000.0} sec)")

    tests.delete()

    assert(failed == 0, s"Failed $failed idempotency checks (out of $total)")
  }


  private val (compilerSources, backendSources, backendJvmSources) = {
    def sources(paths: JStream[Path], excludedFiles: List[String] = Nil): List[String] =
      paths.iterator().asScala
        .filter(path =>
          (path.toString.endsWith(".scala") || path.toString.endsWith(".java"))
            && !excludedFiles.contains(path.getFileName.toString))
        .map(_.toString).toList

    val compilerDir = Paths.get("../compiler/src")
    val compilerSources0 = sources(Files.walk(compilerDir))

    val backendDir = Paths.get("../scala-backend/src/compiler/scala/tools/nsc/backend")
    val backendJvmDir = Paths.get("../scala-backend/src/compiler/scala/tools/nsc/backend/jvm")

    // NOTE: Keep these exclusions synchronized with the ones in the sbt build (Build.scala)
    val backendExcluded =
      List("JavaPlatform.scala", "Platform.scala", "ScalaPrimitives.scala")
    val backendJvmExcluded =
      List("BCodeICodeCommon.scala", "GenASM.scala", "GenBCode.scala", "ScalacBackendInterface.scala")

    val backendSources0 =
      sources(Files.list(backendDir), excludedFiles = backendExcluded)
    val backendJvmSources0 =
      sources(Files.list(backendJvmDir), excludedFiles = backendJvmExcluded)

    (compilerSources0, backendSources0, backendJvmSources0)
  }
}

object CompilationTests {
  implicit val summaryReport: SummaryReporting = new SummaryReport
  @AfterClass def cleanup(): Unit = summaryReport.echoSummary()
}