package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{NetworkType, Address}
import java.io.PrintStream

case class AddressCmd(toolConf: ErgoToolConfig, name: String, network: NetworkType, mnemonic: String, mnemonicPass: String) extends Cmd {
  override def run(out: PrintStream): Unit = {
    val address = Address.fromMnemonic(network, mnemonic, mnemonicPass)
    out.print(address.toString)
  }
}

object AddressCmd extends CmdFactory(
  name = "address", cmdParamSyntax = "testnet|mainnet <mnemonic> [<mnemonic password>]",
  description = "return address for a given mnemonic and password pair") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    if (args.length < 3) error("not enough parameters")
    val network = args(1)
    val networkType = network match {
      case "testnet" => NetworkType.TESTNET
      case "mainnet" => NetworkType.MAINNET
      case _ => error(s"Invalid network type $network")
    }
    val mnemonic = args(2)
    val mnemonicPass = if (args.isDefinedAt(3)) args(3) else ""
    AddressCmd(toolConf, name, networkType, mnemonic, mnemonicPass)
  }
}