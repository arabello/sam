package io.sam.controllers.test

import java.io.FileNotFoundException
import java.nio.file.Paths

import io.sam.controllers.airelation.{SoftwareModule, SourceCodeFile}
import io.sam.controllers.{Failure, NotScalaFile, Success}
import io.sam.domain.airelation.{DataGateway, OutputBoundary, OutputData}
import org.scalatest.FlatSpec

class AIRelationTest extends FlatSpec{
	object gateway extends DataGateway{}
	object presenter extends OutputBoundary{
		override def deliver(outputData: OutputData): Unit = {}
	}

	val resPath = "controllers/src/test/resources"

	"SourceCodeFile factory" should "create instance from file" in {
		val scalaFile = Paths.get(s"$resPath/1.scala")
		val invalidFile = Paths.get(s"$resPath/@@@@.scala")
		val notScalaFile = Paths.get(s"$resPath/notScalaFile.java")

		SourceCodeFile.createFromFile(invalidFile) match{
			case Failure(why: FileNotFoundException) => assert(why.toString.contains(invalidFile.toString))
			case _ => fail()
		}

		SourceCodeFile.createFromFile(notScalaFile) match{
			case Failure(why: NotScalaFile) => assert(why.toString.contains(notScalaFile.toString))
			case _ => fail()
		}

		SourceCodeFile.createFromFile(scalaFile) match{
			case Success(content) => assert(content.file == scalaFile)
			case _ => fail()
		}
	}

	"SoftwareModule factory" should "create instance from folder" in {
		val folder = Paths.get(s"$resPath/module")
		val invalidFolder = Paths.get(s"$resPath/not/exists/for/sure")
		val notAFolder = Paths.get(s"$resPath/1.scala")

		val scf1 = SourceCodeFile.createFromFile(Paths.get(s"$resPath/module/src/main/scala/io/pack/1.scala")).asInstanceOf[Success[SourceCodeFile]].content
		val scf2 = SourceCodeFile.createFromFile(Paths.get(s"$resPath/module/src/main/scala/io/pack/age/2.scala")).asInstanceOf[Success[SourceCodeFile]].content

		SoftwareModule.createFromFolder(invalidFolder) match {
			case Failure(why: FileNotFoundException) => assert(why.toString.contains(invalidFolder.toString))
			case _ => fail()
		}

		SoftwareModule.createFromFolder(notAFolder) match {
			case Failure(why: Exception) => assert(why.toString.contains(notAFolder.toString))
			case _ => fail()
		}

		SoftwareModule.createFromFolder(folder) match {
			case Success(content) =>
				assert(content.sources.size == 2)
				assert(content.sources.contains(scf1) && content.sources.contains(scf2))
			case _ => fail()
		}
	}

	/*

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
	*/
}
