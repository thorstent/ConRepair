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

import scalax.collection.Graph
import scala.collection.{GenSet, mutable}
import scalax.collection.GraphPredef._
import scala.Some
import scalax.collection.edge.LDiEdge
import scalax.collection.GraphEdge.{DiEdgeLike, DiEdge}

/**
 * Some additional functions because they are not working properly in the graph library
 */
object GraphExtensions {

  import scala.language.higherKinds
  implicit class RichGraph[N, E[X] <: EdgeLikeIn[X]](graph: Graph[N, E]) {
    /**
     * The NodeT.shortestPathTo seems to have a bug ... Here is a reimplementation with a breath-first search
     * @param from origin to search from
     * @param to target of the search
     * @return None if no path, otherwise the shortest path
     */
    def findPath(from:Graph[N,E]#NodeT, to:Graph[N,E]#NodeT, edgeFilter:Graph[N,E]#EdgeT => Boolean = _ => true): Option[List[Graph[N,E]#EdgeT]] = {
      val queue = new mutable.Queue[(Graph[N,E]#NodeT,List[Graph[N,E]#EdgeT])]()
      val globalSeen = new mutable.HashSet[Graph[N,E]#NodeT]()
      queue.enqueue((from,List()))
      while (queue.nonEmpty) {
        val (curr,seen) = queue.dequeue()
        if (curr == to)
          return Some(seen.reverse)
        if (globalSeen.add(curr)) {
          for (e <- curr.outgoing) {
            if (edgeFilter(e))
              queue.enqueue((e.to,e::seen))
          }
        }
      }
      None
    }


    /**
      * Not very efficient probably, but it removes transitive edges. This is mainly for display...
      * @param vertexSet The vertices that we want to remove transitive edges between
      * @param edgeFilterDelete What edges to delete when found transitive
      * @param edgeFilterWay What edges to consider to build a way.
      * @return A new graph without those edges
      */
    def removeTransitive(vertexSet:GenSet[N], edgeFilterDelete:Graph[N,E]#EdgeT => Boolean = _ => true, edgeFilterWay:Graph[N,E]#EdgeT => Boolean = _ => true):Graph[N,E] = {
      val elements = vertexSet.map(graph.find(_).get)
      var g = graph
      for (i <- elements)
        for (j <- elements) {
          if (i.hasSuccessor(j,edgeFilter=edgeFilterWay)) {
            for (k <- elements) {
               if (j.hasSuccessor(k,edgeFilter=edgeFilterWay)) {
                 for (e <- i.outgoingTo(k))
                   if (edgeFilterDelete(e))
                    g -= e
               }
            }
          }
        }
      g
    }

  }

  implicit class RichGraph2[N, E[X] <: LDiEdge[X]](graph: scalax.collection.mutable.Graph[N, E]) {

    /**
     * Not very efficient probably, but it removes transitive edges. This is mainly for display...
     * @param edgeFilterDelete What edges to delete when found transitive
     * @param edgeFilterWay What edges to consider to build a way.
     * @return A new graph without those edges
     */
    def removeTransitive(edgeFilterDelete:scalax.collection.mutable.Graph[N,E]#EdgeT => Boolean = _ => true, edgeFilterWay:scalax.collection.mutable.Graph[N,E]#EdgeT => Boolean = _ => true):Unit = {

      for (e <- graph.edges) {
        if (edgeFilterDelete(e)) {
          if (e.from.hasSuccessor(e.to, edgeFilter= x => edgeFilterWay(x) && x!=e))
            graph -= e
        }
      }
      /*       val elements = graph.nodes
      for (i <- elements)
        for (j <- elements) {
          if (i.hasSuccessor(j,edgeFilter=edgeFilterWay)) {
            for (k <- elements) {
              if (j.hasSuccessor(k,edgeFilter=edgeFilterWay)) {
                for (e <- i.outgoingTo(k))
                  if (edgeFilterDelete(e))
                    graph -= e
              }
            }
          }
        }*/
    }

  }
}
