package io.sam.presenters.airelation

import io.sam.domain.airelation.{OutputBoundary, OutputData}

class AIRelationScreenPresenter(view: AIRelationScreenView) extends OutputBoundary{
	private val initState = AIRelationViewModel("Abstractness / Instability Relation", "Instability", "Abstractness", Set(), Set())

	override def deliver(outputData: OutputData): Unit = view.receiveUpdate(
			outputData.modules.foldLeft[AIRelationViewModel](initState){ (acc, curr) =>
				val error = if (curr.abstractness.isNaN)
					"cannot calculate abstractness"
						else if (curr.instability.isNaN)
					"cannot calculate instability"
						else ""

				if (error.isEmpty)
					acc.copy(points = acc.points + PlottedModule(curr.name, curr.instability, curr.abstractness))
				else
					acc.copy(errors = acc.errors + InvalidModule(curr.name, error))
			}
		)
}
