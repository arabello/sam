package io.sam.domain.levelsmap

import io.sam.core.Component

case class Dependency(in: Set[Component], module: Component, out: Set[Component])
