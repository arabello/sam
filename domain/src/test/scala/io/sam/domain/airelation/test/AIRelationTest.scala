package io.sam.domain.airelation.test

import java.io.File

import io.sam.domain.airelation._
import org.scalatest.FlatSpec

class AIRelationTest extends FlatSpec{
	val resPath = "domain/src/test/resources"

	"AIRelationInteractor" should "submit modules" in{
		object presenter extends OutputBoundary{
			override def deliver(outputData: OutputData): Unit = {
				println(outputData)
			}
		}

		object gateway extends DataGateway{}

		val interactor = new AIRelationInteractor(presenter, gateway)

		val inputData = InputData(Map(
			"Ca" -> Set[File](new File(s"$resPath/q"), new File(s"$resPath/r")),
			"Cb" -> Set[File](new File(s"$resPath/s")),
			"Cc" -> Set[File](new File(s"$resPath/t"), new File(s"$resPath/u")),
			"Cd" -> Set[File](new File(s"$resPath/v"))
		))

		interactor.measure(inputData)
	}
}
