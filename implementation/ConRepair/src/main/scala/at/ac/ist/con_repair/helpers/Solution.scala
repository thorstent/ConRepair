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

object SolutionType extends Enumeration {
  type SolutionType = Value
  val MoveDown, MoveUp, AtomicSection, Initial = Value

  def toString(v:Value):String = {
    v match {
      case MoveDown|MoveUp => "Reorder"
      case _ => v.toString
    }
  }
}
import SolutionType._
import at.ac.ist.con_repair.structures.Structure


/**
 * A solution is a class representing one possible solution to fix the program
 * @param so The reordered program that is the possible solution
 * @param roundId The number of the bad round when this solution was discovered
 * @param parentSolution The parent of this solution
 * @param solutionType The type of the solution.
 * @param originalCost The cost of this reordering
 * @param swapped The original swapping (just for printing purposes)
 * @param originalNumber The original number (so to map the solution to the graph)
 */
class Solution(val so:StatementOrder, val roundId:Int, val parentSolution:Option[Solution], val solutionType:SolutionType, val originalCost:Int, val swapped:Option[(Structure,Structure)], val originalNumber:Int) extends Ordered[Solution]{

  override def toString = {
    swapped match {
      case None => "Root"
      case Some((s1,s2)) => s"${SolutionType.toString(solutionType)}(${s1.number} ${if (SolutionType.MoveUp==solutionType) "<--" else "-->"} ${s2.number})"
    }
  }

  def toArray:Array[String] = {
    swapped match {
      case None => Array("Root")
      case Some((s1,s2)) => Array((roundId,originalNumber).toString, ":", solutionType.toString, cost.toString, "-", s1.number.toString, " -> ", s2.number.toString)
    }
  }

  val cost = (roundId, if (solutionType==AtomicSection) originalCost+15 else originalCost)

  /**
   * Solutions are equal if they have the same solutionType and swapping
   */
  override def equals(o:Any) = {
    o match {
      case that:Solution => this.solutionType == that.solutionType && this.swapped == that.swapped && this.roundId==that.roundId
    }
  }

  override def hashCode() = {
    solutionType.hashCode() ^ swapped.hashCode() ^ roundId.hashCode()
  }

  def compare(that: Solution): Int = {
    import scala.math.Ordering.Implicits._
    if (that.cost==this.cost) 0
    else if (that.cost<this.cost) -1 else 1  // the lower the cost the better
  }
}
