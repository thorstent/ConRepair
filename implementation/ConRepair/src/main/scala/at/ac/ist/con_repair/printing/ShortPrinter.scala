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

class ShortPrinter() extends Printer {
  override protected def printProgramBegin(prog: Program): Unit = {
    append("Program")
    stop = true
  }

  override protected def printIfBegin(i: If): Unit = {
    append("if (")
    append(i.condition.toASTString)
    append(") {...}")
    stop = true
  }

  override protected def printFunctionBegin(fun: Function): Unit = {
    append(fun.declaration.getType.toASTString(fun.name))
    append(" {...}")
    stop = true
  }

  override protected def printStatement(stmt: Statement): Unit = {
    append(stmt.stmt.toASTString)
  }

  override protected def printWhileBegin(whil: While): Unit = {
    append("while (")
    append(whil.condition.toASTString)
    append(") {...}")
    stop = true
  }

    override protected def printFunctionCallStatement(fcall: FunctionCallStatement): Unit = {
    //if (fcall.async) append("spawn ")
    printStatement(fcall)
  }

  override protected def printSpecialStatement(stmt: SpecialStatement): Unit = {
    append(stmt.orgStmt.toASTString)
  }

  override protected def printSectionBegin(stmt:SectionBlock): Unit = {
    if (stmt.atomic) append("at{ ") else append("nr{ ")
    print(stmt.stmts(0), appender)
    append(" ...}")
    stop = true
  }
}
