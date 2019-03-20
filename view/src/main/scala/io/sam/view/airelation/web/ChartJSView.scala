package io.sam.view.airelation.web

import java.io.PrintWriter
import java.nio.file.{Path, Paths}

import io.sam.presenters.airelation.{AIRelationScreenView, AIRelationViewModel}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.io.Source

class ChartJSView(output: Path, val templateFile: Path) extends AIRelationScreenView{
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

	private val placeholderKey = "<{[jsonData]}>"

	private val pointRadius = 12 // px
	private val step = BigDecimal("0.01")
	private val zonePointRadius = 2

	private val domain = AIRelationViewModel.domain
	private val msl = AIRelationViewModel.mainSequenceLine
	private val zpl = AIRelationViewModel.zoneOfPainLine
	private val zul = AIRelationViewModel.zoneOfUselessnessLine

	private var errors = Set[String]()
	def getErrors: Set[String] = errors

	override def receiveUpdate(viewModel: AIRelationViewModel): Unit = {

		errors = viewModel.errors.map(m => s"Error on '${m.name}': ${m.why}. Module excluded and not plotted.")

		val points = (for(point <- viewModel.points) yield Dataset(point.label, Seq(Point(point.x, point.y, pointRadius)), RGBAColor.randomWithAlpha(0.2f).toString)).toSeq

		def lineGen(line: Float => Float) = (for ( x <- domain.by(step)) yield Point(x.toFloat, line(x.toFloat), zonePointRadius)).filter(p => 0 <= p.y && p.y <= 1)

		val lines = Seq(
			Dataset("Main Sequence", lineGen(msl), RGBAColor(0,0,0, 1f).toString),
			Dataset("Zone of Pain", lineGen(zpl), RGBAColor(250,0,0, 1f).toString),
			Dataset("Zone of Uselessness", lineGen(zul), RGBAColor(250,0,0, 1f).toString)
		)

		val dataset = points ++ lines

		val chart = Chart(viewModel.title, viewModel.xAxis, viewModel.yAxis, dataset)
		val json = Json.toJson(chart)

		val content = Source.fromFile(templateFile.toUri).mkString.replace(placeholderKey, json.toString())

		output.toFile.getParentFile.mkdirs()
		val pw = new PrintWriter(output.toFile)

		try{
			pw.write(content)
		}finally{
			pw.close()
		}
	}
}
