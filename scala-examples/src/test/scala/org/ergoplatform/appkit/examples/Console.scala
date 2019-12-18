package org.ergoplatform.appkit.examples

import java.io.{PrintStream, BufferedReader}

abstract class Console {
  def print(s: String): Console
  def println(s: String): Console
  def readLine(): String
  def readLine(msg: String): String
  def readPassword(): Array[Char]
  def readPassword(msg: String): Array[Char]
}

class ConsoleImpl(in: BufferedReader, out: PrintStream) extends Console {
  override def print(s: String): Console = { out.print(s); this }

  override def println(s: String): Console = { out.println(s); this }

  override def readLine(): String = { in.readLine() }

  override def readLine(msg: String): String = {
    print(msg).readLine()
  }

  override def readPassword(): Array[Char] = {
    val line = readLine()
    line.toCharArray
  }

  override def readPassword(msg: String): Array[Char] = {
    print(msg).readPassword()
  }
}


