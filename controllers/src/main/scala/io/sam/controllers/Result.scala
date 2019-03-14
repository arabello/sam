package io.sam.controllers

sealed trait Result[T]

case class Success[S](content: S) extends Result[S]

case class Failure[T, F <: Exception](why: F) extends Result[T]


