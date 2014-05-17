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

import at.ac.ist.con_repair.helpers.{VariableAnalysisResult, SomeVars}
import org.sosy_lab.cpachecker.cfa.ast.c.{CParameterDeclaration, CIdExpression, CVariableDeclaration, CFunctionDeclaration}

/**
 * Represents a function
 * @param name Name of the function
 * @param commands A list of commands in the function
 * @param declaration The declaration of the function
 * @param localVariables Local variable declarations
 * @param async True if the function represents the entry point to a new thread
 * @param returnVariable Variable that should be returned in the end
 * @param childrenSortable If the children of this function can be reordered
 */
class Function(val name: String, val commands: List[Structure], val declaration: CFunctionDeclaration,
               val localVariables:Set[CVariableDeclaration], val async:Boolean, val returnVariable:Option[CIdExpression],
               override val childrenSortable:Boolean = true) extends Structure {
  commands.foreach(_.parent = this)
  if (async && returnVariable.nonEmpty) throw new IllegalArgumentException("Async function must not return anything")

  val number:Id = Structure.getId(declaration.getFileLocation, this)

  val parameters:List[CParameterDeclaration] = {
    import scala.collection.JavaConversions._
    declaration.getType.getParameters.toList
  }

  def usedVariables = {
    commands.map(_.usedVariables).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l)
  }

  def changedVariables = {
    commands.map(_.changedVariables).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l)
  }

  def usedVariablesInitial = {
    commands.map(_.usedVariablesInitial).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l)
  }

  def changedVariablesInitial = {
    commands.map(_.changedVariablesInitial).foldLeft(SomeVars(Set.empty):VariableAnalysisResult)((s,l)=>s++l)
  }

  def transform(transformer: List[Structure] => List[Structure]) : Function = {
    val cmds = transformer(commands.map(_.transform(transformer)))
    val f = new Function(name, cmds, declaration, localVariables, async, returnVariable, childrenSortable)
    copyValues(f)
    f
  }

  def getName = name

  def processAllStructures(processor:(Structure,List[Structure])=>Boolean): Unit = {
    if (processor(this,commands))
      commands.foreach(_.processAllStructures(processor))
  }

  def blockSize: Int = commands.foldLeft(1)((i,s) => i+s.blockSize)
}
