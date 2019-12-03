package org.ergoplatform.appkit.examples

import java.io.{ByteArrayOutputStream, PrintStream}

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.examples.ergotool.ErgoTool
import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ErgoToolSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"
  val mnemonic = "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet"

  def runCommand(name: String, args: String*) = {
    val os = new ByteArrayOutputStream(1024)
    val out = new PrintStream(os)
    ErgoTool.run(name +: args, out)
    os.toString
  }

  property("mnemonic command") {
    val res = runCommand("mnemonic")
    res.split(" ").length shouldBe 15
  }

  property("address command") {
    val res = runCommand("address", "testnet", mnemonic)
    res shouldBe addrStr
  }

}

