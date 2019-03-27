package io.sam.domain.levelsmap

import io.sam.core.{Analyzer, CodeFactory, Component}
import io.sam.domain.{InputBoundary, OutputBoundary}

import scala.annotation.tailrec

class LevelsMapUseCase(out: OutputBoundary[OutputData]) extends Analyzer with InputBoundary[InputData] with CodeFactory{
	override def measure(data: InputData): Unit = {
		val submittedModules = data.components.map{
			case (name, resources) => Component(name, resources.map{ case (id, src) => mkCodeFromSource(id, src) })
		}.toSet

		val output = List()
		out.deliver(OutputData(output))
	}

	private class LevelsMapTraverser(module: Component) extends Traverser{
		private var packages = Set[String]()
		private var imports = Set[String]()

		override def traverse(tree: Tree): Unit = tree match {
			case Import(expr, selectors) =>
				imports += expr.toString()

				super.traverse(expr)

			case PackageDef(pid, stats) =>
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

		@tailrec
		def orderDepsRec(upperLevel: Set[Dependency], acc: List[Set[Dependency]]): List[Set[Dependency]] = { // TODO Fix
			val currLevel = dependencies.filter(d => d.out.intersect(for{ dep <- upperLevel} yield dep.module).nonEmpty)
			if (currLevel.isEmpty)
				List(currLevel)
			else
				orderDepsRec(currLevel, acc :+ currLevel)
		}

		firstLevel +: orderDepsRec(firstLevel, List())
	}
}
