package io.sam.main.cli

import java.io.File

case class Config(
     out: File = new File("out"),
     mode: String = "",
     fileName: String = ""
 )
