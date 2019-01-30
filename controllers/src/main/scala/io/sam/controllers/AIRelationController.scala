package io.sam.controllers

import java.io.File

import io.sam.domain.airelation.{InputBoundary, InputData}

class AIRelationController(inputBoundary: InputBoundary) {
	private val data = scala.collection.mutable.Map[String, Set[File]]()

	def clear(): Unit = {data.keys foreach( key => data.remove(key))}

	def snapshot: Map[String, Set[File]] = data.toMap

	def addFiles(moduleName: String, resources: Set[File]): Result = {
		resources.foreach(f =>
			addFile(moduleName, f) match {
				case Success() =>
				case error: Error => return error
			}
		)
		Success()
	}

	def addFile(moduleName: String, file: File): Result = {
		if (!file.exists())
			return FileNotExists(file)

		if (!file.isFile)
			return NotAFile(file)

		if (!data.contains(moduleName))
			data += (moduleName -> Set())

		data(moduleName) += file
		Success()
	}

	def submit(): Unit = inputBoundary.measure(InputData(data.toMap))
}
