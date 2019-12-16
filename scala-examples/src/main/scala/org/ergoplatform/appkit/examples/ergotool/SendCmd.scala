package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{ErgoClient, Address, BoxOperations}
import java.io.PrintStream

import scala.io.StdIn

case class SendCmd(toolConf: ErgoToolConfig, name: String, storagePass: String, recipient: Address, amountToSend: Long) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, out: PrintStream): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val sender = BoxOperations.createProver(ctx, "storage.json", storagePass)
      BoxOperations.send(ctx, sender, recipient, amountToSend)
    })
    out.print(res)
  }
}
object SendCmd extends CmdFactory(
  name = "send", cmdParamSyntax = "<recipientAddr> <amountToSend>",
  description = "send the given <amountToSend> to the given <recipientAddr>, request storage password") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    val recipient = Address.create(if (args.length > 1) args(1) else error("address is not specified"))
    val amountToSend = if (args.length > 2) args(2).toInt else error("amountToSend is not specified")
    val pass = StdIn.readLine("Storage password:")
    SendCmd(toolConf, name, pass, recipient, amountToSend)
  }
}

