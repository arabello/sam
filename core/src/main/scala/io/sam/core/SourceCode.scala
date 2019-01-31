package io.sam.core

case class SourceCode(id: String, code: String) extends Code {
	override val content: String = code
}

