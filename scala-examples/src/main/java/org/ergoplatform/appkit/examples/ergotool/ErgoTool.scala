package org.ergoplatform.appkit.examples.ergotool

import org.ergoplatform.appkit.config.ErgoToolConfig

import scala.util.control.NonFatal
import java.io.PrintStream

object ErgoTool {
  val commands = Array(ListCmd, MnemonicCmd, AddressCmd, FreezeCmd).map(c => (c.name, c)).toMap

  def main(args: Array[String]) = {
    run(args, Console.out)
  }

  def run(args: Seq[String], out: PrintStream) = {
    try {
      val cmd = parseCmd(args)
      cmd.run(out)
    }
    catch { case NonFatal(t) =>
      out.println(t.getMessage)
      printUsage(out)
    }
  }

  def parseCmd(args: Seq[String]): Cmd = {
    if (args.isEmpty) sys.error(s"Please specify command name and parameters.")
    val cmdName = args(0)
    val toolConf = ErgoToolConfig.load("freeze_coin_config.json")
    commands.get(cmdName) match {
      case Some(c) => c.parseCmd(args, toolConf)
      case _ =>
        sys.error(s"Unknown command: $cmdName")
    }
  }

  def printUsage(out: PrintStream) = {
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
    out.println(msg)
  }

}
