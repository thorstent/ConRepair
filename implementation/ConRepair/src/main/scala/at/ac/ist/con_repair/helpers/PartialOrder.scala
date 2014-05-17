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

import collection.mutable.ListBuffer
import scala.collection.mutable
import scalax.collection.constrained.{DAG, Graph}
import scalax.collection.io.dot._
import scalax.collection.GraphEdge.DiEdge
import java.io.File
import ListExtensions._
import GraphExtensions._

object PartialOrder {
  def empty[T:Manifest] : PartialOrder[T] = {
    implicit val config = new scalax.collection.config.CoreConfig()
    new PartialOrder[T](DAG.empty[T], Map.empty, Map.empty)
  }
}

/**
 *
 * @param graph the underlying graph
 * @param buckets maps buckets to their parents
 * @param elementBuckets maps an element to a bucket
 * @tparam T The types this partial order should consider
 */
class PartialOrder[T : Manifest] private (val graph : Graph[T,DiEdge], buckets: Map[T,Option[T]], private val elementBuckets: Map[T, T]) {

  /**
   * x should happen before y Breadth first graph search
   */
  def isOrdered(x:T, y:T) : Boolean = {
    graph.find(x) match {
      case None => throw new IllegalArgumentException("x not in graph")
      case Some(x1) => graph.find(y) match {
        case None => throw new IllegalArgumentException("y not in graph")
        case Some(y1) => graph.findPath(x1,y1).nonEmpty
      }
    }
  }

  def isOrderedDirect(x:T, y:T) : Boolean = {
    graph.find(x) match {
      case None => throw new IllegalArgumentException("x not in graph")
      case Some(x1) => graph.find(y) match {
        case None => throw new IllegalArgumentException("y not in graph")
        case Some(y1) => x1.diSuccessors.contains(y1)
      }
    }
  }

  def addBucket(bucket:T, parent:Option[T]) : PartialOrder[T] = {
    if(parent.nonEmpty && !buckets.contains(parent.get)) throw new IllegalArgumentException("The parent was not yet added")
    new PartialOrder[T](graph, buckets + (bucket -> parent), elementBuckets)
  }

  def addElement(element: T, bucket: T) : PartialOrder[T] = {
    var eB: Map[T, T] = elementBuckets
    if(!buckets.contains(bucket)) throw new IllegalArgumentException("The bucket was not yet added")
    eB.get(element) match {
      case None =>
      case Some(e) => if (eB(element) != bucket)
        throw new IllegalArgumentException("element was added previously with different bucket")
    }
    eB += (element -> bucket)
    val g = graph + element
    new PartialOrder[T](g, buckets, eB)
  }

  def removeElement(element: T) : PartialOrder[T] = {
    val g = graph - element
    new PartialOrder[T](g, buckets, elementBuckets)
  }

  def elementMerge(newElement:T, oldElements:List[T]) : PartialOrder[T] = {
    val b = buckets + (newElement -> Some(elementBuckets(oldElements(0))))
    var eB = elementBuckets + (newElement -> elementBuckets(oldElements(0)))
    eB = oldElements.foldLeft (eB) ((eb, e) => eb.updated(e, newElement))
    val pointTo: List[T] = oldElements.map(graph.find(_).get.diSuccessors.map(_.value.asInstanceOf[T])).flatten.filterNot(oldElements.contains(_))
    val pointFrom: List[T] = oldElements.map(graph.find(_).get.diPredecessors.map(_.value.asInstanceOf[T])).flatten.filterNot(oldElements.contains(_))
    var g = oldElements.foldLeft(graph) ((g,e) => g-e) // remove old element to remove edges to these elements
    g = g + newElement
    g = pointTo.foldLeft(g) ((g,t) => g + DiEdge(newElement, t))
    g = pointFrom.foldLeft(g) ((g,t) => g + DiEdge(t, newElement))
    // readd elements and enforce order
    g = oldElements.foldLeft (g) ((g,e) => g+e)
    g = oldElements.foldLeftPair (g) ((g,e1,e2) => g+DiEdge(e1,e2))
    new PartialOrder[T](g, b, eB)
  }

  def getBucket(element: T) = elementBuckets(element)

  def contains(element: T) : Boolean = graph.find(element).nonEmpty

  def add(pointsFrom: T, pointsTo: T) : PartialOrder[T] = {
    graph.find(pointsFrom) match {
      case None => throw new IllegalStateException("Elements not yet added to order")
      case Some(_) =>
    }
    graph.find(pointsTo) match {
      case None => throw new IllegalStateException("Elements not yet added to order")
      case Some(_) =>
    }

    if (elementBuckets(pointsFrom) != elementBuckets(pointsTo))
      throw new IllegalArgumentException("in different bucket")
    val edge = DiEdge(pointsFrom, pointsTo)
    if (graph.preAdd(edge).abort)
      throw new IllegalArgumentException("this would cause a cycle")
    new PartialOrder[T](graph + edge, buckets, elementBuckets)
  }

  def addEdges(edges:collection.Set[(T,T)]) : PartialOrder[T] = {
    val ng = edges.foldLeft (graph) {(g,e) => e match {case (from,to) => g + DiEdge(from,to)}}
    new PartialOrder(ng, buckets, elementBuckets)
  }

  /**
   * Removes transitive edges
   */
  def reduced : PartialOrder[T] = {
    val bucketElements = new mutable.HashMap[T,mutable.HashSet[T]]()
    for ((e,b) <- elementBuckets) {
      bucketElements.get(b) match {
        case None =>
          val be = new mutable.HashSet[T]()
          be += e
          bucketElements.put(b,be)
        case Some(be) =>
          be += e
      }
    }
    var g = graph
    for (be <- bucketElements.values) {
      g = g.removeTransitive(be).asInstanceOf[Graph[T,DiEdge]]
    }
    new PartialOrder[T](g, buckets, elementBuckets)
  }

  /**
   * Uses the partial order to order a list of items. If an item has its precondition not fulfilled it will be moved down.
   * @param items The list to sort
   * @param map Maps items to ids used by the order
   * @tparam Y The type of the items
   * @return The sorted list
   */
  def orderItemsDown[Y](items : List[Y], map:Y => T, up:Boolean = false) : List[Y] = {
    val result = new ListBuffer[Y]()
    val pendingNeed = new mutable.HashMap[Y,mutable.HashSet[T]]()

    // checks if all the predecessors are already added
    def readyToInsert(elem:Y):scala.collection.Set[T] = {
      graph.get(map(elem)).diPredecessors.map(_.value.asInstanceOf[T]).filterNot(n => result.map(map(_)).contains(n))
    }

    // adds an element to the result
    def addElement(elem:Y) {
      result += elem
      // check if we can now add another element from pending
      for (p <- pendingNeed) {
        p._2 -= map(elem)
        if (p._2.isEmpty) {
          pendingNeed -= p._1
          addElement(p._1)
        }
      }
    }

    for (i <- items) {
      val rem = readyToInsert(i)
      if (rem.isEmpty) {
        addElement(i)
      } else {
        val set = new mutable.HashSet[T]()
        set ++= rem
        pendingNeed.put(i, set)
      }
    }

    result.toList
  }

  /**
   * Uses the partial order to order a list of items. If an item has its precondition not fulfilled needed items will be moved up.
   * @param items The list to sort
   * @param map Maps items to ids used by the order
   * @tparam Y The type of the items
   * @return The sorted list
   */
  def orderItemsUp[Y](items : List[Y], map:Y => T) : List[Y] = {
    val result = new ListBuffer[Y]()

    val revMap = new mutable.MapBuilder[T,Y,Map[T,Y]](Map.empty)
    for (i<-items)
      revMap += ((map(i),i))
    val revMap2 = revMap.result()

    // adds an element to the result
    def addElement(elem:Y) {
      // add items needed, but not yet added
      for (n <- graph.get(map(elem)).diPredecessors.map(_.value.asInstanceOf[T]).filterNot(n => result.map(map(_)).contains(n))) {
        addElement(revMap2(n))
      }
      result += elem
    }

    for (i <- items) {
      if (!result.contains(i)) // ensure we don't add elements double
        addElement(i)
    }

    result.toList
  }

  def exportToDOT(file:File): Unit = {
    exportToDOT(file.getAbsolutePath)
  }

  def exportToDOT(filename: String): Unit = {
    import scala.language.existentials
    // map the buckets the other way round
    val dotRoot = new DotRootGraph(true, Some("PartialOrder"), kvList = List(DotAttr("compound","true")))
    var i = 0
    val bucketsMap = new mutable.HashMap[T, DotSubGraph]()
    def addRecursive(b:T):DotSubGraph = {
      if (bucketsMap.contains(b)) return bucketsMap(b)
      buckets(b) match {
        case None =>
          i = i + 1
          val res = new DotSubGraph(dotRoot, "cluster" + i.toString, List(DotAttr("label", b.toString), DotAttr("color", "blue")))
          bucketsMap.put(b, res)
          res
        case Some(x) =>
          val parent = addRecursive(x)
          i = i + 1
          val res = new DotSubGraph(parent, "cluster" + i.toString, List(DotAttr("label", b.toString), DotAttr("color", "blue")))
          bucketsMap.put(b, res)
          res
      }
    }
    for (b <- buckets.keys) addRecursive(b)
    def edgeTransformer(innerEdge: scalax.collection.Graph[T,DiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      val edge = innerEdge.edge
      val attributes = new ListBuffer[DotAttr]()
      val from = bucketsMap.get(edge.from.value.asInstanceOf[T]) match {
        case None => edge.from.value.toString
        case Some(x) => attributes += new DotAttr("ltail", x.subgraphId)
          // find an element in the bucket
          elementBuckets.find(_._2 == edge.from.value.asInstanceOf[T]).get._1.toString
      }
      val to:String = bucketsMap.get(edge.to.value.asInstanceOf[T]) match {
        case None => edge.to.value.toString
        case Some(x) => attributes += new DotAttr("lhead", x.subgraphId)
          // find an element in the bucket
          elementBuckets.find(_._2 == edge.to.value.asInstanceOf[T]).get._1.toString // this in itself may be a bucket
      }
      Some((dotRoot,
        DotEdgeStmt(from, to, attributes)))
    }
    def nodeTransformer(innerNode: scalax.collection.Graph[T,DiEdge]#NodeT): Option[(DotGraph,DotNodeStmt)] = {
      bucketsMap.get(innerNode.value.asInstanceOf[T]) match {
        case None =>
          Some((bucketsMap(elementBuckets(innerNode.value.asInstanceOf[T])),
            DotNodeStmt(innerNode.value.toString,
              List(DotAttr("label", innerNode.value.toString))
            )))
        case Some(x) => Some((bucketsMap(elementBuckets(innerNode.value.asInstanceOf[T])),
          DotNodeStmt(innerNode.value.toString,
            List(DotAttr("label", " "), DotAttr("style","invisible"))
          )))
      }

    }

    val x: String = graph.toDot(dotRoot, edgeTransformer, Some(nodeTransformer), Some(nodeTransformer))
    IO.writeToFile(filename, x)
  }

  override def equals(o:Any) = {
    o match {
      case o:PartialOrder[T] => o.graph.equals(this.graph)
      case _ => false
    }
  }

  override def hashCode() = graph.hashCode()

}
