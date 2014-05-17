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
import org.sosy_lab.cpachecker.cfa.ast.c.CFileLocation
import at.ac.ist.con_repair.printing.{CPrinter, ShortPrinter, Printer}
import at.ac.ist.con_repair.model_checker.CtexStmt

object Structure {
  private def getParent(ctex:(Structure,List[FunctionCallStatement])):(Structure,List[FunctionCallStatement]) = {
    ctex match {
      case (s,cf) =>
        if (s.isInstanceOf[Function] && cf.nonEmpty)
          (cf.head, cf.tail)
        else
          (s.parent, cf)
    }
  }


  def findCommandParent(s1:(Structure,List[FunctionCallStatement]), s2:(Structure,List[FunctionCallStatement])):Id = {
    var s1i = s1
    var parents:List[Structure] = List.empty
    while (s1i._1.number != ZeroId)
    {
      parents ::= s1i._1
      s1i = getParent(s1i)
    }
    parents = parents.filter(s => !s.isInstanceOf[Statement])

    var s2i = s2
    while (s2i._1.number != ZeroId)
    {
      if (parents.contains(s2i._1))
        return s2i._1.number
      s2i = getParent(s2i)
    }
    ErrorId
  }

  /**
   * Finds the first non-synthetic parent
   * @param ctex The thing to find the parent from
   * @return The argument if it is non-synthetic
   */
  def findNonSynthetic(ctex:CtexStmt) : Structure = {
    var c:(Structure, List[FunctionCallStatement]) = (ctex.stmt,ctex.calledFrom)
    while (c._1.synthetic) {
      c = getParent(c)
    }
    c._1
  }

  // this function makes sure s1 and s2 are at the same level
  def getSameLevel(s1:CtexStmt, s2:CtexStmt):Option[(Structure, Structure)] = {
    getSameLevel((s1.stmt,s1.calledFrom),(s2.stmt,s2.calledFrom)) match {
      case Some((e1,e2)) if e1==e2 => None
      case Some((e1,e2)) => Some((e1._1, e2._1))
      case None => None
    }

  }
  def getSameLevel(s1:(Structure,List[FunctionCallStatement]), s2:(Structure,List[FunctionCallStatement])):Option[((Structure,List[FunctionCallStatement]),(Structure,List[FunctionCallStatement]))] = {
    val parent = findCommandParent(s1,s2)
    if (parent == ErrorId) return None
    var s1i = s1
    var s2i = s2
    while (getParent(s1i)._1.number != parent)
    {
      s1i = getParent(s1i)
    }
    while (getParent(s2i)._1.number != parent)
    {
      s2i = getParent(s2i)
    }
    // in case of if we have two basic blocks
    s1i._1.parent match {
      case i:If if i.the.contains(s1i._1) && i.els.contains(s2i._1) || i.els.contains(s1i._1) && i.the.contains(s2i._1) => Some((getParent(s1i),getParent(s2i)))
      case _ => Some((s1i, s2i))
    }
  }

  def isSameLevel(s1:Structure, s2:Structure):Boolean = {
    s1.parent.number == s2.parent.number
  }

  def getId(location:CFileLocation, structure:Structure):Id = {
    if (Expressions.isSyntheticLocation(location)) {
      val (l,a) = Expressions.getSyntheticLocation(location)
      new Id(l,a, structure.toString)
    } else
      new Id(location.getStartingLineNumber, command=structure.toString)
  }
}

class Id(val line:Int, val appendix:Char = '\0', command :String) {
  override def toString = line.toString + (if (appendix!='\0') appendix.toString else "") + ": " + command

  def toId = line.toString + (if (appendix!='\0') appendix.toString else "")

  override def equals(other:Any):Boolean = {
    other match {
      case o: Id => o.line == this.line && o.appendix == this.appendix
      case _ => false
    }
  }

  override def hashCode = line.hashCode() ^ appendix.hashCode()
}

object ErrorId extends Id(-1,command="error") {
  override def toString = "error"
}

object ZeroId extends Id(0,command="program") {
}

abstract class Structure {
  /**
   * means that this structure was created artificially, not part of the original program
   */
  var synthetic = false

  val number:Id

  /**
   * Get variables used by the expression
   * @return Returns a set of variables
   */
  def usedVariables:VariableAnalysisResult
  def changedVariables:VariableAnalysisResult

  /**
   * This function is called by the initial analysis that tests if statements can be reordered sequentially
   * @return A list of variables used by the statement
   */
  def usedVariablesInitial:VariableAnalysisResult

  /**
   * This function is called by the initial analysis that tests if statements can be reordered sequentially
   * @return A list of variables changed by the statement
   */
  def changedVariablesInitial:VariableAnalysisResult

  var parent:Structure = null

  def blockSize : Int

  val childrenSortable = true

  def sortable:Boolean = {
    if (synthetic) false
    else
      parent.childrenSortable
  }

  protected def copyValues(newS: Structure) = {
    newS.synthetic = synthetic

  }

  // gets the name of the function this statement is in
  def functionLevel:Function = {
    var str = this
    while (true) {
      str match {
        case function: Function => return function
        case _ => str = str.parent
      }
    }
    null // will be never reached
  }

  def getFunctionName:String = {
    functionLevel.getName
  }

  def getThreadName:String = {
    var str = this
    while (true) {
      str match {
        case function: Function => return function.getThreadName
        case _ => str = str.parent
      }
    }
    null // will be never reached
  }

  def toLongString : String = {
    (new CPrinter).print(this)
  }

  override def toString : String = {
    (new ShortPrinter).print(this)
  }

  // the processor returns if the structure processing should continue
  def processAllStructures(processor:(Structure,List[Structure])=>Boolean):Unit

  // this processes all structures, but only one at a time
  def processAllStructuresByOne(processor:(Structure)=>Boolean):Unit = {
    def newProcessor(str:Structure,l:List[Structure]):Boolean = {
      for (s<-l)
        if (!processor(s)) return false
      true
    }

    if (processor(this))
      this.processAllStructures(newProcessor)
  }

  def transform(transformer: List[Structure] => List[Structure]) : Structure

  def programLevel:Program = {
    var str = this
    while (true) {
      str match {
        case program: Program => return program
        case _ => str = str.parent
      }
    }
    null // will be never reached
  }

  override def equals(other:Any):Boolean = {
    other match {
      case o: Structure => this.number.equals(o.number)
      case _ => false
    }
  }

  override def hashCode() = this.number.hashCode()
}
