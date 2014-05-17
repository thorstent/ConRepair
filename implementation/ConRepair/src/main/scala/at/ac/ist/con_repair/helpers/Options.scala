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

import java.io.File
import java.lang.management.ManagementFactory
import javax.management.ObjectName

class Options {
  import Options.Mode._

  var in:File = null
  var cache:Boolean = false
  var renew = false

  /**
   * Put in the middle of the outputted files to distinguish output from various modes
   */
  var mode:Mode = Mixed

  def fileInfix = {
    val fromMode = mode match {
      case BadOnly => ".badOnly"
      case Mixed => ".mixed"
      case Combined | CombinedS => ""
      case Verify => ".verify"
    }
    fromMode
  }

  var threads:Int = Options.defaultThreads

  val is64bit = (System.getProperty("os.arch").indexOf("64") != -1)

  var placeAtomicSections = false
}

object Options {
  object Mode extends Enumeration {
    type Mode = Value
    val Verify, BadOnly, Mixed, Combined, CombinedS = Value
  }
  import Mode._

  val defaultThreads = {
    // calculate how many threads to produce
    val cpus = Runtime.getRuntime().availableProcessors()
    // physical memory
    val mBeanServer = ManagementFactory.getPlatformMBeanServer()
    val mem = mBeanServer.getAttribute(new ObjectName("java.lang","type","OperatingSystem"), "TotalPhysicalMemorySize").asInstanceOf[Long]
    val memCores = (mem / (2l*1000l*1000l*1000l)).toInt // 2 gb are needed for each z3
    Math.max(1,Math.min(cpus, memCores))
  }

  private val parser = new scopt.OptionParser[Options]("ConRepair") {
    head("ConRepair", "1.0")
    opt[Unit]('c', "cache") action { (x, c) =>
      c.cache = true; c } text("Caches CBMC results between invocations")
    opt[Unit]('r', "renew") action { (x, c) =>
      c.renew = true; c } text("Only process files that have already been processed")
    opt[String]('m', "mode") action { (x, c) =>
      if (x=="verify") {c.mode = Verify}
      else if (x=="badOnly") {c.mode = BadOnly} else if (x=="combined") {c.mode = Combined} else if (x=="semaphore") {c.mode = CombinedS}; c} validate { x =>
      if (x=="mixed" || x=="verify" || x=="badOnly" || x=="combined" || x=="semaphore") success else failure("mode can be mixed | badOnly | verify | combined | semaphore")
      } text("Mode of processing the file (can be mixed | badOnly | verify | combined | semaphore)")
    opt[Int]('t', "thread") action { (x, c) =>
      c.threads = x; c } text(s"How many threads to start in parallel (default on this machine: $defaultThreads)")
    opt[Unit]('a', "atomicsections") action { (x, c) =>
      c.placeAtomicSections = true; c } text("bad trace analysis: shorted backtracking by placing atomic sections")
    help("help") text("prints this usage text")
    arg[File]("input") required() action { (x, c) =>
      c.in = x; c } text("File or directory to process")
    note("Caching will result in wrong statistics because the cached calls take less time.")
    note("If you specify a directory all *.c files in the directory will be processed.")
    note("mixed mode is the default and results in best results. " +
      "badOnly skips learning from good traces. combined will generate a statistic for badOnly and mixed. " +
      "verify is a special mode that only checks the program and outputs one bad trace.")
  }

  def parse(args:Array[String], options:Options):Boolean = {
    parser.parse(args, options).nonEmpty
  }
}
