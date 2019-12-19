package org.ergoplatform.appkit.examples

import org.ergoplatform.appkit.examples.ergotool.ErgoTool
import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

/** To run in IDEA set `Working directory` in Run/Debug configuration. */
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
    testCommand(
      s"""Mnemonic password> ::;
         |$addrStr::;
         |""".stripMargin,
      "address", "testnet", mnemonic)
  }

  property("checkAddress command") {
    testCommand(
      s"""Mnemonic password> ::;
         |Ok::;
         |""".stripMargin,
      "checkAddress", "testnet", mnemonic, addrStr)
  }

  property("checkAddress command validates address format") {
    val res = runCommand(
      s"""Mnemonic password> ::;
        |""".stripMargin,
      "checkAddress", "testnet", mnemonic, "someaddress")
    res.contains("Invalid address encoding, expected base58 string: someaddress") shouldBe true
  }

  property("checkAddress command validates network type") {
    val res = runCommand(
      s"""Mnemonic password> ::;
        |""".stripMargin,
      "checkAddress", "testnet", mnemonic, "9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v")
    res.contains("Network type of the address MAINNET don't match expected TESTNET") shouldBe true
  }

  ignore("send command") {
    val res = runCommand(
      s"""Storage password> ::abc;
        |""".stripMargin,
      "send", "storage/E1.json", "9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K", "1000000")
    println(res)
  }


}

