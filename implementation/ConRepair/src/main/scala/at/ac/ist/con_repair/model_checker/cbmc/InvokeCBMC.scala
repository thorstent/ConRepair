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

package at.ac.ist.con_repair.model_checker.cbmc

import java.io._
import scala.collection.mutable.ListBuffer
import scala._
import scala.Predef._
import at.ac.ist.con_repair.model_checker.{CheckerResult, CheckerBase}
import at.ac.ist.con_repair.helpers.IO
import CheckerResult._
import java.util.regex.Pattern
import java.util.Calendar
import java.text.SimpleDateFormat
import java.nio.file.Files
import at.ac.ist.con_repair.Main
import scala.collection.mutable

object InvokeCBMC {
  private var pathToCBMC:Option[String] = None

  def getCBMCPath:String = {
    pathToCBMC match {
      case Some(p) => p
      case None => // we need to put cbmc to tmpfs
        // We extract cbmc and z3 executable from the jar files (maven dependencies)
        // take care of 32bit systems
        val inC = this.getClass.getClassLoader.getResourceAsStream(if (Main.options.is64bit) "model-checkers/cbmc" else "model-checkers/cbmc-32")
        val inZ = this.getClass.getClassLoader.getResourceAsStream(if (Main.options.is64bit) "smt-solvers/z3" else "smt-solvers/z3-32")
        if (inC!=null || inZ!=null) new Exception("CBMC or Z3 resources not available (this is a build problem)")
        val temp = Files.createTempDirectory("cbmc").toFile
        val cbmc = new File(temp, "cbmc")
        val z3 = new File(temp, "z3")
        // copy CBMC
        val outC = new FileOutputStream(cbmc)
        IO.copy(inC, outC)
        outC.close()
        inC.close()
        // copy z3
        val outZ = new FileOutputStream(z3)
        IO.copy(inZ, outZ)
        outZ.close()
        inZ.close()
        // set execute permission
        cbmc.setExecutable(true)
        z3.setExecutable(true)
        // set to delete on terminate
        cbmc.deleteOnExit()
        z3.deleteOnExit()
        temp.deleteOnExit()
        // return path
        pathToCBMC = Some(temp.getAbsolutePath)
        temp.getAbsolutePath
    }
  }
}

class InvokeCBMC()(implicit debugOutDir: Option[File]) extends CheckerBase {
  val endpoint = CBMCEndpoint

  protected def createProcess(code:String, unwinding:Int, skipLoop:Boolean) : Unit = {
    val temp = File.createTempFile("output",".c")
    temp.deleteOnExit()
    IO.writeToFile(temp, code)
    val arguments = (List(InvokeCBMC.getCBMCPath + "/cbmc", "--unwind", unwinding.toString, "--no-unwinding-assertions", "--z3") ++
      (if (skipLoop) List("--partial-loops") else List())) :+ temp.getAbsolutePath
    import scala.collection.JavaConversions._
    val pb = new ProcessBuilder(arguments)
    pb.environment().put("PATH", InvokeCBMC.getCBMCPath + ":" + pb.environment().get("PATH"))
    pb.redirectInput()
    pb.redirectError()
    val p = pb.start()
    val out = IO.readToString(p.getInputStream)
    val err = IO.readToString(p.getErrorStream)
    val exit = p.waitFor()
    if (err != "")
      throw new Exception("CBMC had an error: " + err)
    temp.delete()
    result = out
  }

  protected def interpretResult() : CheckerResult = {
    if (result.contains("VERIFICATION SUCCESSFUL")) {
      Success
    } else if (result.contains("VERIFICATION FAILED")) {
      Failure
    } else {
      Undecided
    }
  }

  val pattern = Pattern.compile("State \\d+ file \\S*output\\S*\\.c line (?<line>\\d+) function (?<function>\\S+) thread (?<thread>\\d+)\n-*\n\\s*(?<variable>\\S+)=(?<value>\\S+)")

  protected def parseTrace(): List[super.CtexLine] = {
    val lb = new ListBuffer[super.CtexLine]
    val matcher = pattern.matcher(result)
    val lastLine = new mutable.HashMap[Int,Int]()
    while(matcher.find()) {
      val line = Integer.parseInt(matcher.group("line"))
      val func = matcher.group("function")
      val thread = Integer.parseInt(matcher.group("thread"))
      val variable = matcher.group("variable")
      val value = matcher.group("value")
      if ((!lastLine.contains(thread) || lastLine(thread)!=line))
        lb += new super.CtexLine(line, thread, func, variable, value)
      lastLine(thread) = line
    }
    lb.result()
  }



  // not usable due to bug in XML output of CBMC
  /*protected def parseTrace(): List[super.CtexLine] = {
    val lb = new ListBuffer[super.CtexLine]
    val trace = result \ "goto_trace"
    var lastLine = 0
    var lastThread = 0
    (trace \ "_").foreach { step =>
      val location = step \ "location"
      if ((location \ "file").text.contains("output")) {
        val line = Integer.parseInt((location \ "line").text)
        val func = (location \ "function").text
        val thread = Integer.parseInt((step \ "thread").text)
        if (thread != lastThread || lastLine != line)
          lb += new super.CtexLine(line, thread, func)
        lastThread = thread
        lastLine = line
      }
    }

    lb.result()
  }*/
}


