package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{NetworkType, Address}
import java.io.PrintStream

case class AddressCmd(
    toolConf: ErgoToolConfig,
    name: String, network: NetworkType, mnemonic: String, mnemonicPass: Array[Char])
  extends Cmd {
  override def run(out: PrintStream): Unit = {
    val address = Address.fromMnemonic(network, mnemonic, String.valueOf(mnemonicPass))
    out.print(address.toString)
  }
}

object AddressCmd extends CmdFactory(
  name = "address", cmdParamSyntax = "testnet|mainnet <mnemonic>",
  description = "return address for a given mnemonic and password pair") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    val network = if (args.length > 1) args(1) else error("network is not specified (mainnet or testnet)")
    val networkType = parseNetwork(network)
    val mnemonic = if (args.length > 2) args(2) else error("mnemonic is not specified")
    val console = System.console()
    val mnemonicPass = console.readPassword("Mnemonic password> ")
    AddressCmd(toolConf, name, networkType, mnemonic, mnemonicPass)
  }
}