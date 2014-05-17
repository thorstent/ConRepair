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

import at.ac.ist.con_repair.structures.{AssumeStatement, Statement}
import org.sosy_lab.cpachecker.cfa.ast.c.CUnaryExpression

/**
 * This class prints out the C program as it was originally
 */
class CPrinter extends Printer {
  override protected def printStatement(stmt: Statement): Unit = {
    assert (!stmt.synthetic) // synthetic statements should not reach the printer
    super.printStatement(stmt)
  }

  // ensure that if it was an assume whas a while, we print it as an assume
  override protected def printAssumeStatement(stmt: AssumeStatement): Unit = {
    if (stmt.wasWhile) {
      val cond = stmt.condition.asInstanceOf[CUnaryExpression].getOperand.toASTString
      indent()
      append("while (" + cond + ") {}\n")
    } else
      super.printAssumeStatement(stmt)
  }
}
