package io.sam.main

import java.io.{File, PrintWriter}

import io.sam.controllers.AIRelationController
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway}
import io.sam.presenters.airelation.AIRelationScreenPresenter
import io.sam.view.airelation.AIRelationJSONView

object CLI extends App {

	override def main(args: Array[String]): Unit = {
		object ignored extends DataGateway{}

		val out = new File("/Users/MatteoPellegrino/Desktop/airelation.json")

		val callbackView: String => Unit  = { json =>
			val pw = new PrintWriter(out)
			pw.write(json)
			pw.close()
		}

		val view = new AIRelationJSONView(callbackView)
		val presenter = new AIRelationScreenPresenter(view)
		val interactor = new AIRelationInteractor(presenter, ignored)
		val controller = new AIRelationController(interactor)

		controller.addFile("main", new File("/Users/MatteoPellegrino/Documents/Dev/Project/sam/main/src/main/scala/io/sam/main/CLI.scala"))
		controller.addFile("view", new File("/Users/MatteoPellegrino/Documents/Dev/Project/sam/view/src/main/scala/io/sam/view/airelation/Chart.scala"))

		controller.submit()
	}
}