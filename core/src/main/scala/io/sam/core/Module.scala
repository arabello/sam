package io.sam.core

case class Module(id: String, sources: Set[Code]) extends Code {
	override val content: String = {
		val builder = StringBuilder.newBuilder
		sources.foreach( source => builder.append(source.content))
		builder.mkString
	}
}
