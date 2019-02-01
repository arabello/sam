package io.sam.presenters.airelation

import io.sam.presenters.airelation.graph.{Point2D, Subject}

case class AIRelationViewModel(
	                          title: String,
	                          xAxis: String,
	                          yAxis: String,
	                          points: Set[Subject]){

	private def walkDomain(granularity: Float)(block: BigDecimal => Unit) = (BigDecimal(0f) to BigDecimal(1f) by BigDecimal(granularity)).foreach(block)

	private def reverseLine(x: Float, q: Float) = q - x

	// TODO Add main sequence math func in a functional way
	def mainSequenceLine(granularity: Float): Seq[Point2D] = {
		var seq = Seq[Point2D]()
		walkDomain(granularity){ bd =>
			val p = new Point2D {
				override val x: Float = bd.toFloat
				override val y: Float = reverseLine(bd.toFloat, 1f)
			}

			if (p.x >= 0 && p.x <= 1f)
				seq = seq :+ p
		}
		seq
	}

	// TODO Add zone of pain math func in a functional way
	def zoneOfPainLine(granularity: Float): Seq[Point2D] = {
		var seq = Seq[Point2D]()
		walkDomain(granularity){ bd =>
			val p = new Point2D {
				override val x: Float = bd.toFloat
				override val y: Float = reverseLine(bd.toFloat, 0.25f)
			}

			if (p.x >= 0 && p.x <= 0.25f)
				seq = seq :+ p
		}
		seq
	}

	// TODO Add zone of useless math func in a functional way
	def zoneOfUselessnessLine(granularity: Float): Seq[Point2D] = {
		var seq = Seq[Point2D]()
		walkDomain(granularity){ bd =>
			val p = new Point2D {
				override val x: Float = bd.toFloat
				override val y: Float = reverseLine(bd.toFloat, 1.75f)
			}
			if (p.x >= 0.75f && p.x <= 1)
				seq = seq :+ p
		}
		seq
	}
}