package io.sam.view

import java.io.File

trait Reporter[R <: Report] {
	def generateReport(): R
	def writeReport(file: File)
}
