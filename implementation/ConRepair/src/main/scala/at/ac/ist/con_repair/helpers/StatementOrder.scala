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

import at.ac.ist.con_repair.structures._
import at.ac.ist.con_repair.parsing.PostParser
import org.sosy_lab.cpachecker.cfa.ParseResult
import ListExtensions._
import at.ac.ist.con_repair.model_checker.CtexStmt
import scala.collection.GenSet
import scala.Some

object StatementOrder {
  // CLEANUP
  var sem_counter = 'a'

  private def getPartialOrder(program:Program) : PartialOrder[Id] = {
    var res = PartialOrder.empty[Id]

    def processLine(currentStruct:Structure, structures : List[Structure]):Boolean = {
      val bucket = currentStruct.number
      if (currentStruct.isInstanceOf[Function])
        res = res.addBucket(bucket, None)
      else
        res = res.addBucket(bucket, Some(currentStruct.parent.number))

      structures.foreach(s => res = res.addElement(s.number, bucket) )
      structures.foldLeftAnyPair (()) { (po,s1,s2) =>
        if (hasToHappenBefore(s1, s2))
          res = res.add(s1.number, s2.number)
      }

      true
    }

    program.processAllStructures(processLine)

    res.reduced
  }

  private def hasToHappenBefore(before: Structure, after: Structure): Boolean = {
    // some structures cannot be sorted
    if (!before.sortable || !after.sortable)
      return true
    val changedVariablesBefore: VariableAnalysisResult = before.changedVariablesInitial
    val changedVariablesAfter: VariableAnalysisResult = after.changedVariablesInitial
    val usedVariablesBefore: VariableAnalysisResult = before.usedVariablesInitial
    val usedVariablesAfter: VariableAnalysisResult = after.usedVariablesInitial
    // otherwise check if variables interefere
    if ((!changedVariablesBefore.intersectionEmpty(changedVariablesAfter)) ||
       (!changedVariablesBefore.intersectionEmpty(usedVariablesAfter)) ||
       (!usedVariablesBefore.intersectionEmpty(changedVariablesAfter))) {
      return true
    }
    false
  }

  def adjustFirmEdges(edges : GenSet[(CtexStmt,CtexStmt)]) : GenSet[(Id, Id)] = {
    edges.map { e => val (e1,e2) = e
      (Structure.findNonSynthetic(e1).number, Structure.findNonSynthetic(e2).number)
    }
  }
}

class StatementOrder private (val program: Program, val order:PartialOrder[Id]) {

  private def this(program: Program) = this(program, StatementOrder.getPartialOrder(program))

  def this(parseResult: ParseResult ,originalProgram:String) = this(PostParser.postParse(parseResult, originalProgram))

  /**
   * Tests if two statements are ordered due to their synthetic nature
   * @param stmtNo1 The first statement
   * @param stmtNo2 The second statement
   * @return true if the order is enforced
   */
  private def syntheticOrdered(stmtNo1 : Structure, stmtNo2 : Structure):Boolean = {
    if (stmtNo1.synthetic && stmtNo1.parent.isInstanceOf[If]) {
      if (stmtNo2.parent == stmtNo1.parent) return true
    }
    false
  }

  def isOrdered(stmtNo1 : Structure, stmtNo2 : Structure) = {
    if (stmtNo1.synthetic || stmtNo2.synthetic) false else
      order.isOrdered(stmtNo1.number, stmtNo2.number)
  }
  def isOrderedDirect(stmtNo1 : Structure, stmtNo2 : Structure) = {
    if (stmtNo1.synthetic || stmtNo2.synthetic) false else
      order.isOrderedDirect(stmtNo1.number, stmtNo2.number)
  }

  def printOrder(file:String) = {
    order.exportToDOT(file)
  }

  def integrateAtomicSection(e1: Structure, e2: Structure): Option[(PartialOrder[Id], Program)] = {
    var po = order
    var parentAS = false // parent is already an atomic section
    var functionCall = false // there is a function call inside the atomic section (this is not allowed)
    def filterOutAS(list:List[Structure]) = {
      if (list.contains(e1)) {
        assert (list.contains(e2))
        val parent = e1.parent
        var p = parent
        while (p != null) {
          if (p.isInstanceOf[SectionBlock] && p.asInstanceOf[SectionBlock].atomic)
            parentAS = true
          p = p.parent
        }
        var (prefix,t) = list.span(_ != e1)
        var (as,suffix) = t.span(_ != e2)
        if (suffix.isEmpty) {
          // e1 and e2 are in the wrong order
          list.span(_ != e2) match {
            case (p, t) => prefix = p
              t.span(_ != e1) match {
              case (a,s) => as = a; suffix = s
            }
          }
        }
        as = as :+ suffix.head // move the last element
        suffix = suffix.tail
        assert (prefix.length + as.length + suffix.length == list.length)
        // check if we got a function call in the proposed atomic section
        as.foreach(_.processAllStructuresByOne({ s =>
          val fc = s.isInstanceOf[FunctionCallStatement]
          if (fc) functionCall = fc
          !functionCall // continue if we did not yet find
        }))
        val newAs = new SectionBlock(as, atomic = true)
        newAs.parent = parent
        po = po.elementMerge(newAs.number, as.map(_.number))
        (prefix :+ newAs) ++ suffix
      } else
        list // unchanged because this was not the place where the element was found
    }
    val newProg = program.transform(filterOutAS)
    if (parentAS || functionCall)
      None // there is already an atomic section, so this is not a solution
    else
      Some((po, newProg))
  }

  def integrateSemaphore(eSet: Structure, eAssert: Structure): StatementOrder = {
    var po = order
    StatementOrder.sem_counter = (StatementOrder.sem_counter + 1).toChar
    val variable = "sem_" + eAssert.number.line + StatementOrder.sem_counter
    val decl = Expressions.makeDeclaration(variable, true)
    val wait = new AssumeStatement(Expressions.makeAssume(Expressions.makeName(variable), Expressions.syntheticLocation(eAssert.number.line,StatementOrder.sem_counter)))
    val assign = new Statement(Expressions.makeAssignment(Expressions.makeName(variable, true), Expressions.makeIntConst(1), Expressions.syntheticLocation(eSet.number.line,StatementOrder.sem_counter)))

    def filterSemaphore(list:List[Structure]) = {
      if (list.contains(eAssert)) {
        val parent = eAssert.parent
        val (prefix,suffix) = list.span(_ != eAssert)
        // insert element in between
        val result = (prefix :+ wait) ++ suffix
        wait.parent = parent
        po = po.addElement(wait.number, parent.number)
        po = po.add(wait.number, suffix(0).number)
        result
      } else if (list.contains(eSet)) {
        val parent = eSet.parent
        val (prefix,suffix) = list.span(_ != eSet)
        // insert element in between
        val result = (prefix :+ suffix(0) :+ assign) ++ suffix.tail
        assign.parent = parent
        po = po.addElement(assign.number, parent.number)
        po = po.add(suffix(0).number, assign.number)
        result
      } else
        list // unchanged because this was not the place where the element was found
    }
    val newProg = program.transform(filterSemaphore).addDeclaration(decl)
    new StatementOrder(newProg, po)
  }

  // this one doesn't change the statement order, but gives back modified clones
  private def integrateReordering(en1: Structure, en2: Structure): Option[PartialOrder[Id]] = {
    if (order.isOrdered(en2.number,en1.number)) None else {
      Some(order.add(en1.number,en2.number))
  }
  }

  // CLEANUP: This should use less parameters
  /**
   * This will integrate one constraint (red edge) into the given statement order
   * @param constraints The red edge to integrate
   * @param parent The parent solution (so the order of applied change becomes clear)
   * @param roundNumber The number of the current round
   * @param originalCost1 The cost metric for the red edge
   * @param originalNumber The number given to the output file
   * @param suggestions A list of suggestions from the good traces
   * @return It produces a solution that carries enough information to order the attempts
   */
  def integrate(constraints: collection.Set[(CtexStmt,CtexStmt)], parent:Solution, roundNumber:Int, originalCost1:Int, originalNumber:Int, suggestions:Set[(Structure,Structure)]): List[Solution] = {
    var originalCost = originalCost1
    /**
     *
     * @param order the function that can reorder lists
     * @param en1 the structure that we currently reorder in
     * @param resultList the resulting list of the changed block of code
     * @param reordered true if something was actually reordered
     * @return
     */
    def doOrder(order:List[Structure] => List[Structure], en1:Structure, resultList:Mut[List[Structure]], reordered:Mut[Boolean]): List[Structure] => List[Structure] = {
      def o(in:List[Structure]):List[Structure] = {
        if (in.contains(en1)) {
          val newO = order(in)
          reordered.value = in != newO
          resultList.value = newO
          newO
        } else
          in // do nothing because the element we try to reorder is not there
      }
      o
    }

    assert (constraints.size==1)
    val (e1,e2) = constraints.head
    if (e1.stmt.synthetic || e2.stmt.synthetic) return List() // with these we cannot deal
    Structure.getSameLevel(e1,e2) match {
      case Some((en1,en2)) if en1 != en2 && !(order.isOrdered(en1.number, en2.number)) => // ensures that the elements are not already in this order
        // first try reordering
        integrateReordering(en1, en2) match {
          case Some(o) =>
            // was this suggested before
            if (suggestions.contains((en1,en2))) originalCost -= 1000 // prefer this option
            val order1 = Mut[List[Structure]](List())
            val order2 = Mut[List[Structure]](List())
            val reordered = Mut[Boolean](false)
            val up = program.transform(doOrder(i => o.orderItemsUp(i, (s:Structure) => s.number), en1, order1, reordered))
            val down = program.transform(doOrder(i => o.orderItemsDown(i, (s:Structure) => s.number), en1, order2, reordered))
            val solUp = new Solution(new StatementOrder(up, o), roundNumber, Some(parent), SolutionType.MoveUp, originalCost, Some(en1,en2), originalNumber)
            val solDown = new Solution(new StatementOrder(down, o), roundNumber, Some(parent), SolutionType.MoveDown, originalCost, Some(en1,en2), originalNumber)
            val reorderings = if (!reordered.value) List()
            else
              if (order1 != order2) {
                List(solUp, solDown)
              } else List(solUp) // moving down is default
            // also try to make it an atomic section in case both reorderings are not good
            val as = integrateAtomicSection(en2, en1) match {
              case Some ((newOrder, newProg)) =>
                List(new Solution(new StatementOrder(newProg, newOrder), roundNumber, Some(parent), SolutionType.AtomicSection, originalCost+100, Some(en2,en1), originalNumber))
              case None => List()
            }
            reorderings ++ as
          case None =>
            integrateAtomicSection(en2, en1) match {
              case Some ((newOrder, newProg)) =>
                List(new Solution(new StatementOrder(newProg, newOrder), roundNumber, Some(parent), SolutionType.AtomicSection, originalCost, Some(en2,en1), originalNumber))
              case None => List()
            }
        }
      case _ => List() // no solution found
    }
  }

  def addBlackEdge(edge: (CtexStmt,CtexStmt)) : Option[(StatementOrder,Structure, Structure)] = {
    val (e1,e2) = edge
    if (e1.stmt.synthetic || e2.stmt.synthetic) None else {
      Structure.getSameLevel(e1,e2) match {
        case Some((en1,en2)) if en1.number.line<en2.number.line => Some(new StatementOrder(program, order.add(en1.number,en2.number)), en1, en2) // ensure line number is smaller, otherwise we would reorder the statements
          // this can happen because of a loop unrolling
        case None => assert(assertion = false);None // should never happen
        case _ => None
      }
    }
  }

}


