package io.sam.view.airelation

import io.sam.presenters.airelation.{AIRelationScreenView, AIRelationViewModel}
import play.api.libs.functional.syntax._
import play.api.libs.json._

class AIRelationJSONView(callback: String => Unit) extends AIRelationScreenView{
	implicit val pointWrites: Writes[Point] = (
		(JsPath \ "x").write[Float] and
		(JsPath \ "y").write[Float] and
		(JsPath \ "r").write[Int]
	)(unlift(Point.unapply))

	implicit val datasetWrites: Writes[Dataset] = (
		(JsPath \ "label").write[String] and
		(JsPath \ "data").write[Seq[Point]]
	)(unlift(Dataset.unapply))

	implicit val chartWrites: Writes[Chart] = (
		(JsPath \ "title").write[String] and
		(JsPath \ "xAxisLabel").write[String] and
		(JsPath \ "yAxisLabel").write[String] and
		(JsPath \ "datasets").write[Seq[Dataset]]
	)(unlift(Chart.unapply))

	private val pointRadius = 12 // px

	override def receiveUpdate(viewModel: AIRelationViewModel): Unit = {

		val datasets = for(
			points <- viewModel.points
		) yield Dataset(points.label, Seq(Point(points.x, points.y, pointRadius)))

		val chart = Chart(viewModel.title, viewModel.xAxis, viewModel.yAxis, datasets.toSeq)
		val json = Json.toJson(chart)

		callback(json.toString())
	}
}
