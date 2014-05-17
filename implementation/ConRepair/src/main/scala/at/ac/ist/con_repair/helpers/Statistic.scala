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

import java.io._
import java.text.DecimalFormat
import at.ac.ist.con_repair.helpers.IO.Column
import scala.collection.mutable
import at.ac.ist.con_repair.helpers.Statistic.{VerificationIteration, Iteration}
import at.ac.ist.con_repair.Main
import java.util.concurrent.atomic.AtomicInteger
import at.ac.ist.con_repair.structures.Id

object Statistic {

  /**
   * One iteration of the  algorithm
   * @param analysisTime The time our graph algorithms took to analyse the round
   * @param proverTime The time spend in the prover
   * @param solution The solution that was applied (None if not applicable)
   */
  class Iteration(val analysisTime:Long, val proverTime:Long, val solution:Option[Solution], val triggeredAssertion:Option[Id]) {
    val totalTime = analysisTime+proverTime
    if (totalTime<proverTime) throw new IllegalArgumentException("totalTime must be >= inProver")

    def percentage:String = if (totalTime>0) f"${proverTime.toFloat / totalTime*100}%5.2f%%" else ""

    def +(operand:Iteration):Iteration = {
      new Iteration(this.analysisTime + operand.analysisTime, this.proverTime + operand.proverTime, this.solution, this.triggeredAssertion)
    }

    def +(operand:Option[Iteration]):Iteration = {
      operand match {
        case None => this
        case Some(o) => this + o
      }
    }

    override def toString = {
      f"${totalTime.toFloat / 1000}%5.2fs ($percentage)¹"
    }

  }

  /**
   * The (final) verification step
   * @param time Time it took (only prover here)
   */
  class VerificationIteration(time:Long, solution:Option[Solution]) extends Iteration(0, time, solution, None) {
    def +(operand:VerificationIteration):VerificationIteration = {
      new VerificationIteration(this.proverTime + operand.proverTime, this.solution)
    }
  }

  /**
   * Reads the invariant values from the statistic file
   * @param filename the file to read from
   * @return the partial statistic
   */
  def readFromFile(filename:String):Statistic = {
    val s = new Statistic
    val inStream = new FileInputStream(filename)
    val in = new BufferedReader(new InputStreamReader(inStream))
    in.readLine() // line just says Summary:
    val line1 = in.readLine()
    s.goodRounds.set(readInt(line1))
    val line2 = in.readLine()
    s.badRounds.set(readInt(line2))
    in.close()
    inStream.close()
    s
  }

  private def readInt(line:String): Int = {
    line.substring(0,line.indexOf(' ')).toInt
  }
}

/**
 * Statistic of this run
 */
class Statistic {

  var goodRounds = new AtomicInteger(0)
  val badRounds = new AtomicInteger(0)

  val goodIterations=new mutable.ArrayBuffer[Iteration]() with mutable.SynchronizedBuffer[Iteration]
  val badIterations=new mutable.ArrayBuffer[Iteration]() with mutable.SynchronizedBuffer[Iteration]

  /**
   * Did we acutally find a solution?
   */
  var solutionFound = false

  /**
   * This is a semaphore run
   */
  var semaphore = false


  /**
   * Time taken by the good round (will be estimated if 0)
   */
  var goodTime:Long = 0

  def sumIterations(list:Iterable[Iteration]):Iteration = {
    list.foldLeft(new Iteration(0,0,None, None)) ((r,i) => r+i)
  }

  def clear(): Unit = {
    goodRounds.set(0)
    badRounds.set(0)
    goodIterations.clear()
    badIterations.clear()
    solutionFound = false
    semaphore = false
    goodTime = 0
  }

  /**
   * Compares those values that are invariant between runs
   */
  def sameSignature(o:Statistic) = {
    o.goodRounds.equals(this.goodRounds) && o.badRounds.equals(this.badRounds)
  }

  def writeToFile(filename:String): Unit = {
    val outStream = new FileOutputStream(filename)
    val out = new PrintStream(outStream)
    writeToFile(out)
    out.close()
    outStream.close()
  }

  def writeToFile(out:Appendable): Unit = {
    def printReorderings(i:Iteration):Unit = {
      def printParent(s:Solution):Unit = {
        if (s.solutionType==SolutionType.Initial) return
        printParent(s.parentSolution.get)
        out.append(s"$s\n")
      }
      i.solution match {
        case Some(x) if x.solutionType!=SolutionType.Initial=>
          out.append("Changes up to this iteration³:\n")
          printParent(x)
        case _ =>
      }
    }
    def printSeperator() = out.append("---------------------------------------------------\n")

    out.append("Summary:\n")
    out.append(goodIterations.size.toString)
    out.append(" # of good rounds\n")
    out.append(badIterations.count(!_.isInstanceOf[VerificationIteration]).toString)
    out.append(" # of bad rounds\n")
    val bad = sumIterations(badIterations)
    val goodRaw = sumIterations(goodIterations)
    val goodFactor: Float = goodRaw.proverTime.toFloat/(goodRaw.analysisTime + goodRaw.proverTime).toFloat
    val good = if (goodTime == 0)
      new Iteration(goodRaw.analysisTime/Main.options.threads, goodRaw.proverTime/Main.options.threads, None, None)
    else {
      new Iteration(goodTime - (goodTime*goodFactor).toInt, (goodTime*goodFactor).toInt, None, None)
    }
    val total = bad+good
    out.append(total.toString)
    out.append(" total\n")

    if (goodIterations.nonEmpty && badIterations.nonEmpty) {
      if (goodTime == 0)
        out.append(good.toString)
      else {
        out.append(f"${goodTime.toFloat / 1000}%5.2fs (${good.percentage})¹")
      }
      out.append(" good trace learning²\n")
      out.append(bad.toString)
      out.append(" bad trace program repair\n")
    }
    out.append("\n")
    if (Main.cancel)
      out.append("Manualy cancelled by the user.\n")
    else if (solutionFound)
      out.append("Solution found, fixed the program.\n")
    else
      out.append("No solution found, unable to fix the program.\n")
    printSeperator()
    if (goodIterations.nonEmpty) {
      out.append("\nGood trace learning:\n")
      out.append("- - - - - - - - - - -\n")
      var n = 0
      for (g <- goodIterations) {
        n = n + 1
        out.append(f"Iteration $n%2d: $g\n")
      }
    }
    printSeperator()
    if (badIterations.nonEmpty) {
      if (semaphore) out.append("\nPlacing semaphores:\n") else out.append("\nBad trace program repair:\n")
      out.append("- - - - - - - - - - - - - \n")
      var n = 0
      var lastVerification = false // the last step was the verification
      var deadlockMode = false
      for (b <- badIterations) {
        b match {
          case _:VerificationIteration =>
            out.append(s"Verification: $b\n")
            printReorderings(b)
            lastVerification = true
          case _ =>
            if (lastVerification && !deadlockMode) {
              out.append("--Switched to non-deadlock mode--\n")
              deadlockMode = true
            }
            lastVerification = false
            n = n + 1
            out.append(f"Iteration $n%2d: $b\n")
            b.triggeredAssertion match {
              case Some(a) => out.append(s"Assertion triggered: $a\n")
              case None =>
            }
            printReorderings(b)
        }
        printSeperator()
      }
    }
  }

  def column:Iterable[String] = {
    val sb = new java.lang.StringBuilder
    writeToFile(sb)
    sb.toString().split("\n")
  }

  def writeLegend(out:Appendable): Unit = {
    out.append("\n\n¹ All times in seconds. In parenthesis the time spent in the external model-checker.\n")
    out.append("² The total time is less than the sum of individual iterations because of parallel execution of good iterations.\n")
    out.append("³ For each bad iteration all changes are printed from the beginning. The algorithm can backtrack, therefore\n" +
      "  the list is not necessarily incremental.")
  }
}
