package io.sam.controllers.result

import java.io.File

case class NotAFile(file: File) extends Result with Error{
	override def mkHuman: String = s"${file.getAbsolutePath} is not a file"

	override val isSuccessfully: Boolean = false
}
