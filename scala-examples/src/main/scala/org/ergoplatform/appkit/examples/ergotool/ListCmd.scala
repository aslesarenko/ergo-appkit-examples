package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{ErgoClient, InputBox}
import java.io.PrintStream

case class ListCmd(toolConf: ErgoToolConfig, name: String, limit: Int) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, out: PrintStream): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes(0).get().convertTo[IndexedSeq[InputBox]]
      val lines = boxes.take(this.limit).map(b => b.toJson(true)).mkString("[", ",\n", "]")
      lines
    })
    out.print(res)
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