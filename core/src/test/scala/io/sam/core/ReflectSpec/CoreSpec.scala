package io.sam.core.ReflectSpec


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

	"Traverser" should "traverse abstract classes" in{

		val sc = SourceCode("~", Source.fromString("package io.core \n trait T{\ntrait T2{}} \n abstract class A{}\n object O{}"))

		var n_abstracts = 0
		object traverser extends Analyzer.Traverser {
			override def traverse(tree: Analyzer.Tree): Unit = tree match {
				case node @  ClassDef(mods, name, tparams, impl) =>
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

	it should "traverse object (singleton)" in{

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
