package io.sam.core

import scala.io.Source


case class SourceCode(id: String, source: Source) extends Code {
	override val code: String = source.getLines().mkString //TODO: Resolve package issue
}