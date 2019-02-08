package io.sam.main

import java.io.{File, PrintWriter}

import io.sam.controllers._
import io.sam.controllers.result.{Failure}
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway}
import io.sam.presenters.airelation.AIRelationScreenPresenter
import io.sam.view.airelation.AIRelationJSONView

import scala.io.StdIn

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
		val config = Config.Gradle()
		val controller = new AIRelationController(interactor, config)

		val projectPath = StdIn.readLine()

		controller.addProject(projectPath) match {
			case Failure(who, why) =>
				println(s"Error by $who: $why")
				return
		}

		controller.submit()
	}
}