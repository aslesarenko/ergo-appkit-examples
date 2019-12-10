package org.ergoplatform.appkit.examples.ergotool

import java.io.PrintStream

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{ErgoClient, Parameters, ConstantsBuilder}
import java.util.Arrays

case class FreezeCmd(toolConf: ErgoToolConfig, name: String, payAmount: Long) extends Cmd with RunWithErgoClient {

  override def runWithClient(ergoClient: ErgoClient, out: PrintStream): Unit = {
    val delay = toolConf.getParameters.get("newBoxSpendingDelay").toInt
    val res = ergoClient.execute(ctx => {
      out.println(s"Context: ${ctx.getHeight}, ${ctx.getNetworkType}")
      val prover = ctx.newProverBuilder()
          .withMnemonic(this.seed, this.password)
          .build()
      out.println(s"ProverAddress: ${prover.getP2PKAddress}")
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes(0).get()
      val box = boxes.get(0)
      out.println(s"InputBox: ${box.toJson(true)}")
      val txB = ctx.newTxBuilder()
      val newBox = txB.outBoxBuilder()
          .value(this.payAmount)
          .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("freezeDeadline", ctx.getHeight + delay)
                .item("pkOwner", prover.getP2PKAddress.pubkey)
                .build(),
            "{ sigmaProp(HEIGHT > freezeDeadline) && pkOwner }"))
          .build()
      val tx = txB.boxesToSpend(Arrays.asList(box))
          .outputs(newBox)
          .fee(Parameters.MinFee)
          .sendChangeTo(prover.getP2PKAddress)
          .build()
      val signed = prover.sign(tx)
      val txId = ctx.sendTransaction(signed)
      (signed.toJson(true), txId)
    })
    out.println(s"SignedTransaction: ${res}")
  }
}

object FreezeCmd extends CmdFactory(
  name = "freeze", cmdParamSyntax = "<amount>",
  description = "Create a new box with given <amount> of NanoErg protectes with Freezer contract") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    val amount = if (args.length > 1) args(1).toLong else sys.error(s"Parameter <amound> is not defined")
    FreezeCmd(toolConf, name, amount)
  }
}