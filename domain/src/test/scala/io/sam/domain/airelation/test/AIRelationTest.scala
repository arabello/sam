package io.sam.domain.airelation.test

import java.io.File

import io.sam.domain.airelation._
import org.scalatest.FlatSpec

class AIRelationTest extends FlatSpec{
	val resPath = "domain/src/test/resources"
	object gateway extends DataGateway{}
	val inputData = InputData(Map(
		"Ca" -> Set[File](new File(s"$resPath/q"), new File(s"$resPath/r")),
		"Cb" -> Set[File](new File(s"$resPath/s")),
		"Cc" -> Set[File](new File(s"$resPath/t"), new File(s"$resPath/u")),
		"Cd" -> Set[File](new File(s"$resPath/v"))
	))

	val abstractness = Map(
		"Ca" -> 0.0,
		"Cb" -> 0.0,
		"Cc" -> 0.5,
		"Cd" -> 1.0
	)

	val instability = Map(
		"Ca" -> 1.0,
		"Cb" -> 1.0,
		"Cc" -> 0.25,
		"Cd" -> 0.0
	)

	val distance = Map(
		"Ca" -> 0.0,
		"Cb" -> 0.0,
		"Cc" -> 0.25,
		"Cd" -> 0.0
	)

	"AIRelationInteractor" should "submit modules and deliver response" in{
		object presenter extends OutputBoundary{
			override def deliver(outputData: OutputData): Unit = {
				assert(true)
			}
		}
		new AIRelationInteractor(presenter, gateway).measure(inputData)
	}

	it should "calculate abstractness, instability and distance" in{
		object presenter extends OutputBoundary{
			override def deliver(outputData: OutputData): Unit = {
				outputData.modules foreach { mod =>
					assert(mod.abstractness == abstractness(mod.name))
					assert(mod.instability == instability(mod.name))
					assert(mod.distance == distance(mod.name))
				}
			}
		}
		new AIRelationInteractor(presenter, gateway).measure(inputData)
	}

}
