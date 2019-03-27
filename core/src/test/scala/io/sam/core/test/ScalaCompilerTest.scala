package io.sam.core.test

import org.scalatest.FlatSpec

import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.{Global, Settings}

class ScalaCompilerTest extends FlatSpec{

	"Global" should "parse Scala code" in{
		object compiler extends Global(new Settings, new StoreReporter){
			settings.embeddedDefaults(getClass.getClassLoader)
			settings.usejavacp.value = true

			def parse(code: String) = {
				val run = new Run()
				phase = run.parserPhase
				run.cancel()

				newUnitParser(code).parse()
			}
		}

		val code = "package test{ class Test{} }"

		val tree = compiler.parse(code)

		assert(tree.toString().contains("class Test extends scala.AnyRef"))
	}

	"Traverser" should "traverse Scala AST" in{
		object compiler extends Global(new Settings){
			settings.embeddedDefaults(getClass.getClassLoader)
			settings.usejavacp.value = true

			val run = new Run()
			phase = run.parserPhase
			def parse(code: String) = {
				newUnitParser(code).parse()
			}
		}

		class T extends compiler.Traverser{
			override def traverse(tree: compiler.Tree): Unit = tree match {
				case compiler.ClassDef(sym, _, _, _) => assert(sym.toString().contains("class Test"))
				case compiler.PackageDef(pid, _) => assert(pid.toString() == "test")
				case _ => fail()
			}
		}

		val code = "package test{ class Test{} }"

		val traverser = new T()
		val tree = compiler.parse(code)
		traverser.traverse(tree)
	}
}
