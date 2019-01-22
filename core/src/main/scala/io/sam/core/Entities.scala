package io.sam.core

trait Entities {
	def fromFolder(path: String): Code
	def fromFile(path: String): Code
}
