package io.sam.domain.levelsmap

import io.sam.core.{Analyzer, CodeFactory, Component}
import io.sam.domain.{InputBoundary, OutputBoundary}

import scala.annotation.tailrec

class LayersMapUseCase(out: OutputBoundary[OutputData]) extends Analyzer with InputBoundary[InputData] with CodeFactory{
	override def measure(data: InputData): Unit = {
		val submittedModules = data.components.map{
			case (name, resources) => Component(name, resources.map{ case (id, src) => mkCodeFromSource(id, src) })
		}.toSet

		val deps = mkDependencies(submittedModules.map{ m => analyze(m)(new LevelsMapTraverser(m)).result })
		val orderedDeps = orderDependencies(deps)

		out.deliver(OutputData(orderedDeps))
	}

	private class LevelsMapTraverser(module: Component) extends Traverser{
		private var packages = Set[String]()
		private var imports = Set[String]()

		override def traverse(tree: Tree): Unit = tree match {
			case Import(expr, _) =>
				imports += expr.toString()

				super.traverse(expr)
			case PackageDef(pid, stats) =>
				packages += pid.toString()

				super.traverseTrees(stats)
				super.traverse(pid)

			case _ => super.traverse(tree)
		}

		def result: TraverserResult = TraverserResult(module, packages, imports)
	}

	private case class TraverserResult(module: Component, packages: Set[String], imports: Set[String])

	private def mkDependencies(traverserResults: Set[TraverserResult]): Set[Dependency] =
		for{curr <- traverserResults} yield Dependency(
			for {
				from <- traverserResults.filter(it => it != curr)
				imp <- from.imports
				pck <- curr.packages
				if imp.contains(pck)
			} yield from.module,

			curr.module,

			for {
				to <- traverserResults.filter(it => it != curr)
				imp <- curr.imports
				pck <- to.packages
				if imp.contains(pck)
			} yield to.module
		)

	private def orderDependencies(dependencies: Set[Dependency]): List[Set[Dependency]] = {
		@tailrec
		def orderDepsRec(remaining: Set[Dependency], acc: List[Set[Dependency]]): List[Set[Dependency]] = {
			val currLevel = remaining.filter(d => d.out.intersect(remaining.map(m => m.module)).isEmpty)
			if (currLevel.isEmpty)
				acc
			else
				orderDepsRec(remaining -- currLevel, acc :+ currLevel)
		}

		orderDepsRec(dependencies, List())
	}
}
