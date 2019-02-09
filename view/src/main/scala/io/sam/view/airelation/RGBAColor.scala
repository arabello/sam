package io.sam.view.airelation

case class RGBAColor(r: Int, g: Int, b: Int, a: Float){
	override def toString: String = s"rgba($r, $g, $b, $a)"
}

object RGBAColor{
	def random: RGBAColor = RGBAColor(
		(math.random() * 255).toInt,
		(math.random() * 255).toInt,
		(math.random() * 255).toInt,
		math.random().toFloat
	)

	def randomWithAlpha(a: Float): RGBAColor = RGBAColor(
		(math.random() * 255).toInt,
		(math.random() * 255).toInt,
		(math.random() * 255).toInt,
		a
	)
}
