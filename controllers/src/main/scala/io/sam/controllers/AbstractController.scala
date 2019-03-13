package io.sam.controllers

abstract class AbstractController[T]{
	def snapshot: T = stateHistory.head

	private var stateHistory: List[T] = List(baseState())

	protected def pushState(state: T): Unit = stateHistory = state :: stateHistory

	protected def popState(): T = try{
		val head = stateHistory.head
		stateHistory = stateHistory.tail
		head
	} catch {
		case _: Throwable =>
			stateHistory = List(baseState())
			stateHistory.head
	}

	def baseState(): T

	def submit(): Unit
}
