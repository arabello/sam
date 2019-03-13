package io.sam.controllers.test

import java.io.FileNotFoundException
import java.nio.file.Paths

import io.sam.controllers.airelation.{AIRelationController, SourceCodeFile}
import io.sam.controllers.{Failure, NotScalaFile, Success}
import io.sam.domain.airelation._
import org.scalatest.FlatSpec

class AIRelationTest extends FlatSpec{
	object gateway extends DataGateway{}
	object presenter extends OutputBoundary{
		override def deliver(outputData: OutputData): Unit = {}
	}
	object domainBoundary extends InputBoundary {
		override def measure(data: InputData): Unit = {}
	}

	val resPath = "controllers/src/test/resources"

	"AIRelationController" should "create instances from file" in {
		val scalaFile = Paths.get(s"$resPath/1.scala")
		val invalidFile = Paths.get(s"$resPath/@@@@.scala")
		val notScalaFile = Paths.get(s"$resPath/notScalaFile.java")

		AIRelationController.createFromFile(invalidFile) match{
			case Failure(why: FileNotFoundException) => assert(why.toString.contains(invalidFile.toString))
			case _ => fail()
		}

		AIRelationController.createFromFile(notScalaFile) match{
			case Failure(why: NotScalaFile) => assert(why.toString.contains(notScalaFile.toString))
			case _ => fail()
		}

		AIRelationController.createFromFile(scalaFile) match{
			case Success(content) => assert(content.file == scalaFile)
			case _ => fail()
		}
	}

	it should "create instances from folder" in {
		val folder = Paths.get(s"$resPath/module")
		val invalidFolder = Paths.get(s"$resPath/not/exists/for/sure")
		val notAFolder = Paths.get(s"$resPath/1.scala")

		val scf1 = AIRelationController.createFromFile(Paths.get(s"$resPath/module/src/main/scala/io/pack/1.scala")).asInstanceOf[Success[SourceCodeFile]].content
		val scf2 = AIRelationController.createFromFile(Paths.get(s"$resPath/module/src/main/scala/io/pack/age/2.scala")).asInstanceOf[Success[SourceCodeFile]].content

		AIRelationController.createFromFolder(invalidFolder) match {
			case Failure(why: FileNotFoundException) => assert(why.toString.contains(invalidFolder.toString))
			case _ => fail()
		}

		AIRelationController.createFromFolder(notAFolder) match {
			case Failure(why: Exception) => assert(why.toString.contains(notAFolder.toString))
			case _ => fail()
		}

		AIRelationController.createFromFolder(folder) match {
			case Success(content) =>
				assert(content.sources.size == 2)
				assert(content.sources.contains(scf1) && content.sources.contains(scf2))
			case _ => fail()
		}
	}

	it should "manage state history" in {
		val folder = Paths.get(s"$resPath/module")
		val ctrl = new AIRelationController(domainBoundary)

		assert(ctrl.snapshot.isEmpty)

		AIRelationController.createFromFolder(folder) match {
			case Success(content) => ctrl.add(content)
			case _ => fail()
		}

		assert(ctrl.snapshot.head.sources.size == 2)

		ctrl.undo()

		assert(ctrl.snapshot.isEmpty)
	}
}
