package io.sam.main.cli

import java.io.{File, FileInputStream}
import java.nio.file.Paths
import java.util.Properties

import io.sam.controllers.airelation.AIRelationController
import io.sam.controllers.{Failure, Success}
import io.sam.domain.airelation.AIRelationInteractor
import io.sam.presenters.airelation.AIRelationScreenPresenter
import io.sam.view.airelation.web.ChartJSView
import scopt.OptionParser

import scala.io.StdIn

object MainCLI {

	private def readVersion(): String = {
		val props = new Properties()
		var fis: FileInputStream = null
		var version = ""

		try {
			fis = new FileInputStream(new File("version.properties"))
			props.load(fis)
			version = props.getProperty("version")
		}finally{
			if (fis != null)
				fis.close()
		}
		version
	}

	def main(args: Array[String]): Unit = {
		val optionParser: OptionParser[Config] = new scopt.OptionParser[Config]("SAM") {
			head("sam", readVersion())

			opt[File]('o', "out")
				.valueName("<path>")
				.action((x, c) => c.copy(out = x))
				.text("Specify the output path. Default is 'out'")

			cmd("airelation")
				.action( (_, c) => c.copy(mode = "airelation") )
				.text("Print out the Abstractness/Instability relation graph")
				.children(
					opt[String]("fName")
						.abbr("fn")
    					.withFallback(() => "airelation")
						.action( (x, c) => c.copy(fileName = s"$x.html") )
						.text("Specify the output file name. Default is 'airelation.html'")
				)

			help("help")
    			.abbr("h")
		}
		optionParser.parse(args, Config()) match {
			case Some(config) =>
				config.mode match {
					case "airelation" =>
						val outpath = s"${config.out}${File.separator}${config.fileName}"
						val view = new ChartJSView(Paths.get(outpath))
						val presenter = new AIRelationScreenPresenter(view)
						val interactor = new AIRelationInteractor(presenter)
						val controller = new AIRelationController(interactor)

						var in = "Y"
						while(in == "Y" || in == "y" || in.isEmpty){
							print("Insert module path: ")
							val path = Paths.get(StdIn.readLine())

							print("Insert module name: ")
							val moduleName = StdIn.readLine()

							controller.createFromFolder(path) match {
								case Failure(why) => System.err.println(s"Error: $why")
								case Success(content) => controller.add(content.copy(name = moduleName))
							}

							println("\nCurrent loaded modules:")
							controller.snapshot foreach(sm => println(s"- ${sm.name}"))

							println("\nInsert another module? [Y/n]")
							in = StdIn.readLine()
						}

						controller.submit()

						println()
						for (e <- view.getErrors)
							System.err.println(e)

						println()
						println(s"airelation graph printed out to $outpath")

					case _ => optionParser.showTryHelp()
				}

			case None => optionParser.help("help")
		}

	}
}
