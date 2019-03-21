package io.sam.view.test

import java.nio.file.Paths

import io.sam.domain.airelation.MeasuredModule
import io.sam.presenters.airelation.{AIRelationViewModel, PlottedModule}
import io.sam.view.airelation.web.ChartJSView
import org.scalatest.FlatSpec

import scala.io.Source

class AIRelationWebViewTest extends FlatSpec{


	"AIRelationWebView" should "create HTML file" in{
		val templateFile = Paths.get("view/src/test/resources/airelation/web/index.html.txt")
		val htmlFile = Paths.get("view/src/test/resources/out.html")
		htmlFile.toFile.delete()

		val view = new ChartJSView(htmlFile, templateFile)
		val points = for(i <- 1 to 5) yield {
				val mod = MeasuredModule(s"m$i", math.random().toFloat, math.random().toFloat, math.random().toFloat)
				PlottedModule(mod.name, mod.instability, mod.abstractness)
			}

		val id = math.ceil(math.random() * 100)
		val viewModel = AIRelationViewModel(s"AI Relation Plot {$id}", "Abstractness", "Instability", points.toSet, Set())
		view.receiveUpdate(viewModel)

		assert(htmlFile.toFile.exists())
		assert(Source.fromFile(htmlFile.toUri).mkString.contains(s"{$id}"))
	}
}
