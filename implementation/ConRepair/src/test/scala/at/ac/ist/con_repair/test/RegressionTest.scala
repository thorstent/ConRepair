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
import java.io.{FilenameFilter, File}
import at.ac.ist.con_repair.helpers.Statistic
import at.ac.ist.con_repair.Main

class RegressionTest extends FeatureSpec with GivenWhenThen {
  feature("Regression Test") {
    scenario("examples") {
      val folder = new File("../../examples")
      assert(folder.exists(), "Examples folder not found")

      for (e <- folder.listFiles(new FilenameFilter {
        def accept(p1: File, p2: String): Boolean = p2.endsWith(".c")
      })) {
        val statFile = e.getAbsolutePath.replace(".c", ".statistic.txt")
        if (new File(statFile).exists()) {
          Given(e.getName)
          // we found a file to test
          val oldStatistic = Statistic.readFromFile(statFile)
          implicit val debugOutDir = None
          implicit val statistic = new Statistic()
          Main.processFileRaw(e) match {
            case None => assert(false, "Not processed")
            case Some(_) => assert(statistic.sameSignature(oldStatistic), "Statistics do not match")
          }
        }
      }
      }
    }

}
