package io.sam.controllers.result


sealed trait Log[L] {
	val fount: L
	val log: String
}

trait Result[W]{
	val report: Seq[Log[W]]
}

case class Success[S](who: S) extends Log[S] {
	override val fount: S = who
	override val log: String = ""
}

case class Failure[F](who: F, why: String) extends Log[F] {
	override val fount: F = who
	override val log: String = why
}

object Result{
	def mkSuccess[W](who: W): Result[W] = new Result[W] {
		override val report: Seq[Log[W]] = Seq(new Success[W](who))
	}

	def mkFailure[W](who: W, why: String): Result[W] = new Result[W] {
		override val report: Seq[Log[W]] = Seq(new Failure[W](who, why))
	}
}