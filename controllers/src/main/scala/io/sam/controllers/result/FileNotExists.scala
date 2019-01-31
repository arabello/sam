package io.sam.controllers.result

import java.io.File

case class FileNotExists(file: File) extends Result with Error {
	override def mkHuman: String = s"${file.getAbsolutePath} does not exists"

	override val isSuccessfully: Boolean = false
}
