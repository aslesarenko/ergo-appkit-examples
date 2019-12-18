package org.ergoplatform.appkit.console

import java.io.{InputStreamReader, BufferedReader, PrintStream}

abstract class Console {
  def print(s: String): Console
  def println(s: String): Console
  def readLine(): String
  def readLine(prompt: String): String
  def readPassword(): Array[Char]
  def readPassword(prompt: String): Array[Char]
}
object Console {
  def instance(): Console = {
    val in = new BufferedReader(new InputStreamReader(System.in))
    val out = new PrintStream(System.out)
    new ConsoleImpl(in, out)
  }
}

class ConsoleImpl(in: BufferedReader, out: PrintStream) extends Console {
  override def print(s: String): Console = { out.print(s); this }

  override def println(s: String): Console = { out.println(s); this }

  override def readLine(): String = { in.readLine() }

  override def readLine(msg: String): String = {
    print(msg).readLine()
  }

  // TODO security: these methods should be reimplemented without using String (See java.io.Console)
  override def readPassword(): Array[Char] = {
    val line = readLine()
    line.toCharArray
  }

  override def readPassword(msg: String): Array[Char] = {
    print(msg).readPassword()
  }
}


