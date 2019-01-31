package io.sam.presenters.airelation.graph

case class AIRelationGraph(
	                          title: Label,
	                          xAxis: Label,
	                          yAxis: Label,
	                          points: Set[Subject]){

	// TODO Add main sequence math func in a functional way
	// TODO Add Seq of main deviation (Z) math func in a functional way
	// TODO Add zone of pain math func in a functional way
	// TODO Add zone of useless math func in a functional way
}
