package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{Mnemonic, SecretStorage}
import java.io.PrintStream
import java.util

case class CreateStorageCmd(toolConf: ErgoToolConfig, name: String, mnemonic: Mnemonic, storagePass: Array[Char]) extends Cmd {
  override def run(out: PrintStream): Unit = {
    val storage = SecretStorage.createFromMnemonicIn("storage", mnemonic, String.valueOf(storagePass))
    util.Arrays.fill(storagePass, 0.asInstanceOf[Char])
    val filePath = storage.getFile.getPath
    out.println(s"Storage File: $filePath")
  }
}
object CreateStorageCmd extends CmdFactory(
  name = "createStorage", cmdParamSyntax = "<mnemonic>",
  description = "Creates an encrypted storage file for the given <mnemonic> (requests storage password)") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    val phrase = if (args.length > 1) args(1) else error("mnemonic is not specified")
    val console = System.console()
    val mnemonicPass = console.readPassword("Mnemonic password> ")
    val recipient = Mnemonic.create(phrase, mnemonicPass.toString)
    val storagePass = console.readPassword("Storage password> ")
    CreateStorageCmd(toolConf, name, recipient, storagePass)
  }
}




