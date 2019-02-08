package io.sam.controllers

import java.io.File

trait Config {
	val acceptFileExtension: Set[String]
	val relativeMainSrcPath: String
	val excludeModule: File => Boolean
	val excludeFile: File => Boolean
}

object Config {
	val SCALA_EXT: Set[String] = Set("scala")

	private object Default extends Config{
		override val acceptFileExtension: Set[String] = Set()
		override val relativeMainSrcPath: String = ""
		override val excludeModule: File => Boolean = file => file.isHidden
		override val excludeFile: File => Boolean = file => file.isHidden
	}

	case class Simple(extension: Set[String]) extends Config {
		override val excludeFile: File => Boolean = file => Default.excludeFile(file)
		override val acceptFileExtension: Set[String] = extension
		override val relativeMainSrcPath: String = ""
		override val excludeModule: File => Boolean = file => Default.excludeFile(file)
	}

	case class Gradle(extension: Set[String]) extends Config {
		override val excludeFile: File => Boolean = file =>
			Default.excludeFile(file)
		override val acceptFileExtension: Set[String] = extension
		override val relativeMainSrcPath: String = "src/main"
		override val excludeModule: File => Boolean = file =>
			Default.excludeFile(file) || file.getName == "gradle"
	}
}