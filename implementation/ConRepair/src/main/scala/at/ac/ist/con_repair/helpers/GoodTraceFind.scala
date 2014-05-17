/*
 * Copyright 2013-2014, IST Austria
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

import at.ac.ist.con_repair.model_checker.CtexStmt
import at.ac.ist.con_repair.structures.{ZeroId, AssertStatement, Id}

// CLEANUP: This class should somehow tell what it is, not just by implicit usage
/**
 * The class holds information about information found by good traces (reordering or black edge)
 * @param stmt1 First statement
 * @param stmt2 Second statement
 * @param foundby What assertion we found this from. ZeroId to indicate it was found by the deadlock analysis step
 */
class GoodTraceFind(val stmt1:CtexStmt, val stmt2:CtexStmt, val foundby:Id) {
  override def equals(o:Any) = {
    o match {
      case that:GoodTraceFind => this.stmt1 == that.stmt1 && this.stmt2 == that.stmt2
    }
  }
  override def hashCode() = this.stmt1.hashCode() ^ this.stmt2.hashCode()
}

object GoodTraceFind {
  /**
   * Maps an edge to a GoodTraceFind object
   * @param origin The assertion we got this info from
   * @param s the orginal statement
   * @return The GoodTraceFind
   */
  def mapBlack(origin:Option[AssertStatement])(s:(CtexStmt,CtexStmt)) = {
    origin match {case Some(a) => new GoodTraceFind(s._1, s._2, a.number) case None => new GoodTraceFind(s._1, s._2, ZeroId) }
  }
}