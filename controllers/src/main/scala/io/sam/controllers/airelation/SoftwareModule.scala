package io.sam.controllers.airelation

import java.io.FileNotFoundException
import java.nio.file.Path

import io.sam.controllers.{Failure, Result, Success}

case class SoftwareModule(name: String, sources: Set[SourceCodeFile])

object SoftwareModule{

	def createFromFolder(path: Path): Result[SoftwareModule] ={
		val p = path.toFile

		if (!p.exists())
			return Failure(new FileNotFoundException(s"$p does not exists"))

		if (!p.isDirectory)
			return Failure(new Exception(s"$p is not a directory"))

		val sm = SourceCodeFile
			.allFromFolder(path)
			.foldLeft[SoftwareModule](SoftwareModule(path.toString, Set())){ (acc, scf) => scf match {
					case Success(content) => acc.copy(acc.name, acc.sources + content)
					case _ => acc
				}
			}

		Success(sm)
	}
}
