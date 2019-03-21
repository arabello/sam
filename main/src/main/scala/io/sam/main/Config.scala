package io.sam.main

import java.io.{File, FileInputStream}
import java.nio.file.Path
import java.util.Properties

case class Config(version: String, airelationTemplateFile: String)

object Config{
	def fromProperties(props: Properties): Config = Config(
		version = props.getProperty("version"),
		airelationTemplateFile = props.getProperty("airelationTemplateFile")
	)

	def fromPropertiesFile(file: Path): Config = fromProperties(
		manage(new FileInputStream(new File(file.toUri))){ res =>
			val props = new Properties()
			props.load(res)
			props
		}
	)
}

