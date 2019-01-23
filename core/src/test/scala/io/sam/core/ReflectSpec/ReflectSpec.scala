package io.sam.core.ReflectSpec


import java.io._
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files

import com.sun.org.apache.bcel.internal.classfile.SourceFile
import io.sam.core.{ComponentCode, Factory, InvalidSourceCode, SourceCode}
import org.scalatest.FlatSpec

import scala.io.Source
import scala.reflect.io._
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import scala.tools.nsc._
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.reflect.ToolBox

class ReflectSpec extends FlatSpec{

	val resPath = "core/out/test/resources"

	"ToolBox" should "parse packages" in{

		val content =
			"""package io.core
			  | trait Test{}
			  |""".stripMargin

		val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()
		val ast = toolbox.parse(content)
		toolbox.typecheck(ast)
	}

	"nsc" should "works" in{

		val content =
			"""package io.core
			  | trait Test{}
			  |""".stripMargin

		val path = s"$resPath/T.scala"
		new PrintWriter(path) { write(content); close() }

		val settings = new Settings
		settings.embeddedDefaults(getClass.getClassLoader)
		settings.usejavacp.value = true
		val reporter = new StoreReporter
		val compiler: Global = new Global(settings, reporter)
		val run = new compiler.Run()
		compiler.phase = run.parserPhase
		run.cancel()

		val code  = AbstractFile.getFile(path)
		val bfs = new BatchSourceFile(code,code.toCharArray)

		val tree = compiler.newUnitParser(content).parse()

		println(showRaw(tree))
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
