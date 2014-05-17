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

import at.ac.ist.con_repair.structures._
import java.util.regex.{Pattern, Matcher}

/**
 * Base class that has the code to print standard C
 * This class is not thread safe
 */
abstract class Printer {
  protected var depth : Int = 0
  protected var appender:Appendable  = null
  // causes this class to stop printing after (used for short prints)
  protected var stop = false

  // how many atomic sections we printed (nested in one another)
  protected var atomicLevel = 0
  
  protected def printProgramBegin(prog: Program): Unit = {
    // print includes
    val matcher: Matcher = Pattern.compile("#.*").matcher(prog.originalProgram)
    while (matcher.find()){
      indent()
      append(matcher.group)
      append("\n")
    }
    append("\n")

    //print global declarations
    for (gd <- prog.globalDeclarations) {
      indent()
      append(gd.toASTString)
      append("\n")
    }
    append("\n")

    //print global function declarations
    for (f <- prog.functions) {
      indent()
      append(f.declaration.getType.toASTString(f.name))
      append(";\n")
    }
    append("\n")
    append("\n")
  }

  protected def printProgramEnd(prog: Program): Unit = {}

  protected def printFunctionBegin(fun: Function): Unit = {
    indent()
    append(fun.declaration.getType.toASTString(fun.name))
    append(" {\n")
    depth += 1
    for (d <- fun.localVariables.toList.sortBy(_.getName())) {
      indent()
      append(d.getType.toASTString(d.getName))
      append(";\n")
    }
  }

  protected def printFunctionEnd(fun: Function): Unit = {
    fun.returnVariable match {
      case Some(x) =>
        indent()
        append("return ")
        append(x.getName)
        append(";\n")
      case None =>
    }
    depth -= 1
    indent()
    append("}\n\n")
  }

  protected def printIfBegin(i: If): Unit = {
    indent()
    append("if (")
    append(i.condition.toASTString)
    append(") {\n")
    depth += 1
  }

  protected def printIfElse(i: If): Unit = {
    depth -= 1
    indent()
    append("} else {\n")
    depth += 1
  }

  protected def printIfEnd(i: If): Unit = {
    depth -= 1
    indent()
    append("}\n")
  }

  protected def printWhileBegin(whil: While): Unit = {
    indent()
    append("while (")
    append(whil.condition.toASTString)
    append(") {\n")
    depth += 1
  }

  protected def printWhileEnd(whil: While): Unit = {
    depth -= 1
    indent()
    append("}\n")
  }

  protected def printStatement(stmt: Statement): Unit = {
    indent()
    append(stmt.stmt.toASTString)
    append("\n")
  }

  protected def printFunctionCallStatement(stmt: FunctionCallStatement): Unit = printStatement(stmt)

  protected def printAssertStatement(stmt: AssertStatement): Unit = printStatement(stmt)

  protected def printAssumeStatement(stmt: AssumeStatement): Unit = printStatement(stmt)

  protected def printSpecialStatement(stmt: SpecialStatement): Unit = {
    indent()
    append(stmt.orgStmt.toASTString)
    append("\n")
  }

  protected def printSectionBegin(stmt: SectionBlock): Unit = {
    if (stmt.atomic) {
      if (atomicLevel == 0) {
        indent()
        printAtomicBegin()
        append("\n")
        depth += 1
      }
      atomicLevel += 1
    } else {
      indent()
      printNoReorderBegin()
      append("\n")
      depth += 1
    }
  }

  protected def printSectionEnd(stmt: SectionBlock): Unit = {
    if (stmt.atomic) {
      atomicLevel -= 1
      if (atomicLevel==0) {
        depth -= 1
        indent()
        printAtomicEnd()
        append("\n")
      }
    } else {
      depth -= 1
      indent()
      printNoReorderEnd()
      append("\n")
    }
  }

  protected def printAtomicBegin(): Unit = {
    append("atomicBegin();")
  }

  protected def printNoReorderBegin(): Unit = {
    append("noReorderBegin();")
  }

  protected def printAtomicEnd(): Unit = {
    append("atomicEnd();")
  }

  protected def printNoReorderEnd(): Unit = {
    append("noReorderEnd();")
  }

  def print(structure: Structure) : String = {
    val sb = new java.lang.StringBuilder()
    print(structure, sb)
    sb.toString
  }

  protected def printList(l:List[Structure]): Unit = {
    for (i <- l) print(i, appender)
  }

  protected def append(str:String): Unit = {
    appender.append(str)
  }

  protected def indent(offset:Int = 0): Unit = {
    for (a <- 1 to (depth+offset))
      append("  ")
  }

  def print(structure: Structure, appender:Appendable) = {
    this.appender = appender
    structure match {
      case s: Program =>
        printProgramBegin(s)
        if (!stop) {
          printList(s.functions)
          printProgramEnd(s)
        }
      case s: Function =>
        printFunctionBegin(s)
        if (!stop) {
          printList(s.commands)
          printFunctionEnd(s)
        }
      case s: If =>
        printIfBegin(s)
        if (!stop) {
          printList(s.the)
          if (s.els.nonEmpty) {
            printIfElse(s)
            printList(s.els)
          }
          printIfEnd(s)
        }
      case s: While =>
        printWhileBegin(s)
        if (!stop) {
          printList(s.loop)
          printWhileEnd(s)
        }
      case s: FunctionCallStatement =>
        printFunctionCallStatement(s)
      case s: AssertStatement =>
        printAssertStatement(s)
      case s: AssumeStatement =>
        printAssumeStatement(s)
      case s: Statement =>
        printStatement(s)
      case s: SpecialStatement =>
        printSpecialStatement(s)
      case s: SectionBlock =>
        printSectionBegin(s)
        if (!stop) {
          printList(s.stmts)
          printSectionEnd(s)
        }
    }

  }


}
