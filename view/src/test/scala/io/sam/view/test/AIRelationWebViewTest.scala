package io.sam.view.test

import io.sam.domain.airelation.MeasuredModule
import io.sam.presenters.airelation.AIRelationViewModel
import io.sam.presenters.airelation.graph.Subject
import io.sam.view.airelation.AIRelationWebView
import org.scalatest.FlatSpec

class AIRelationWebViewTest extends FlatSpec{
	"View" should "create the report as file" in{
		val view = new AIRelationWebView()
		val viewModel = new AIRelationViewModel("title", "x", "y", Set(
			Subject(MeasuredModule("m1", 1.0f, 1.0f, 1.0f))
		))
		view.receiveUpdate(viewModel)
	}
}
