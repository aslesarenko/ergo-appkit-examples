package org.ergoplatform.appkit.examples

import java.io.{InputStreamReader, BufferedReader, PrintStream}

object Example {
  def main(args: Array[String]): Unit = {
    val in = new BufferedReader(new InputStreamReader(System.in))
    val out = new PrintStream(System.out)
    process(new ConsoleImpl(in, out))
  }

  def process(console: Console) = {
    console.print("Enter line 1> ")
    val line1 = console.readLine()
    console.print("Enter line 2> ")
    val line2 = console.readLine()
    val res = line1 + line2
    console.println(s"You entered: $res")
  }
}
