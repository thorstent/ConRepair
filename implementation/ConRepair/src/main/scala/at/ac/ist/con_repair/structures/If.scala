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

import org.sosy_lab.cpachecker.cfa.ast.c._
import at.ac.ist.con_repair.helpers._
import at.ac.ist.con_repair.helpers.SomeVars

/**
 *
 * @param condition If not present, then it is a non-deterministic if
 * @param the The then part
 * @param els The else part
 */
class If(val condition:CExpression, val the: List[Structure], val els: List[Structure]) extends Structure{
  the.foreach(_.parent = this)
  els.foreach(_.parent = this)

  val isNondet = Expressions.isNondet(condition)

  val assumeThe = isNondet match {
    case true => None
    case false =>
      val assumeTheLocation = Expressions.syntheticLocation(condition.getFileLocation.getStartingLineNumber, 'a')
      val assumeThe = new AssumeStatement(Expressions.makeAssume(condition, assumeTheLocation))
      assumeThe.synthetic = true
      assumeThe.parent = this
      Some(assumeThe)
  }

  val assumeEls = isNondet match {
    case true => None
    case false =>
      val assumeElsLocation = Expressions.syntheticLocation(condition.getFileLocation.getStartingLineNumber, 'b')
      val assumeEls = new AssumeStatement(Expressions.makeAssume(Expressions.makeUnaryOperation(condition,CUnaryExpression.UnaryOperator.NOT), assumeElsLocation))
      assumeEls.synthetic = true
      assumeEls.parent = this
      Some(assumeEls)
  }

  val number:Id = Structure.getId(condition.getFileLocation, this)

  def usedVariables = {
    the.map(_.usedVariables).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l) ++
    els.map(_.usedVariables).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l) ++
      (if (!isNondet) SomeVars(Expressions.getUsedVariables(condition)) else VariableAnalysisResult.empty)
  }

  def changedVariables = {
    the.map(_.changedVariables).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l) ++
    els.map(_.changedVariables).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l)
  }

  def usedVariablesInitial = {
    the.map(_.usedVariablesInitial).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l) ++
      els.map(_.usedVariablesInitial).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l) ++
      (if (!isNondet) SomeVars(Expressions.getUsedVariables(condition)) else VariableAnalysisResult.empty)
  }

  def changedVariablesInitial = {
    the.map(_.changedVariablesInitial).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l) ++
      els.map(_.changedVariablesInitial).foldLeft(VariableAnalysisResult.empty)((s,l)=>s++l)
  }
  //def getNegCondition():CUnaryExpression = ExpressionHelpers.makeUnaryOperation(condition, CUnaryExpression.UnaryOperator.NOT)

  def transform(transformer: List[Structure] => List[Structure]) : If = {
    val t = transformer(the.map(_.transform(transformer)))
    val e = transformer(els.map(_.transform(transformer)))
    val i = new If(condition, t, e)
    copyValues(i)
    i
  }

  def processAllStructures(processor:(Structure,List[Structure])=>Boolean): Unit = {
    if (processor(this,the))
      if (processor(this,els)) {
        the.foreach(_.processAllStructures(processor))
        els.foreach(_.processAllStructures(processor))
      }
  }

  def blockSize: Int = the.foldLeft(1)((i,s) => i+s.blockSize) + els.foldLeft(0)((i,s) => i+s.blockSize)
}
