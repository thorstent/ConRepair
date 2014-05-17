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

package at.ac.ist.con_repair.algorithm

import at.ac.ist.con_repair.model_checker.CtexStmt
import scalax.collection.mutable.Graph
import scala.collection.mutable
import at.ac.ist.con_repair.helpers._
import ListExtensions._
import scalax.collection.io.dot._
import scalax.collection.edge.LDiEdge
import scalax.collection.edge.Implicits._
import java.io.{FileWriter, File}
import scalax.collection.io.dot.DotAttr
import scala.Some
import at.ac.ist.con_repair.helpers.SomeVars
import scalax.collection.io.dot.DotRootGraph
import scalax.collection.io.dot.DotSubGraph
import scalax.collection.io.dot.DotEdgeStmt
import scalax.collection.io.dot.DotNodeStmt
import scala.collection.mutable.ListBuffer
import at.ac.ist.con_repair.helpers.GraphExtensions._


class Generalisation(trace1: List[CtexStmt], so:StatementOrder)(implicit debugOutDir: Option[File], number:Int) {
  object Colour extends Enumeration {
    type Colour = Value
    val Black, White, RedDotted, Red, NewBlack = Value
  }
  import Colour._

  val failingAssertion = trace1.find(_.assertionFailure).get

  val deadlock = failingAssertion.threadName.contains("deadlock")

  val trace:List[CtexStmt] = {
    val tf = trace1.filter(_.primitive)
      if (deadlock) tf else { // filter out the deadlock thread if this is not about a deadlock
        tf.filter(!_.threadName.contains("deadlock"))
      }
  }
  //val trace:List[CtexStmt] = trace1.filter(_.primitive)

  class Solution(val red:Set[(CtexStmt,CtexStmt)], val black:Set[(CtexStmt,CtexStmt)], val cycle:List[CtexStmt], val graph: ()=>Graph[CtexStmt,LDiEdge]) extends Ordered[Solution]{
    /**
     * The cost of implementing this solution
     */
    val metric:Int = {
      // check the size of the number of instructions between the red edge
      val start = red.head._2
      val end = red.head._1
      val thread = start.threadId
      val length = trace.filter(_.threadId==thread).dropWhile(_ != start).takeWhile(_ != end).length
      // the cycle should not go through several threads
      val noThreads = cycle.map(_.threadId).distinct.length-2
      // the cycle should not use dotted edges in the thread where we reorder
      val before = cycle.takeWhile(_!=end)
      val noBefore = before.reverse.takeWhile(_.threadId==thread).length
      val after = cycle.dropWhile(_!=start).tail
      val noAfter = after.takeWhile(_.threadId==thread).length
      // there should be the error thread in the cycle
      val errorIncluded = if (!deadlock) (cycle.find(_.threadId == failingAssertion.threadId)) match {case None => 1 case Some(_) => 0} else 0
      // some variable that is swapped should be used by the assertion
      val usedVar = if (interfere(List(start), failingAssertion) || interfere(List(end), failingAssertion)) 0 else 1
      // We should not swap in the same thread as the assertion happened (possibly not so bad)
      val swapErrorThread = if (start.threadId == failingAssertion.threadId) 1 else 0
      // calculate the metric
      length*2 + noThreads*10 + (noBefore+noAfter)*3 + errorIncluded*30 + usedVar*20 + swapErrorThread*5
    }

    override def equals(o:Any) = {
      o match {
        case o:Solution => o.red == this.red
        case _ => false
      }
    }

    override def hashCode() = red.hashCode()

    def compare(that: Solution): Int =
      this.metric - that.metric
  }


  // try to reduce trace by putting only the last thread before the actual error into account


  private val threads = trace.map(_.threadId).distinct
  private val threadNames = trace.map(t => (t.threadId,t.threadName)).toMap
  private val errorThread = trace.find(_.assertionFailure) match {case Some(x) => x.threadId case None => throw new Exception("No assertion violation")}

  val solutions = getSolutions.toList.sorted


  private def getSolutions:Set[Solution] = {
    var edges = new ListBuffer[(CtexStmt,CtexStmt)]
    var head1 = trace.head
    var tail1 = trace.tail
    while (tail1.nonEmpty) {
      if (tail1.head.threadId != head1.threadId) {
        // this is the end of one thread, add an edge
        edges += ((head1,tail1.head))
        val threadsReached = mutable.Set(head1.threadId, tail1.head.threadId)
        var head2 = tail1.head
        var tail2 = tail1.tail
        while (tail2.nonEmpty) {
          if (tail2.head.threadId != head2.threadId && !threadsReached.contains(tail2.head.threadId)) {
            // add an edge that to every other thread switch
            edges += ((head1,tail2.head))
            threadsReached += tail2.head.threadId
          }
          head2 = tail2.head
          tail2 = tail2.tail
        }
      }
      head1 = tail1.head
      tail1 = tail1.tail
    }

    val whiteEdges: List[LDiEdge[CtexStmt]] = threads.map{ t =>
      trace.filter(_.threadId == t).foldLeftPair[List[LDiEdge[CtexStmt]]] (List()) { (l, e1, e2) =>
        (e1 ~+> e2)(White)::l
      }
    }.flatten

    /*val innerEdges: List[LDiEdge[CtexStmt]] = threads.map{ t =>
      trace.filter(_.threadId == t).foldLeftAnyPair[List[LDiEdge[CtexStmt]]] (List()) { (l, e1, e2) =>
        if (so.isOrderedDirect(e1.stmt,e2.stmt)) (e1 ~+> e2)(Black)::l else l
      }
    }.flatten*/

    // print the non-generalised edges
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      exportToDOT(new File(debugOutDir2, "B-" + number + "-graph-begin.dot"), Graph.from[CtexStmt,LDiEdge](trace, edges.map{e => val (e1,e2) = e; (e1 ~+> e2)(Black)}) ++ whiteEdges)
    }

    val genEndpoints = new mutable.HashSet[CtexStmt]()

    val edgesGen = new ListBuffer[(CtexStmt,CtexStmt)] // generalised set of edges
    for ((e1,e2) <- edges) {
      val gen: List[(CtexStmt, CtexStmt)] = generalise(e1,e2)
      edgesGen ++= gen
      val genLists = gen.unzip
      genEndpoints ++= genLists._1 ++ genLists._2
    }

    val redEdges = threads.map{ t =>
      trace.filter(_.threadId == t).foldLeftAnyPair[List[LDiEdge[CtexStmt]]] (List()) { (l, e1, e2) =>
        if (genEndpoints.contains(e1) && genEndpoints.contains(e2)) (e2 ~+> e1)(RedDotted)::l else l
      }
    }.flatten

    val graph = Graph.from[CtexStmt,LDiEdge](trace, (edgesGen.map{e => val (e1,e2) = e; (e1 ~+> e2)(Black)}) ++ whiteEdges ++ redEdges)

    graph.removeTransitive(e => Black == e.edge.label.asInstanceOf[Colour] && e.edge.to.value.asInstanceOf[CtexStmt].threadId != e.edge.from.value.asInstanceOf[CtexStmt].threadId
      , e => Set(Black,White).contains(e.edge.label.asInstanceOf[Colour]))

    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      exportToDOT(new File(debugOutDir2, "B-" + number + "-graph-gen.dot"), graph, redDotted = false)
    }

    // get start of every thread (but main)
    val seenThreads = mutable.Set[Int](0)
    val starts = new mutable.ListBuffer[CtexStmt]()
    for (c <- trace)
      if (!seenThreads.contains(c.threadId)) {
        starts += c
        seenThreads += c.threadId
      }

    val cycles = starts.par.aggregate(Set():Set[List[CtexStmt]]) ((x,s) => findCycles(List(), graph.find(s).get, false, s.threadId, 0) ++ x, _ ++ _)

    var n = 0
    for (c <- cycles) yield {
      val red = Set.newBuilder[(CtexStmt,CtexStmt)]
      val black = Set.newBuilder[(CtexStmt,CtexStmt)]
      c.foldLeftPair () {(_,c1,c2) =>
        val edge = graph.find(c1).get.findOutgoingTo(graph.find(c2).get).get
        edge.edge.label.asInstanceOf[Colour] match {
          case RedDotted =>
            red += ((c1,c2))
            //g - edge + (c1 ~+> c2)(Red)
          case White =>
            black += ((c1,c2))
            //g - edge + (c1 ~+> c2)(NewBlack)
          case _ =>
        }
      }

      val ng = {() =>
        val g = graph.clone()
        for ((c1,c2) <- red.result()) {
          g.remove(((c1~+>c2)(RedDotted)).asInstanceOf[LDiEdge[CtexStmt]])
          g.add(((c1~+>c2)(Red)).asInstanceOf[LDiEdge[CtexStmt]])
        }
        for ((c1,c2) <- black.result()) {
          g.remove(((c1~+>c2)(White)).asInstanceOf[LDiEdge[CtexStmt]])
          g.add(((c1~+>c2)(NewBlack)).asInstanceOf[LDiEdge[CtexStmt]])
        }
        g
      }
      /*n = n + 1
      debugOutDir match { case None =>  case Some(debugOutDir) =>
        exportToDOT(new File(debugOutDir, "B-" + number + "-graph-poss-sol-" + n + ".dot"), ng)
      }*/
      new Solution(red.result(), black.result(), c, ng)
    }
  }

  /**
   * Assume an edge. We move the start up and the end down as much as possible.
   * @param e1 Start of the edge
   * @param e2 End of the edge
   * @return The new edge or None if edge disappeared
   */
  protected def generalise(e1:CtexStmt, e2:CtexStmt):List[(CtexStmt,CtexStmt)] = {
    // get instructions before and after
    // before is reversed because we need to remove stuff from the beginning of the list
    var before = trace.reverse.dropWhile(_!=e1).filter(_.threadId == e1.threadId)
    var after = trace.dropWhile(_!=e2).filter(_.threadId == e2.threadId)

    // first move the after down as long as possible
    // CLEANUP: probably no longer needed
    while (!interfere(before, after.head)) {
      after = after.tail
      if (after.isEmpty) return List()
    }

    while (!interfere(after, before.head)) {
      before = before.tail
      if (before.isEmpty) return List()
    }

    val edges = new ListBuffer[(CtexStmt,CtexStmt)]

    //compare the first statement in after to the list before, then the next and so on
    while(after.nonEmpty) {
      var before2 = before
      before = List()
      // if these two interfere, make and edge and forget about the rest of the
      while (before2.nonEmpty && !interfere(List(after.head), before2.head)) {
        before = before2.head :: before
        before2 = before2.tail
      }
      if (before2.nonEmpty) edges += ((before2.head,after.head))
      after = after.tail
      before = before.reverse // because we implicitly reversed the list above
    }

    edges.result()
  }

  /**
   * Checks if a statements interferes with a list of other statements
   * @param list The list of statements
   * @param stmt The individual statement
   * @return true if so
   */
  protected def interfere(list: List[CtexStmt], stmt: CtexStmt):Boolean = {
    val changedVariablesList: VariableAnalysisResult = list.map(_.stmt.changedVariables).foldLeft (VariableAnalysisResult.empty) ((v1,v2) => v1++v2)
    val changedVariablesStmt: VariableAnalysisResult = stmt.stmt.changedVariables
    val usedVariablesList: VariableAnalysisResult = list.map(_.stmt.usedVariables).foldLeft (VariableAnalysisResult.empty) ((v1,v2) => v1++v2)
    val usedVariablesStmt: VariableAnalysisResult = stmt.stmt.usedVariables
    // otherwise check if variables interefere
    if ((!changedVariablesList.intersectionEmpty(changedVariablesStmt, true)) ||
      (!changedVariablesList.intersectionEmpty(usedVariablesStmt, true)) ||
      (!usedVariablesList.intersectionEmpty(changedVariablesStmt, true))) {
      return true
    }
    false
  }

  /**
   * This finds cycles in a graph
   * @param path The path so far
   * @param node The current node
   * @param redUsed If we already used a red edge
   * @return Set of valid cycles
   *
   * @note This function should be move to the graph extensions, but it contains logic that goes beyond cycle finding. Such as to cut off
   *       any path that contains more than one red edge
   */
  protected def findCycles(path: List[CtexStmt], node: Graph[CtexStmt,LDiEdge]#NodeT, redUsed:Boolean=false, thisThread:Int, otherThread:Int = 0) : Set[List[CtexStmt]] = {
    // we check if this cycle is useful for us (must involve more than one thread)
    def goodCycle(cycle: List[CtexStmt]): Boolean = {
      val t1 = cycle.head.threadId
      var threads = false // contains 2 threads in the cycle
      var assertion = false // contains the failing assertion as part of the cycle
      for (s <- cycle.tail) {
        if (s.threadId != t1) threads = true
        if (s == failingAssertion) assertion = true
      }
      threads
      //if (!deadlock) threads else threads && assertion // we punish solutions without the failing assertion later
    }

    var newOtherThread = otherThread

    val currNode = node.value.asInstanceOf[CtexStmt]
    if (!deadlock && currNode.threadId != thisThread && currNode.threadId != otherThread)
      if (otherThread==0)
        newOtherThread = currNode.threadId
      else
        return Set()
    val newPath = path :+ currNode

    // we found our cycle
    if (path.contains(currNode)) {
      val cycle = newPath.dropWhile(_ != currNode)
      if (goodCycle(cycle)) return Set(cycle) else return Set()
    }

    // we explore further
    node.outgoing.foldLeft (Set():Set[List[CtexStmt]]) {(l,e) =>
      val n = e.edge.to
      val colour = e.edge.label.asInstanceOf[Colour]
      assert (colour != Red) // there can be no red edges yet
      if (colour == RedDotted) {
        if (!redUsed)
          findCycles(newPath, n, redUsed = true, thisThread, newOtherThread)++l // we now used the red edge
        else l
      } else
        findCycles(newPath, n, redUsed, thisThread, newOtherThread)++l

    }
  }

  def exportToDOT(file:File,graph:Graph[CtexStmt,LDiEdge], redDotted:Boolean = false): Unit = {
    exportToDOT(file.getAbsolutePath, graph, redDotted)
  }

  private def exportToDOT(filename:String, graph: Graph[CtexStmt, LDiEdge], redDotted:Boolean): Unit = {
    import scala.language.existentials
    val dotRoot = new DotRootGraph(true, Some("Counterexample"))
    val threadMap = threads.map(t => (t,new DotSubGraph(dotRoot, "cluster" + t.toString, List(DotAttr("label", threadNames(t)), DotAttr("color", "blue"))))).toMap
    def edgeTransformer(innerEdge: scalax.collection.Graph[CtexStmt,LDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      val edge = innerEdge.edge
      val from = edge.from.value.asInstanceOf[CtexStmt]
      val to = edge.to.value.asInstanceOf[CtexStmt]
      val label = edge.label.asInstanceOf[Colour]
      label match {
        case RedDotted => if (!redDotted) None else
          Some((dotRoot,
            DotEdgeStmt(edge.from.value.asInstanceOf[CtexStmt].position.toString,
              edge.to.value.asInstanceOf[CtexStmt].position.toString,
              DotAttr("color", "red") :: DotAttr("style", "dotted") :: DotAttr("constraint", "false") :: Nil)
            ))
        case _ =>
          val constraint = if (from.threadId != to.threadId || label==Red)  List(DotAttr("constraint", "false")) else List()
          val colour = label match {
            case NewBlack => "black"
            case White if redDotted => "gray"
            case _ => label.toString.toLowerCase
          }
          val lineType = DotAttr("style", label match {case NewBlack=>"dotted" case _ => "solid"})
          Some((dotRoot,
            DotEdgeStmt(edge.from.value.asInstanceOf[CtexStmt].position.toString,
              edge.to.value.asInstanceOf[CtexStmt].position.toString,
              DotAttr("color", colour) :: lineType :: constraint)
          ))
      }
    }
    def nodeTransformer(innerNode: scalax.collection.Graph[CtexStmt,LDiEdge]#NodeT): Option[(DotGraph,DotNodeStmt)] = {
      val attr = if (innerNode.value.asInstanceOf[CtexStmt] == failingAssertion) List(DotAttr("color","red")) else List()
      Some((threadMap(innerNode.value.asInstanceOf[CtexStmt].threadId),
        DotNodeStmt(innerNode.value.asInstanceOf[CtexStmt].position.toString,
          DotAttr("label", innerNode.value.asInstanceOf[CtexStmt].stmt.number.toString)::attr
        )))
    }
    val x: String = graph.toDot(dotRoot, edgeTransformer, Some(nodeTransformer), Some(nodeTransformer))
    IO.writeToFile(filename, x)
  }
}
