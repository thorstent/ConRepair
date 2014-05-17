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

import org.sosy_lab.cpachecker.cfa.ast.c.CDeclaration

class Program(val functions:List[Function], val originalProgram:String, val globalDeclarations: List[CDeclaration]) extends Structure {
  functions.foreach(_.parent = this)
  val functionMap = functions.map(f=>(f.getName,f)).toMap
  private var lockNames:Set[String] = Set.empty

  val number:Id = ZeroId

  val threads = functions.filter(_.async).map(_.name)

  def getLockNames = lockNames

  override def sortable = true

  def addLockName(name:String) = {
    lockNames += name
  }

  def usedVariables = throw new Exception("not supported")

  def changedVariables = throw new Exception("not supported")

  def usedVariablesInitial = throw new Exception("not supported")

  def changedVariablesInitial = throw new Exception("not supported")

  def transform(transformer: List[Structure] => List[Structure]) = {
    new Program(functions.map(_.transform(transformer)), originalProgram, globalDeclarations)
  }

  def processAllStructures(processor:(Structure,List[Structure]) => Boolean): Unit = {
    //processor(functions)
    functions.foreach(_.processAllStructures(processor))
  }

  def blockSize: Int = functions.foldLeft(1)((i,s) => i+s.blockSize)

  def addDeclaration(decl:CDeclaration) = {
    new Program(functions, originalProgram, decl::globalDeclarations)
  }
}
