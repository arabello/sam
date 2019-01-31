package io.sam.presenters

trait ObservableViewModel[S] {
	this: S =>
	private var observers: List[S => Unit] = Nil
	def addObserver(observer: S => Unit) = observers = observer :: observers
	def notifyObservers() = observers.foreach(_.apply(this))
}
