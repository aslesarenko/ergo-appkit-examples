package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{InputBox, UnsignedTransactionBuilder, ErgoClient, Address, OutBox, Parameters, ConstantsBuilder, SignedTransaction, UnsignedTransaction, BoxOperations}
import java.io.{File, PrintStream}
import java.util.List

import org.ergoplatform.appkit.Parameters.MinFee
import org.ergoplatform.appkit.console.Console

case class SendCmd(toolConf: ErgoToolConfig, name: String, storageFile: File, storagePass: Array[Char], recipient: Address, amountToSend: Long) extends Cmd with RunWithErgoClient {
  def loggedStep[T](msg: String, console: Console)(step: => T): T = {
    console.print(msg + "...")
    val res = step
    val status = if (res != null) "Ok" else "Error"
    console.println(s" $status")
    res
  }

  override def runWithClient(ergoClient: ErgoClient, console: Console): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val senderProver = loggedStep("Creating prover", console) {
        BoxOperations.createProver(ctx, storageFile.getPath, String.valueOf(storagePass))
      }
//      BoxOperations.send(ctx, sender, recipient, amountToSend)
      val sender: Address = senderProver.getAddress
      val unspent: List[InputBox] = ctx.getUnspentBoxesFor(sender)
      val boxesToSpend: List[InputBox] = BoxOperations.selectTop(unspent, amountToSend + MinFee)
      val txB: UnsignedTransactionBuilder = ctx.newTxBuilder
      val newBox: OutBox = txB.outBoxBuilder.value(amountToSend).contract(ctx.compileContract(ConstantsBuilder.create.item("recipientPk", recipient.getPublicKey).build, "{ recipientPk }")).build
      val tx: UnsignedTransaction = txB.boxesToSpend(boxesToSpend).outputs(newBox).fee(Parameters.MinFee).sendChangeTo(senderProver.getP2PKAddress).build
      val signed: SignedTransaction = senderProver.sign(tx)
//      ctx.sendTransaction(signed)
      signed.toJson(true)
    })
    console.println(s"Tx: $res")
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
    if (amountToSend < MinFee) error(s"Please specify amount no less than $MinFee (MinFee)")
    val pass = console.readPassword("Storage password>")
    SendCmd(toolConf, name, storageFile, pass, recipient, amountToSend)
  }
}

