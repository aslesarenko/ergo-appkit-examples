package org.ergoplatform.appkit.examples.ergotool

import java.io.PrintStream

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.sandbox.Mnemonic

case class MnemonicCmd(toolConf: ErgoToolConfig, name: String) extends Cmd {
  override def run(out: PrintStream): Unit = {
    val m = Mnemonic.generateEnglishMnemonic()
    out.print(m)
  }
}

object MnemonicCmd extends CmdFactory(
  name = "mnemonic", cmdParamSyntax = "",
  description = "generate new mnemonic phrase using english words and default cryptographic strength") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    MnemonicCmd(toolConf, name)
  }
}