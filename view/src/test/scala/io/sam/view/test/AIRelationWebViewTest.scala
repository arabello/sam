package io.sam.view.test

import java.io.{File, PrintWriter}

import io.sam.domain.airelation.MeasuredModule
import io.sam.presenters.airelation.{AIRelationViewModel, PlottedModule}
import io.sam.view.airelation.AIRelationJSONView
import org.scalatest.FlatSpec

import scala.io.Source

class AIRelationWebViewTest extends FlatSpec{

	val jsonFile = new File("view/src/test/resources/airelation.json")

	"JSONView" should "create json file" in{
		jsonFile.delete()
		val callbackView: String => Unit  = { json =>
			val pw = new PrintWriter(jsonFile)
			pw.write(json)
			pw.close()
		}
		val view = new AIRelationJSONView(callbackView)
		var points = Set[PlottedModule]()
		(1 to 5).foreach{ i =>
			points += PlottedModule(MeasuredModule(s"m$i", math.random().toFloat, math.random().toFloat, math.random().toFloat))
		}
		val id = math.ceil(math.random() * 100)
		val viewModel = AIRelationViewModel(s"AI Relation Plot {$id}", "Abstractness", "Instability", points)
		view.receiveUpdate(viewModel)

		assert(jsonFile.exists())
		assert(Source.fromFile(jsonFile).mkString.contains(s"{$id}"))
	}
}
