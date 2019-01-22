package io.sam.core.ReflectSpec


import java.io._

import io.sam.core.{ComponentCode, Factory, InvalidSourceCode, SourceCode}
import org.scalatest.FlatSpec

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

class ReflectSpec extends FlatSpec{

	val resPath = "core/out/test/resources"

	"ToolBox" should "parse packages" in{
		val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()

		val content =
			"""package io.core{
			  | trait Test{}
			  |}""".stripMargin

		val ast = toolbox.parse(content)
		toolbox.typecheck(ast)
	}

	"Traverser" should "catch abstract classes" in{
		val toolbox: ToolBox[universe.type] = runtimeMirror(getClass.getClassLoader).mkToolBox()

		val content = "trait Test{}\nabstract class Test2{}"

		val ast: toolbox.u.Tree = toolbox.parse(content)

		object traverser extends Traverser {
			var n_abstracts = 0
			override def traverse(tree: Tree): Unit = tree match {
				case node @ ClassDef(modifiers, name, parameters, impl) =>
					if (modifiers.hasFlag(Flag.ABSTRACT))
						n_abstracts += 1
				case _ =>
					super.traverse(tree)
			}
		}

		traverser.traverse(ast)

		assert(traverser.n_abstracts == 2)
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
