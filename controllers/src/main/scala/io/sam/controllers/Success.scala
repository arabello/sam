package io.sam.controllers

case class Success() extends Result{
	override val isSuccessfully: Boolean = true
}