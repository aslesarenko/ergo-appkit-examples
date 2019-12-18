package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{ErgoClient, InputBox}
import java.io.PrintStream

import org.ergoplatform.appkit.console.Console

case class ListWalletBoxesCmd(toolConf: ErgoToolConfig, name: String, limit: Int) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, console: Console): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes(0).get().convertTo[IndexedSeq[InputBox]]
      val lines = boxes.take(this.limit).map(b => b.toJson(true)).mkString("[", ",\n", "]")
      lines
    })
    console.print(res)
  }
}

object ListWalletBoxesCmd extends CmdFactory(
  name = "listWalletBoxes", cmdParamSyntax = "<limit>",
  description = "list top <limit> confirmed wallet boxes which can be spent") {

  override def parseCmd(args: Seq[String], toolConf: ErgoToolConfig, console: Console): Cmd = {
    val limit = if (args.length > 1) args(1).toInt else 10
    ListWalletBoxesCmd(toolConf, name, limit)
  }
}