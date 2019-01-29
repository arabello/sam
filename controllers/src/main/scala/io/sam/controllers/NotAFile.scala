package io.sam.controllers

import java.io.File

case class NotAFile(file: File) extends Error{
	override def mkHuman: String = s"${file.getAbsolutePath} is not a file"

	override val isSuccessfully: Boolean = false
}