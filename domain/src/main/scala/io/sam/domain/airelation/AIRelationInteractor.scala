package io.sam.domain.airelation

import java.io.File

import io.sam.core._

import scala.io.Source

class AIRelationInteractor extends OutputBoundary with InputBoundary with DataGateway with Observers[AIRelationInteractor] {
	private var inputData = InputData(Map[String, Set[File]]())
	private var outputData = OutputData(Set[MeasuredModule]())

	private var modules = Set[Module]()

	private def measureAbstraction(module: Module): Float = {

		object traverser extends Analyzer.Traverser{
			var n_abstractClasses = 0
			var n_totClasses = 0
			override def traverse(tree: Analyzer.Tree) = tree match {

				case Analyzer.ClassDef(mods, name, tparams, impl) =>

					if (mods.hasFlag(Analyzer.Flag.ABSTRACT))
						n_abstractClasses += 1
					n_totClasses += 1

					super.traverseTrees(tparams)
					super.traverse(impl)
				case _ => super.traverse(tree)
			}
		}

		val ast = Analyzer.parseCode(module)
		traverser.traverse(ast)

		traverser.n_abstractClasses / traverser.n_totClasses
	}

	private def measureInstabilityFor(module: Module): Float = {
		var fanin = 0
		var fanout = 0

		var packageMap = Map[Module, Set[String]]()

		modules foreach{ mod =>

			object packageTraverser extends Analyzer.Traverser{
				var n_abstractClasses = 0
				var n_totClasses = 0
				override def traverse(tree: Analyzer.Tree) = tree match {

					case node @ Analyzer.PackageDef(pid, stats) =>

						packageMap += (mod -> pid.name.toChars.mkString)

						super.traverseTrees(stats)
						super.traverse(pid)
					case _ => super.traverse(tree)
				}
			}

			val ast = Analyzer.parseCode(mod)
			packageTraverser.traverse(ast)
		}


		val ast = Analyzer.parseCode(module)
		object instabilityTraverser extends Analyzer.Traverser{
			var n_abstractClasses = 0
			var n_totClasses = 0
			override def traverse(tree: Analyzer.Tree) = tree match {

				case node @ Analyzer.Import(expr, selectors) =>
					//TODO

					super.traverse(expr)
				case _ => super.traverse(tree)
			}
		}

		instabilityTraverser.traverse(ast)

		fanout / (fanin + fanout)
	}

	override def measure(): Unit = {
		outputData = OutputData(Set[MeasuredModule]())
		modules foreach{ module =>
			val A = measureAbstraction(module)
			val I = measureInstabilityFor(module)
			val D = scala.math.abs(A + I - 1)
			outputData.modules += MeasuredModule(module.id, A, I, D)
		}
		notifyObservers()
	}

	override def submitModules(data: InputData): Unit = {
		data.modules foreach{ case (name, files) =>
			var sources = Set[Code]()
			files foreach{ file =>
				sources += SourceCode(file.getCanonicalPath, Source.fromFile(file))
			}
			modules += Module(name, sources)
		}
	}
}
