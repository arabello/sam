package io.sam.core

import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.{Global, Settings}

abstract class Analyzer extends Global(new Settings, new StoreReporter) {
	settings.embeddedDefaults(getClass.getClassLoader)
	settings.usejavacp.value = true

	val run = new Run()
	phase = run.parserPhase

	protected def analyze[T <: Traverser](code: Code)(implicit traverser: T): T = {
		traverser.traverse(newUnitParser(code.content).parse())
		traverser
	}
}