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

import org.sosy_lab.cpachecker.cfa.ast.c.{CFileLocation, CStatement}
import at.ac.ist.con_repair.helpers.{Expressions, VariableAnalysisResult}

object SectionBlock {
  private var no = -1000 // number of atomic sections

  def freshLocation() = {
    no = no - 1
    Expressions.syntheticLocation(no, 'a')
  }
}

/**
 * Represents a block of not reorderable statements (atomic or not)
 * @param location Location of the begin command
 * @param stmts The statements
 * @param atomic If they should be atomic
 */
class SectionBlock(location:CFileLocation, val stmts:List[Structure], val atomic:Boolean) extends Structure {
  stmts.foreach(_.parent = this)

  def this(orgStmt:CStatement, stmts:List[Structure], atomic:Boolean) {
    this(orgStmt.getFileLocation, stmts, atomic)
  }

  def this(stmts:List[Structure], atomic:Boolean) {
    this(SectionBlock.freshLocation(), stmts, atomic)
  }

  val number:Id = Structure.getId(location, this)

  override val childrenSortable = false

  override def usedVariables : VariableAnalysisResult = {
    stmts.foldLeft (VariableAnalysisResult.empty) ((v,s) => s.usedVariables ++ v)
  }

  override def changedVariables : VariableAnalysisResult = {
    stmts.foldLeft (VariableAnalysisResult.empty) ((v,s) => s.changedVariables ++ v)
  }

  override def usedVariablesInitial : VariableAnalysisResult = {
    stmts.foldLeft (VariableAnalysisResult.empty) ((v,s) => s.usedVariablesInitial ++ v)
  }

  override def changedVariablesInitial : VariableAnalysisResult = {
    stmts.foldLeft (VariableAnalysisResult.empty) ((v,s) => s.changedVariablesInitial ++ v)
  }

  override def transform(transformer: List[Structure] => List[Structure]) : SectionBlock = {
    val cmds = transformer(stmts.map(_.transform(transformer)))
    val n = new SectionBlock(location, cmds, atomic)
    copyValues(n)
    n
  }

  def processAllStructures(processor:(Structure,List[Structure])=>Boolean): Unit = {
    if (processor(this,stmts))
      stmts.foreach(_.processAllStructures(processor))
  }

  def blockSize: Int = stmts.foldLeft(1)((i,s) => i+s.blockSize)

}