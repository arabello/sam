package io.sam.presenters.airelation

import scala.collection.immutable.NumericRange

case class AIRelationViewModel(title: String, xAxis: String, yAxis: String, points: Set[PlottedModule])

object AIRelationViewModel{
	def domain: Range.Partial[BigDecimal, NumericRange.Inclusive[BigDecimal]] = BigDecimal("0") to BigDecimal("1")
	def mainSequenceLine: Float => Float = (x: Float) => 1 - x
	def zoneOfPainLine: Float => Float = (x: Float) => 0.25f - x
	def zoneOfUselessnessLine: Float => Float = (x: Float) => 1.75f - x
}