package io.sam.core.ReflectSpec


import java.io._

import io.sam.core._
import io.sam.core.Analyzer._
import org.scalatest.FlatSpec

import scala.io.Source
import scala.reflect.runtime.universe
import scala.tools.reflect.ToolBox

class ReflectSpec extends FlatSpec{

	val resPath = "core/out/test/resources"

	"ToolBox" should "works" in{

		val content =
			"""package io.core
			  | trait Test{}
			  |""".stripMargin

		val toolbox = universe.runtimeMirror(getClass.getClassLoader).mkToolBox()

	}

	"Traverser" should "catch abstract classes" in{

		val sc = Analyzer.mkSourceCode("~", """package io.core
								  | trait T{}
								  | abstract class A{}
								  |""".stripMargin)

		var n_abstracts = 0
		object traverser extends Analyzer.Traverser {
			override def traverse(tree: Analyzer.Tree): Unit = tree match {
				case ClassDef(mods, _, _, _) =>
					if (mods.hasFlag(Flag.ABSTRACT)) n_abstracts += 1
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parseCode(sc)
		traverser.traverse(ast)

		assert(n_abstracts == 2)
	}

	"Entities" should "create SourceCode from file" in{
		val expectedContent = "abstract class A{}"
		val path = s"$resPath/A.scala"
		new PrintWriter(path) { write(expectedContent); close() }

		Analyzer.mkSourceCode(path, Source.fromFile(path)) match {
			case sc @ SourceCode (_, _) =>
				assert(sc.codeContent == expectedContent)
			case _ => fail()
		}

		Analyzer.mkSourceCode("invalid", "invalid path") match {
			case isc @ InvalidSourceCode (_) =>
				assert(isc.codeContent.isEmpty)
			case _ => fail()
		}
	}

	"Entities" should "create Module from multiple files" in{

		var files = Map[String, String]()
		files += ("A" -> "abstract class A{}")
		files += ("T" -> "trait T{}")
		files += ("C" -> "case class C{}")

		files.foreach{ case (name, content) =>
			val pathA = s"$resPath/$name.scala"
			new PrintWriter(pathA) { write(content); close() }
		}

		Analyzer.mkModuleFromFolder(resPath) match {
			case cc @ Module(_, _) =>
				assert(cc.codeContent.contains(files("A")))
				assert(cc.codeContent.contains(files("C")))
				assert(cc.codeContent.contains(files("T")))
		}
	}
}
