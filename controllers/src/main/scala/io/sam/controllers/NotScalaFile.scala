package io.sam.controllers

import java.nio.file.Path

class NotScalaFile(f: Path) extends Exception(s"$f is not a scala file")