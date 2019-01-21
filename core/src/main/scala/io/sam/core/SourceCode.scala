package io.sam.core

import scala.io.Source


case class SourceCode(name: String, source: Source) extends CodeTools {
	override val packageName: String =
		source.getLines().take(1).mkString.trim.split(" ")(1)

	override val normalizedContent: String =
		source.getLines().mkString
}