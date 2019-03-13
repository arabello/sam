package io.sam.controllers.airelation

import java.io.FileNotFoundException
import java.nio.file.Path

import io.sam.controllers.{Failure, NotScalaFile, Result, Success}

case class SourceCodeFile(name: String, file: Path)

object SourceCodeFile{
	private val acceptedFileExt = "scala"
	private val extensionRegExp = """.*\.(\w+)""".r

	def createFromFile(file: Path): Result[SourceCodeFile] = {
		val f = file.toFile
		val name = file.toString

		if (!f.exists())
			return Failure(new FileNotFoundException(s"$file does not exists"))

		if (!f.isFile)
			return Failure(new Exception(s"$file is not a file"))


		f.getName match {
			case extensionRegExp(ext) =>
				if (ext != acceptedFileExt)
					Failure(new NotScalaFile(file))
				else
					Success(SourceCodeFile(name, file))
			case _ =>
				Failure(new Exception("cannot resolve file extension pattern matching"))
		}
	}

	def allFromFolder(path: Path): List[Result[SourceCodeFile]] ={
		val p = path.toFile

		if (!p.exists())
			return List(Failure(new FileNotFoundException(s"$p does not exists")))

		if (!p.isDirectory)
			return List(Failure(new Exception(s"$p is not a directory")))

		FSUtility.walkTree(p).foldLeft[List[Result[SourceCodeFile]]](List()) { (results, file) =>
			results :+ SourceCodeFile.createFromFile(file.toPath)
		}
	}
}