package io.sam.core.test


import java.io.File

import io.sam.core.Analyzer._
import io.sam.core._
import org.scalatest.FlatSpec

import scala.io.Source

class CoreSpec extends FlatSpec{

	val resPath = "core/src/test/resources"

	/////////////////////
	///// Traverser /////
	/////////////////////

	"Analyzer" should "reads abstract classes" in{

		val sc = SourceCode("~", "package io.core \n trait T{\ntrait T2{}} \n abstract class A{}\n object O{}")

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
		assert(results == "io.pck.a\nio.pck.b\n")
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

		val sc = SourceCode("~", "package io.core \n object O{\nobject O2{}} \n abstract class A{}")

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

	"CodeFactory" should "make SourceCode from file" in{
		object factory extends CodeFactory{}

		val f = new File(s"$resPath/t")
		val sc = factory.mkCodeFromFile(f)

		sc match{
			case SourceCode(id, content) =>
				assert(id == f.getCanonicalPath)
				assert(content == "package c.c.t\n\ncase class T(val n: Int){\n    val kkjkk = q\n}")
		}
	}

	it should "make SourceCode from source" in{
		object factory extends CodeFactory{}

		val f = Source.fromFile(s"$resPath/t")
		val sc = factory.mkCodeFromSource(s"$resPath/t", f)

		sc match{
			case SourceCode(id, content) =>
				assert(id == s"$resPath/t")
				assert(content == "package c.c.t\n\ncase class T(val n: Int){\n    val kkjkk = q\n}")
		}
	}

	it should "make Component from files" in{
		object factory extends CodeFactory{}
		val m = Map(
			"t" -> new File(s"$resPath/t"),
			"u" -> new File(s"$resPath/u")
		)
		val c = factory.mkCodeFromFiles("test", Set(m("t"), m("u")))

		c match{
			case com @ Component(id, sources) =>
				assert(id == "test")
				val str = StringBuilder.newBuilder
				sources foreach{ src =>
					val s = Source.fromFile(new File(src.id)).mkString
					assert(src.content == s)
					str.append(s+ "\n")
				}

				assert(com.content == str.mkString)
		}
	}

	"Component" should "inserts newline between each given resources (avoiding package lost)" in {
		object factory extends CodeFactory{}

		val t = factory.mkCodeFromFile(new File(s"$resPath/t"))
		val u = factory.mkCodeFromFile(new File(s"$resPath/u"))
		val Cc = Component("Cc", Set(t, u))

		object packageTraverser extends Analyzer.Traverser{
			var packages = Set[String]()

			override def traverse(tree: Analyzer.Tree) = tree match {
				case Analyzer.PackageDef(pid, stats) =>

					packages += pid.toString()

					super.traverseTrees(stats)
					super.traverse(pid)
				case _ => super.traverse(tree)
			}

		}

		val ast = Analyzer.parseCode(Cc)
		packageTraverser.traverse(ast)
		assert(packageTraverser.packages.contains("c.c.t") &&
		packageTraverser.packages.contains("c.c.u"))
	}
}
