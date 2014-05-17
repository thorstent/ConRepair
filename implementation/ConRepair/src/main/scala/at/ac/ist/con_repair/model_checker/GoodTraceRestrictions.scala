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

package at.ac.ist.con_repair.model_checker

import at.ac.ist.con_repair.structures.Id
import scala.collection.GenSet
import at.ac.ist.con_repair.helpers._

/**
 * The class restricts the good traces that are possible
 * @param reachId Makes sure at least this id is reached
 * @param readFrom Indicate where a certain variable is known to be read from. It should then be read from a different location
 *                 (location where it was read from originally, name of the variable, location where it is used)
 * @param finishTrace If false the trace will not be finished, meaning we get a partial trace
 */
class GoodTraceRestrictions(val reachId: Id, readFrom:GenSet[GenSet[(Id, Variable, Id)]], val finishTrace:Boolean) {
  val readFromList = readFrom.toList
}
