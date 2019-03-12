package io.sam.controllers

abstract class AbstractController[T]{
	val snapshot: T = popState()

	private var stateHistory: List[T] = List(baseState())

	protected def pushState(state: T): Unit = stateHistory = state :: stateHistory

	protected def popState(): T = try{
		stateHistory.head
	} catch {
		case _: Throwable =>
			stateHistory = List(baseState())
			stateHistory.head
	}

	abstract def baseState(): T

	abstract def submit(): Unit
}
