package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{NetworkType, SecretStorage}
import java.io.PrintStream

import org.ergoplatform.wallet.secrets.ExtendedSecretKeySerializer
import scorex.util.encode.Base16

case class ExtractStorageCmd(
    toolConf: ErgoToolConfig, name: String,
    storageFile: String, storagePass: Array[Char], prop: String, network: NetworkType) extends Cmd {
  import ExtractStorageCmd._
  override def run(out: PrintStream): Unit = {
    val storage = SecretStorage.loadFrom(storageFile)
    storage.unlock(String.valueOf(storagePass))
    val secret = storage.getSecret
    prop match {
      case PropAddress =>
        out.println(storage.getAddressFor(network).toString)
      case PropMasterKey =>
        val secretStr = Base16.encode(ExtendedSecretKeySerializer.toBytes(secret))
        out.println(secretStr)
      case PropSecretKey =>
        val sk  = Base16.encode(secret.keyBytes)
        assert(sk == secret.key.w.toString(16), "inconsistent secret")
        out.println(sk)
      case PropPublicKey =>
        val pk = Base16.encode(secret.key.publicImage.pkBytes)
        out.println(pk)
      case _ =>
        sys.error(s"Invalid property requested: $prop")
    }
  }
}

object ExtractStorageCmd extends CmdFactory(
  name = "extractStorage", cmdParamSyntax = "",
  description = "Reads the file, unlocks it using password and extract the requested property from the given storage file.") {

  val PropAddress = "address"
  val PropMasterKey = "masterKey"
  val PropPublicKey = "publicKey"
  val PropSecretKey = "secretKey"

  val supportedKeys: Seq[String] = Array(PropAddress, PropMasterKey, PropPublicKey, PropSecretKey)
  override val cmdParamSyntax = s"<storage file> ${supportedKeys.mkString("|")} mainnet|testnet"

  private def propErrorMsg = {
    error(s"Please specify one of the supported properties: ${supportedKeys.map(k => s"`$k`").mkString(",")}")
  }

  private def parsePropName(prop: String): String =
    if (supportedKeys.contains(prop)) prop
    else propErrorMsg

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig, out: PrintStream): Cmd = {
    val storageFile = if (args.length > 1) args(1) else error("storage file is not specified")
    val prop = if (args.length > 2) parsePropName(args(2)) else propErrorMsg
    val network = parseNetwork(if (args.length > 3) args(3) else error("please specify network type (mainnet|testnet)"))
    val console = System.console()
    val storagePass = console.readPassword("Storage password> ")
    ExtractStorageCmd(toolConf, name, storageFile, storagePass, prop, network)
  }
}





