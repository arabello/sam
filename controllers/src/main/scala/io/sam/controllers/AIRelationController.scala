package io.sam.controllers

import java.io.File
import java.nio.file.{Files, Paths}

import io.sam.controllers.result._
import io.sam.domain.airelation.{InputBoundary, InputData}

import scala.io.Source

class AIRelationController(inputBoundary: InputBoundary, config: Config) {
	private val data = scala.collection.mutable.Map[String, Set[(String, File)]]()
	private val extensionRegExp = """.*\.(\w+)""".r

	def clear(): Unit = {data.keys foreach( key => data.remove(key))}

	def snapshot: Map[String, Set[(String, File)]] = data.toMap

	def addProject(path: String): Seq[Result] = {
		val dir = new File(path)
		if (!dir.exists() || !dir.isDirectory)
			return Seq(NotADirectory(dir))

		var results = Seq[Result]()

		config match{
			case ProjectConfig.ScalaGradle() =>
				dir.listFiles().filterNot(config.excludeClause) foreach { dir =>
					results = results ++ addFilesRecursively(dir.getName, dir.getCanonicalPath)
				}

			case _ =>
				return Seq(InvalidConfig())
		}

		results
	}

	def addFilesRecursively(path: String): Seq[Result] = addFilesRecursively(path, path)

	def addFilesRecursively(moduleName: String, path: String): Seq[Result] = {
		var results = Seq[Result]()
		Files.walk(Paths.get(path)).distinct().forEach{ path =>
			results = results :+ addFile(moduleName, new File(path.toString))
		}
		results
	}

	def addFiles(moduleName: String, resources: Set[File]): Seq[Result] = {
		var results = Seq[Result]()
		resources.foreach { f =>
			results = results :+ addFile(moduleName, f)
		}
		results
	}

	def addFile(moduleName: String, file: File): Result = {
		if (!file.exists())
			return FileNotExists(file)

		if (!file.isFile)
			return NotAFile(file)

		file.getName match{
			case extensionRegExp(ext) =>
				if (!config.acceptExtension.contains(ext))
					return ExtensionExcluded(file)

				if (!data.contains(moduleName))
					data += (moduleName -> Set())

				data(moduleName) += (file.getCanonicalPath -> file)
				Success(file)
			case _ =>
				ExtensionExcluded(file)
		}
	}

	def submit(): Unit = {
		var components = Map[String, Set[(String, Source)]]()

		data foreach { case (compName, srcs) =>
			var res = Set[(String, Source)]()
			srcs foreach{ case (srcName, src) =>
				res += (srcName -> Source.fromFile(src))
			}
			components += (compName -> res)
		}

		val inputData = InputData(components)
		inputBoundary.measure(inputData)
	}
}
