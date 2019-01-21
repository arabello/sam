package io.sam.core.ReflectSpec

import org.scalatest.FlatSpec
import scala.reflect.runtime._
import scala.io.Source

class ReflectSpec extends FlatSpec{

	abstract class A {}
	object A {val fileRes = "AbstractClassTest.scala"}

	"An abstract class" should "reflects the 'abstract' symbol" in{
		assert(universe.typeOf[A].typeSymbol.isAbstract)
	}

	"An abstract class from file" should "reflects the 'abstract' symbol" in{
		val abstractClassSource = Source.fromResource("AbstractClassTest.scala", getClass.getClassLoader)
		assert(abstractClassSource.reader().ready())
	}
}
