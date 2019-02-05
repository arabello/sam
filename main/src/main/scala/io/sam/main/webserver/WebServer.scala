package io.sam.main.webserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import io.sam.controllers.{AIRelationController, ProjectConfig}
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway}
import io.sam.presenters.airelation.AIRelationScreenPresenter
import io.sam.view.airelation.AIRelationJSONView

import scala.io.StdIn

object WebServer extends CORSHandler {

	val HOST = "localhost"
	val PORT = 8080

	implicit val system = ActorSystem("sam")
	implicit val materializer = ActorMaterializer()
	// needed for the future flatMap/onComplete in the end
	implicit val executionContext = system.dispatcher

	object ignoredDataway extends DataGateway{}

	def main(args: Array[String]) {
		val routes =
			path("") {
				post {
					var data: String = ""

					val view = new AIRelationJSONView({ json => data = json})
					val presenter = new AIRelationScreenPresenter(view)
					val interactor = new AIRelationInteractor(presenter, ignoredDataway)
					val controller = new AIRelationController(interactor, ProjectConfig.ScalaGradle())

					controller.addProject("/Users/MatteoPellegrino/Documents/Dev/Project/sam")

					controller.submit()
					corsHandler(
						complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, data))
					)
				}
			}

		val bindingFuture = Http().bindAndHandle(routes, HOST, PORT)

		println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
		StdIn.readLine() // let it run until user presses return
		bindingFuture
			.flatMap(_.unbind()) // trigger unbinding from the port
			.onComplete(_ => system.terminate()) // and shutdown when done
	}
}
