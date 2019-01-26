package io.sam.domain.airelation

trait Observers[S] {
	this: S =>
	private var observers: List[S => Unit] = List[S => Unit]()
	def addObserver(observer: S => Unit): Unit = observers = observer :: observers
	def notifyObservers(): Unit = observers.foreach(_.apply(this))
}
