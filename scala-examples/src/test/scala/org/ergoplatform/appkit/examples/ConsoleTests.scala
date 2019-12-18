package org.ergoplatform.appkit.examples

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}

class ConsoleTests extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with ConsoleTesting {
  val line1 = "input line 1"
  val line2 = "input line 2"
  val scenario = ConsoleScenario(Seq(
    RequestResponse("Enter line 1> ", "input line 1"),
    RequestResponse("Enter line 2> ", "input line 2")
  ))

  def expectedOutput(scenario: ConsoleScenario): String = {
    val line1 = scenario.interactions(0)
    val line2 = scenario.interactions(1)
    line1.request + line2.request +
        s"You entered: ${line1.response + line2.response}\n"
  }

  property("read input string") {
    val (console, out) = prepareConsole(scenario.getResponseText)
    Example.process(console)

    val output = expectedOutput(scenario)

    output shouldBe out.toString()
  }

  property("read input from file") {

  }
}



