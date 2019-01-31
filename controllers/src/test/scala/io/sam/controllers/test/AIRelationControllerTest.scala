package io.sam.controllers.test

import java.io.File

import io.sam.controllers.AIRelationController
import org.scalatest.FlatSpec
import io.sam.domain.airelation.{AIRelationInteractor, DataGateway, OutputBoundary, OutputData}

class AIRelationControllerTest extends FlatSpec{
	object gateway extends DataGateway{}
	object presenter extends OutputBoundary{
		override def deliver(outputData: OutputData): Unit = {}
	}

	val resPath = "controllers/src/test/resources"

	"AIRElationController" should "add a file to a new module" in {
		val interactor = new AIRelationInteractor(presenter, gateway)
		val ctrl = new AIRelationController(interactor)
		val file = new File(s"$resPath/single")

		assert(ctrl.addFile("newModule", file).isSuccessfully)
		assert(ctrl.snapshot.contains("newModule"))
		assert(ctrl.snapshot("newModule").contains(file.getCanonicalPath -> file))
	}
}
