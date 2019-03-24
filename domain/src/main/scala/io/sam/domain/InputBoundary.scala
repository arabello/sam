package io.sam.domain

trait InputBoundary[T] {
	def measure(data: T): Unit
}
