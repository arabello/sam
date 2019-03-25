package io.sam.domain.levelsmap.test

import java.io.File

import io.sam.domain.OutputBoundary
import io.sam.domain.levelsmap.{InputData, LevelsMapUseCase, OutputData}
import org.scalatest.FlatSpec

import scala.io.Source

class LevelsMapTest extends FlatSpec{
	val resPath = "domain/src/test/resources/levelsmap"

	val inputData = InputData(Map(
		"a-level" -> Set(("a-level", Source.fromFile(new File(s"$resPath/a-level")))),
		"b-level" -> Set(("b-level", Source.fromFile(new File(s"$resPath/b-level")))),
		"c-level" -> Set(("c-level", Source.fromFile(new File(s"$resPath/c-level")))),
		"d-level" -> Set(("d-level", Source.fromFile(new File(s"$resPath/d-level"))))
	))

	val outputCheck = List(Set("A1", "A2", "A3"), Set("B1", "B2", "B3"), Set("C1", "C2", "C3"), Set("D1", "D2", "D3"))

	"LevelsMapUseCase" should "submit modules and deliver response" in{
		object presenter extends OutputBoundary[OutputData]{
			override def deliver(outputData: OutputData): Unit = {
				assert(true)
			}
		}
		val lm = new LevelsMapUseCase(presenter)
		lm.measure(inputData)
	}

	it should "calculate levels map" in{
		object presenter extends OutputBoundary[OutputData]{
			override def deliver(outputData: OutputData): Unit = {
				assert(outputData.levels.size == 4)
				for {
					i <- 0 to 4
					check <- outputCheck(i)
					dep <- outputData.levels(i)
				}{
					assert(dep.module.content.contains(check))
				}
			}
		}
		val lm = new LevelsMapUseCase(presenter)
		lm.measure(inputData)
	}
}
