package io.sam.main.cli

import java.io.File

case class Config(
     out: File = new File("."),
     mode: String = "",
     fileName: String = ""
 )
