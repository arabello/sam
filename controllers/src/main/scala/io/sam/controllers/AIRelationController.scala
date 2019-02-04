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

	def addProject(path: String): Result[File] = {
		val dir = new File(path)
		if (!dir.exists())
			return Failure(dir, s"${dir.getCanonicalPath} does not exists")

		if(!dir.isDirectory)
			return Failure(dir, s"${dir.getCanonicalPath} is not a directory")

		var wrns = Warning(Seq[Log[File]]())

		config match{
			case ProjectConfig.ScalaGradle() =>
				dir.listFiles().filterNot(config.excludeClause) foreach { dir =>
					addFilesRecursively(dir.getName, dir.getCanonicalPath) match {
						case w @Warning(logs) =>
							wrns = w.copy(logs = logs ++ wrns.logs)
						case _ =>
					}
				}
		}

		wrns
	}

	def addFilesRecursively(path: String): Result[File] = addFilesRecursively(path, path)

	def addFilesRecursively(moduleName: String, path: String): Result[File] = {
		var wrns = Warning(Seq[Log[File]]())
		Files.walk(Paths.get(path)).distinct().forEach{ path =>
			addFile(moduleName, new File(path.toString)) match {
				case w @Warning(logs) =>
					wrns = w.copy(logs = logs ++ wrns.logs)
				case _ =>
			}
		}
		wrns
	}

	def addFiles(moduleName: String, resources: Set[File]): Result[File] = {
		var wrns = Warning(Seq[Log[File]]())
		resources.foreach { f =>
			addFile(moduleName, f) match {
				case f @ Failure(_, _) =>
					return f
				case w @Warning(logs) =>
					wrns = w.copy(logs = logs ++ wrns.logs)
				case _ =>
			}
		}
		wrns
	}

	def addFile(moduleName: String, file: File): Result[File] = {
		if (!file.exists())
			return Failure(file, s"$file does not exists")

		if (!file.isFile)
			return Failure(file, s"$file is not a file")

		if (config.excludeClause.apply(file))
			return Failure(file, s"$file excluded by configuration clause ${config.excludeClause}")

		file.getName match{
			case extensionRegExp(ext) =>
				if (config.acceptExtension.nonEmpty && !config.acceptExtension.contains(ext))
					return Warning(Seq(new Log[File] {
						override val fount: File = file
						override val log: String = s"$ext extension is not accepted"
					}))

				if (!data.contains(moduleName))
					data += (moduleName -> Set())

				data(moduleName) += (file.getCanonicalPath -> file)
				Success(file)
			case _ => Warning(Seq(new Log[File] {
				override val fount: File = file
				override val log: String = s"$file extension not recognized"
			}))
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
