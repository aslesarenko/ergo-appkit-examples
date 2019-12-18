package org.ergoplatform.appkit.examples

import java.io.{ByteArrayOutputStream, PrintStream}

import org.ergoplatform.appkit.examples.ergotool.ErgoTool
import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ErgoToolSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with ConsoleTesting {
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"
  val mnemonic = "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet"

  /** @param name command name
   * @param consoleOps input and output operations with the console
   * @param args arguments of command line
   */
  def runCommand(consoleOps: ConsoleScenario, name: String, args: String*): String = {
    runScenario(consoleOps) { console =>
      ErgoTool.run(name +: args, console)
    }
  }

  def runCommand(consoleScenario: String, name: String, args: String*): String = {
    val consoleOps = parseScenario(consoleScenario)
    runCommand(consoleOps, name, args:_*)
  }

  def testCommand(consoleOps: ConsoleScenario, name: String, args: String*): Unit = {
    testScenario(consoleOps) { console =>
      ErgoTool.run(name +: args, console)
    }
    ()
  }

  def testCommand(consoleScenario: String, name: String, args: String*): Unit = {
    val consoleOps = parseScenario(consoleScenario)
    testCommand(consoleOps, name, args:_*)
  }

  property("mnemonic command") {
    val res = runCommand("", "mnemonic")
    res.split(" ").length shouldBe 15
  }

  property("address command") {
    val res = testCommand(
      s"Mnemonic password> ::;\n$addrStr::;\n",
      "address", "testnet", mnemonic)
  }

}

