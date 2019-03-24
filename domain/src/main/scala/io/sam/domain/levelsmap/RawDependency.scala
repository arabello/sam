package io.sam.domain.levelsmap

import io.sam.core.Component

case class RawDependency(module: Component, packages: Set[String], imports: Set[String])
