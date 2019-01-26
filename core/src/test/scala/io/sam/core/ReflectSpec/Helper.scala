package io.sam.core.ReflectSpec

import java.io.File
import java.nio.file.{Files, Paths}

import io.sam.core.{Code, InvalidSourceCode, Module, SourceCode}

import scala.io.Source

object Helper {
	def mkSourceCodeFromFile(path: String): Code = {
		val file = new File(path)
		if (Files.isRegularFile(file.toPath) && path.endsWith(".scala"))
			SourceCode(path, Source.fromFile(path))
		else
			InvalidSourceCode(path)
	}

	def mkModuleFromFiles(id: String, sources: Set[File]): Code = {
		var codes = Set[Code]()
		sources.foreach{ source =>
			if (source.isFile && source.getCanonicalPath.endsWith(".scala"))
				codes += mkSourceCodeFromFile(source.getCanonicalPath)
		}
		Module(id, codes)
	}

	def mkModuleFromFolder(folderPath: String): Code = {
		var sources = Set[Code]()
		Files.walk(Paths.get(folderPath)).forEach { path =>
			sources += mkSourceCodeFromFile(path.toString)
		}
		Module(folderPath, sources)
	}
}
