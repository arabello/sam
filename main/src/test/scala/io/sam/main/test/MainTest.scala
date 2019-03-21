package io.sam.main.test

import java.io.File
import java.nio.file.Paths

import io.sam.controllers.Success
import io.sam.controllers.airelation.AIRelationController
import io.sam.domain.airelation.AIRelationInteractor
import io.sam.main.Config
import io.sam.main.cli.ConfigCLI
import io.sam.presenters.airelation.AIRelationScreenPresenter
import io.sam.view.airelation.web.ChartJSView
import org.scalatest.FlatSpec

class MainTest extends FlatSpec{

	"dev.properties" should "exists" in {
		assert(new File("dev.properties").exists())
	}

	"dist.properties" should "exists" in {
		assert(new File("dist.properties").exists())
	}

	it should "contains airelationTemplateFile property" in {
		assert(Config.fromPropertiesFile(Paths.get("config.properties")).airelationTemplateFile.nonEmpty)
	}

	it should "contains version property" in {
		assert(Config.fromPropertiesFile(Paths.get("config.properties")).version.nonEmpty)
	}

	"Main" should "provide AIRelation metric" in{
		val CONFIG = Config.fromPropertiesFile(Paths.get("dist.properties"))
		val cliConfig = ConfigCLI()
		val outpath = s"${cliConfig.out}${File.separator}${cliConfig.fileName}"
		val view = new ChartJSView(Paths.get(outpath), Paths.get(CONFIG.airelationTemplateFile))
		val presenter = new AIRelationScreenPresenter(view)
		val interactor = new AIRelationInteractor(presenter)
		val controller = new AIRelationController(interactor)

		val mockModulesName = List("controllers", "core", "domain", "main", "presenters", "view")

		for {
			module <- mockModulesName
			sm = controller.createFromFolder(Paths.get(s"$module/src/main/scala")) match { case Success(content) => content }
		} controller.add(sm)

		assert(controller.snapshot.size == 6)

		val tmp = new File(outpath)

		if (tmp.exists())
			tmp.delete()

		controller.submit()

		assert(tmp.isFile)
	}
}
