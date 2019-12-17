package org.ergoplatform.appkit.examples.ergotool

case class CmdOption(name: String, description: String) {
  def cmdText: String = s"--$name"
  def helpString: String = s"  $cmdText\n\t$description"
}

object ConfigOption extends CmdOption(
  "conf",
  "configuration file path (relative to local directory) e.g. `--conf ergo_tool.json`")
