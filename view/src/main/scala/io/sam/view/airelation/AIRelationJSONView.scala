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
		(JsPath \ "data").write[Seq[Point]] and
		(JsPath \ "backgroundColor").write[String]
	)(unlift(Dataset.unapply))

	implicit val chartWrites: Writes[Chart] = (
		(JsPath \ "title").write[String] and
		(JsPath \ "xAxisLabel").write[String] and
		(JsPath \ "yAxisLabel").write[String] and
		(JsPath \ "datasets").write[Seq[Dataset]]
	)(unlift(Chart.unapply))

	private val pointRadius = 12 // px
	private val granularity = 0.01f
	private val zonePointRadius = 2

	override def receiveUpdate(viewModel: AIRelationViewModel): Unit = {

		val datasets =
			(for(point <- viewModel.points) yield Dataset(point.label, Seq(Point(point.x, point.y, pointRadius)), RGBAColor.randomWithAlpha(0.2f).toString)) +
			Dataset("Main Sequence", viewModel.mainSequenceLine(granularity).map( p => Point(p.x, p.y, zonePointRadius)), RGBAColor(0,0,0, 1f).toString) +
			Dataset("Zone of Pain", viewModel.zoneOfPainLine(granularity).map( p => Point(p.x, p.y, zonePointRadius)), RGBAColor(250,0,0, 1f).toString) +
			Dataset("Zone of Uselessness", viewModel.zoneOfUselessnessLine(granularity).map( p => Point(p.x, p.y, zonePointRadius)), RGBAColor(250,0,0, 1f).toString)

		val chart = Chart(viewModel.title, viewModel.xAxis, viewModel.yAxis, datasets.toSeq)
		val json = Json.toJson(chart)

		callback(json.toString())
	}
}
