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

package at.ac.ist.con_repair.printing

import at.ac.ist.con_repair.printing.endpoints.EndpointBase
import at.ac.ist.con_repair.structures._
import scala.collection.{GenSet, mutable}
import at.ac.ist.con_repair.helpers._
import org.sosy_lab.cpachecker.cfa.ast.c.CIntegerLiteralExpression
import at.ac.ist.con_repair.helpers.FunctionEnd
import at.ac.ist.con_repair.helpers.StmtLine
import at.ac.ist.con_repair.helpers.FunctionBegin
import at.ac.ist.con_repair.model_checker.GoodTraceRestrictions

// CLEANUP: These parameters need to be sorted out
/**
 * Prints the program out so that CBMC can read it
 * @param endpoint The object that has the model-checker specific stuff
 * @param goodTraceInfo Some if we wanto a good trace, otherwise None
 * @param preventDeadlock This enforces that a deadlock should not occure (weakens assertions). For bad traces only.
 * @param noTrace Generates code for verification that does not allow for a meaningful trace to be extracted.
 *                However, CBMC will be faster on this code. True gives a different meaning to prevent deadlock: It will then try to find a complete trace.
 */
class VerPrinter(endpoint : EndpointBase, goodTraceInfo:Option[GoodTraceRestrictions], preventDeadlock:Boolean, noTrace:Boolean) extends Printer {
  private val goodTrace = goodTraceInfo.nonEmpty

  val statementMap = Map.newBuilder[Int, PrintLine]

  // keep track that all constraints were actually printed (this is an assertion to test this code works ok)
  //protected val constraintArray = new Array[Int](constraintCounter+1)

  private var lineNumber = 1

  // how many chars to leave empty when indenting (to make space for atomic begin)
  private val align = endpoint.atomicBegin.length + 15
  private val align2 = align + 70

  // characters in the current line of printing
  private var charInCurrLine = 0

  def addStmtToMap(stmt: Statement, assertFailed:Boolean = false): Unit = {
    statementMap += ((lineNumber, StmtLine(stmt, assertFailed)))
  }

  override protected def printProgramBegin(prog: Program): Unit = {
    for (l <- endpoint.head.split("\\n")) {
      indent()
      append(l)
      append("\n")
    }
    append("\n\n")
    super.printProgramBegin(prog)
    if (!noTrace) {
      indent()
      append("int __dummy = 0; //dummy to ensure the line shows up in the trace\n")
    }
    if (!noTrace || preventDeadlock)
      for (t <- prog.threads) {
        indent()
        append("int __" + t + "_finished = 0;\n")
      }
    if (!noTrace) {
      if (goodTrace) {
        // add constraints to the program order
        val declared = new mutable.HashSet[(Int,String)]()
        for (n <- 0 to goodTraceInfo.get.readFromList.length - 1) {
          indent()
          append("int __constraint" + n + "_ok = 1;\n")
          for (x <- goodTraceInfo.get.readFromList(n)) {
            val (from, (name,_), _) = x
            if (!declared.contains((n,name))) {
              declared.add((n,name))
              indent()
              if (from == ZeroId) append("int __constraint" + n + name + " = 1;\n") else append("int __constraint" + n + name + " = 0;\n")
            }
          }
        }
        indent()
        append("int __assert_reached = 1;\n") // will make sure we acutally reach the assertion we are interested in (the assertion will set this to 0)
      } else {
        indent()
        append("int __assert_fail = 0;\n")
        if (preventDeadlock) {
          indent()
          append("int __assume_test;")
        }
      }
    }

    append("\n\n")
  }

  override protected def printProgramEnd(prog: Program): Unit = {
    append("\n")
    append("// Line: " + lineNumber.toString)
    // assert we printed all the constraints
    /*for (i <- 1 to constraintCounter) {
      assert (constraintArray(i) == 3)
    }*/
  }

  override protected def printWhileBegin(whil:While): Unit = {
    assert(whil.condition.isInstanceOf[CIntegerLiteralExpression] || whil.isNondet) // we only support infinite loops
    indent()
    /*if (goodTrace) {
      append("// while\n")
    } else {*/
      append("while (")
      append(endpoint.nondet)
      append(") {\n")
    //}
    depth += 1
  }

  override protected def printWhileEnd(whil:While): Unit = {
    /*if (goodTrace) {
      depth -= 1
      indent()
      append("// end while\n")
    } else {*/
      super.printWhileEnd(whil)
    //}
  }

  override protected def printFunctionBegin(fun:Function): Unit = {
    super.printFunctionBegin(fun)
    if (!noTrace) {
      statementMap += ((lineNumber, FunctionBegin(fun)))
      indent()
      append("__dummy=0; // function begin\n")
    }
  }

  override protected def printFunctionEnd(fun: Function): Unit = {
    if (!noTrace) {
      statementMap += ((lineNumber, FunctionEnd(fun)))
      indent()
      append("__dummy=0; // function end\n")
    }
    if (fun.name == "main") {
      // ensure all threads are finished
      if ((goodTraceInfo.isEmpty || goodTraceInfo.get.finishTrace) && (!noTrace || preventDeadlock))
        for (t <- fun.parent.asInstanceOf[Program].threads) {
          indent()
          append(endpoint.printAssume("__" + t + "_finished == 1"))
          append(";\n")
        }
      indent()
      if (goodTrace) {
        if (goodTraceInfo.get.reachId!=ZeroId) {
          if (goodTraceInfo.get.finishTrace) {
            append(endpoint.printAssert("__assert_reached"))
            append(";\n")
          }
        } else {
          append(endpoint.printAssert("0")) // for the case ZeroId, we just want the program to finish
          append(";\n")
        }
      } else if (noTrace && preventDeadlock) {
        append(endpoint.printAssert("0"))
        append(";\n")
      } else if (!noTrace) {
        append(endpoint.printAssert("__assert_fail == 0"))
        append(";\n")
      }

    }

    if ((!noTrace || preventDeadlock) && fun.async) {
      if (true || goodTraceInfo.get.reachId==ZeroId) {
        indent()
        append("__" + fun.name + "_finished = 1;\n")
      }
    }
    super.printFunctionEnd(fun)
  }

  override protected def printFunctionCallStatement(stmt: FunctionCallStatement): Unit = {
    indent()
    if (!noTrace) {
      addStmtToMap(stmt)
      append("__dummy=0; ") // to ensure the function call statement shows up in the trace
    }
    if (stmt.async)
      append(endpoint.printAsyncCall(stmt.stmt.toASTString))
    else {
      append(stmt.stmt.toASTString)
      //addConstraints(stmt)
    }
    append("\n")
  }

  override protected def printIfBegin(i: If): Unit = {
    if (i.isNondet) {
      indent()
      append("if (nondet_int()) {\n")
      depth += 1
    } else {
      if (!noTrace) {
        indent()
        val testvar = "__test" + lineNumber
        append("int ")
        append(testvar)
        append(";\n")
        statementMap += ((lineNumber, Decision(i.assumeThe.get, i.assumeEls.get)))
        printAtomicBegin()
        indent()
        append(testvar)
        append(" = ")
        append(i.condition.toASTString)
        append(";")
        indent2()
        //addConstraints(i)
        printAtomicEnd()
        append("\n")
        indent()
        append("if (")
        append(testvar)
        append(") {\n")
        depth += 1
      } else {
        super.printIfBegin(i)
      }
    }
  }

  override protected def append(str:String): Unit = {
    super.append(str)
    val newlines = str.count(_=='\n')
    lineNumber += newlines
    if (newlines>0)
      charInCurrLine = 0
    else
      charInCurrLine += str.length
  }

  override protected def indent(offset:Int = 0): Unit = {
    for (a <- charInCurrLine to align)
      append(" ")
    for (a <- 1 to (depth+offset))
      append("  ")
    charInCurrLine = align + ((depth+offset)-1)*2
  }

  protected def indent2(): Unit = {
    for (a <- charInCurrLine to align2)
      append(" ")
  }

  override protected def printAssertStatement(stmt: AssertStatement): Unit = {
    if (goodTrace) {
      printStatement(stmt, endpoint.printAssume(stmt.condition.toASTString) + ";")
    }
    else if (noTrace) {
      printStatement(stmt, endpoint.printAssert(stmt.condition.toASTString) + ";")
    } else {
      // make a distinction between failing and succeding assertions
      printAtomicBegin()
      append("\n")
      indent()
      append("if (")
      append(stmt.condition.toASTString)
      append(")\n")
      indent(1)
      addStmtToMap(stmt, assertFailed = false)
      append("__dummy=0; //assertion passed\n")
      indent()
      append("else\n")
      indent(1)
      addStmtToMap(stmt, assertFailed = true)
      append("__assert_fail=1;\n")
      indent()
      indent2()
      printAtomicEnd()
      append("\n")
    }
  }

  override protected def printAssumeStatement(stmt: AssumeStatement): Unit = {
    val condition =
      (if (goodTrace|| !preventDeadlock || noTrace) "" else "__assert_fail>0 || " ) +
      stmt.condition.toASTString
    printStatement(stmt, (if (goodTrace || !preventDeadlock || noTrace) "" else s"__assume_test = ${stmt.condition.toASTString}; ") + endpoint.printAssume(condition) + ";")
  }

  override protected def printStatement(stmt: Statement): Unit = {
    printStatement(stmt, stmt.stmt.toASTString)
  }

  override protected def printSpecialStatement(stmt: SpecialStatement): Unit = {
    printAtomicBegin()
    atomicLevel += 1
    append("\n")
    printList(stmt.transStmt)
    indent()
    indent2()
    atomicLevel -= 1
    printAtomicEnd()
    append("\n")
  }

  override protected def printAtomicBegin(): Unit = {
    if (atomicLevel == 0)
      append(endpoint.atomicBegin)
  }

  override protected def printAtomicEnd(): Unit = {
    if (atomicLevel == 0)
      append(endpoint.atomicEnd)
  }

  override protected def printNoReorderBegin(): Unit = {
    append("//noReorderBegin();")
  }


  override protected def printNoReorderEnd(): Unit = {
    append("//noReorderBegin();")
  }

  /**
   * This function does the actual printing
   * @param stmt The statement for meta-information
   * @param stmtString The string to print
   */
  protected def printStatement(stmt: Statement, stmtString:String): Unit = {
    printAtomicBegin()

    indent()
    append(stmtString)

    indent2()
    if (!noTrace) {
      addStmtToMap(stmt)
      addConstraints(stmt)
    }

    indent2()
    printAtomicEnd()
    append("\n")
  }

  private def addConstraints(stmt: Structure) {
    goodTraceInfo match {
      case None =>
      case Some(gti) =>
        stmt match {
          case _:AssertStatement if stmt.number==gti.reachId =>
            val cond = (for (n <- 0 to gti.readFromList.length - 1) yield {
              val variables: GenSet[String] = (gti.readFromList(n).filter{x => val (_,_,to) = x; to==stmt.number}.map(_._2._1).map{ variable =>
              "__constraint" + n + variable + " == 1" })
              (variables + ("__constraint" + n + "_ok")).mkString(" && ")
            }).filter(_.length>5).mkString(" || ")

            if (gti.finishTrace)
              append(s"__assert_reached = __assert_reached && ${if (cond == "") "0" else cond};")
            else
              append(endpoint.printAssert(if (cond == "") "0" else cond) + ";")
          case _ =>
            for (n <- 0 to gti.readFromList.length - 1) {
              for (x <- gti.readFromList(n)) {
                val (from, ver, to) = x; val (name,_) = ver
                if (stmt.changedVariables.contains(ver)) {
                  if (stmt.number==from)
                    append("__constraint" + n + name + " = 1; ")
                  else
                    append("__constraint" + n + name + " = 0; ")
                }
                if (stmt.usedVariables.contains(ver)) {
                  if (stmt.number==to) {
                    append("__constraint" + n + "_ok = __constraint" + n + "_ok && __constraint" + n + name + ";")
                  }
                }
              }
            }
        }
    }
  }
}
