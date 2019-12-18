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

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig, out: PrintStream): Cmd = {
    val phrase = if (args.length > 1) args(1) else error("mnemonic is not specified")
    val console = System.console()

    val mnemonicPass = readNewPassword(3, out) {
      val p1 = console.readPassword("Mnemonic password> ")
      val p2 = console.readPassword("Repeat mnemonic password> ")
      (p1, p2)
    }
    val recipient = Mnemonic.create(phrase, String.valueOf(mnemonicPass))
    val storagePass = readNewPassword(3, out) {
      val p1 = console.readPassword("Storage password> ")
      val p2 = console.readPassword("Repeat storage password> ")
      (p1, p2)
    }
    CreateStorageCmd(toolConf, name, recipient, storagePass)
  }
}




