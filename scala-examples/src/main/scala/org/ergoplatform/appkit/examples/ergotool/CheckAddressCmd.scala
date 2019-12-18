package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{NetworkType, Address}
import org.ergoplatform.appkit.console.Console

case class CheckAddressCmd(toolConf: ErgoToolConfig, name: String, network: NetworkType, mnemonic: String, mnemonicPass: String, address: String) extends Cmd {
  override def run(console: Console): Unit = {
    val addressComputed = Address.fromMnemonic(network, mnemonic, mnemonicPass).toString
    val res = if (addressComputed == address) "Ok"
              else s"$addressComputed != $address"
    console.print(res)
  }
}

object CheckAddressCmd extends CmdFactory(
  name = "checkAddress", cmdParamSyntax = "testnet|mainnet <mnemonic> <mnemonic password> <address>",
  description = "Check the given mnemonic and password pair correspond to the given address") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig, console: Console): Cmd = {
    if (args.length < 5) error("not enough parameters")
    val network = args(1)
    val networkType = network match {
      case "testnet" => NetworkType.TESTNET
      case "mainnet" => NetworkType.MAINNET
      case _ => error(s"Invalid network type $network")
    }
    val mnemonic = args(2)
    val mnemonicPass = args(3)
    val address = args(4)
    CheckAddressCmd(toolConf, name, networkType, mnemonic, mnemonicPass, address)
  }
}