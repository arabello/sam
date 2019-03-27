package io.sam.core.test

import java.io.File

import io.sam.core._
import org.scalatest.FlatSpec

import scala.io.Source

class CoreTest extends FlatSpec{
	val resPath = "core/src/test/resources"


	"Analyzer" should "reads abstract classes" in{
		object usecase extends Analyzer{
			val sc = SourceCode("~", "package io.core \n trait T{\ntrait T2{}} \n abstract class A{}\n object O{}")

			implicit object traverser extends Traverser {
				var n_abstracts = 0
				override def traverse(tree: Tree): Unit = tree match {
					case  ClassDef(mods, _, tparams, impl) =>
						if (mods.hasFlag(Flag.ABSTRACT))
							n_abstracts += 1
						super.traverseTrees(tparams)
						super.traverse(impl)
					case _ => super.traverse(tree)
				}
			}

			analyze(sc)

			assert(traverser.n_abstracts == 3)
		}
	}

	it should "reads PackageDef" in{
		object usecase extends Analyzer{

			implicit object traverser extends Traverser {
				val sb = StringBuilder.newBuilder
				override def traverse(tree: Tree): Unit = tree match {
					case PackageDef(pid, stats) =>
						sb.append(pid.toString()+"\n")
						super.traverseTrees(stats)
						super.traverse(pid)
					case _ => super.traverse(tree)
				}
			}

			analyze(SourceCode("", "package io.pck.a\ntrait A{}\npackage io.pck.b\ntrait B{}"))

			assert(traverser.sb.mkString == "io.pck.a\nio.pck.b\n")
		}
	}

	it should "reads ImportDef" in{
		object usecase extends Analyzer{

			implicit object traverser extends Traverser {
				val sb = StringBuilder.newBuilder
				override def traverse(tree: Tree): Unit = tree match {
					case Import(expr, selectors) =>
						sb.append(expr+"\n")
						super.traverse(expr)
					case _ => super.traverse(tree)
				}
			}

			analyze(SourceCode("", "package io.pck.a\nimport io.pck.b.A\nimport io.pck.b._\nimport io.pck.{h => A}\ntrait A{}"))

			assert(traverser.sb.mkString == "io.pck.b\nio.pck.b\nio.pck\n")
		}
	}

	it should "reads module def (object)" in{
		object usecase extends Analyzer {
			val sc = SourceCode("~", "package io.core \n object O{\nobject O2{}} \n abstract class A{}")


			implicit object traverser extends Traverser {
				var n_object = 0
				override def traverse(tree: Tree): Unit = tree match {
					case ModuleDef(mods, name, impl) =>
						n_object += 1

						super.traverse(impl)
					case _ => super.traverse(tree)
				}
			}

			analyze(sc)

			assert(traverser.n_object == 2)
		}
	}

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

		object usecase extends Analyzer{
			val t = factory.mkCodeFromFile(new File(s"$resPath/t"))
			val u = factory.mkCodeFromFile(new File(s"$resPath/u"))
			val Cc = Component("Cc", Set(t, u))

			implicit object packageTraverser extends Traverser{
				var packages: Set[String] = Set[String]()

				override def traverse(tree: Tree): Unit = tree match {
					case PackageDef(pid, stats) =>

						packages += pid.toString()

						super.traverseTrees(stats)
						super.traverse(pid)
					case _ => super.traverse(tree)
				}

			}

			analyze(Cc)
			assert(packageTraverser.packages.contains("c.c.t") &&
				packageTraverser.packages.contains("c.c.u"))
		}
	}
}
