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

import at.ac.ist.con_repair.helpers.{Expressions, VariableAnalysisResult, SomeVars}
import org.sosy_lab.cpachecker.cfa.ast.c._

class While(val condition: CExpression, val loop: List[Structure]) extends Structure{
  loop.foreach(_.parent = this)
  val isNondet = Expressions.isNondet(condition)

  val number: Id = Structure.getId(condition.getFileLocation, this)

  def usedVariables = {
    loop.map(_.usedVariables).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l) ++
      (if (isNondet) SomeVars(Set()) else SomeVars(Expressions.getUsedVariables(condition)))
  }

  def changedVariables = {
    loop.map(_.changedVariables).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l)
  }

  def usedVariablesInitial = {
    loop.map(_.usedVariablesInitial).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l) ++
      (if (isNondet) SomeVars(Set()) else SomeVars(Expressions.getUsedVariables(condition)))
  }

  def changedVariablesInitial = {
    loop.map(_.changedVariablesInitial).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l)
  }

  def getNegCondition:CUnaryExpression = Expressions.makeUnaryOperation(condition, CUnaryExpression.UnaryOperator.NOT)

  def transform(transformer: List[Structure] => List[Structure]) : While = {
    val l = transformer(loop.map(_.transform(transformer)))
    val w = new While(condition, l)
    copyValues(w)
    w
  }

  def processAllStructures(processor:(Structure,List[Structure])=>Boolean): Unit = {
    if (processor(this,loop)){
      loop.foreach(_.processAllStructures(processor))
    }
  }

  def blockSize: Int = loop.foldLeft(1)((i,s) => i+s.blockSize)


}
