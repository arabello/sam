package io.sam.controllers.test

import java.io.File

import io.sam.controllers.AIRelationController
import io.sam.controllers.result.{FileNotExists, NotAFile, NotScalaFile}
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
		val ctrl = new AIRelationController(interactor)
		val file = new File(s"$resPath/1.scala")

		assert(ctrl.addFile("newModule", file).isSuccessfully)
		assert(ctrl.snapshot.contains("newModule"))
		assert(ctrl.snapshot("newModule").contains(file.getCanonicalPath -> file))
	}

	it should "add a file to an existing module" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor)
		val file = new File(s"$resPath/1.scala")

		ctrl.addFile("existingModule", new File(s"$resPath/2.scala"))

		assert(ctrl.addFile("existingModule", file).isSuccessfully)
		assert(ctrl.snapshot.contains("existingModule"))
		assert(ctrl.snapshot("existingModule").size == 2)
	}

	it should "report errors" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor)
		val file = new File(s"$resPath/notExists")

		val result = ctrl.addFile(file.getCanonicalPath, file)
		assert(!result.isSuccessfully)

		result match {
			case err @ FileNotExists(errFile) =>
				assert(err.mkHuman.contains("not exist"))
				assert(errFile == file)
		}

		val dir = new File(resPath)
		val resultDir = ctrl.addFile("dir", dir)
		assert(!resultDir.isSuccessfully)

		resultDir match {
			case err@NotAFile(errFile) =>
				assert(err.mkHuman.contains("not a file"))
				assert(errFile.getPath == resPath)
		}

		val notScala = new File(s"$resPath/notScalaFile.java")
		val files = Set(file, dir, notScala)
		val res = ctrl.addFiles("multiple", files)
		var c = 0
		res foreach {
			case NotScalaFile(f) =>
				assert(f == notScala)
				c += 1
			case FileNotExists(errFile) =>
				assert(errFile == file)
				c += 1
			case NotAFile(errFile) =>
				assert(errFile == dir)
				c += 1
			case _ =>
		}

		assert(c == 3)
	}

	it should "recursively read a folder" in {
		val path = s"$resPath/folder"
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor)

		ctrl.addFilesRecursively("folder", path) foreach {
			case NotScalaFile(f) => assert(f == new File(s"$path/subfolder/notScalaFile.java"))
			case _ =>
		}

		assert(ctrl.snapshot("folder").size == 3)
	}
}
