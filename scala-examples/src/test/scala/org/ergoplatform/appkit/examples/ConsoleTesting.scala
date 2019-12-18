package org.ergoplatform.appkit.examples

import java.io.{StringReader, ByteArrayOutputStream, BufferedReader, PrintStream}

trait ConsoleTesting {
  def prepareConsole(inputText: String): (Console, ByteArrayOutputStream) = {
    val in = new BufferedReader(new StringReader(inputText))
    val baos = new ByteArrayOutputStream()
    val out = new PrintStream(baos)
    (new ConsoleImpl(in, out), baos)
  }

  case class RequestResponse(request: String, response: String)
  case class ConsoleScenario(interactions: Seq[RequestResponse]) {
    def getResponseText: String = {
      interactions.map(i => s"${i.response}\n").mkString("")
    }
  }

//  def parseScenario(scenarioText: String): ConsoleScenario = {
//  }
}
