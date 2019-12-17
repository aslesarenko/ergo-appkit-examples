package org.ergoplatform.appkit.examples.ergotool

case class CmdOption(name: String, description: String, isFlag: Boolean = false) {
  def cmdText: String = s"--$name"
  def helpString: String = s"  $cmdText\n\t$description"
}

object ConfigOption extends CmdOption(
  "conf",
  "configuration file path (relative to local directory) e.g. `--conf ergo_tool.json`")

object NonInteractiveOption extends CmdOption(
  "ni",
  "turn on non-interactive mode",
  true)

