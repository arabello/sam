package io.sam.presenters.airelation

import io.sam.domain.airelation.{OutputBoundary, OutputData}
import io.sam.presenters.airelation.graph._

class AIRelationScreenPresenter(view: AIRelationScreenView, config: AIRelationGraph = AIRelationGraph(
			Labelled("Abstractness / Instability Relation"),
			Labelled("Instability"),
			Labelled("Abstractness"),
			Set())) extends OutputBoundary{

	override def deliver(outputData: OutputData): Unit = {
		var model = config.copy()
		outputData.modules foreach{ mod =>
			model = model.copy(points = model.points + Subject(mod))
		}
		view.viewModel.update(model)
	}
}
