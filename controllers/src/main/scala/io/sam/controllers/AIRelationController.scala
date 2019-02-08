package io.sam.controllers

import java.io.File

import io.sam.controllers.result._
import io.sam.domain.airelation.{InputBoundary, InputData}

import scala.io.Source

class AIRelationController(inputBoundary: InputBoundary, config: Config) {
	private val extensionRegExp = """.*\.(\w+)""".r

	private var data: Map[String, Set[(String, File)]] = Map()

	def snapshot: Map[String, Set[(String, File)]] = data

	def addProject(path: String): Result[File] = {
		val dir = new File(path)
		if(!dir.isDirectory)
			return Failure(dir, s"${dir.getPath} is not a directory")

		dir.listFiles()
			.filter(dir => dir.isDirectory)
    		.filterNot(config.excludeModule)
			.map( dir => addFilesRecursively(dir.getName, s"${dir.getPath}/${config.relativeMainSrcPath}") )
			.fold[Result[File]](Logs(Seq(), dir)) {
				(acc, curr) =>
					val instance = acc.asInstanceOf[Logs[File]]
					instance.copy(logs = instance.logs :+ curr)
		}
	}

	private def walkTree(file: File): Iterable[File] = {
		val children = new Iterable[File] {
			def iterator = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
		}
		Seq(file) ++: children.flatMap(walkTree)
	}

	def addFilesRecursively(path: String): Result[File] = addFilesRecursively(path, path)

	def addFilesRecursively(moduleName: String, path: String): Result[File] = {
		val start = new File(path)
		if(!start.isDirectory)
			return Failure(start, s"${start.getPath} is not a directory")

		walkTree(start)
    		.filter(file => file.isFile)
			.map(file => addFile(moduleName, file))
			.fold[Result[File]](Logs(Seq(), start)) {
				(acc, curr) =>
				val instance = acc.asInstanceOf[Logs[File]]
				instance.copy(logs = instance.logs :+ curr)
			}
	}

	def addFile(moduleName: String, file: File): Result[File] = {
		if (!file.exists())
			return Failure(file, s"$file does not exists")

		if (!file.isFile)
			return Failure(file, s"$file is not a file")

		if (config.excludeFile.apply(file))
			return Failure(file, s"$file excluded by configuration clause ${config.excludeFile}")

		file.getName match{
			case extensionRegExp(ext) =>
				if (config.acceptFileExtension.nonEmpty && !config.acceptFileExtension.contains(ext))
					return Failure(file, s"$ext extension is not accepted")

				if (!data.contains(moduleName))
					data = data + (moduleName -> Set())

				data = data.map(row =>
					if (row._1 == moduleName)
						(row._1, row._2 + (file.getPath -> file))
					else
						(row._1, row._2)
				)

				Success(file)
			case _ => Failure(file, s"$file extension not recognized")
		}
	}

	def submit(): Unit = {
		val fileToSource = (from: Set[(String, File)]) =>
			from.map(tuple => tuple._1 -> Source.fromFile(tuple._2).asInstanceOf[Source])

		val inputData = data.map(row => (row._1, fileToSource(row._2)))

		inputBoundary.measure(InputData(inputData))
	}
}
