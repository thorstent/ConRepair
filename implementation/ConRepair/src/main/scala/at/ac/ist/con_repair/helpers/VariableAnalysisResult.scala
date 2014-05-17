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

package at.ac.ist.con_repair.helpers

sealed abstract class VariableAnalysisResult extends Set[Variable] {


  def ++(operand:VariableAnalysisResult):VariableAnalysisResult = {
    this match {
      case SomeVars(v1) =>
        operand match {
          case SomeVars(v2) =>
              SomeVars(v1++v2)
        }
    }
  }

  def intersectionEmpty(operand:VariableAnalysisResult, threadChange:Boolean = false):Boolean = {
    this match {
      case SomeVars(v1) =>
        operand match {
          case SomeVars(v2) =>
            val vars1 = (if (threadChange) v1.filter(_._2) else v1) map {_._1}
            val vars2 = (if (threadChange) v2.filter(_._2) else v2) map {_._1}
            vars1.intersect(vars2) == Set.empty
        }
    }
  }

  def contains(elem:Variable, threadChange:Boolean):Boolean
}

case class SomeVars (variables: Set[Variable]) extends VariableAnalysisResult {

  def contains(elem: Variable): Boolean = variables.contains(elem)
  def contains(elem:Variable, threadChange:Boolean):Boolean = {
    if (!elem._2 && threadChange) return false
    contains(elem)
  }

  def +(elem: Variable): Set[Variable] = variables + elem

  def -(elem: Variable): Set[Variable] = variables - elem

  def iterator: Iterator[Variable] = variables.iterator
}

object VariableAnalysisResult {
  def empty:VariableAnalysisResult = SomeVars(Set())
}
