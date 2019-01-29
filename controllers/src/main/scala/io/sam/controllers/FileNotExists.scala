package io.sam.controllers

import java.io.File

case class FileNotExists(file: File) extends Error{
	override def mkHuman: String = s"${file.getAbsolutePath} does not exists"

	override val isSuccessfully: Boolean = false
}
