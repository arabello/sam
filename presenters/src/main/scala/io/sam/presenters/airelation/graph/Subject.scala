package io.sam.presenters.airelation.graph

import io.sam.domain.airelation.MeasuredModule

case class Subject(module: MeasuredModule) extends Point2D with Label{
	override val x: Float = module.instability
	override val y: Float = module.abstractness
	override val label: String = module.name
}
