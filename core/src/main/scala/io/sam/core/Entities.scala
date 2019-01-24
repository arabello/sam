package io.sam.core

import java.io.File
import java.nio.file.{Files, Paths}

import scala.io.Source

trait Entities {

	abstract class Code(id: String) extends HasCode

	case class SourceCode(id: String, source: Source) extends Code(id) {
		override val codeContent: String = source.mkString
	}

	case class Module(id: String, sources: Set[Code]) extends Code(id) {
		override val codeContent: String = {
			val builder = StringBuilder.newBuilder
			sources.foreach( source => builder.append(source.codeContent))
			builder.mkString
		}
	}

	case class InvalidSourceCode(id: String) extends Code(id) {
		override val codeContent: String = ""
	}

	def mkSourceCode(id: String, content: String): Code = SourceCode(id, Source.fromString(content))

	def mkSourceCode(id: String, source: Source): Code = SourceCode(id, source)

	def mkSourceCodeFromFile(file: File): Code = mkSourceCodeFromFile(file.getCanonicalPath)

	def mkSourceCodeFromFile(path: String): Code = {
		val file = new File(path)
		if (Files.isRegularFile(file.toPath) && path.endsWith(".scala"))
			SourceCode(path, Source.fromFile(path))
		else
			InvalidSourceCode(path)
	}

	def mkModule(id: String, sources: Set[Code]): Code = Module(id, sources)

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
