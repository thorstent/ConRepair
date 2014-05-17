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

package at.ac.ist.con_repair.structures

import org.sosy_lab.cpachecker.cfa.ast.c.{CFunctionCallStatement, CExpression}
import at.ac.ist.con_repair.helpers.{Expressions, SomeVars, VariableAnalysisResult}

class AssertStatement(stmt:CFunctionCallStatement) extends Statement(stmt) {
  val condition: CExpression = stmt.getFunctionCallExpression.getParameterExpressions.get(0)
  /**
   * If true the good trace analysis will attempt to make a trace to this assertion and see if it will pass
   */
  var analyse = true
  var deadlockAssertion = false

  override def sortable = deadlockAssertion // deadlock assertions can be reordered

  override def transform(transformer: List[Structure] => List[Structure]) : Statement = {
    // just clone, reorder nothing
    val newStmt = new AssertStatement(stmt)
    copyValues(newStmt)
    newStmt.analyse = analyse
    newStmt.deadlockAssertion = deadlockAssertion
    newStmt
  }

  override def usedVariablesInitial = {
    if (deadlockAssertion)
      SomeVars(Set())
    else
      usedVariables
  }

  override def changedVariablesInitial = {
    if (deadlockAssertion)
      SomeVars(Set())
    else
      changedVariables
  }
}
