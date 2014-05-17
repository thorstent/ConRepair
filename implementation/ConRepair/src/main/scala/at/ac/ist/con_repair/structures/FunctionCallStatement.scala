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

import at.ac.ist.con_repair.helpers.{Expressions, SomeVars, VariableAnalysisResult}
import org.sosy_lab.cpachecker.cfa.ast.c._
import at.ac.ist.con_repair.helpers.SomeVars
import scala.Some

class FunctionCallStatement(stmt:CStatement) extends Statement(stmt) {
  // we must implement CFunctionCall
  if (!stmt.isInstanceOf[CFunctionCall])
    throw new IllegalArgumentException("Must give a Function call")
  val functionCall = stmt.asInstanceOf[CFunctionCall]

  val functionCalledName = functionCall.getFunctionCallExpression.getFunctionNameExpression.asInstanceOf[CIdExpression].getName

  def functionCalled:Function = this.programLevel.functionMap.get(functionCalledName) match {
    case Some(f) => f
    case None => throw new Exception(s"Function $functionCalledName not found. Did you define it in the code?")
  }

  /**
   * Returns if this statement assigns the return value to a variable
   * @return The name of the variable
   */
  val assignedTo:Option[CIdExpression] = {
    stmt match {
      case call:CFunctionCallAssignmentStatement => Some(call.getLeftHandSide.asInstanceOf[CIdExpression])
      case _ => None
    }
  }

  val parameters:List[CExpression] = {
    import scala.collection.JavaConversions._
    functionCall.getFunctionCallExpression.getParameterExpressions.toList
  }

  def async = functionCalled.async

  override def usedVariables : VariableAnalysisResult = {
    functionCalled.usedVariables // local variables in the function do not matter
  }

  override def usedVariablesInitial : VariableAnalysisResult = {
    functionCalled.usedVariablesInitial // local variables in the function do not matter
  }

  override def transform(transformer: List[Structure] => List[Structure]) : FunctionCallStatement = {
    // nothing to reorder
    val newStmt = new FunctionCallStatement(stmt)
    copyValues(newStmt)
    newStmt
  }

  override def changedVariables : VariableAnalysisResult = {
    functionCalled.changedVariables ++
      (assignedTo match {
        case Some(exp) => SomeVars(Set((exp.getName,Expressions.isGlobal(exp))))
        case _ => VariableAnalysisResult.empty
      })
  }

  override def changedVariablesInitial : VariableAnalysisResult = {
    functionCalled.changedVariablesInitial ++
      (assignedTo match {
        case Some(exp) => SomeVars(Set((exp.getName,Expressions.isGlobal(exp))))
        case _ => VariableAnalysisResult.empty
      })
  }
}
