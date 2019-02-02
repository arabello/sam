package io.sam.view.airelation

import java.io.{File, FileReader, PrintWriter}

import io.sam.presenters.airelation.{AIRelationScreenView, AIRelationViewModel}

import scala.io.Source

class AIRelationWebView(outputFile: File) extends AIRelationScreenView{
	val pageChartTemp = new File("view/src/main/resources/page-chart-temp.html")
	val chartTemp = new File("view/src/main/resources/chart-temp.js")

	private def viewModelToJSArray(viewModel: AIRelationViewModel): String = {
		val strBuild = StringBuilder.newBuilder
		strBuild.append("[")
		viewModel.points foreach{ point =>
			strBuild.append(s"{label: '${point.label}', data: [{x:${point.x}, y: ${point.y}}]}")
			if (point != viewModel.points.last)
				strBuild.append(",")

		}
		strBuild.append("]")
		strBuild.mkString
	}

	override def receiveUpdate(viewModel: AIRelationViewModel): Unit = {
		outputFile.getParentFile.mkdirs()
		val html = Source.fromFile(pageChartTemp)
		val js = Source.fromFile(chartTemp)
		val printer = new PrintWriter(outputFile)

		val datasetsJs = viewModelToJSArray(viewModel)
		val outJs = js.mkString.replace("{{datasets}}", datasetsJs)
    		.replace("{{chartTitle}}", viewModel.title)
		val out = html.mkString.replace("{{chartTempJs}}", outJs)

		printer.println(out)
		printer.close()
	}
}
