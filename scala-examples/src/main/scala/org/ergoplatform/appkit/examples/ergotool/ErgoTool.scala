package org.ergoplatform.appkit.examples.ergotool

import java.io.PrintStream

import scala.util.control.NonFatal
import org.ergoplatform.appkit.config.ErgoToolConfig

import scala.collection.mutable.ArrayBuffer

/**
  * Generate native image using
  * native-image --no-server \
  *   -cp build/libs/appkit-examples-3.1.0-all.jar\
  *   --report-unsupported-elements-at-runtime\
  *   --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
  *   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
  *   --allow-incomplete-classpath \
  *   --enable-url-protocols=http,https org.ergoplatform.appkit.examples.ergotool.ErgoTool ergotool
  */
object ErgoTool {
  val commands: Map[String, CmdFactory] = Array(
    AddressCmd, MnemonicCmd, CheckAddressCmd,
    FreezeCmd, ListWalletBoxesCmd, ListAddressBoxesCmd,
    CreateStorageCmd, SendCmd
    ).map(c => (c.name, c)).toMap

  def main(args: Array[String]): Unit = {
    run(args, Console.out)
  }

  def run(args: Seq[String], out: PrintStream): Unit = {
    try {
      val cmd = parseCmd(args)
      cmd.run(out)
    }
    catch { case NonFatal(t) =>
      out.println(t.getMessage)
      printUsage(out)
    }
  }

  val confOptionName = "--conf"
  val options: Seq[CmdOption] = Array(ConfigOption)

  def parseOptions(args: Seq[String]): (Map[String, String], Seq[String]) = {
    var resOptions = Map.empty[String, String]
    val resArgs: ArrayBuffer[String] = ArrayBuffer.empty
    resArgs ++= args.toArray.clone()
    for (o <- options) {
      val pos = resArgs.indexOf(o.cmdText)
      if (pos != -1) {
        resOptions = resOptions + (o.name -> resArgs(pos + 1))
        resArgs.remove(pos + 1) // remove option value
        resArgs.remove(pos)     // remove option name
      }
    }
    (resOptions, resArgs)
  }

  def parseCmd(args: Seq[String]): Cmd = {
    val (cmdOptions, cmdArgs) = parseOptions(args)
    if (cmdArgs.isEmpty) sys.error(s"Please specify command name and parameters.")

    val configFile = cmdOptions.getOrElse(ConfigOption.name, "freeze_coin_config.json")
    val toolConf = ErgoToolConfig.load(configFile)

    val cmdName = cmdArgs(0)
    commands.get(cmdName) match {
      case Some(c) => c.parseCmd(cmdArgs, toolConf)
      case _ =>
        sys.error(s"Unknown command: $cmdName")
    }
  }

  def printUsage(out: PrintStream): Unit = {
    val actions = commands.toSeq.sortBy(_._1).map { case (name, c) =>
      s"""  $name ${c.cmdParamSyntax} - ${c.description}""".stripMargin
    }.mkString("\n")
    val options = ErgoTool.options.sortBy(_.name).map(_.helpString).mkString("\n")
    val msg =
      s"""
        |Usage:
        |ergotool [options] action [action parameters]
        |
        |Available actions:
        |$actions
        |
        |Options:
        |$options
     """.stripMargin
    out.println(msg)
  }

}
