package io.sam.view

trait Report {
	val content: String
}

trait HTML extends Report {}
trait JSON extends Report {}
trait XML extends Report {}