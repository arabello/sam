package io.sam.core

import scala.io.Source

case class SourceCode(id: String, source: Source) extends Code {
	override val content: String = source.mkString
}

