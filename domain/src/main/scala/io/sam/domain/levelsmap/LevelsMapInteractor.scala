package io.sam.domain.levelsmap

import io.sam.core.{Analyzer, Code, CodeFactory, Component}
import io.sam.domain.{InputBoundary, OutputBoundary}

class LevelsMapInteractor(out: OutputBoundary[OutputData]) extends InputBoundary[InputData] with CodeFactory{
	override def measure(data: InputData): Unit = {
		var submittedModules = Set[Component]()

		data.components foreach { case (name, resources) =>
			var sources = Set[Code]()
			resources foreach { case (id, src) =>
				sources += mkCodeFromSource(id, src)
			}
			submittedModules += Component(name, sources)
		}

		val rawDeps = for{
			comp <- submittedModules
			traverser = new LevelsMapTraverser(comp)
			ast = Analyzer.parseCode(comp)
			_ = traverser.traverse(ast)
		} yield traverser.rawDependency

		val deps = analyze(rawDeps)
		val output = orderDeps(deps)
		out.deliver(OutputData(output))
	}

	private class LevelsMapTraverser(module: Component) extends Analyzer.Traverser{
		private var packages = Set[String]()
		private var imports = Set[String]()

		override def traverse(tree: Analyzer.Tree): Unit = tree match {
			case Analyzer.Import(expr, selectors) =>
				imports += expr.toString()

				super.traverse(expr)

			case Analyzer.PackageDef(pid, stats) =>
				packages += pid.toString()

				super.traverseTrees(stats)
				super.traverse(pid)

			case _ => super.traverse(tree)
		}

		def rawDependency: RawDependency = RawDependency(module, packages, imports)
	}

	private def analyze(dependencies: Set[RawDependency]): Set[Dependency] =
		for{curr <- dependencies} yield Dependency(
			for {
				from <- dependencies.filter(it => it != curr)
				imp <- from.imports
				pck <- curr.packages
				if imp.contains(pck)
			} yield from.module,

			curr.module,

			for {
				to <- dependencies.filter(it => it != curr)
				imp <- curr.imports
				pck <- to.packages
				if imp.contains(pck)
			} yield to.module
		)

	private def orderDeps(dependencies: Set[Dependency]): List[Set[Dependency]] = {
		val firstLevel = dependencies.filter(d => d.out.isEmpty)

		def orderDepsRec(upperLevel: Set[Dependency]): List[Set[Dependency]] = {
			val currLevel = dependencies.filter(d => d.out.intersect(for{ dep <- upperLevel} yield dep.module).nonEmpty)
			if (currLevel.isEmpty)
				List(currLevel)
			else
				orderDepsRec(currLevel) :+ currLevel
		}

		firstLevel +: orderDepsRec(firstLevel)
	}
}
