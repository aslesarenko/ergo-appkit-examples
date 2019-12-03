package org.ergoplatform.appkit.examples

import java.io.PrintStream
import java.util.Arrays

import org.ergoplatform.appkit.{RestApiErgoClient, _}

import scala.util.control.NonFatal
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.sandbox.Mnemonic

object ErgoTool {
  val commands = Array(ListCmd, MnemonicCmd, FreezeCmd).map(c => (c.name, c)).toMap

  def main(args: Array[String]) = {
    val cmd = try {
      parseCmd(args)
    }
    catch { case NonFatal(t) =>
      println(t.getMessage)
      printUsage()
      sys.exit(1)
    }
    cmd.run(Console.out)
  }

  def parseCmd(args: Seq[String]): Cmd = {
    val cmdName = args(0)
    val toolConf = ErgoToolConfig.load("freeze_coin_config.json")
    commands.get(cmdName) match {
      case Some(c) => c.parseCmd(args, toolConf)
      case _ =>
        sys.error(s"Unknown command: $cmdName")
    }
  }

  def printUsage() = {
    val actions = commands.toSeq.sortBy(_._1).map { case (name, c) =>
      s"""  ${name} ${c.cmdParamSyntax} - ${c.description}""".stripMargin
    }.mkString("\n")
    val msg =
      s"""
        |Usage:
        |ergotool action [action parameters]
        |
        |Available actions:
        |$actions
     """.stripMargin
    println(msg)
  }

}

sealed abstract class Cmd {
  def toolConf: ErgoToolConfig
  def name: String
  def seed: String = toolConf.getNode.getWallet.getMnemonic
  def password: String = toolConf.getNode.getWallet.getPassword
  def apiUrl: String = toolConf.getNode.getNodeApi.getApiUrl
  def apiKey: String = toolConf.getNode.getNodeApi.getApiKey
  def networkType: NetworkType = toolConf.getNode.getNetworkType
  def run(out: PrintStream): Unit
}
trait RunWithErgoClient extends Cmd {
  override def run(out: PrintStream): Unit = {
    val ergoClient = RestApiErgoClient.create(apiUrl, networkType, apiKey)
    runWithClient(ergoClient, out)
  }

  def runWithClient(ergoClient: ErgoClient, out: PrintStream): Unit
}

/** Base class for all Cmd factories (usually companion objects)
  */
sealed abstract class CmdFactory(
      /** Command name used in command line. */
      val name: String,
      /** parameters syntax specification */
      val cmdParamSyntax: String,
      val description: String) {
  def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd
}

case class ListCmd(toolConf: ErgoToolConfig, name: String, limit: Int) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, out: PrintStream): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes(0).get().convertTo[IndexedSeq[InputBox]]
      val lines = boxes.take(this.limit).map(b => b.toJson(true)).mkString("[", ",\n", "]")
      lines
    })
    out.println(res)
  }
}

object ListCmd extends CmdFactory(
  name = "list", cmdParamSyntax = "<limit>",
  description = "list top <limit> confirmed wallet boxes") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    val limit = if (args.length > 1) args(1).toInt else 10
    ListCmd(toolConf, name, limit)
  }
}

case class MnemonicCmd(toolConf: ErgoToolConfig, name: String) extends Cmd {
  override def run(out: PrintStream): Unit = {
    val m = Mnemonic.generateEnglishMnemonic()
    out.println(m)
  }
}
object MnemonicCmd extends CmdFactory(
  name = "mnemonic", cmdParamSyntax = "",
  description = "generate new mnemonic phrase using english words and default cryptographic strength") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd = {
    MnemonicCmd(toolConf, name)
  }
}

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


