package org.perftester

import ammonite.ops.{%%, Path, read}

case class TestConfig(id: String, commit: String, extraArgs: List[String] = Nil)
