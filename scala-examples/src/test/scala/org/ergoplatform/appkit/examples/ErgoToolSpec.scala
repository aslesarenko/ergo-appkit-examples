package org.ergoplatform.appkit.examples

import org.ergoplatform.appkit.console.Console
import org.ergoplatform.appkit.examples.ergotool.{ErgoTool, ConfigOption}
import org.ergoplatform.appkit.examples.util.FileMockedErgoClient
import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

/** To run in IDEA set `Working directory` in Run/Debug configuration. */
class ErgoToolSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with ConsoleTesting {
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"
  val mnemonic = "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet"

  val addr2Str = "3WwWU3GJLK3aXCsoqptRboPYnvdqwv9QMBbpHybwXdVo9bSaMLEE"
  val mnemonic2 = "burst cancel left report gauge fame fit slow series dial convince satoshi outer magnet filter"

  val responsesDir = "src/test/resources/node_responses"
  def responseFile(name: String) = s"$responsesDir/$name"

  // NOTE, mainnet data is used for testing
  val testConfigFile = "ergo_tool_config.json"

  def runErgoTool(console: Console, name: String, args: String*) = {
    ErgoTool.run(name +: (Seq(ConfigOption.cmdText, testConfigFile) ++ args), console, {
      ctx => new FileMockedErgoClient(
        responseFile("response_NodeInfo.json"),
        responseFile("response_LastHeaders.json"),
        responseFile("response_Box1.json"))
    })
  }

  /** @param name command name
   * @param consoleOps input and output operations with the console
   * @param args arguments of command line
   */
  def runCommand(consoleOps: ConsoleScenario, name: String, args: String*): String =
    runScenario(consoleOps) { console =>
      runErgoTool(console, name, args:_*)
    }

  def runCommand(consoleScenario: String, name: String, args: String*): String = {
    val consoleOps = parseScenario(consoleScenario)
    runCommand(consoleOps, name, args:_*)
  }

  def testCommand(consoleOps: ConsoleScenario, name: String, args: String*): Unit = {
    testScenario(consoleOps) { console =>
      runErgoTool(console, name, args:_*)
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

  property("listAddressBoxes command") {
    val res = runCommand(
      s"""""".stripMargin,
      "listAddressBoxes", "9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K")
//    res.contains("Network type of the address MAINNET don't match expected TESTNET") shouldBe true
  }

  property("send command (dry run)") {
    val res = runCommand(
      s"""Storage password> ::abc;
        |""".stripMargin,
      "send", "storage/addr.json", "3WwWU3GJLK3aXCsoqptRboPYnvdqwv9QMBbpHybwXdVo9bSaMLEE", "1000000")
    println(res)
  }

  ignore("send command") {
    val res = runCommand(
      s"""Storage password> ::abc;
        |""".stripMargin,
      "send", "storage/E1.json", "9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K", "1000000")
    println(res)
  }


}

