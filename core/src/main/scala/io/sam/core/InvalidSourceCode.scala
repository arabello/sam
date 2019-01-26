package io.sam.core

case class InvalidSourceCode(id: String) extends Code {
	override val content: String = ""
}