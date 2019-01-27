package io.sam.domain.airelation

import java.io.File

import io.sam.core._

import scala.io.Source

private class AIRelationInteractor extends OutputBoundary with InputBoundary with DataGateway with Observers[AIRelationInteractor] {
	private var inputData = InputData(Map[String, Set[File]]())
	private var outputData = OutputData(Set[MeasuredModule]())
	private var submittedModules = Set[Module]()

	override def measure(): Unit = {
		outputData = OutputData(Set[MeasuredModule]())
		val analizedModules = analize(submittedModules)

		analizedModules foreach{ analizedModule =>
			val A = analizedModule.abstractness
			val I = analizedModule.instability(analizedModules.dropWhile(exclude => exclude == analizedModule))
			val D = scala.math.abs(A + I - 1)
			outputData.modules += MeasuredModule(analizedModule.code.id, A, I, D)
		}

		notifyObservers()
	}

	override def submitModules(data: InputData): Unit = {
		data.modules foreach{ case (name, files) =>
			var sources = Set[Code]()
			files foreach{ file =>
				sources += SourceCode(file.getCanonicalPath, Source.fromFile(file))
			}
			submittedModules += Module(name, sources)
		}
	}

	private class AIRelationTraverser(module: Module) extends Analyzer.Traverser{
		private var packages = Set[String]()
		private var imports = Set[Analyzer.Import]()
		private var numClasses = 0
		private var numAbstractClasses = 0

		override def traverse(tree: Analyzer.Tree) = tree match {
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

	private def analize(modules: Set[Module]): Set[AnalizedModule] = {
		var result = Set[AnalizedModule]()

		modules foreach{ module =>
			val traverser = new AIRelationTraverser(module)
			val ast = Analyzer.parseCode(module)
			traverser.traverse(ast)
			result += traverser.analizedModule
		}

		result
	}
}
