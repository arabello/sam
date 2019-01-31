package io.sam.controllers.result

import java.io.File

case class NotScalaFile(file: File) extends Result {
	override val isSuccessfully: Boolean = true
}
