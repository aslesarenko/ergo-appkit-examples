package org.ergoplatform.appkit.examples

import java.io.{StringReader, ByteArrayOutputStream, BufferedReader, PrintStream}

import org.scalatest.Matchers

trait ConsoleTesting { self: Matchers =>

  def prepareConsole(inputText: String): (Console, ByteArrayOutputStream) = {
    val in = new BufferedReader(new StringReader(inputText))
    val baos = new ByteArrayOutputStream()
    val out = new PrintStream(baos)
    (new ConsoleImpl(in, out), baos)
  }

  case class WriteRead(write: String, read: String)
  case class ConsoleScenario(operations: Seq[WriteRead]) {
    def getReadText: String = {
      operations.map(i => s"${i.read}\n").mkString("")
    }
    def getWriteText: String = {
      operations.map(i => i.write).mkString("")
    }
  }

  /** Parses text into ConsoleScenario using simple syntax shown below.
   * Example:
   * <pre>
   * |# lines started from '#' are ignored
   * |# to separate output from input '::' combination is used
   * |this it output> ::this is input
   * |# each line should end with '\n' new line symbol
   * |# input may be empty
   * |output::
   * </pre>
   */
  def parseScenario(scenarioText: String): ConsoleScenario = {
    val withoutComments = scenarioText.split("\n")
        .filterNot(l => l.startsWith("#"))
        .mkString("\n") + "\n"
    val operations = withoutComments.split(";\n")
            .filterNot(l => l.isEmpty)
            .map(l => {
              val parts = l.split("::")
              WriteRead(parts(0), if (parts.length > 1) parts(1) else "")
            })
    ConsoleScenario(operations)
  }

  def testScenario(scenario: ConsoleScenario)(action: Console => Unit) = {
    val (console, out) = prepareConsole(scenario.getReadText)

    action(console)

    val output = scenario.getWriteText
    output shouldBe out.toString()
  }


}
