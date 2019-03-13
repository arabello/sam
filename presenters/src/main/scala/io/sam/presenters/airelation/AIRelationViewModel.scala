package io.sam.presenters.airelation

case class AIRelationViewModel(title: String, xAxis: String, yAxis: String, points: Set[PlottedModule])

object AIRelationViewModel{
	def mainSequenceLine: Float => Float = (x: Float) => 1 - x
	def zoneOfPainLine: Float => Float = (x: Float) => 0.25f - x
	def zoneOfUselessnessLine: Float => Float = (x: Float) => 1.75f - x
}