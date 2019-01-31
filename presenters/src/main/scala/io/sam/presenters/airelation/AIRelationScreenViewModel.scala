package io.sam.presenters.airelation

import io.sam.presenters.ObservableViewModel
import io.sam.presenters.airelation.graph.{AIRelationGraph, EmptyLabel}

class AIRelationScreenViewModel() extends ObservableViewModel[AIRelationScreenViewModel]{
	private var data: AIRelationGraph = AIRelationGraph(EmptyLabel(), EmptyLabel(), EmptyLabel(), Set())

	def update(newData: AIRelationGraph) = {
		data = newData
		notifyObservers()
	}
}
