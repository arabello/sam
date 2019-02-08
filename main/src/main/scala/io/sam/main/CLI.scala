package io.sam.main

import java.io.{File, PrintWriter}

import io.sam.controllers._
import io.sam.controllers.result.{Failure, Logs, Success}
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
		val config = Config.Gradle(Config.SCALA_EXT)
		val controller = new AIRelationController(interactor, config)

		val projectPath = "/Users/MatteoPellegrino/Documents/Dev/Project/sam"

		controller.addProject(projectPath) match {
			case Logs(logs) =>
				for(log <- logs) log match {
					case Failure(who, why) => println(s"Error by $who: $why")
					case Success(who) => println(s"Add $who")
					case _ =>
				}
			case _ =>
		}

		controller.submit()
	}
}