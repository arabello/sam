package io.sam.controllers.result

sealed trait Result[R]{
	val fount: R
	val log: String
}

case class Success[S](who: S) extends Result[S] {
	override val fount: S = who
	override val log: String = ""
}

case class Failure[F](who: F, why: String) extends Result[F] {
	override val fount: F = who
	override val log: String = why
}

case class Logs[L](logs: Seq[Result[L]])