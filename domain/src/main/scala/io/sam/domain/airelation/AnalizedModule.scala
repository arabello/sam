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

		imports foreach { thisImport => // check imports of the module
			if (on.packages.contains(thisImport.expr.toString())){ // if an import name expr is contained in the packages of an other component the fanin must be incremented
				nDeps += thisImport.selectors.size // increment the fanin with the num of the classes used by the module
				// Check for WILDCARD, if WILDCARD is used is assumed that the module use all the classes contained by the specified package
				thisImport.selectors foreach{ selector =>
					if (selector.name == Analyzer.termNames.WILDCARD)
						nDeps += on.numClasses
				}
			}
		}

		nDeps
	}

	def instability(others: Set[AnalizedModule]): Float = {
		var fanin = 0
		var fanout = 0

		others foreach{ other =>
			fanin += numDependencies(other)
			fanout += other.numDependencies(this)
		}

		fanout / (fanin + fanout) // instability formula
	}

	def abstractness: Float = numAbstractClasses / numClasses // abstractness formula
}
