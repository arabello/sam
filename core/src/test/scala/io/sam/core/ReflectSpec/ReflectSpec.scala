package io.sam.core.ReflectSpec

import io.sam.core.SourceCode
import org.scalatest.FlatSpec

import scala.io.Source
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

class ReflectSpec extends FlatSpec{

	"A stable universe" should "exists" in{
		val mirror: universe.Mirror =  universe.runtimeMirror(getClass.getClassLoader)
		val toolbox: ToolBox[universe.type] = mirror.mkToolBox()

		val content = Source.fromResource("ReflectTools.scala").getLines().drop(1).mkString("\n") +
			Source.fromResource("CodeTools.scala").getLines().drop(1).mkString("\n") +
			Source.fromResource("SourceCode.scala").getLines().drop(1).mkString("\n")

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

		assert(traverser.n_abstracts > 0)
	}

	"SourceCode" should "read packageName" in{
		val inlinePackage = "package io.sam.core\ntrait Test{}"
		assert(SourceCode("Test", Source.fromString(inlinePackage)).packageName == "io.sam.core")
	}

	it should "normalize content" in{
		val inlinePackage = "package io.sam.core\ntrait Test{}"
		assert(SourceCode("Test", Source.fromString(inlinePackage)).normalizedContent == "trait Test{}")
	}
}
