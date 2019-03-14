package io.sam.domain.airelation

import io.sam.core.{Analyzer, Code, CodeFactory, Component}

class AIRelationInteractor(out: OutputBoundary) extends InputBoundary with CodeFactory {

	override def measure(data: InputData): Unit = {
		var submittedModules = Set[Component]()

		data.components foreach { case (name, resources) =>
			var sources = Set[Code]()
			resources foreach { case (id, src) =>
				sources += mkCodeFromSource(id, src)
			}
			submittedModules += Component(name, sources)
		}

		var measuredModules = Set[MeasuredModule]()
		val analizedModules = analize(submittedModules)

		analizedModules foreach{ current =>
			val A = current.abstractness
			val others = analizedModules.filter(m => m != current)
			val I = current.instability(others)
			val D = scala.math.abs(A + I - 1)
			measuredModules += MeasuredModule(current.code.id, A, I, D)
		}

		out.deliver(OutputData(measuredModules))
	}

	private class AIRelationTraverser(module: Component) extends Analyzer.Traverser{
		private var packages = Set[String]()
		private var imports = Set[Analyzer.Import]()
		private var numClasses = 0
		private var numAbstractClasses = 0

		override def traverse(tree: Analyzer.Tree): Unit = tree match {
			case Analyzer.ModuleDef(mods, name, impl) =>
				numClasses += 1

				super.traverse(impl)
			case Analyzer.ClassDef(mods, name, tparams, impl) =>
				if (mods.hasFlag(Analyzer.Flag.ABSTRACT))
					numAbstractClasses += 1

				numClasses += 1
				super.traverseTrees(tparams)
				super.traverse(impl)
			case node @ Analyzer.Import(expr, selectors) =>

				imports += node

				super.traverse(expr)
			case node @ Analyzer.PackageDef(pid, stats) =>

				packages += pid.toString()

				super.traverseTrees(stats)
				super.traverse(pid)
			case _ => super.traverse(tree)
		}

		def analizedModule: AnalizedModule = AnalizedModule(module, packages, imports, numClasses, numAbstractClasses)
	}

	private def analize(modules: Set[Component]): Set[AnalizedModule] = {
		var result = Set[AnalizedModule]()

		modules foreach{ module =>
			val ast = Analyzer.parseCode(module)
			val traverser = new AIRelationTraverser(module)
			traverser.traverse(ast)
			result += traverser.analizedModule
		}

		result
	}
}
