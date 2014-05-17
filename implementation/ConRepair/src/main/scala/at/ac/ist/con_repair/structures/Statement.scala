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

import at.ac.ist.con_repair.helpers._
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement
import scala.reflect.runtime.{universe=>ru}

class Statement(val stmt:CStatement) extends Structure {

  val number:Id = Structure.getId(stmt.getFileLocation, this)

  def transform(transformer: List[Structure] => List[Structure]) : Statement = {
    // just clone, reorder nothing
    val newStmt = this.getClass.getConstructors.apply(0).newInstance(stmt).asInstanceOf[Statement]
    copyValues(newStmt)
    newStmt
  }

  def usedVariables:VariableAnalysisResult = {
    SomeVars(Expressions.getUsedVariables(stmt))
  }

  def changedVariables:VariableAnalysisResult = {
    SomeVars(Expressions.getChangedVariables(stmt))
  }

  def processAllStructures(processor:(Structure,List[Structure])=>Boolean): Unit = {}

  def blockSize: Int = 1

  override def usedVariablesInitial = usedVariables
  override def changedVariablesInitial = changedVariables

}
