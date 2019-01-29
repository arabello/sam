package io.sam.controllers

import java.io.File

import io.sam.domain.airelation.InputData
import io.sam.domain.airelation.InputBoundary

class AIRelationController(inputBoundary: InputBoundary) {
	private var modules = Map[String, Set[File]]()

	def snapshot: AnyRef = modules.clone()

	def addFiles(moduleName: String, resources: Set[File]): Result = {
		resources.foreach(f =>
			addFile(moduleName, f) match {
				case Success() =>
				case _ => return _
			}
		)
		Success()
	}

	def addFile(moduleName: String, file: File): Result = {
		if (!file.exists())
			return FileNotExists(file)

		if (!file.isFile)
			return NotAFile(file)

		if (!modules.contains(moduleName))
			modules += (moduleName -> Set())

		modules(moduleName) += file
	}

	def submit(): Unit = inputBoundary.measure(InputData(modules))
}
