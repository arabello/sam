package io.sam.core

import java.io.File

import scala.io.Source

trait CodeFactory {
	def mkCodeFromSource(name: String, source: Source): Code = {
		if (source.isEmpty)
			InvalidSourceCode(name)

		SourceCode(name, source.mkString)
	}

	def mkCodeFromSources(componentName: String, sources: Map[String, Source]): Code = {
		var src = Set[Code]()
		sources.foreach { case (name, source) =>
			src += mkCodeFromSource(name, source)
		}

		Component(componentName, src)
	}

	def mkCodeFromFile(file: File): Code = {
		val id = file.getCanonicalPath
		val s = Source.fromFile(id)
		try{
			SourceCode(id, s.mkString)
		}catch{
			case _: Exception => InvalidSourceCode(id)
		}finally{
			s.close()
		}
	}

	def mkCodeFromFiles(componentName: String, files: Set[File]): Code = {
		var sources = Set[Code]()
		files.foreach { file =>
			sources += mkCodeFromFile(file)
		}

		Component(componentName, sources)
	}
}
