package io.sam.domain.airelation

trait OutputBoundary {
	def deliver(outputData: OutputData): Unit
}
