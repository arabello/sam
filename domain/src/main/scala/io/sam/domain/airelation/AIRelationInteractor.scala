package io.sam.domain.airelation

import java.io.File

import io.sam.core._

import scala.io.Source

class AIRelationInteractor extends OutputBoundary with InputBoundary with DataGateway with Observers[AIRelationInteractor] {
	private var inputData = InputData(Map[String, Set[File]]())
	private var outputData = OutputData(Set[MeasuredModule]())

	private var modules = Set[Module]()
	private var packageMap = Map[Module, Set[String]]()
	private var classesMap = Map[Module, Int]()
	private var importsMap = Map[Module, Set[Analyzer.Import]]()
	private var abstractionMap = Map[Module, Int]()

	class AIRelationTraverser(mod: Module) extends Analyzer.Traverser{

		override def traverse(tree: Analyzer.Tree) = tree match {
			case Analyzer.ClassDef(mods, name, tparams, impl) =>
				if (mods.hasFlag(Analyzer.Flag.ABSTRACT))
					abstractionMap(mod) += 1

				classesMap(mod) += 1
				super.traverseTrees(tparams)
				super.traverse(impl)
			case node @ Analyzer.Import(expr, selectors) =>

				importsMap += (mod -> node)

				super.traverse(expr)
			case node @ Analyzer.PackageDef(pid, stats) =>

				packageMap += (mod -> pid.toString())

				super.traverseTrees(stats)
				super.traverse(pid)
			case _ => super.traverse(tree)
		}
	}

	private def measureAbstraction(module: Module): Float = abstractionMap(module) / classesMap(module)

	private def measureInstabilityFor(refModule: Module): Float = {
		/*
			For "import io.pck.b.A\n":
			expr >> io.pck.b
			selectors >> List(ImportSelector(TermName("A"), 33, TermName("A"), 33))
			----
			For "import io.pck.b._\n":
			expr >> io.pck.b
			selectors >> List(ImportSelector(termNames.WILDCARD, 51, null, -1))
			----
			For "import io.pck.b.{h => A}\n":
			expr >> io.pck
			selectors >> List(ImportSelector(TermName("h"), 68, TermName("A"), 73))
		*/

		var fanin = 0
		var fanout = 0

		modules foreach{ thisModule =>
			if (thisModule != refModule) {

				// FANIN CALC.
				importsMap(refModule) foreach { thisImport => // check imports of the refModule
					if (packageMap(thisModule).contains(thisImport.expr.toString())){ // if an import name expr is contained in the packages of an other component the fanin must be incremented
						fanin += thisImport.selectors.size // increment the fanin with the num of the classes used by the refModule
						// Check for WILDCARD, if WILDCARD is used is assumed that the refModule use all the classes contained by the specified package
						thisImport.selectors foreach{ selector =>
							if (selector.name == Analyzer.termNames.WILDCARD)
								fanin += classesMap(thisModule)
						}
					}
				}

				// FANOUT CALC. (as FANIN but import reference inverted)
				importsMap(thisModule) foreach { thisImport =>
					if (packageMap(refModule).contains(thisImport.expr.toString())){
						fanout += thisImport.selectors.size

						thisImport.selectors foreach{ selector =>
							if (selector.name == Analyzer.termNames.WILDCARD)
								fanout += classesMap(refModule)
						}
					}
				}
			}
		}

		fanout / (fanin + fanout) // instability formula
	}

	override def measure(): Unit = {
		modules foreach{ mod =>
			classesMap += (mod -> 0)
			abstractionMap += (mod -> 0)

			val traverser = new AIRelationTraverser(mod)
			val ast = Analyzer.parseCode(mod)
			traverser.traverse(ast)
		}

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
