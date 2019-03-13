package io.sam.presenters.test

import io.sam.domain.airelation.{MeasuredModule, OutputData}
import io.sam.presenters.airelation.{AIRelationScreenPresenter, AIRelationScreenView, AIRelationViewModel}
import org.scalatest.FlatSpec

class AIRelationPresenterTest extends FlatSpec{
	val expectedValues = Map(
		"m1" -> 1.0f,
		"m2" -> 0.5f,
		"m3" -> 0.0f
	)

	val outputData = OutputData(Set(
		MeasuredModule("m1", 1.0f, 1.0f, 1.0f),
		MeasuredModule("m2", 0.5f, 0.5f, 0.5f),
		MeasuredModule("m3", 0.0f, 0.0f, 0.0f)
	))

	"AIRelationPresenter" should "presents data" in {
		object view extends AIRelationScreenView{
			override def receiveUpdate(viewModel: AIRelationViewModel): Unit = {
				viewModel.points foreach{ plottedModule =>
					assert(expectedValues(plottedModule.label) == plottedModule.y)
					assert(expectedValues(plottedModule.label) == plottedModule.x)
				}
			}
		}

		val presenter = new AIRelationScreenPresenter(view)
		presenter.deliver(outputData)
	}

	it should "describes the AI relation graph" in {
		for( i <- BigDecimal("0") to BigDecimal("1") by BigDecimal("0.1") ) {
			val x = i.toFloat

			assert(AIRelationViewModel.mainSequenceLine(x) == 1 - x)
			assert(AIRelationViewModel.zoneOfPainLine(x) == 0.25f - x)
			assert(AIRelationViewModel.zoneOfUselessnessLine(x) == 1.75f - x)
		}
	}
}
