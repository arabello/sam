package io.sam.controllers.airelation

import java.io.FileNotFoundException
import java.nio.file.Path

import io.sam.controllers._
import io.sam.domain.airelation.{InputBoundary, InputData}

import scala.io.Source

class AIRelationController(inputBoundary: InputBoundary) extends AbstractController[List[SoftwareModule]] {
	override def baseState(): List[SoftwareModule] = List()

	override def submit(): Unit = inputBoundary.measure(
		InputData(
			snapshot.map( module =>
				(module.name, module.sources.map( src =>
					(src.name, Source.fromFile(src.file.toFile).asInstanceOf[Source]) ) ) ).toMap
		)
	)

	def add(module: SoftwareModule): Unit = {
		pushState(snapshot :+ module)
	}

	def undo(): Unit = popState()
}

object AIRelationController{
	private val acceptedFileExt = "scala"
	private val extensionRegExp = """.*\.(\w+)""".r

	def createFromFolder(path: Path): Result[SoftwareModule] ={
		val p = path.toFile

		if (!p.exists())
			return Failure(new FileNotFoundException(s"$p does not exists"))

		if (!p.isDirectory)
			return Failure(new Exception(s"$p is not a directory"))

		val sm = allFromFolder(path)
			.foldLeft[SoftwareModule](SoftwareModule(path.toString, Set())){ (acc, scf) => scf match {
				case Success(content) => acc.copy(acc.name, acc.sources + content)
				case _ => acc
			}
		}

		Success(sm)
	}

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
			results :+ createFromFile(file.toPath)
		}
	}
}
