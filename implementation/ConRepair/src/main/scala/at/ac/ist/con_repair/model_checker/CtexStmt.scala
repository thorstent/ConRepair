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

package at.ac.ist.con_repair.model_checker

import at.ac.ist.con_repair.structures.{FunctionCallStatement, Statement}
import at.ac.ist.con_repair.helpers.IO

object CtexStmt {

  def print(stmts:List[CtexStmt],p:Appendable) = {
    val threads =  stmts.map(t => (t.threadId, t.threadName)).sortBy(_._1).distinct.map(t => new IO.Column(t._2))
    for (t <- threads) {
      for (stmt <- stmts) {
        if (stmt.threadName == t.heading) {
          t.rows += "  " * stmt.calledFrom.length + (if (stmt.assertionFailure) "!!" else if (stmt.assumptionFailure) "##" else "") + stmt.stmt.number.toString
        } else {
          t.rows += ""
        }
      }
    }
    IO.writeTable(threads, p)
  }
}

/**
 * Because Scala uses non-reference equality we record the position in the
 * trace for equality checking
 */
class CtexStmt(val stmt:Statement, val assertionFailure:Boolean, val assumptionFailure:Boolean, val threadName:String, val threadId: Int, val calledFrom:List[FunctionCallStatement], val position:Int) {

  val primitive = !stmt.isInstanceOf[FunctionCallStatement]

  override def equals(other:Any):Boolean = {
    other match {
      case o: CtexStmt => this.stmt.equals(o.stmt) && this.position == o.position
      case _ => false
    }
  }

  override def hashCode() = stmt.hashCode() ^ position.hashCode()

  override def toString = {
    val sb = new StringBuilder()
    if (assertionFailure) sb.append("!!")
    if (assumptionFailure) sb.append("##")
    sb.append(threadName)
    sb.append(" - ")
    sb.append(calledFrom.reverse.map(_.functionCalledName).mkString(", "))
    sb.append(": ")
    sb.append(stmt.toString)
    sb.result()
  }
}
