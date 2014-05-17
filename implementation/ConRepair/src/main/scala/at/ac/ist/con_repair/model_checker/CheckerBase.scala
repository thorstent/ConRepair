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

package at.ac.ist.con_repair.model_checker

import at.ac.ist.con_repair.helpers._
import at.ac.ist.con_repair.printing.VerPrinter
import at.ac.ist.con_repair.model_checker.cbmc.CBMCEndpoint
import java.util.Date
import at.ac.ist.con_repair.printing.endpoints.EndpointBase
import java.io.File
import scala.Int
import at.ac.ist.con_repair.structures._
import scala.collection.mutable.ListBuffer
import scala.collection.{GenSet, mutable}
import scala.Some
import at.ac.ist.con_repair.helpers.StmtLine

import CheckerResult._
import org.sosy_lab.cpachecker.cfa.ast.c.CIdExpression
import at.ac.ist.con_repair.Main

abstract class CheckerBase()(implicit debugOutDir: Option[File]) {
  val endpoint : EndpointBase

  /**
   * Represents one program step
   * @param line The line that was executed
   * @param thread The thread number
   * @param function The function name
   * @param variable Variable name if any
   * @param value Value that was assigned to the variable
   */
  protected class CtexLine(val line: Int, val thread: Int, val function:String, val variable:String, val value:String) {
    var assertionFailed = false

    override def toString : String = {
      thread + " - " + function + " : " + thread
    }
  }

  protected var result : String = null

  private val ioCacheFolder = new File("ioCache")
  private val ioCache = Main.options.cache

  private def checkCache(code:String):Option[(String,Long)] = {
    if (!ioCache) return None
    val hash = IO.hashString(code)
    val f = new File(ioCacheFolder, hash + ".output")
    if (f.exists()) {
      val output = IO.readToString(f.getAbsolutePath.replace(".input", ".output"))
      val time = output.substring(0,output.indexOf('\n')).toLong
      return Some((output,time))
    }
    None
  }

  private def addCache(code:String, output:String, time:Long):Unit = {
    if (!ioCache && !ioCacheFolder.exists()) return
    if (!ioCacheFolder.exists()) ioCacheFolder.mkdir()

    val hash = IO.hashString(code)
    val codeFile = new File(ioCacheFolder, hash + ".input")
    val outputFile = new File(ioCacheFolder, hash + ".output")
    IO.writeToFile(codeFile, code)
    IO.writeToFile(outputFile, time.toString + "\n" + output)
  }

  /**
   * Starts the model checker process
   * @param code The code that should be checked
   * @param unwinding How often to unwind the loops
   * @param skipLoop Allows the loop body to be skiped (if this leads to a counter-example)
   */
  protected def createProcess(code : String, unwinding:Int, skipLoop:Boolean) : Unit

  protected def interpretResult() : CheckerResult

  protected def parseTrace() : List[CtexLine]

  /**
   * Obtains the trace from the result
   * @param stmtMap The map of line numbers to statements
   * @return The trace as a list of counter-examples
   */
  protected def getTrace(stmtMap: Map[Int,PrintLine]) : List[CtexStmt] = {

    class CallStack(val threadName:String) {
      val stack = new mutable.ArrayStack[FunctionCallStatement]
    }

    val lb = new ListBuffer[CtexStmt]
    val trace = parseTrace().filter(_.function!="") // we are not interested in stuff without function
    val threads = new mutable.OpenHashMap[Int, CallStack]()
    var pos = 0
    for (l <- trace) {
      pos = pos + 1
      // we want to ignore constraints
      if (!l.variable.startsWith("__constraint")) {
        // find a matching line
        stmtMap.get(l.line) match {
          case None => ()
          case Some(c) => c match {
            case FunctionBegin(fun) =>
              val t: CallStack = threads.get(l.thread) match {
                case None =>
                  val cs = new CallStack(fun.name)
                  threads.put(l.thread, cs)
                  cs
                case Some (x) =>
                  // this is not the beginning of a thread, therefore we should have already noted this function
                  // in the call stack
                  assert (x.stack.top.functionCalledName == fun.name)
                  x
              }
            case FunctionEnd(fun) =>
              if (threads(l.thread).stack.isEmpty)
                assert (fun.name == threads(l.thread).threadName)
              else {
                val last = threads(l.thread).stack.pop()
                assert (last.functionCalled == fun)
                // if there is a return variable, put an equals in between
                last.assignedTo match {
                  case None =>
                  case Some(asgn) =>
                    fun.returnVariable match {
                      case None => throw new Exception("return value expected, but function returns nothing")
                      case Some(ret) =>
                        val cstmt = Expressions.makeAssignment(asgn, ret, Expressions.syntheticLocation(asgn.getFileLocation.getStartingLineNumber, '\0')) // in case there is a constraint constructed to this line
                        val stmt = new Statement(cstmt)
                        stmt.synthetic = true
                        stmt.parent = last
                        val ctexstmt = new CtexStmt(stmt, false, false, threads(l.thread).threadName, l.thread, threads(l.thread).stack.toList, pos)
                        lb += ctexstmt
                    }
                }
              }
            case StmtLine(stmt, assertionFailure) =>
              assert (!(stmt.isInstanceOf[FunctionCallStatement] && stmt.asInstanceOf[FunctionCallStatement].async && l.thread != 0), "we only spawn in main")

              val t = threads(l.thread)
              val assumptionFailure = (l.variable == "__assume_test" && Integer.parseInt(l.value) == 0)


              stmt match {
                case funCall:FunctionCallStatement if (l.thread != 0) =>
                  lb += new CtexStmt(funCall, false, false, threads(l.thread).threadName, l.thread, threads(l.thread).stack.toList, pos)
                  t.stack.push(funCall)
                  if (funCall.parameters.length != funCall.functionCalled.parameters.length) throw new Exception("length of lists needs to be the same")
                  val zip = funCall.parameters.zip(funCall.functionCalled.parameters)
                  var suffix = 'a'
                  for ((s,a) <- zip) {
                    val asgn = Expressions.makeName(a)
                    val cstmt = Expressions.makeAssignment(asgn, s, Expressions.syntheticLocation(funCall.functionCall.getFunctionCallExpression.getFileLocation.getStartingLineNumber, suffix)) // in case there is a constraint constructed to this line
                    val stmt = new Statement(cstmt)
                    stmt.synthetic = true
                    stmt.parent = funCall
                    val ctexstmt = new CtexStmt(stmt, false, false, threads(l.thread).threadName, l.thread, threads(l.thread).stack.toList, pos)
                    lb += ctexstmt
                    suffix = (suffix + 1).toChar
                  }
                case funCall:FunctionCallStatement =>
                case _ =>
                  val ctexstmt = new CtexStmt(stmt, assertionFailure, assumptionFailure, t.threadName, l.thread, t.stack.toList, pos)
                  lb += ctexstmt
              }

            case Decision(stmtTrue, stmtFalse) =>
              val t = threads(l.thread)

              assert (l.variable.contains("test"))
              val value = Integer.parseInt(l.value)
              if (value != 0)
                lb += new CtexStmt(stmtTrue, false, false, t.threadName, l.thread, t.stack.toList, pos)
              else
                lb += new CtexStmt(stmtFalse, false, false, t.threadName, l.thread, t.stack.toList, pos)

          }
        }

      }
    }
    lb.result()
  }

  private def getBugId(trace:List[CtexStmt]):Int = {
    val tf = trace.filter(_.primitive)
    val (t1,t2) = tf.span(!_.assertionFailure)
    val failingAssertion = t2(0)
    /*if (failingAssertion.stmt.asInstanceOf[AssertStatement].deadlockAssertion) {
      failingAssertion.stmt.number.line.hashCode
    } else {*/
      // take into account how we got to the bugs (
      val thread1 = failingAssertion.threadId
      val thread2:Int = (t1.reverse.find(s => !s.stmt.changedVariables.intersectionEmpty(failingAssertion.stmt.usedVariables) && s.threadId!=thread1 && s.threadId!=0) getOrElse {
        t2.tail.find(s => !s.stmt.changedVariables.intersectionEmpty(failingAssertion.stmt.usedVariables) && s.threadId!=thread1) getOrElse tf(0)
      }).threadId
      failingAssertion.stmt.number.line.hashCode ^ thread1.hashCode ^ thread2.hashCode
    //}
  }

  /**
   *
   * @param so The program to check
   * @param goodTraceInfo Some if a good trace should be produced
   * @param preventDeadlock Ensure that the program cannot deadlock at the cost of getting strange counter-examples
   *                        This is not relevant for good traces
   * @param noTrace Generates code for verification that does not allow for a meaningful trace to be extracted.
   *                However, CBMC will be faster on this code. True implies preventDeadlock.
   * @return (The result of the check, the trace (if not successful), the id of the bug, the time that passed)
   */
  def invokeChecker(so: StatementOrder, goodTraceInfo : Option[GoodTraceRestrictions], preventDeadlock:Boolean, noTrace:Boolean) : (CheckerResult, List[CtexStmt], Int, Long) = {
    val printer = new VerPrinter(CBMCEndpoint, goodTraceInfo, preventDeadlock, noTrace)
    val code = printer.print(so.program)
    val statementmap = printer.statementMap.result()
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      IO.writeToFile(new File(debugOutDir2, "output.c"), code)
    }
    var time:Long = 0

    //val unwind = if (goodTrace) 2 else 2 // magic number
    val unwind = 2
    checkCache(code) match {
      case None =>
        val startTime = new Date()
        createProcess(code, unwind, goodTraceInfo.isEmpty)
        time = new Date().getTime - startTime.getTime
        addCache(code, result, time)
      case (Some((r,t))) =>
        result = r
        time = t
    }
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      IO.writeToFile(new File(debugOutDir2, "model-checker.log"), result)
    }

    val res = interpretResult()
    if (res == Success)
      (Success, List.empty, 0, time)
    else if (res == Failure) {
      val trace = if (!noTrace) getTrace(statementmap) else List()
      val bugId = if (goodTraceInfo.isEmpty && !noTrace) {
        getBugId(trace)
      } else 0
      (Failure, trace, bugId, time)
    } else {
      throw new Exception("model-checker undecided.")
    }
  }

}
