package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{Mnemonic, SecretStorage, NetworkType}
import java.io.PrintStream
import java.util

case class ExtractStorageCmd(
    toolConf: ErgoToolConfig, name: String,
    storageFile: String, storagePass: Array[Char], key: String, network: NetworkType) extends Cmd {
  override def run(out: PrintStream): Unit = {
    val storage = SecretStorage.loadFrom(storageFile)
    storage.unlock(String.valueOf(storagePass))
    key match {
      case "address" =>
        out.println(storage.getAddressFor(network).toString)
      case _ =>
        sys.error(s"Invalid info requested: $key")
    }
  }
}

object ExtractStorageCmd extends CmdFactory(
  name = "extractStorage", cmdParamSyntax = "<storage file> address mainnet|testnet",
  description = "Reads the file, unlocks it using password and extract the requested information from the given storage file.") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    val storageFile = if (args.length > 1) args(1) else error("storage file is not specified")
    val key = if (args.length > 2) args(2) else error("please specify which info to extract `address` or `secret`")
    val network = parseNetwork(if (args.length > 3) args(3) else error("please specify network type (mainnet|testnet)"))
    val console = System.console()
    val storagePass = console.readPassword("Storage password> ")
    ExtractStorageCmd(toolConf, name, storageFile, storagePass, key, network)
  }
}





