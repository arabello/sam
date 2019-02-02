package io.sam.view.test

import java.io.File

import io.sam.domain.airelation.MeasuredModule
import io.sam.presenters.airelation.AIRelationViewModel
import io.sam.presenters.airelation.graph.{Point2D, Subject}
import io.sam.view.airelation.AIRelationWebView
import org.scalatest.FlatSpec

class AIRelationWebViewTest extends FlatSpec{

	val outFile = new File("view/src/test/resources/airelation.html")

	"View" should "create the report as file" in{
		val view = new AIRelationWebView(outFile)
		var points = Set[Subject]()
		(1 to 10).foreach{ i =>
			points += Subject(MeasuredModule(s"m$i", math.random().toFloat, math.random().toFloat, math.random().toFloat))
		}
		val viewModel = AIRelationViewModel("AI Relation Plot", "Abstractness", "Instability", points)
		view.receiveUpdate(viewModel)
	}
}
