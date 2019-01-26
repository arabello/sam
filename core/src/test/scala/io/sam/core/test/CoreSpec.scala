package io.sam.core.test


import java.io._

import io.sam.core.Analyzer._
import io.sam.core._
import org.scalatest.FlatSpec

import scala.io.Source

class CoreSpec extends FlatSpec{

	val resPath = "core/out/test/resources"

	/////////////////////
	///// Traverser /////
	/////////////////////

	"Traverser" should "reads abstract classes" in{

		val sc = SourceCode("~", Source.fromString("package io.core \n trait T{\ntrait T2{}} \n abstract class A{}\n object O{}"))

		var n_abstracts = 0
		object traverser extends Analyzer.Traverser {
			override def traverse(tree: Analyzer.Tree): Unit = tree match {
				case  ClassDef(mods, name, tparams, impl) =>
					if (mods.hasFlag(Flag.ABSTRACT))
						n_abstracts += 1
					super.traverseTrees(tparams)
					super.traverse(impl)
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parseCode(sc)

		traverser.traverse(ast)

		assert(n_abstracts == 3)
	}

	it should "reads PackageDef" in{
		val sb = StringBuilder.newBuilder

		object traverser extends Analyzer.Traverser{
			override def traverse(tree: Analyzer.Tree) = tree match {
				case Analyzer.PackageDef(pid, stats) =>
					sb.append(pid.toString()+"\n")
					super.traverseTrees(stats)
					super.traverse(pid)
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parse("package io.pck.a\ntrait A{}\npackage io.pck.b\ntrait B{}")
		traverser.traverse(ast)

		val results = sb.mkString
		assert(results.contains("io.pck.a\n") && results.contains("io.pck.b\n"))
	}

	it should "reads fully qualified name" in{
		val sb = StringBuilder.newBuilder

		object traverser extends Analyzer.Traverser{
			override def traverse(tree: Analyzer.Tree) = tree match {
				case Analyzer.ClassDef(mods, name, tparams, impl) =>
					println(name)
					super.traverseTrees(tparams)
					super.traverse(impl)
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parse("package io.pck.a{\ntrait A{}}\npackage io.pck.b\ntrait B{}")
		traverser.traverse(ast)

		val results = sb.mkString
		//assert(results.contains("io.pck.a\n") && results.contains("io.pck.b\n"))
	}

	it should "reads ImportDef" in{
		val sb = StringBuilder.newBuilder

		object traverser extends Analyzer.Traverser{
			override def traverse(tree: Analyzer.Tree) = tree match {
				case Analyzer.Import(expr, selectors) =>
					sb.append(expr+"\n")
					super.traverse(expr)
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parse("package io.pck.a\nimport io.pck.b.A\nimport io.pck.b._\nimport io.pck.{h => A}\ntrait A{}")
		traverser.traverse(ast)

		val results = sb.mkString
		assert(results == "io.pck.b\nio.pck.b\nio.pck\n")
	}

	it should "reads module def (object)" in{

		val sc = SourceCode("~", Source.fromString("package io.core \n object O{\nobject O2{}} \n abstract class A{}"))

		var n_object = 0
		object traverser extends Analyzer.Traverser {
			override def traverse(tree: Analyzer.Tree): Unit = tree match {
				case ModuleDef(mods, name, impl) =>
					n_object += 1

					super.traverse(impl)
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parseCode(sc)

		traverser.traverse(ast)

		assert(n_object == 2)
	}

	////////////////////
	///// Entities /////
	////////////////////

	"Entities" should "create SourceCode from file" in{
		val expectedContent = "abstract class A{}"
		val path = s"$resPath/A.scala"
		new PrintWriter(path) { write(expectedContent); close() }

		SourceCode(path, Source.fromFile(path)) match {
			case sc @ SourceCode (_, _) =>
				assert(sc.content == expectedContent)
			case _ => fail()
		}

		Helper.mkSourceCodeFromFile("invalid path") match {
			case isc @ InvalidSourceCode (_) =>
				assert(isc.content.isEmpty)
			case _ => fail()
		}
	}

	it should "create Module from multiple files" in{

		var files = Map[String, String]()
		files += ("A" -> "abstract class A{}")
		files += ("T" -> "trait T{}")
		files += ("C" -> "case class C{}")

		files.foreach{ case (name, content) =>
			val pathA = s"$resPath/$name.scala"
			new PrintWriter(pathA) { write(content); close() }
		}

		Helper.mkModuleFromFolder(resPath) match {
			case cc @ Module(_, _) =>
				assert(cc.content.contains(files("A")))
				assert(cc.content.contains(files("C")))
				assert(cc.content.contains(files("T")))
		}
	}
}
