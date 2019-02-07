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
		assert(ctrl.snapshot("newModule").contains(file.getPath -> file))
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

		ctrl.addFile(file.getPath, file) match {
			case err @ Failure(who, why) =>
				assert(why.contains("not exist"))
				assert(who == file)
		}

		val dir = new File(resPath)

		ctrl.addFile("dir", dir) match {
			case Failure(who: File, why) =>
				assert(why.contains("not a file"))
				assert(who.getPath == resPath)
			case _ => fail()
		}
	}

	it should "recursively add a folder recursively" in {
		val path = s"$resPath/folder"
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, ProjectConfig.Scala())

		val check = new File (s"$path/subfolder/notScalaFile.java")

		ctrl.addFilesRecursively("folder", path) match {
			case Logs(logs, _) => for (log <- logs) log match {
					case Failure(who, why) =>
						assert(who == check)
						assert(why.contains("extension is not accepted"))
					case _ => fail()
				}
			case _ => fail()
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
