package io.sam.core.ReflectSpec


import java.io._

import io.sam.core._
import org.scalatest.FlatSpec

import scala.reflect.runtime.universe.showRaw

class ReflectSpec extends FlatSpec{

	val resPath = "core/out/test/resources"



	"nsc" should "works" in{

		val content =
			"""package io.core
			  | trait Test{}
			  |""".stripMargin

		val path = s"$resPath/T.scala"
		new PrintWriter(path) { write(content); close() }

		val ast = Parse.fromCode(Factory.fromFile(path))

		println(showRaw(ast))
	}

	"Traverser" should "catch abstract classes" in{
		object traverser {
			var n_abstracts = 0
			def traverse(tree:  Parse.syntaxAnalyzer.global.Tree): Unit = tree match {
				case ClassDef(modifiers, name, parameters, impl, _, _) =>
					if (modifiers.hasFlag(Flag.ABSTRACT))
						n_abstracts += 1
				case _ =>
					traverse(tree)
			}
		}

		val content =
			"""package io.core
			  | trait T{}
			  | abstract class A{}
			  |""".stripMargin

		val ast = Parse.fromString(content)

		println(showRaw(ast))

		traverser.traverse(ast) // TODO find a way to traverse syntaxAnalyzer.global.Tree

		//assert(traverser.n_abstracts == 2)
	}

	"Factory" should "create Code from file" in{
		val expectedContent = "abstract class A{}"
		val path = s"$resPath/A.scala"
		new PrintWriter(path) { write(expectedContent); close() }

		Factory.fromFile(path) match {
			case sc @ SourceCode (_, _) =>
				assert(sc.code == expectedContent)
			case _ => fail()
		}

		Factory.fromFile("invalid") match {
			case InvalidSourceCode (_) =>
				assert(true)
			case _ => fail()
		}
	}

	"Factory" should "create Code (Component) from multiple files" in{

		var files = Map[String, String]()
		files += ("A" -> "abstract class A{}")
		files += ("T" -> "trait T{}")
		files += ("C" -> "case class C{}")

		files.foreach{ case (name, content) =>
			val pathA = s"$resPath/$name.scala"
			new PrintWriter(pathA) { write(content); close() }
		}

		Factory.fromFolder(resPath) match {
			case cc @ ComponentCode(_, _) =>
				assert(cc.code.contains(files("A")))
				assert(cc.code.contains(files("C")))
				assert(cc.code.contains(files("T")))
		}
	}
}
