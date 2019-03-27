package io.sam.domain.airelation

import io.sam.core._
import io.sam.domain.{InputBoundary, OutputBoundary}

class AIRelationUseCase(out: OutputBoundary[OutputData]) extends Analyzer with InputBoundary[InputData] with CodeFactory {
	override def measure(data: InputData): Unit = {

		val analyzedModules = data.components.map{
			case (name, resources) =>
				val sc = resources.map{ case (id, src) => mkCodeFromSource(id, src) }
				val c = Component(name, sc)
				analyze(c)(new AIRelationTraverser()).results(c)
		}.toSet

		val measuredModules = analyzedModules.map{ current =>
			val A = current.abstractness
			val others = analyzedModules.filter(m => m != current)
			val I = current.instability(others)
			val D = scala.math.abs(A + I - 1)
			MeasuredModule(current.code.id, A, I, D)
		}

		out.deliver(OutputData(measuredModules))
	}

	private class AIRelationTraverser extends Traverser {
		private var packages = Set[String]()
		private var imports = Set[Import]()
		private var numClasses = 0
		private var numAbstractClasses = 0

		override def traverse(tree: Tree): Unit = tree match {
			case ModuleDef(mods, name, impl) =>
				numClasses += 1

				super.traverse(impl)
			case ClassDef(mods, name, tparams, impl) =>
				if (mods.hasFlag(Flag.ABSTRACT))
					numAbstractClasses += 1

				numClasses += 1
				super.traverseTrees(tparams)
				super.traverse(impl)
			case node @ Import(expr, selectors) =>

				imports += node

				super.traverse(expr)
			case node @ PackageDef(pid, stats) =>

				packages += pid.toString()

				super.traverseTrees(stats)
				super.traverse(pid)
			case _ => super.traverse(tree)
		}

		def results(module: Component): AnalyzedModule = AnalyzedModule(module, packages, imports, numClasses, numAbstractClasses)
	}

	private case class AnalyzedModule(code: Component, packages: Set[String], imports: Set[Import], numClasses: Int, numAbstractClasses: Int){
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

		private def numDependencies(on: AnalyzedModule): Int = {
			var nDeps = 0

			imports foreach{ imp =>

				on.packages foreach{ pck =>
					if (imp.expr.toString().contains(pck)){
						nDeps += imp.selectors.size
						imp.selectors foreach{ selector =>
							if (selector.name == AIRelationUseCase.this.termNames.WILDCARD)
								nDeps += on.numClasses - 1
						}
					}
				}
			}

			nDeps
		}

		def instability(others: Set[AnalyzedModule]): Float = {
			var fanin = 0
			var fanout = 0

			others foreach{ other =>
				fanin += other.numDependencies(this)
				fanout += numDependencies(other)
			}

			fanout.toFloat / (fanin + fanout) // instability formula
		}

		def abstractness: Float =  numAbstractClasses.toFloat / numClasses // abstractness formula
	}
}
