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
import at.ac.ist.con_repair.structures._
import scalax.collection.edge.LkDiEdge
import scalax.collection.edge.Implicits._
import scalax.collection.io.dot._
import at.ac.ist.con_repair.helpers.{Variable, IO, VariableAnalysisResult}
import scalax.collection.mutable.Graph
import java.io.File
import at.ac.ist.con_repair.helpers.GraphExtensions._
import scala.collection.{mutable, GenSet}

/**
 * Proof objects generally get the good traces and learn from them new black edges
 * @param trace1 The good trace
 */
class ProofObject(trace1: List[CtexStmt], assertion:Option[AssertStatement] = None) {
  object Colour extends Enumeration {
    type Colour = Value
    val Black, Dotted, Red, Green, Blue, RedDotted = Value
  }
  import Colour._

  val trace = trace1.filter(_.primitive)

  //protected implicit val config = new ConstrainedConfig(constraintCompanion = Acyclic)
  protected val graph = Graph.from[CtexStmt,LkDiEdge](trace, Set())

  protected val threads = trace.map(_.threadId).distinct
  protected val threadNames = trace.map(t => (t.threadId,t.threadName)).toMap
  protected val firmB = Set.newBuilder[(CtexStmt, CtexStmt)] // uncovered edges

  // put dotted edges
  for (t <- threads) {
    var unprocessed = trace.filter(_.threadId == t)
    while (unprocessed.nonEmpty) {
      val head = unprocessed.head
      val tail = unprocessed.tail
      for(next <- tail) {
        graph.add((head ~+#> next)(Dotted))
      }
      unprocessed = tail
    }
  }


  // the case where we look at only one assertion
  // we add the red edge, but we also get the threads involved in this
  val assertionThreads:Option[Set[Int]] = assertion match {
    case Some(assertion) =>
      var after = trace
      var before:List[CtexStmt] = Nil
      val sb = Set.newBuilder[Int]
      while (after.nonEmpty) {
        val (before1,after1) = after.span(_.stmt!=assertion); after = after1
        before ++= before1
        after match {
          case a::_ =>
            sb ++= a.stmt.usedVariables.foldLeft(Set():Set[Int])((s,v) => s++findDependency(a, before.reverse, v, Red))
            after = after1.tail
          case _ =>
        }
      }
      Some(sb.result())
    case None => None
  }



  // find red and blue edges
  var unprocessed = trace.reverse
  while (unprocessed.nonEmpty) {
    val head = unprocessed.head
    val tail = unprocessed.tail
    head.stmt match {
        // assert analysis only if it did not happen above
      case x: AssertStatement if assertion.isEmpty => for (v<-head.stmt.usedVariables) findDependency(head, tail, v, Red)
      case _: AssumeStatement if assertionThreads.isEmpty || assertionThreads.get.contains(head.threadId) => for (v<-head.stmt.usedVariables) findDependency(head, tail, v, Blue)
      case _ =>
    }
    unprocessed = tail
  }

  // CLEANUP: Find better way (without findPath)
  //exportToDOT("test.dot", graph.clone)
  // find cycles that would indicate possible deadlocks
  // make dotted black edges black in the cycles
  graph.edges.filter(e => e.label == Blue && (e.from.value.asInstanceOf[CtexStmt].stmt.isInstanceOf[AssumeStatement] || e.to.value.asInstanceOf[CtexStmt].stmt.isInstanceOf[AssumeStatement])).foreach { blue =>
    val startThread = blue.from.value.asInstanceOf[CtexStmt].threadId
    val lastCmd = trace.reverse.find(_.threadId == startThread).get
    val lastCmdNode = graph.find(lastCmd).get
    val threads = Set(startThread, blue.to.value.asInstanceOf[CtexStmt].threadId)
    graph.findPath(blue.to, lastCmdNode, e => Set(Dotted,Black,Blue).contains(e.edge.label.asInstanceOf[Colour]) && threads.contains(e.edge.to.value.asInstanceOf[CtexStmt].threadId)) match {
      case None =>
      case Some(p) =>
        for (e1 <- p) {
          val e:graph.EdgeT = e1.asInstanceOf[graph.EdgeT] // needed for some reason to match the types
          if (e.label.asInstanceOf[Colour] == Blue &&
            (e.from.value.asInstanceOf[CtexStmt].stmt.isInstanceOf[AssumeStatement] || e.to.value.asInstanceOf[CtexStmt].stmt.isInstanceOf[AssumeStatement]) &&
            (e.from.value.asInstanceOf[CtexStmt].threadId != e.to.value.asInstanceOf[CtexStmt].threadId)) {
            // we found the other blue edge
            graph.remove((blue.from.value.asInstanceOf[CtexStmt] ~+#> e.to.value.asInstanceOf[CtexStmt])(Dotted))
            graph.add((blue.from.value.asInstanceOf[CtexStmt] ~+#> e.to.value.asInstanceOf[CtexStmt])(Black))
            graph.remove((blue.to.value.asInstanceOf[CtexStmt] ~+#> e.from.value.asInstanceOf[CtexStmt])(Dotted))
            graph.add((blue.to.value.asInstanceOf[CtexStmt] ~+#> e.from.value.asInstanceOf[CtexStmt])(Black))
            assert (blue.from.value.asInstanceOf[CtexStmt].threadId == e.to.value.asInstanceOf[CtexStmt].threadId)
            assert ((blue.to.value.asInstanceOf[CtexStmt].threadId == e.from.value.asInstanceOf[CtexStmt].threadId))
          }
        }
      }
    }



  val readFrom:GenSet[(Id, Variable, Id)] = {
    graph.edges.filter(e => e.label == Red).map { red =>
      val set = red.from.value.asInstanceOf[CtexStmt]
      val used = red.to.value.asInstanceOf[CtexStmt]
      val variable = set.stmt.changedVariables.head
      (set.stmt.number, variable, used.stmt.number)
    }
  }

  RichGraph2(graph).removeTransitive(e => e.edge.label.asInstanceOf[Colour]==Green, e => Set(Black,Dotted,Green).contains(e.edge.label.asInstanceOf[Colour]))

  // check red and green edges for coverage
  val edges: mutable.Set[ProofObject.this.graph.EdgeT] = graph.edges.filter(e => Set(Red,Green).contains(e.label.asInstanceOf[Colour]))
  while (edges.nonEmpty) {
    val edge = edges.head
    edges.remove(edge)
    if (edge.from.value.asInstanceOf[CtexStmt].threadId != edge.to.value.asInstanceOf[CtexStmt].threadId) {
      graph.findPath(edge.from, edge.to, e => Set(Dotted,Black,Blue).contains(e.edge.label.asInstanceOf[Colour])) match {
        case None => // not covered
          firmB += ((edge.from.value.asInstanceOf[CtexStmt], edge.to.value.asInstanceOf[CtexStmt]))
          suggestReordering(edge.from.value.asInstanceOf[CtexStmt], edge.to.value.asInstanceOf[CtexStmt], edge.label.asInstanceOf[Colour] == Green)
        case Some(p) => // we are covered, make edges black
          for (e1 <- p) {
            val e:graph.EdgeT = e1.asInstanceOf[graph.EdgeT] // needed for some reason to match the types
            if (e.label.asInstanceOf[Colour] == Dotted) {
              // make edge black
              val newEdge = (e.from.value.asInstanceOf[CtexStmt] ~+#> e.to.value.asInstanceOf[CtexStmt])(Black)
              graph -= e
              graph.add(newEdge)
            } else if (e.label.asInstanceOf[Colour] == Blue) {
              /*val newEdges = intefere_blue(e.from.value.asInstanceOf[CtexStmt], e.to.value.asInstanceOf[CtexStmt],
                edge.from.value.asInstanceOf[CtexStmt], edge.to.value.asInstanceOf[CtexStmt])
              edges ++= newEdges*/
            }
          }
      }
    }
  }

  /**
   * Find the green edge for a specific red edge
   * @param from The origin of the red edge
   * @param to The destination of the red edge
   * @return The set of other threads involved
   */
  def intefere_red(from:CtexStmt, to:CtexStmt):Set[Int] = {
    // find green edges
    val start = from
    val end = to
    val startThreads = mutable.Set(0,start.threadId) // 0 is the main thread
    val endThreads = mutable.Set(0,end.threadId)
      val before = trace.takeWhile(_!=start).reverse
      val after = trace.dropWhile(_!=end).tail
      for (c <- before) {
        if (!startThreads.contains(c.threadId) && !c.stmt.changedVariables.intersectionEmpty(start.stmt.changedVariables, start.threadId!=c.threadId)) {
          graph.add((c ~+#> start)(Green))
          startThreads += c.threadId
        }
      }
      for (c <- after) {
        if (!endThreads.contains(c.threadId) && !c.stmt.changedVariables.intersectionEmpty(end.stmt.usedVariables, end.threadId!=c.threadId)) {
          graph.add((end ~+#> c)(Green))
          endThreads += c.threadId
        }
      }
    endThreads.toSet
  }

  /**
   * Find the green interference edge for a blue edge that is used in the cover (to protect) a specific red or green edge
   * The edges that interefer are those that write to the same value as the blue edge reads from and therefore must be scheduled after the command
   * the red edge reads from
   * @param from The origin of the blue edge
   * @param to The destination of the edge edge
   * @param pfrom The origin of the edge we want to protect
   * @param pto The destination of the edge we want to protect
   * @return The set of newly inserted green edges
   */
  def intefere_blue(from:CtexStmt, to:CtexStmt, pfrom:CtexStmt, pto:CtexStmt):mutable.Set[ProofObject.this.graph.EdgeT] = {
    // find green edges
    val newEdges:mutable.Set[ProofObject.this.graph.EdgeT] = mutable.Set() // 0 is the main thread
    if (pfrom.threadId!=0) {
      val other = trace.filter(_ != from)
      for (c <- other) {
        if (c.threadId != 0 && !c.stmt.changedVariables.intersectionEmpty(from.stmt.changedVariables, from.threadId != c.threadId)) {
          val newedge = (pfrom ~+#> c)(Green)
          if (graph.add(newedge)) {
            newEdges += graph.find(newedge).get
          }
        }
      }
    }
    newEdges
  }

  /**
   *
   * @param from The origin of the red or green edge
   * @param to The target of the red or green edge
   * @param green true if the edge is green, false if it is red
   */
  def suggestReordering(from:CtexStmt, to:CtexStmt, green:Boolean = false) {
    if (from.threadId== 0 || to.threadId == 0)
      return
    // the red edge ends in an assertion. To protect it we need to find a blue edge pointing to a node above
    // the blue edge currently comes from above the red edge. We want to swap in the other thread to ensure the
    // red edge is protected
    // we will not attempt to swap the assertion (for the moment)
    if (!green) {
      val beforeAssert = trace.filter(_.threadId == to.threadId).takeWhile(_!=to)
      for (x <- beforeAssert.reverse) {
        graph.find(x).get.incoming.filter(_.label.asInstanceOf[Colour]==Blue).map(_.from.value.asInstanceOf[CtexStmt]).filter(_.threadId==from.threadId).foreach {above =>
          graph.add((from ~+#> above)(RedDotted))
        }
      }
    } else {
      val afterGreen = trace.filter(_.threadId == to.threadId).dropWhile(_!=to).tail
      for (x <- afterGreen) {
        graph.find(x).get.incoming.filter(_.label.asInstanceOf[Colour]==Blue).map(_.from.value.asInstanceOf[CtexStmt]).filter(_.threadId==from.threadId).foreach {other =>
          if (other.position > from.position)
            graph.add((x ~+#> to)(RedDotted))
        }
      }
    }
  }



  // make blue edges firm
  /*graph.edges.filter(e => e.label == Blue).foreach { blue =>
    firmB += ((blue.from.value.asInstanceOf[CtexStmt], blue.to.value.asInstanceOf[CtexStmt]))
  }

  // more firm for blue edges
  graph.edges.filter(e => e.label == Blue).foreach { blue =>
    val start = blue.from.value.asInstanceOf[CtexStmt]
    val end = blue.to.value.asInstanceOf[CtexStmt]
    val startThreads = mutable.Set(0,start.threadId) // 0 is the main thread
    val endThreads = mutable.Set(0,end.threadId)
    val before = trace.takeWhile(_!=start).reverse
    val after = trace.dropWhile(_!=end).tail
    for (c <- before) {
      if (!startThreads.contains(c.threadId) && c.stmt.changedVariables.intersectionEmpty(start.stmt.changedVariables, start.threadId!=c.threadId)) {
        firmB += ((c,start))
        startThreads += c.threadId
      }
    }
    for (c <- after) {
      if (!endThreads.contains(c.threadId) && c.stmt.changedVariables.intersectionEmpty(end.stmt.changedVariables, end.threadId!=c.threadId)) {
        firmB += ((end, c))
        endThreads += c.threadId
      }
    }
  }*/

  val firm = firmB.result().filter(f => f._1.threadId!=0 && f._2.threadId!=0).toSet
  val black : Set[(CtexStmt,CtexStmt)] = graph.edges.filter(e => e.label == Black).map {e =>
    (e.from.value.asInstanceOf[CtexStmt], e.to.value.asInstanceOf[CtexStmt])
  }.toSet

  val suggestedReorderings : Set[(CtexStmt,CtexStmt)] = graph.edges.filter(e => e.label == RedDotted).map {e =>
    (e.from.value.asInstanceOf[CtexStmt], e.to.value.asInstanceOf[CtexStmt])
  }.toSet

  /**
   * Recursively finds the place a variable reads from and places edges to this place
   * @param curr The current statement
   * @param remaining Statements before this statement
   * @param variable The variable we are looking for
   * @param colour The colour of the edges to insert
   * @return The threads involved in the dependencies
   */
  private def findDependency(curr: CtexStmt, remaining:List[CtexStmt], variable:Variable, colour: Colour): Set[Int] = {
    var unprocessed = remaining
    while (unprocessed.nonEmpty) {
      val head = unprocessed.head
      val tail = unprocessed.tail
      if (head.stmt.changedVariables.contains(variable, curr.threadId!=head.threadId)) {
        // add new edge and change string
        graph.add((head ~+#> curr)(colour))
        //if (head.threadId != 0) {// from thread 0 there is no need to continue looking
          val otherThreads:Set[Int] = if (colour==Red) {
            intefere_red(head,curr)
          } else Set()
          return otherThreads // no recursion at this point ++ head.stmt.usedVariables.foldLeft(Set(curr.threadId))((s,v) => s ++ findDependency(head, tail, v, colour))
        //} else
          //return Set(curr.threadId)
      }
      unprocessed = tail
    }
    // we could not find where this assumption or assertion is fulfilled
    throw new Exception(s"Could not trace edge back. Did you forget to initialise global variable ${variable._1} in main?")
  }

  def exportToDOT(file:File): Unit = {
    exportToDOT(file.getAbsolutePath)
  }

  def exportToDOT(filename:String, graph:scalax.collection.mutable.Graph[CtexStmt,LkDiEdge] = graph): Unit = {
    graph.removeTransitive(e => e.edge.label.asInstanceOf[Colour]==Dotted, e => Set(Black,Dotted).contains(e.edge.label.asInstanceOf[Colour]))

    import scala.language.existentials
    val dotRoot = new DotRootGraph(true, Some("ProofObject"))
    val threadMap = threads.map(t => (t,new DotSubGraph(dotRoot, "cluster" + t.toString, List(DotAttr("label", threadNames(t)), DotAttr("color", "blue"))))).toMap
    def edgeTransformer(innerEdge: scalax.collection.Graph[CtexStmt,LkDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      val edge = innerEdge.edge
      val label = edge.label.asInstanceOf[Colour]
      val colour = if (label==RedDotted) "red" else if (label==Dotted) "black" else label.toString
      Some((dotRoot,
        DotEdgeStmt(edge.from.value.asInstanceOf[CtexStmt].position.toString,
          edge.to.value.asInstanceOf[CtexStmt].position.toString,
          (if (label==Dotted || label==RedDotted) List(DotAttr("style", "dotted")) else List()) ++
            List(DotAttr("color", colour)) ++
            (if (Set(Black,Dotted).contains(label)) List() else List(DotAttr("constraint", "false")))
          )))
    }
    def nodeTransformer(innerNode: scalax.collection.Graph[CtexStmt,LkDiEdge]#NodeT): Option[(DotGraph,DotNodeStmt)] = {
      Some((threadMap(innerNode.value.asInstanceOf[CtexStmt].threadId),
        DotNodeStmt(innerNode.value.asInstanceOf[CtexStmt].position.toString,
          List(DotAttr("label", innerNode.value.asInstanceOf[CtexStmt].stmt.number.toString))
        )))
    }
    val x: String = graph.toDot(dotRoot, edgeTransformer, Some(nodeTransformer), Some(nodeTransformer))
    IO.writeToFile(filename, x)
  }
  //private def discoverEdges() :

}
