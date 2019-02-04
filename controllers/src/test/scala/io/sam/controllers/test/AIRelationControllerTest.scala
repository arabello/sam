package io.sam.controllers.test

import java.io.File

import io.sam.controllers.result._
import io.sam.controllers.{AIRelationController, ProjectConfig}
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway, OutputBoundary, OutputData}
import org.scalatest.FlatSpec

class AIRelationControllerTest extends FlatSpec{
	object gateway extends DataGateway{}
	object presenter extends OutputBoundary{
		override def deliver(outputData: OutputData): Unit = {}
	}

	val resPath = "controllers/src/test/resources"

	"AIRElationController" should "add a file to a new module" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, ProjectConfig.None())
		val file = new File(s"$resPath/1.scala")

		ctrl.addFile("newModule", file) match {
			case Success(f) => assert(file == f)
			case _ => fail()
		}
		assert(ctrl.snapshot.contains("newModule"))
		assert(ctrl.snapshot("newModule").contains(file.getCanonicalPath -> file))
	}

	it should "add a file to an existing module" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, ProjectConfig.None())
		val file = new File(s"$resPath/1.scala")

		ctrl.addFile("existingModule", new File(s"$resPath/2.scala"))

		ctrl.addFile("existingModule", file) match {
			case Success(f) => assert(file == f)
			case _ => fail()
		}
		assert(ctrl.snapshot.contains("existingModule"))
		assert(ctrl.snapshot("existingModule").size == 2)
	}

	it should "report errors" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, ProjectConfig.None())
		val file = new File(s"$resPath/notExists")

		val result = ctrl.addFile(file.getCanonicalPath, file)


		result match {
			case err @ Failure(errFile, why) =>
				assert(why.contains("not exist"))
				assert(errFile == file)
		}

		val dir = new File(resPath)
		val resultDir = ctrl.addFile("dir", dir)

		resultDir match {
			case err@Failure(errFile, why) =>
				assert(why.contains("not a file"))
				assert(errFile.getPath == resPath)
		}

		val notScala = new File(s"$resPath/notScalaFile.java")
		val files = Set(notScala, dir, file)
		val res = ctrl.addFiles("multiple", files)
		res match {
			case Failure(who, why) =>
				assert(who == dir)
				assert(why.contains("not a file"))
		}
	}

	it should "recursively add a folder recursively" in {
		val path = s"$resPath/folder"
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, ProjectConfig.Scala())

		ctrl.addFilesRecursively("folder", path) match {
			case Warning(logs) =>
				assert(logs.count(log => log.fount == new File(s"$path/subfolder/notScalaFile.java")) == 1)
			case _ =>
		}

		assert(ctrl.snapshot("folder").size == 3)
	}

	it should "recursively add a gradle project folder" in {
		val path = s"$resPath/sam copy"
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, ProjectConfig.ScalaGradle())

		ctrl.addProject(path)

		assert(ctrl.snapshot.size == 6)
		assert(ctrl.snapshot.contains("controllers"))
		assert(ctrl.snapshot.contains("core"))
		assert(ctrl.snapshot.contains("domain"))
		assert(ctrl.snapshot.contains("presenters"))
		assert(ctrl.snapshot.contains("view"))
		println(ctrl.snapshot)
	}
}
