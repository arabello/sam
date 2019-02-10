package io.sam.main.webserver

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import io.sam.controllers.result.{Failure, Logs, Success}
import io.sam.controllers.{AIRelationController, Config}
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway}
import io.sam.presenters.airelation.AIRelationScreenPresenter
import io.sam.view.airelation.AIRelationJSONView

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer extends CORSHandler {

	val HOST = "localhost"
	val PORT = 8080

	implicit val system: ActorSystem = ActorSystem("sam")
	implicit val materializer: ActorMaterializer = ActorMaterializer()
	implicit val executionContext: ExecutionContextExecutor = system.dispatcher

	object ignoredDataway extends DataGateway{}


	def main(args: Array[String]) {

		if (args.length < 1){
			println("Please provide a project path as argument")
			return
		}

		val projectFile = new File(args(0))

		if (!projectFile.isDirectory){
			println("The argument provided is not a directory")
			return
		}

		var data: String = ""
		val view = new AIRelationJSONView({ json => data = json})
		val presenter = new AIRelationScreenPresenter(view)
		val interactor = new AIRelationInteractor(presenter, ignoredDataway)
		val controller = new AIRelationController(interactor, Config.Gradle(Config.SCALA_EXT))

		controller.addProject(projectFile.getAbsolutePath) match {
			case Logs(logs) =>
				for(log <- logs) log match {
					case Failure(who, why) => println(s"Error: $why")
					case Success(who) => println(s"Add $who")
					case _ =>
				}
			case _ =>
		}

		controller.submit()

		val routes =
				path("") {
					get {
						corsHandler(
							complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, data))
						)
					}
				}


		val bindingFuture = Http().bindAndHandle(routes, HOST, PORT)

		println(s"Server online at http://$HOST:$PORT/")
		println("Request data using a GET http request")
		println("Press RETURN to stop...")
		StdIn.readLine() // let it run until user presses return
		bindingFuture
			.flatMap(_.unbind()) // trigger unbinding from the port
			.onComplete(_ => system.terminate()) // and shutdown when done
	}
}
