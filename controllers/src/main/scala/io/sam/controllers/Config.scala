package io.sam.controllers

import java.io.File

trait Config {
	val excludeClause: File => Boolean
	val acceptExtension: Set[String]
}