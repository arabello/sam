package io.sam.presenters.test

import io.sam.domain.airelation.{MeasuredModule, OutputData}
import io.sam.presenters.airelation.{AIRelationScreenPresenter, AIRelationScreenView, AIRelationViewModel}
import org.scalatest.FlatSpec

class AIRelationPresenterTest extends FlatSpec{
	"Presenter" should "presents data" in {
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

		object view extends AIRelationScreenView{
			override def receiveUpdate(viewModel: AIRelationViewModel): Unit = {
				viewModel.points foreach{ subj =>
					assert(expectedValues(subj.label) == subj.y)
					assert(expectedValues(subj.label) == subj.x)
				}
			}
		}

		val presenter = new AIRelationScreenPresenter(view)
		presenter.deliver(outputData)
	}
}
