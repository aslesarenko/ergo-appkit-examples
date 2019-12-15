package org.ergoplatform.appkit.examples.ergotool

case class CmdOption(name: String) {
  def cmdText: String = s"--$name"
}

object ConfigOption extends CmdOption("conf")
