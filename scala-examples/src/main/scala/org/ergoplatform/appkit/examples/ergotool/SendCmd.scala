package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{ErgoClient, Address, BoxOperations}
import java.io.{File, PrintStream}

import org.ergoplatform.appkit.console.Console

case class SendCmd(toolConf: ErgoToolConfig, name: String, storageFile: File, storagePass: Array[Char], recipient: Address, amountToSend: Long) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, console: Console): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val sender = BoxOperations.createProver(ctx, storageFile.getPath, String.valueOf(storagePass))
      BoxOperations.send(ctx, sender, recipient, amountToSend)
    })
    console.print(s"Tx: $res")
  }
}
object SendCmd extends CmdFactory(
  name = "send", cmdParamSyntax = "<wallet file> <recipientAddr> <amountToSend>",
  description = "send the given <amountToSend> to the given <recipientAddr> using \n " +
      "the given <wallet file> to sign transaction (requests storage password)") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig, console: Console): Cmd = {
    val storageFile = new File(if (args.length > 1) args(1) else error("Wallet storage file path is not specified"))
    if (!storageFile.exists()) error(s"Specified wallet file is not found: $storageFile")
    val recipient = Address.create(if (args.length > 2) args(2) else error("recipient address is not specified"))
    val amountToSend = if (args.length > 3) args(3).toLong else error("amountToSend is not specified")
    if (amountToSend <= 0) error("Positive amount of NanoErg is expected")
    val console = System.console()
    val pass = console.readPassword("Storage password>")
    SendCmd(toolConf, name, storageFile, pass, recipient, amountToSend)
  }
}

