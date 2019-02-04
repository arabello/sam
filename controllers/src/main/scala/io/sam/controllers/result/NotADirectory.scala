package io.sam.controllers.result

import java.io.File

case class NotADirectory(file: File) extends Result with Error{
	override def mkHuman: String = s"${file.getAbsolutePath} is not a directory. Cannot find project."

	override val isSuccessfully: Boolean = false
}
