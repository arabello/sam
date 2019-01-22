package io.sam.core

case class ComponentCode(id: String, sources: Set[Code]) extends Code {
	override val code: String = {
		val builder = StringBuilder.newBuilder
		sources.foreach( source => builder.append(source.code))
		builder.mkString
	}
}
