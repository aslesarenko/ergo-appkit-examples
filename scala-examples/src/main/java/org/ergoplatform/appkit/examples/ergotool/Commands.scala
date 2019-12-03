package org.ergoplatform.appkit.examples.ergotool

import java.io.PrintStream
import java.util.Arrays

import org.ergoplatform.appkit.{RestApiErgoClient, _}

import scala.util.control.NonFatal
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.sandbox.Mnemonic

abstract class Cmd {
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
abstract class CmdFactory(
      /** Command name used in command line. */
      val name: String,
      /** parameters syntax specification */
      val cmdParamSyntax: String,
      val description: String) {
  def parseCmd(args: Seq[String], toolConf: ErgoToolConfig): Cmd
  def error(msg: String) = {
    sys.error(s"Error executing command `$name`: $msg")
  }
}

