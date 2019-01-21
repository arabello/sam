package io.sam.core

import scala.reflect.runtime.universe
import scala.tools.reflect.ToolBox

trait ReflectTools {
	val toolbox: ToolBox[universe.type] = universe.runtimeMirror(getClass.getClassLoader).mkToolBox()
}
