package io.sam.presenters.airelation

import io.sam.domain.airelation.{OutputBoundary, OutputData}

class AIRelationScreenPresenter(view: AIRelationScreenView) extends OutputBoundary{
	private val initState = AIRelationViewModel("Abstractness / Instability Relation", "Instability", "Abstractness", Set())

	override def deliver(outputData: OutputData): Unit = view.receiveUpdate(
			outputData.modules.foldLeft[AIRelationViewModel](initState){ (acc, curr) =>
				acc.copy(points = acc.points + PlottedModule(curr.name, curr.instability, curr.abstractness))
			}
		)
}
