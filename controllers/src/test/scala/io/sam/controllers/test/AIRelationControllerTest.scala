package io.sam.controllers.test

import java.io.File

import io.sam.controllers.result._
import io.sam.controllers.{AIRelationController, Config}
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway, OutputBoundary, OutputData}
import org.scalatest.FlatSpec

class AIRelationControllerTest extends FlatSpec{
	object gateway extends DataGateway{}
	object presenter extends OutputBoundary{
		override def deliver(outputData: OutputData): Unit = {}
	}

	val resPath = "controllers/src/test/resources"

	"AIRElationController" should "add a file" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, Config.Simple(Config.SCALA_EXT))
		val file = new File(s"$resPath/1.scala")
		val errFile = new File("notExist")
		val errDir = new File(s"$resPath")

		ctrl.addFile("error", errFile) match {
			case Failure(who, why) =>
				assert(who == errFile)
				assert(why.contains("does not exists"))
			case _ => fail()
		}

		ctrl.addFile("error", errDir) match {
			case Failure(who, why) =>
				assert(who == errDir)
				assert(why.contains("is not a file"))
			case _ => fail()
		}

		ctrl.addFile("newModule", file) match {
			case Success(f) => assert(file == f)
			case _ => fail()
		}
		assert(ctrl.snapshot.contains("newModule"))
		assert(ctrl.snapshot("newModule").contains(file.getPath -> file))
	}

	it should "add files recursively in a folder" in {
		val path = s"$resPath/folder"
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor, Config.Simple(Config.SCALA_EXT))
		val check = new File (s"$path/subfolder/notScalaFile.java")

		ctrl.addFilesRecursively("folder", path) match {
			case Logs(logs) =>
				for (log <- logs) log match {
					case Failure(who, why) =>
						assert(who == check)
						assert(why.contains("extension is not accepted"))
					case _ =>
				}
			case _ => fail()
		}

		assert(ctrl.snapshot("folder").size == 3)
		assert(ctrl.snapshot("folder").contains((s"$resPath/folder/2.scala", new File(s"$resPath/folder/2.scala"))))
		assert(ctrl.snapshot("folder").contains((s"$resPath/folder/subfolder/1.scala", new File(s"$resPath/folder/subfolder/1.scala"))))
		assert(ctrl.snapshot("folder").contains((s"$resPath/folder/subfolder/supersubfolder/1.scala", new File(s"$resPath/folder/subfolder/supersubfolder/1.scala"))))
	}

	it should "add project as a folder using scala/gradle config" in {
		val path = s"$resPath/add-project-gradle"
		val interactor = new AIRelationInteractor(presenter, gateway)
		val config = Config.Gradle(Config.SCALA_EXT)
		val ctrl = new AIRelationController(interactor, config)

		ctrl.addProject("/err") match {
			case Logs(logs) =>
				assert(logs.size == 1)
				assert(logs(0).fount == new File("/err"))
			case _ => fail()
		}

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
