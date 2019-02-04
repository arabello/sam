package io.sam.controllers.result

case class InvalidConfig() extends Result with Error {
	override val isSuccessfully: Boolean = false

	override def mkHuman: String = s"Configuration provided for is not valid"
}
