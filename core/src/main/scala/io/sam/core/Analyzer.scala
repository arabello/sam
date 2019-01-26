package io.sam.core

import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.{Global, Settings}

object Analyzer extends Global(new Settings, new StoreReporter) {

	settings.embeddedDefaults(getClass.getClassLoader)
	settings.usejavacp.value = true

	def parse(content: String): Tree = {
		val run = new Run()
		phase = run.parserPhase
		run.cancel()

		newUnitParser(content).parse()
	}

	def parseCode(code: Code): Tree = parse(code.content)
}
