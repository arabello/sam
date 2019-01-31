package io.sam.presenters.airelation.graph

case class Labelled(name: String) extends Label{
	override val label: String = name
}
