package io.sam.controllers

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import io.sam.controllers.result._
import io.sam.domain.airelation.{InputBoundary, InputData}

import scala.io.Source

class AIRelationController(inputBoundary: InputBoundary, config: Config) {
	private val extensionRegExp = """.*\.(\w+)""".r

	type FileId = String
	type ModuleSources = Set[(FileId, File)]
	type ModuleName = String
	type State = Map[ModuleName, ModuleSources]

	private var data: State = Map()

	val snapshot: State = data

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

	private def walkTree(file: File): Iterable[File] = {
		val children = new Iterable[File] {
			def iterator = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
		}
		Seq(file) ++: children.flatMap(walkTree)
	}

	def addFilesRecursively(path: String): Result[File] = addFilesRecursively(path, path)

	def addFilesRecursively(moduleName: String, path: String): Result[File] =
		walkTree(new File(path))
			.map(file => addFile(moduleName, file))
			.fold[Result[File]](new Result[File] {override val report: Seq[Log[File]] = Seq()}) { (acc, res) =>
				new Result[File] {
					override val report: Seq[Log[File]] = acc.report ++ res.report
				}
			}

	def addFile(moduleName: String, file: File): Result[File] = {
		if (!file.isFile)
			return Result.mkFailure(file, s"$file is not a file")

		if (!file.exists())
			return Result.mkFailure(file, s"$file does not exists")

		if (config.excludeClause.apply(file))
			return Result.mkFailure(file, s"$file excluded by configuration clause ${config.excludeClause}")

		file.getName match{
			case extensionRegExp(ext) =>
				if (config.acceptExtension.nonEmpty && !config.acceptExtension.contains(ext))
					return Result.mkFailure(file, s"$ext extension is not accepted")

				data(moduleName) += (file.getCanonicalPath -> file)

				Result.mkSuccess(file)
			case _ => Result.mkFailure(file, s"$file extension not recognized")
		}
	}

	def submit(): Unit = {
		val fileToSource = (from: Set[(String, File)]) =>
			from.map(tuple => tuple._1 -> Source.fromFile(tuple._2).asInstanceOf[Source])

		val inputData = data.map(row => (row._1, fileToSource(row._2)))

		inputBoundary.measure(InputData(inputData))
	}
}
