package io.sam.domain.airelation

import io.sam.core.{Analyzer, Module}

private case class AnalizedModule(code: Module, packages: Set[String], imports: Set[Analyzer.Import], numClasses: Int, numAbstractClasses: Int){
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

	private def numDependencies(on: AnalizedModule): Int = {
		var nDeps = 0

		imports foreach{ imp =>

			on.packages foreach{ pck =>
				if (imp.expr.toString().contains(pck)){
					nDeps += 1
					imp.selectors foreach{ selector =>
						if (selector.name == Analyzer.termNames.WILDCARD)
							nDeps += on.numClasses - 1
					}
				}
			}
		}

		nDeps
	}

	def instability(others: Set[AnalizedModule]): Float = {
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
