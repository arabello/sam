package io.sam.core

import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.{Global, Settings}

object Parse extends Global(new Settings, new StoreReporter){

	settings.embeddedDefaults(getClass.getClassLoader)
	settings.usejavacp.value = true

	def fromCode(code: Code) = fromString(code.code)

	def fromString(code: String): Parse.syntaxAnalyzer.global.Tree = {
		val run = new Run()
		phase = run.parserPhase
		run.cancel()

		newUnitParser(code).parse()
	}
}
