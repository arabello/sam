package io.sam.main.cli

import java.io.File

case class ConfigCLI(
     out: File = new File("out"),
     mode: String = "",
     fileName: String = ""
 )
