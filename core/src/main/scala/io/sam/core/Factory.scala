package io.sam.core

import java.io.File
import java.nio.file.{Files, Paths}

import scala.io.Source


object Factory extends Entities {
	override def fromFolder(path: String): Code = {
		var sources = Set[Code]()
		Files.walk(Paths.get(path)).forEach { path =>
				sources += fromFile(path.toString)
			}
		ComponentCode(path, sources)
	}

	override def fromFile(path: String): Code = {
			val file = new File(path)
		if (Files.isRegularFile(file.toPath) && path.endsWith(".scala"))
			SourceCode(path, Source.fromFile(path))
		else
			InvalidSourceCode(path)
	}

}
