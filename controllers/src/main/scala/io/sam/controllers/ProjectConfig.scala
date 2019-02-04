package io.sam.controllers
import java.io.File

object ProjectConfig {
	val SCALA_EXT = "scala"

	case class ScalaGradle() extends Config {
		override val excludeClause: File => Boolean = file =>
			(file.isDirectory && (file.getName == "gradle" || file.getName == "test")) ||
			file.isHidden

		override val acceptExtension: Set[String] = Set(SCALA_EXT)
	}

	case class Scala() extends Config {
		override val excludeClause: File => Boolean = file => file.isHidden
		override val acceptExtension: Set[String] = Set(SCALA_EXT)
	}

	case class None() extends Config {
		override val excludeClause: File => Boolean = _ => false // include all, exclude nothing
		override val acceptExtension: Set[String] = Set()
	}
}
