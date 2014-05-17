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

import scala.language.implicitConversions
import at.ac.ist.con_repair.model_checker.CtexStmt

object ListExtensions {
  implicit class RichList[A](l: List[A]) {
    /**
     * Folds adjacent pairs
     * @param z initial result
     * @param f function that turns a pair into a new result
     * @tparam B result type
     * @return the final result
     */
    def foldLeftPair[B](z: B)(f: (B, A, A) => B): B = {
      var acc = z
      var these = l
      while (!these.isEmpty && !these.tail.isEmpty) {
        acc = f(acc, these.head, these.tail.head)
        these = these.tail
      }
      acc
    }

    /**
     * Folds by presenting any possible combination to the function (under the constraint that
     * the first element is always before the second in the list)
     * @param z initial result
     * @param f function that turns a pair into a new result
     * @tparam B result type
     * @return the final result
     */
    def foldLeftAnyPair[B](z: B)(f: (B, A, A) => B): B = {
      var acc = z
      var l1 = l
      while (!l1.isEmpty) {
        var l2 = l1.tail
        while (!l2.isEmpty) {
          acc = f(acc, l1.head, l2.head)
          l2 = l2.tail
        }
        l1 = l1.tail
      }
      acc
    }
  }

  implicit class TraceList(l: List[CtexStmt]) {
    def filterOutMain = l.filterNot(_.threadId == 0)
  }
}
