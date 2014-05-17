/*
 * Copyright 2014, IST Austria
 *
 * This file is part of ConRepair.
 *
 * ConRepair is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * ConRepair is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with ConRepair. If not, see <http://www.gnu.org/licenses/>.
 */

package at.ac.ist.con_repair.helpers

import java.io._
import scala.collection.mutable.ListBuffer
import java.nio.CharBuffer
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import scala.collection.{GenSeq, GenSeqLike}

object IO {
  /**
   * The base directory for all output
   */
  var baseDir = "."

  def readToString(file: File):String = {
    readToString(file.getAbsolutePath)
  }

  def readToString(file: String):String = {
    val reader = new FileInputStream(file)
    val res = readToString(reader)
    reader.close()
    res
  }

  def readToString(in: InputStream):String = {
    val reader = new InputStreamReader(in, "UTF-8")
    val br = new BufferedReader(reader)
    val ca: Array[Char] = new Array[Char](1000)
    val sb = new StringBuilder
    var r = 0;
    while ({r = br.read(ca, 0, 1000); r} != -1) {
      sb.appendAll(ca, 0, r)
    }
    sb.toString()
  }

  def copy(in: InputStream, out: OutputStream) = {
    val data = new Array[Byte](1024)
    var read = 0
    while (in.available() > 0)
    {
      read = in.read(data)
      out.write(data, 0, read)
    }
  }

  def writeToFile(outFile: File, out:String):Unit = writeToFile(outFile.getAbsolutePath, out)

  def writeToFile(outFile: String, out:String) = {
    val outStream = new FileOutputStream(outFile)
    val writer = new OutputStreamWriter(outStream, "UTF-8")
    val bw = new BufferedWriter(writer)
    bw.write(out)
    bw.close()
    outStream.close()
  }

  def waitForKeypress() = {
    System.out.println("Press any key to continue...")
    System.in.read()
  }

  def prepDirectory(directory: File) = {
    if (directory.exists()) {
      for (f <- directory.listFiles()) f.delete()
    } else {
      directory.mkdir()
    }
    directory
  }

  class Column(val heading:String) {
    val rows = new ListBuffer[String]()
    def length = Math.max(rows.map(_.length).max, heading.length)

    def printHeading(p:Appendable): Unit = {
      p.append(heading)
      p.append(" " * (length-heading.length))
    }

    def printSeperator(p:Appendable): Unit = {
      p.append("=" * length)
    }

    def printRowSeperator(p:Appendable): Unit = {
      p.append("-" * length)
    }

    def printRow(n:Int,p:Appendable): Unit = {
      if (n<rows.length) {
        p.append(rows(n))
        p.append(" " * (length-rows(n).length))
      } else
        printEmptyRow(p)
    }

    def printEmptyRow(p:Appendable):Unit = {
      p.append(" " * length)
    }

    def rowIsSeperator(n:Int):Boolean = rows(n).startsWith("----")

    def finished(n:Int):Boolean = n>=rows.length
  }

  def writeTable(columns : Iterable[Column], p:Appendable, seperator:String = " | ", printHeading:Boolean = true): Unit = {
    class ColumnsWithCounter(c:Column) {
      var n = 0

      def printRow() {
        if (!rowIsSeperator) {
          c.printRow(n, p)
          n += 1
        } else c.printEmptyRow(p)
      }

      def printRowSeperator() {
          c.printRowSeperator(p)
          n += 1
      }

      def rowIsSeperator = finished || c.rowIsSeperator(n)
      def finished = c.finished(n)
    }

    val columnsCount = columns.count(_=>true)
    // write heading
    if (printHeading) {
      p.append(seperator)
      for (c <- columns) {
        c.printHeading(p)
        p.append(seperator)
      }
      p.append("\n")
      p.append(seperator)

      for (c <- columns) {
        c.printSeperator(p)
        p.append(seperator)
      }
      p.append("\n")
    }

    val columnsPrint = columns.map(new ColumnsWithCounter(_))
    while(!columnsPrint.forall(_.finished)) {
      if (columnsPrint.forall(_.rowIsSeperator)) {
          for (c<-columnsPrint) {
            p.append(seperator)
            c.printRowSeperator()
          }
          p.append(seperator)
        }
      else {
        for (c<-columnsPrint) {
          p.append(seperator)
          c.printRow()
        }
        p.append(seperator)
      }
      p.append("\n")
    }

  }

  def writeTable(rows:GenSeq[Array[String]], p:Appendable, seperator:String): Unit = {
    val columns = rows(0).map(c => new Column(""))
    for (r <- rows) {
      for (i <- 0 to r.length-1) {
        columns(i).rows += r(i)
      }
    }
    writeTable(columns, p, seperator, printHeading = false)
  }

  private val hexArray = "0123456789ABCDEF".toCharArray()
  def hashString(input:String):String = {

    val md = MessageDigest.getInstance("SHA-256")
    val encoder = StandardCharsets.UTF_8.encode(input)
    md.update(encoder)
    val bytes = md.digest()

    val hexChars = new Array[Char](bytes.length * 2)
    for ( j <-  0 to bytes.length-1) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = hexArray(v >>> 4)
      hexChars(j * 2 + 1) = hexArray(v & 0x0F)
    }
    return new String(hexChars)
  }
}
