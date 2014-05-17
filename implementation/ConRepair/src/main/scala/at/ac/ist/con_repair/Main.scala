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

package at.ac.ist.con_repair

import at.ac.ist.con_repair.parsing.Parsing
import at.ac.ist.con_repair.helpers._
import at.ac.ist.con_repair.model_checker.cbmc.InvokeCBMC
import at.ac.ist.con_repair.model_checker.{GoodTraceRestrictions, CheckerResult, CtexStmt}
import at.ac.ist.con_repair.algorithm.{Generalisation, ProofObject}
import ListExtensions._
import scala.collection.{mutable, GenSet}
import at.ac.ist.con_repair.printing.CPrinter
import java.util.Date
import at.ac.ist.con_repair.structures.{Structure, ZeroId, AssertStatement, Id}
import at.ac.ist.con_repair.helpers.IO.Column
import scala.collection.mutable.ListBuffer
import at.ac.ist.con_repair.model_checker.CheckerResult.CheckerResult
import at.ac.ist.con_repair.model_checker.CheckerResult
import at.ac.ist.con_repair.helpers.Statistic.VerificationIteration
import java.io._
import scala.Some
import scala.Console

object Main {
  import at.ac.ist.con_repair.helpers.Options.Mode._
  val options:Options = new Options()

  private def setParallelism() {
    def setParallelismGlobally(numThreads: Int): Unit = {
      val parPkgObj = scala.collection.parallel.`package`
      val defaultTaskSupportField = parPkgObj.getClass.getDeclaredFields.find{
        _.getName == "defaultTaskSupport"
      }.get

      defaultTaskSupportField.setAccessible(true)
      defaultTaskSupportField.set(
        parPkgObj,
        new scala.collection.parallel.ForkJoinTaskSupport(
          new scala.concurrent.forkjoin.ForkJoinPool(numThreads)
        )
      )
    }

    try {
      setParallelismGlobally(options.threads)
    } catch {
      case _:Exception => Console.println("Cannot set concurrency level in this Java VM. This waring is harmless.")
    }
  }

  /**
   * If set to true, then we should cancel as soon as possible
   */
  @volatile var cancel = false

  def processAssertion(so:StatementOrder, a:Option[AssertStatement])(implicit debugOutDir: Option[File], statistic: Statistic):(Set[GoodTraceFind],Set[GoodTraceFind],Set[GoodTraceFind]) = {
    var rounds = 0
    var lastRes = CheckerResult.Failure
    val readFrom : mutable.Set[GenSet[(Id, Variable, Id)]] = mutable.Set()
    val be = Set.newBuilder[(CtexStmt,CtexStmt)]
    val sug = Set.newBuilder[(CtexStmt,CtexStmt)]
    val firm = Set.newBuilder[(CtexStmt,CtexStmt)]
    var finishTrace = true
    while(rounds < 10 && (lastRes!=CheckerResult.Success || finishTrace) && !cancel && !(a.isEmpty && rounds>0)) {
      rounds+=1
      val goodRound = a match { case Some(a) => s"${a.number.line.toString}.$rounds" case None => statistic.goodRounds.incrementAndGet().toString }
      System.out.println("Iteration " + goodRound + " started")
      val (res, trace, _, proverTime) = (new InvokeCBMC).invokeChecker(so, a match {case Some(a) => Some(new GoodTraceRestrictions(a.number, readFrom, finishTrace)) case None => Some(new GoodTraceRestrictions(ZeroId, Set(), finishTrace))},
        false, false)
      val startTime = new Date()
      lastRes = res
      res match {
        case CheckerResult.Failure =>
          debugOutDir match { case None =>  case Some(debugOutDir2) =>
            val fw = new FileWriter(new File(debugOutDir2, "A-" + goodRound + "-goodTrace.txt"))
            a match { case Some(a) => fw.write("Assertion: " + a.number.toString + "\n\n") case None => fw.write("Deadlock step\n\n")}
            CtexStmt.print(trace,fw)
            fw.close()
          }
          val proof = new ProofObject(trace, a)
          debugOutDir match { case None =>  case Some(debugOutDir2) =>
            proof.exportToDOT(new File(debugOutDir2, "A-" + goodRound + "-proofObject.dot"))
          }
          be ++= proof.black
          sug ++= proof.suggestedReorderings
          readFrom += proof.readFrom
          firm ++= proof.firm
        case CheckerResult.Success =>
          if (finishTrace) {
            finishTrace = false
            lastRes = CheckerResult.Failure
          }
          if (rounds == 0)
          a match {case Some(a) =>
            Console.println(s"Assertion either unreachable or never succeeds: ${a.number}")
          case None => Console.println("Could not find a single good trace through the program.") }
        case _ =>
      }
      System.out.println("Iteration " + goodRound + " finished")
      statistic.goodIterations += new Statistic.Iteration(new Date().getTime - startTime.getTime, proverTime, None, None)
    }
    (be.result().map(GoodTraceFind.mapBlack(a)), sug.result().map(GoodTraceFind.mapBlack(a)), firm.result().map(GoodTraceFind.mapBlack(a)))
  }

  /**
   *
   * @param so The program to begin with
   * @param debugOutDir A debug directory where additional information is printed
   * @param statistic A statistic that is kept while processing the file
   * @param semaphores Should we concern ourselves with semaphore placement
   * @return The program with the new constraints (black edges) and a suggestion what to switch
   */
  private def learnFromGoodTraces(so: StatementOrder, semaphores:Boolean = false)(implicit debugOutDir: Option[File], statistic: Statistic) : (StatementOrder,Set[(Structure,Structure)],Set[(Structure,Structure)],Set[(Structure,Structure)]) = {

    System.out.println("Learning from good traces phase ...")

    val startTime = new Date()

    // analyse where assertions can read from
    // find assertions
    val assertions = ListBuffer[Option[AssertStatement]](None)
    so.program.processAllStructuresByOne(s => {
      if (s.isInstanceOf[AssertStatement] && !s.getFunctionName.contains("deadlock") && s.asInstanceOf[AssertStatement].analyse) assertions+=Some(s.asInstanceOf[AssertStatement])
      true
    })

    def addTriple(x:(Set[GoodTraceFind],Set[GoodTraceFind],Set[GoodTraceFind]),y:(Set[GoodTraceFind],Set[GoodTraceFind],Set[GoodTraceFind])) = {
      val (x1,x2,x3) = x
      val (y1,y2,y3) = y
      (x1++y1, x2++y2, x3++y3)
    }

    val passertions = if (true) {
      assertions.par
    } else {
      Console.println("Running in non-parallel mode")
      assertions
    }

    val (blackEdges, suggestions, firm) = passertions.aggregate(Set():Set[GoodTraceFind],Set():Set[GoodTraceFind],Set():Set[GoodTraceFind])((x,a) => addTriple(processAssertion(so, a),x),addTriple)

    if (!options.cache) statistic.goodTime = (new Date()).getTime - startTime.getTime

    val blackEdgesList = blackEdges.toList.sortBy(_.foundby.line)
    val suggestionsEdgesList = suggestions.toList.sortBy(_.foundby.line)
    val firmList = firm.toList.sortBy(_.foundby.line)

    //CLEANUP: unduplicate

    val suggestionSet = Set.newBuilder[(Structure,Structure)]
    val rowsSUG = new mutable.ListBuffer[Array[String]]()
    for (g <- suggestionsEdgesList)
      Structure.getSameLevel(g.stmt1, g.stmt2) match {
        case Some((en1,en2)) if en1.number.line > en2.number.line => // > so we actually swap stuff
          rowsSUG += Array(if (g.foundby==ZeroId) "Deadlock-Round" else g.foundby.toString, ":", en1.getFunctionName + "()", "-", en1.number.toString, "-->", en2.number.toString)
          suggestionSet += ((en1,en2))
        case _ =>
      }

    // print out suggestions we discovered
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      val fw = new FileWriter(new File(debugOutDir2, "A-suggestions.txt"))
      if (rowsSUG.length>0) IO.writeTable(rowsSUG, fw, " ")
      fw.close()
    }

    val blackEdgesSet = Set.newBuilder[(Structure,Structure)]
    val rowsBE = new mutable.ListBuffer[Array[String]]()
    var s = so
    for (g <- blackEdgesList)
      s.addBlackEdge((g.stmt1, g.stmt2)) match {
        case None =>
        case Some((snew,en1,en2)) =>
          rowsBE += Array(if (g.foundby==ZeroId) "Deadlock-Round" else g.foundby.toString, ":", en1.getFunctionName + "()", "-", en1.number.toString, "-->", en2.number.toString)
          s = snew
          blackEdgesSet += ((en1,en2))
      }

    // print out black edges we discovered
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      val fw = new FileWriter(new File(debugOutDir2, "A-blackEdges.txt"))
      if (rowsBE.length>0) IO.writeTable(rowsBE, fw, " ")
      fw.close()
    }

    val rowsFirm = new mutable.ListBuffer[Array[String]]()
    for (g <- firmList)
        rowsFirm += Array(if (g.foundby==ZeroId) "Deadlock-Round" else g.foundby.toString, "-", g.stmt1.stmt.number.toString, "-->", g.stmt2.stmt.number.toString)


    // print out semaphores we discovered
    if (semaphores) {
      debugOutDir match { case None =>  case Some(debugOutDir2) =>
        val fw = new FileWriter(new File(debugOutDir2, "A-semaphores.txt"))
        if (rowsFirm.length>0) IO.writeTable(rowsFirm, fw, " ")
        fw.close()
      }
    }

    (s,blackEdgesSet.result(),suggestionSet.result(), firm.map(s => (s.stmt1.stmt,s.stmt2.stmt)))
  }

  /**
   * Verifies if the trace we generated is correct
   * @param so The program to check
   * @param output Write the faulty trace to if any. If None no trace will be produced and verification is faster
   * @param preventDeadlock If deadlock prevention should be applied (only relevant if trace is requested)
   * @param debugOutDir A debug directory where additional information is printed
   * @param statistic A statistic that is kept while processing the file
   * @return
   */
  def verify(so:StatementOrder, output:Option[FileWriter], preventDeadlock:Boolean)(implicit debugOutDir: Option[File], statistic: Statistic) : (CheckerResult,VerificationIteration) = {
    val (res, trace, _, proverTime) = (new InvokeCBMC).invokeChecker(so, None, preventDeadlock, output.isEmpty)
    output match {
      case Some(fw) if res==CheckerResult.Failure => CtexStmt.print(trace,fw)
      case _ =>
    }
    (res,new VerificationIteration(proverTime, None))
  }

  /**
   * Repair the program through reorderings or atomic sections
   * @param initialSo The program
   * @param learnFromGood Allows learning new black edges from good traces before attempting reorderings
   * @param debugOutDir A debug directory where additional information is printed
   * @param statistic A statistic that is kept while processing the file
   * @return Some if there is a solution, otherwise None
   */
  def repairProgram(initialSo: StatementOrder, learnFromGood: Boolean)(implicit debugOutDir: Option[File], statistic: Statistic) : Option[StatementOrder] = {
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      initialSo.order.exportToDOT(new File (debugOutDir2, "A-po.dot"))
    }


    val solutionsToTry = new mutable.PriorityQueue[Solution]()
    val solutionStash = new mutable.Stack[(Int,List[Solution])]() // we stash solutions before we delete them
    /**
     * We keep these solutions waiting until the last minute. This is equivalent to the old algorithm where atomic sections were used as a last resort
     */
    val atomicSectionSolutions = new mutable.ListBuffer[Solution]()

    // initial solution is the starting program
    val (startSo, blackEdges, suggestions,_) = if (learnFromGood) learnFromGoodTraces(initialSo) else (initialSo,Set():Set[(Structure,Structure)],Set():Set[(Structure,Structure)],Set():Set[(Structure,Structure)])
    System.out.println("Repairing with bad traces phase ...")

    solutionsToTry += new Solution(startSo, 0, None, SolutionType.Initial, 0, None, 0)


    var lastBugId: Int = 0
    var preventDeadlock = false // this will be set to true to see if we got a real solution
    // This requires some explanation: We may not discover bugs due to enforcing assertions even when asserts fail.
    // This can lead to a deadlock. Therefore we need to switch this future of, even though that will
    // prevent us from finding good bug fixes
    /**
     * immediatelly after we switch to deadlock mode there is no need to verify right away again
     * Also we want to save the last solution to repeat its analysis
     */
    var lastVerified: Option[Solution] = None

    /**
     * This saves the verification statistic so that we can add it to the round statistic later
     */
    var verifyStatistic: Option[VerificationIteration] = None

    // find a fix for the program
    while ((solutionsToTry.nonEmpty || lastVerified.nonEmpty) && !cancel) {
      val badRound = statistic.badRounds.incrementAndGet()
      verifyStatistic = None

      val solution = lastVerified match {
        case None => solutionsToTry.dequeue()
        case Some(s) => s
      }
      val so = solution.so
      debugOutDir match { case None =>  case Some(debugOutDir2) =>
        so.order.exportToDOT(new File (debugOutDir2, "B-" + badRound + "-po.dot"))
        val fw = new FileWriter(new File(debugOutDir2, "B-" + badRound + "-program.c"))
        (new CPrinter).print(so.program,fw)
        fw.close()
      }

      System.out.print("Iteration " + badRound + ".")

      if (preventDeadlock && lastVerified.isEmpty) {
        // because verification in non-deadlock mode can take forever
        val (vres,verifyStatistic1) = verify(so, None, false)
        verifyStatistic = Some(verifyStatistic1)
        vres match {
          case CheckerResult.Success =>
            // check if we deadlock by any chance
            val (vres2,verifyStatistic2) = verify(so, None, true)
            verifyStatistic = Some(verifyStatistic1 + verifyStatistic2)
            vres2 match {
              case CheckerResult.Failure => //program still finishes
                System.out.println("..Done")
                statistic.badIterations += (new Statistic.VerificationIteration(0, Some(solution)) + verifyStatistic1 + verifyStatistic2)
                statistic.solutionFound = true; return Some(so)
              case _ =>
            }
          case _ =>
        }
      }
      System.out.print(".")

      val (res, trace, bugId, proverTime) = (new InvokeCBMC).invokeChecker(so, None, preventDeadlock, false)
      val startTime = new Date()
      System.out.print(".")

      if (res == CheckerResult.Success && !preventDeadlock) {
        System.out.println("Done")
        val verify1 = new Statistic.VerificationIteration(proverTime, Some(solution))
          assert (!preventDeadlock) // we should have already found a solution above
          System.out.println("Found possible solution. Verifying...")
          val (vres,verify2) = verify(so, None, false)
          statistic.badIterations += verify1 + verify2
          vres match {
            case CheckerResult.Success =>
              statistic.solutionFound = true; return Some(so)
            case CheckerResult.Failure =>
              preventDeadlock = true
              System.out.println("Switching to non-deadlock mode, redoing iteration.")
              lastVerified = Some(solution)
              statistic.badRounds.decrementAndGet()
          }
      } else {
        if (res != CheckerResult.Success) {
          // compare bugIds (if this is a different bug, then mark the previous one as fixed)
          if (bugId != lastBugId && solutionsToTry.nonEmpty) {
            solutionStash.push((lastBugId,solutionsToTry.toList))
            solutionsToTry.clear()
          }
          lastBugId = bugId
          debugOutDir match { case None =>  case Some(debugOutDir2) =>
            val fw = new FileWriter(new File(debugOutDir2, "B-" + badRound + "-badTrace.txt"))
            CtexStmt.print(trace,fw)
            fw.close()
          }

          implicit val number = badRound
          val gen = new Generalisation(trace.filterOutMain, so)
          var n = 1
          for (s <- gen.solutions) {
            for (j <- so.integrate(s.red, solution, badRound, s.metric, n, suggestions)) {
              if (!solutionsToTry.exists(_==j)) {
                if ((learnFromGood || !options.placeAtomicSections) && !blackEdges.contains(j.swapped.get) && j.solutionType==SolutionType.AtomicSection)  // if the atomic section is suggested by the good trace analysis we allow it
                  atomicSectionSolutions += j
                else
                  solutionsToTry += j
                debugOutDir match { case None =>  case Some(debugOutDir2) =>
                  gen.exportToDOT(new File(debugOutDir2, "B-" + badRound + "-graph-sol-" + n + ".dot"), s.graph())
                  n = n + 1
                }
              }
            }
          }

          statistic.badIterations += (new Statistic.Iteration(new Date().getTime - startTime.getTime, proverTime, Some(solution), Some(gen.failingAssertion.stmt.number)) + verifyStatistic)
        } else {
          statistic.badIterations += (new Statistic.Iteration(0, proverTime, Some(solution), None) + verifyStatistic)
        }
        System.out.println("Done")

        if (solutionsToTry.isEmpty) {
          if (solutionStash.nonEmpty) {
            val (b,s) = solutionStash.pop()
            solutionsToTry ++= s
            lastBugId = b
          } else if (atomicSectionSolutions.nonEmpty) {
            solutionsToTry ++= atomicSectionSolutions
            atomicSectionSolutions.clear()
          }
        }

      // print out the current list of solutions to try
      debugOutDir match { case None =>  case Some(debugOutDir2) =>
        val rows: List[Array[String]] = solutionsToTry.map(_.toArray).toList
        val fw = new FileWriter(new File(debugOutDir2, "B-" + badRound + "-solutions.txt"))
        if (rows.length>0) IO.writeTable(rows, fw, " ")
        fw.close()
      }

      if (solutionsToTry.nonEmpty)
        System.out.println("Attempting fix: " + solutionsToTry.head.toString)
      lastVerified = None
      }
    }
    None
  }

  def placeSemaphores(initialSo: StatementOrder)(implicit debugOutDir: Option[File], statistic: Statistic):Option[StatementOrder] = {
    statistic.semaphore = true
    var so = initialSo
    val (_,_,_,semaphores) = learnFromGoodTraces(initialSo, true)
    for (s<-semaphores) {
      so = so.integrateSemaphore(s._1, s._2)
    }

    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      so.order.exportToDOT(new File (debugOutDir2, "B-po.dot"))
      val fw = new FileWriter(new File(debugOutDir2, "B-program.c"))
      (new CPrinter).print(so.program,fw)
      fw.close()
    }

    Console.println("Placed semaphores...verifying")

    val (res, time) = verify(so, None, false)
    statistic.badIterations += time
    if (res == CheckerResult.Success) {
      statistic.solutionFound = true
      Some(so)
    } else None
  }

  /**
   * Processes a file without writing anything to the disk
   * @param fileContent The C code
   * @param debugOutDir A debug directory where additional information is printed
   * @param statistic A statistic that is kept while processing the file
   * @return Some if there is a solution, otherwise None
   */
  def processFileRaw(fileContent:String)(implicit debugOutDir: Option[File], statistic: Statistic) : Option[StatementOrder] = {
    val po = Parsing.parse(fileContent)
    val so = new StatementOrder(po, fileContent)
    repairProgram(so, true)
  }

  def processFileRaw(file:File)(implicit debugOutDir: Option[File], statistic: Statistic) : Option[StatementOrder] = {
    val org = IO.readToString(file)
    processFileRaw(org)
  }

  def processFile(in:File) : Unit = {
    def printResult(res:Option[StatementOrder]):Unit = {
      res match {
        case _ if cancel =>
          Console.println("Cancelled by the user")
        case None =>
          System.out.println("No solution found")
        case Some(newSo) =>
          val newFile = in.getAbsolutePath.replace(".c", options.fileInfix + ".corrected.c")

          val fw = new FileWriter(newFile)
          (new CPrinter).print(newSo.program, fw)
          fw.close()
          System.out.println("Solution written to " + newFile)
      }
    }

    val inDir = in.getParentFile
    val newStatistic = in.getAbsolutePath.replace(".c", options.fileInfix + (if (options.placeAtomicSections) ".AS" else "") + ".statistic.txt")

    Console.print("Parsing...")
    val org = IO.readToString(in)
    implicit val debugOutDir = None // Some(IO.prepDirectory(new File (inDir, in.getName.replace(".c",""))))
    val po = Parsing.parse(org)
    val so = new StatementOrder(po, org)
    Console.println("Done")

    val orgFile = in.getAbsolutePath.replace(".c", ".original.c")
    val fw = new FileWriter(orgFile)
    (new CPrinter).print(so.program, fw)
    fw.close()
    System.out.println("Original written to " + orgFile)

    implicit val statistic = new Statistic()

    val statisticStream = new PrintStream(new FileOutputStream(newStatistic))
    statisticStream.println("File: " + in.getName)
    statisticStream.println("Mode: " + options.mode + "\n")

    options.mode match {
      case Verify =>
        val tracePath = in.getPath.replace(".c", ".buggyTrace.txt")
        implicit val debugOutDir = None
        System.out.println("Verifying program ...")
        verify(so, None, false)._1 match {
          case CheckerResult.Success => System.out.println("No bugs found")
          case CheckerResult.Failure =>
            System.out.println("Bug found ... producing trace")
            val fw = new FileWriter(tracePath)
            verify(so, Some(fw), false)._1 match {
              case CheckerResult.Success =>
                Console.println("After the bug the program cannot be finished (some assumption does not hold).")
                Console.println("Will attempt to produce a trace anyhow, beware it may not be faithful after the bug occured.")
                verify(so, Some(fw), true)._1 match {
                  case CheckerResult.Success =>
                    assert(false) // this should really never happen
                  case CheckerResult.Failure => System.out.println("Written fauty trace to " + tracePath)
                }
              case CheckerResult.Failure => System.out.println("Written fauty trace to " + tracePath)
            }
            fw.close()
        }
      case Mixed | BadOnly =>
        implicit val debugOutDir = Some(IO.prepDirectory(new File (inDir, in.getName.replace(".c",options.fileInfix))))
        printResult(repairProgram(so, options.mode == Mixed))
        statistic.writeToFile(statisticStream)
      case CombinedS => // place Semaphores and make comparison
        System.out.println("Discovering semaphores ...")
        implicit var debugOutDir = Some(IO.prepDirectory(new File (inDir, in.getName.replace(".c",".semaphores"))))
        printResult(placeSemaphores(so))
        val cSemaphores = new Column("Semaphores")
        cSemaphores.rows ++= statistic.column
        statistic.clear()
        System.out.println("\nRunning in BadOnly mode ...")
        debugOutDir = Some(IO.prepDirectory(new File (inDir, in.getName.replace(".c",".badOnly"))))
        repairProgram(so, false)
        val cBad = new Column("BadOnly")
        cBad.rows ++= statistic.column
        IO.writeTable(List(cBad, cSemaphores), statisticStream)
        statisticStream.println()
      case Combined =>
        System.out.println("Running in Mixed mode ...")
        implicit var debugOutDir = Some(IO.prepDirectory(new File (inDir, in.getName.replace(".c",".mixed"))))
        printResult(repairProgram(so, true))
        val cMixed = new Column("Mixed")
        cMixed.rows ++= statistic.column
        statistic.clear()
        System.out.println("\nRunning in BadOnly mode ...")
        debugOutDir = Some(IO.prepDirectory(new File (inDir, in.getName.replace(".c",".badOnly"))))
        repairProgram(so, false)
        val cBad = new Column("BadOnly")
        cBad.rows ++= statistic.column
        IO.writeTable(List(cBad, cMixed), statisticStream)
        statisticStream.println()
    }

    statistic.writeLegend(statisticStream)
    statisticStream.close()
  }

  def main(args: Array[String]): Unit = {
    // check some basic stuff
    val version = System.getProperty("java.version")
    if (version.startsWith("1.4")||version.startsWith("1.5")||version.startsWith("1.6")) {
      Console.println("Java version 1.7 (Java 7) is required.")
      System.exit(1)
    }
    val os = System.getProperty("os.name")
    if (os.contains("Windows") || os.contains("Mac")) {
      Console.println("Unfortunatelly Windows or Mac is not supported. Linux only.")
      System.exit(1)
    }

    if (!Options.parse(args, options))
    {
      // command line parsing failed, exit
      System.exit(1)
    }
    setParallelism()
    CommandReader.start()

    val in = options.in
    val renew = options.renew


    if (in.isFile) {
      processFile(in)
    } else
    if (in.isDirectory) {
      // go through all c files
      for (f <- in.listFiles(new FilenameFilter {
        def accept(p1: File, p2: String): Boolean = p2.endsWith(".c") && !p2.contains("corrected") && !p2.contains("original")
      })) {
        val newStatistic = f.getAbsolutePath.replace(".c", options.fileInfix + ".statistic.txt")
        if (!renew || new File(newStatistic).exists() && !cancel) {
          Console.println()
          Console.println(s"Processing file ${f.getName}")
          Console.println("=============================")
          processFile(f)
        }

      }
    } else {
      Console.println(s"File not found: ${in.getAbsolutePath}")
      System.exit(1)
    }
  }
}
