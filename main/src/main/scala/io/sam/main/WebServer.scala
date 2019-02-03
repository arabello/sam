package io.sam.main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer extends App {
	val host = "localhost"
	val port = 8080

	override def main(args: Array[String])= {
		 implicit val system: ActorSystem = ActorSystem("sam-web")
		 implicit val materializer: ActorMaterializer = ActorMaterializer()
		 implicit val executionContext: ExecutionContextExecutor = system.dispatcher

		 val route =
			 path("hello") {
				 get {
					 complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
				 }
			 }

		 val bindingFuture = Http().bindAndHandle(route, host, port)

		 println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
		 StdIn.readLine() // let it run until user presses return
		 bindingFuture
			 .flatMap(_.unbind()) // trigger unbinding from the port
			 .onComplete(_ => system.terminate()) // and shutdown when done
	}
}