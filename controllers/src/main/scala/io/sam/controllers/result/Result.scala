package io.sam.controllers.result

sealed trait Result[W]

trait Log[L]{
	val fount: L
	val log: String
}

case class Success[S](who: S) extends Result[S]

case class Warning[W](logs: Seq[Log[W]]) extends Result[W]

case class Failure[F](who: F, why: String) extends Result[F] with Log[F] {
	override val fount: F = fount
	override val log: String = log
}