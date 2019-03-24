package io.sam.domain

trait OutputBoundary[T] {
	def deliver(outputData: T): Unit
}
