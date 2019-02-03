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
			out.delete()
			val pw = new PrintWriter(out)
			pw.write(json)
			pw.close()
		}

		val view = new AIRelationJSONView(callbackView)
		val presenter = new AIRelationScreenPresenter(view)
		val interactor = new AIRelationInteractor(presenter, ignored)
		val controller = new AIRelationController(interactor)

		controller.addFilesRecursively("domain", "/Users/MatteoPellegrino/Documents/Dev/Project/sam/domain")
		controller.addFilesRecursively("core", "/Users/MatteoPellegrino/Documents/Dev/Project/sam/core")

		controller.submit()
	}
}