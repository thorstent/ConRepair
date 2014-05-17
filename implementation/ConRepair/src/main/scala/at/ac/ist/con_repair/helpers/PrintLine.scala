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

/**
 * Class that reorders
 */
sealed abstract class PrintLine {

}

case class FunctionBegin(fun:Function) extends PrintLine
case class FunctionEnd(fun:Function) extends PrintLine
case class StmtLine(stmt: Statement, assertionFailed:Boolean = false) extends PrintLine

/**
 * The statement depends on the assigned variable value
 * @param stmtTrue The statement if the value is true
 * @param stmtFalse The statement if the value is false
 */
case class Decision(stmtTrue: Statement, stmtFalse: Statement) extends PrintLine
