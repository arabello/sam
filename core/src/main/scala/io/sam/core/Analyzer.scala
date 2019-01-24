package io.sam.core

import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.{Global, Settings}

object Analyzer extends Global(new Settings, new StoreReporter) with Entities {

	settings.embeddedDefaults(getClass.getClassLoader)
	settings.usejavacp.value = true

	/* Reflect ToolBox if needed
	def parseWithMirror(path:String) = {
	    val source = Files.toString(new File(path),Charset.forName("UTF-8"))
	    val toolBox = ru.runtimeMirror(getClass.getClassLoader).mkToolBox()
	    toolBox.parse(source)
	}
	def parseWithMirrorTypeCheck(path:String) = {
		val source = Files.toString(new File(path),Charset.forName("UTF-8"))
	    val toolBox = ru.runtimeMirror(getClass.getClassLoader).mkToolBox()
	    toolBox.typecheck(toolBox.parse(source))
	}
	 */

	def parse(content: String): Tree = {
		val run = new Run()
		phase = run.parserPhase
		run.cancel()

		newUnitParser(content).parse()
	}

	def parseCode(code: Code): Tree = parse(code.codeContent)

	def analyze(code: Code)(block: Tree => Unit): Unit = {

		val ast = parseCode(code)
	}
}
