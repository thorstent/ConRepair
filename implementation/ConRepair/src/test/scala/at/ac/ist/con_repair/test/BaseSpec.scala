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

package at.ac.ist.con_repair.test

import org.scalatest.{GivenWhenThen, FeatureSpec}
import at.ac.ist.con_repair.helpers.{StatementOrder, IO}
import at.ac.ist.con_repair.parsing.Parsing

abstract class BaseSpec  extends FeatureSpec with GivenWhenThen {
  /**
   * The directory where the tests are located
   */
  val testSuite:String
  implicit val debugOutDir = null

  protected def getFile(file:String):String = {
    val in = this.getClass.getClassLoader.getResourceAsStream("tests/" + testSuite + "/" + file)
    IO.readToString(in)
  }

  protected def getStatementOrder(file:String):StatementOrder = {
    val org = getFile(file)
    val po = Parsing.parse(org)
    new StatementOrder(po, org)
  }
}
