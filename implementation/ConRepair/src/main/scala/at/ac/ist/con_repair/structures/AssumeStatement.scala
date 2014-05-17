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

import org.sosy_lab.cpachecker.cfa.ast.c.{CExpression, CFunctionCallStatement}

class AssumeStatement(stmt:CFunctionCallStatement, val wasWhile:Boolean = false) extends Statement(stmt) {
  val condition: CExpression = stmt.getFunctionCallExpression.getParameterExpressions.get(0)

  override def transform(transformer: List[Structure] => List[Structure]) : Statement = {
    // just clone, reorder nothing
    val newStmt = new AssumeStatement(stmt, wasWhile)
    copyValues(newStmt)
    newStmt
  }
}
