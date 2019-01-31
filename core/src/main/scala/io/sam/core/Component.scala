package io.sam.core

case class Component(id: String, codeSources: Set[Code]) extends Code {
	override val content: String = {
		val builder = StringBuilder.newBuilder
		codeSources.foreach(source => builder.append(source.content).append("\n"))
		builder.mkString
	}
}
