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

package at.ac.ist.con_repair.model_checker.cbmc

import at.ac.ist.con_repair.printing.endpoints.EndpointBase

object CBMCEndpoint extends EndpointBase {
  val head: String = "unsigned int nondet_int();\nint __assume_dummy = 0;"

  val nondet:String = "nondet_int()"

  val atomicBegin = "__CPROVER_atomic_begin();"
  val atomicEnd = "__CPROVER_atomic_end();"

  def printAssume(condition: String): String = {
    "__CPROVER_assume(" + condition + "); __assume_dummy=0" // needed because CBMC does not note assumption locations
  }

  def printAssert(condition: String): String = {
    "assert(" + condition + ")"
  }

  def printAsyncCall(functionCall: String): String = {
    "__CPROVER_ASYNC_1: " + functionCall
  }
}
