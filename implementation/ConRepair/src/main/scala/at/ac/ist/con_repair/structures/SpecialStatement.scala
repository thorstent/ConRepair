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

import at.ac.ist.con_repair.helpers.{Expressions, VariableAnalysisResult}
import org.sosy_lab.cpachecker.cfa.ast.c._

object SpecialStatement {
  def from(orgStmt:CStatement): Option[SpecialStatement] = {
    var number:Char = 'a'
    def getLocation:CFileLocation = {
      val res = Expressions.syntheticLocation(orgStmt.getFileLocation.getStartingLineNumber, number)
      number = (number.toByte + 1).toChar
      res
    }

    orgStmt match {
      case funCall : CFunctionCallStatement =>
        funCall.getFunctionCallExpression.getFunctionNameExpression match {
          case id:CIdExpression =>
            val functionCalled = id.getName
            functionCalled match {
              case "up" | "unlock" =>
                val arg = funCall.getFunctionCallExpression.getParameterExpressions.get(0)
                val newS = List(new Statement(new CExpressionAssignmentStatement(getLocation, arg, Expressions.makeIntConst(0))))
                Some (new SpecialStatement(orgStmt, newS))
              case "down" | "lock" =>
                val arg = funCall.getFunctionCallExpression.getParameterExpressions.get(0)
                val callExpr = Expressions.makeFunctionCall("assume", List (Expressions.makeBinaryOperation(arg, Expressions.makeIntConst(0), CBinaryExpression.BinaryOperator.EQUALS)))
                val newS1 = new AssumeStatement(new CFunctionCallStatement(getLocation, callExpr))
                val newS2 = new Statement(new CExpressionAssignmentStatement(getLocation, arg, Expressions.makeIntConst(1)))
                Some (new SpecialStatement(orgStmt, List(newS1, newS2)))
              case _ => None
            }
          case deref: CUnaryExpression =>
            val varName = deref.getOperand.asInstanceOf[CIdExpression]
            val condition = Expressions.makeBinaryOperation(varName, Expressions.makeIntConst(0), CBinaryExpression.BinaryOperator.NOT_EQUALS)
            val newS1 = new AssertStatement(new CFunctionCallStatement(getLocation, Expressions.makeFunctionCall("assert", List(condition))))
            Some (new SpecialStatement(orgStmt, List(newS1)))
        }
      case _ => None
    }
  }
}

/**
 * The statement that was derived from an original statement and is composed of several primitive statements
 * @param orgStmt the original statement
 * @param transStmt a list of statements that will be used during the analysis
 */
class SpecialStatement(val orgStmt:CStatement, val transStmt:List[Statement]) extends SectionBlock(orgStmt.getFileLocation, transStmt, atomic = true) {
  override def transform(transformer: List[Structure] => List[Structure]) : SpecialStatement = {
    val s = transStmt.map(_.transform(s => s)) // we do not transform anything in this list
    val n = new SpecialStatement(orgStmt, s)
    copyValues(n)
    n
  }
}
