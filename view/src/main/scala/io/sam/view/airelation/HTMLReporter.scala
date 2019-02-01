package io.sam.view.airelation

import java.io.{File, PrintWriter}

import io.sam.presenters.airelation.AIRelationViewModel
import io.sam.view.{HTML, Reporter}

class HTMLReporter(viewModel: AIRelationViewModel) extends Reporter[HTML]{
	override def generateReport(): HTML = ???

	override def writeReport(file: File): Unit = {
		file.getParentFile.mkdirs()
		val printer = new PrintWriter(file)
		printer.println(generateReport().content)
		printer.close()
	}
}