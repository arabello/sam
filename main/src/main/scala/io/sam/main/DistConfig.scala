package io.sam.main

import java.io.{File, FileInputStream}
import java.nio.file.Path
import java.util.Properties

case class DistConfig(
	airelationTemplateFile: String
)

object DistConfig{
	def fromProperties(props: Properties): DistConfig = DistConfig(
		airelationTemplateFile = props.getProperty("airelationTemplateFile")
	)

	def fromPropertiesFile(file: Path): DistConfig = fromProperties(
		manage(new FileInputStream(new File(file.toUri))){ res =>
			val props = new Properties()
			props.load(res)
			props
		}
	)
}

