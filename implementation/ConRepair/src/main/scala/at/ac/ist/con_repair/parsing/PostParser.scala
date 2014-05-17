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

package at.ac.ist.con_repair.parsing

import org.sosy_lab.cpachecker.cfa.model.{FunctionExitNode, FunctionEntryNode, CFAEdgeType, CFANode}
import at.ac.ist.con_repair.structures._
import collection.mutable.ListBuffer
import java.lang.IndexOutOfBoundsException
import org.sosy_lab.cpachecker.cfa.ast.c._
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge
import at.ac.ist.con_repair.helpers.{VariableAnalysisResult, Expressions}
import org.sosy_lab.cpachecker.cfa.ParseResult
import scala.collection.mutable

object PostParser {

  /**
   * Find the correct if structure from the CPA checker CFA
   */
  private def getIf(node: CFANode, localVariables: mutable.Set[CVariableDeclaration], otherFunctions: Set[String]): (CFANode,If) = {
    if (node.getNumLeavingEdges>1) {
      var joinNode: CFANode = null
      var the:List[Structure] = null
      var els:List[Structure] = null
      var condition:CExpression = null
      var i = node.getNumLeavingEdges - 1
      assert (node.getNumLeavingEdges == 2)

      // find the join node
      val seenNodes : mutable.ListBuffer[CFANode] = new mutable.ListBuffer()
      // go down left side
      var currNode = node.getLeavingEdge(0).getSuccessor
      while (currNode != null && !seenNodes.contains(currNode)) {
        seenNodes += currNode
        currNode = if (currNode.getNumLeavingEdges==0) null else currNode.getLeavingEdge(0).getSuccessor
      }
      currNode = node.getLeavingEdge(1).getSuccessor
      while(!seenNodes.contains(currNode)) {
        currNode = currNode.getLeavingEdge(0).getSuccessor
      }
      joinNode = currNode

      val (join1,e,_) = getProcessNode(node.getLeavingEdge(1).getSuccessor, localVariables, otherFunctions, joinNode)
      if (join1 != joinNode) throw new Exception("problems parsing: did you use && or || in the code?")
      els = e

      val (join2,t,_) = getProcessNode(node.getLeavingEdge(0).getSuccessor, localVariables, otherFunctions, joinNode)
      if (join2 != joinNode) throw new Exception("problems parsing: did you use && or || in the code?")
      the = t
      val assume = node.getLeavingEdge(0).asInstanceOf[CAssumeEdge]
      condition = if (assume.getTruthAssumption)
        assume.getExpression
      else
        Expressions.makeUnaryOperation(assume.getExpression, CUnaryExpression.UnaryOperator.NOT)

      (joinNode, new If(condition, the, els))
    }
    else
    {
      assert (assertion = false) // not an if note
      (null, null) // never reached
    }
  }

  private def getJoinNodeWhile(node: CFANode, localVariables: mutable.Set[CVariableDeclaration], otherFunctions: Set[String]): (CFANode,Structure) = {
    var cond:CExpression = null
    var negCond:CExpression = null
    var loop:List[Structure] = null
    var nextNode:CFANode = null
    if (node.getNumLeavingEdges == 2)
    {
      val condEdge = node.getLeavingEdge(0) // this edge loops
      assert (condEdge.asInstanceOf[CAssumeEdge].getTruthAssumption) // ensure that the condition is positive
      cond = condEdge.asInstanceOf[CAssumeEdge].getExpression
      negCond = Expressions.makeUnaryOperation(cond, CUnaryExpression.UnaryOperator.NOT)
      val (_,l,_) = getProcessNode(condEdge.getSuccessor, localVariables, otherFunctions, node)
      loop = l
      nextNode = node.getLeavingEdge(1).getSuccessor //this edge exists
    }
    else
    {
      // for while(true) there is only one leaving edge
      val condEdge = node.getLeavingEdge(0)
      cond = Expressions.makeIntConst(1, new CFileLocation(condEdge.getLineNumber, "", 0, 0, condEdge.getLineNumber))
      val (_,l,_) = getProcessNode(condEdge.getSuccessor, localVariables, otherFunctions, node)
      loop = l
    }
    if (loop.nonEmpty)
      (nextNode,new While(cond,loop))
    else {
      // this is only a waiting loop, we may make it an assume
      val assume = new AssumeStatement(Expressions.makeAssume(negCond, cond.getFileLocation), true)
      (nextNode,assume)
    }
  }

  private def getAtomicSection(node: CFANode, localVariables: mutable.Set[CVariableDeclaration], otherFunctions: Set[String],
                               origStmt:CStatement, atomic:Boolean): (CFANode,SectionBlock) = {
    val (next, stmt, _) = getProcessNode(node, localVariables, otherFunctions)
    (next, new SectionBlock(origStmt, stmt, atomic))
  }

  /**
   * Processes a node in the parse tree
   * @param node The current node
   * @param localVariables local variables that are discovered are saved here
   * @param otherFunctions names of other known functions
   * @param knownJoinNode Upon reaching this node we stop
   * @return (The next node, a list of structures, the return variable (if any))
   */
  private def getProcessNode(node: CFANode, localVariables: mutable.Set[CVariableDeclaration], otherFunctions: Set[String] ,knownJoinNode:CFANode = null) : (CFANode,List[Structure],Option[CIdExpression]) = {
    val lb = new ListBuffer[Structure]()
    var blockEnd = false // set to true when we found an atomic end or reorder end
    var retVar:Option[CIdExpression] = None

    // helper to see if the while should be finished
    def shouldLoopStop(node:CFANode,ignoreJoinNode:Boolean):Boolean = {
      if (blockEnd)
        return true
      if (node == null)
        return true
      if (node == knownJoinNode)
        return true
      try {
        if (node.getNumEnteringEdges >=1 && (node.getLeavingEdge(0).getEdgeType eq CFAEdgeType.FunctionReturnEdge))
          return true
        // a bug in CFAchecker forces us to add this check
      } catch
      {
        case e: IndexOutOfBoundsException => return true
      }
      if (!node.isLoopStart && node.getNumEnteringEdges > 1 && !ignoreJoinNode && !node.isInstanceOf[FunctionEntryNode])
        return true
      false
    }

    var nextNode = node
    var ignoreJoinNode = false // Normally we exit the loop as soon as we see a joining, assuming we reached the end of the
    // if. However, if we just returned from an if we should ignore that
    while (!shouldLoopStop(nextNode, ignoreJoinNode)) {
      ignoreJoinNode = false
      if (nextNode.isLoopStart) {
        // while statement
        val(n,wh) = getJoinNodeWhile(nextNode, localVariables, otherFunctions)
        nextNode = n
        lb += wh
      }
      else if (nextNode.getNumLeavingEdges>1) {
        // if statement
        val(n,ifs) = getIf(nextNode, localVariables, otherFunctions)
        nextNode = n
        lb += ifs
        ignoreJoinNode = true
      }
      else if (nextNode.getLeavingEdge(0).getEdgeType == CFAEdgeType.FunctionCallEdge)
      {
        assert (assertion = false) // we should not see this case
      }
      else
      {
        val edge = nextNode.getLeavingEdge(0)
        nextNode = edge.getSuccessor
        if (edge.getEdgeType != CFAEdgeType.BlankEdge) {
          val stmt:Any = edge.getRawAST.get()
          // in case it is a declaration merged with an assignment
          stmt match {
            case decl: CVariableDeclaration =>
              localVariables.add(decl)
              // make an assignment out of the edge
              if (decl.getInitializer != null) {
                val asgn = new CExpressionAssignmentStatement(decl.getFileLocation,
                  new CIdExpression(decl.getFileLocation, decl.getType, decl.getName, decl),
                  decl.getInitializer.asInstanceOf[CInitializerExpression].getExpression)
                lb += new Statement(asgn)
              }
            case stmt:CStatement =>
              SpecialStatement.from(stmt) match {
                case Some(s) => lb += s
                case None =>
                  if (stmt.isInstanceOf[CFunctionCallStatement] || stmt.isInstanceOf[CFunctionCallAssignmentStatement]) {
                    // this is a function call, but if we call assume or assert, this needs to be handled
                    val functionCalled = stmt.asInstanceOf[CFunctionCall].getFunctionCallExpression.getFunctionNameExpression.asInstanceOf[CIdExpression].getName
                    functionCalled match {
                      case "assume" => lb += new AssumeStatement(stmt.asInstanceOf[CFunctionCallStatement])
                      case "assert" => lb += new AssertStatement(stmt.asInstanceOf[CFunctionCallStatement])
                      case "asserta" => val n = new AssertStatement(stmt.asInstanceOf[CFunctionCallStatement])
                        n.analyse = false
                        lb += n
                      case "assertd" => val n = new AssertStatement(stmt.asInstanceOf[CFunctionCallStatement])
                        n.deadlockAssertion = true
                        lb += n
                      case "atomicBegin" => val(n,section) = getAtomicSection(nextNode, localVariables, otherFunctions, stmt, atomic = true)
                        nextNode = n // overwrite nextNode
                        lb += section
                      case "noReorderBegin" => val(n,section) = getAtomicSection(nextNode, localVariables, otherFunctions, stmt, atomic = false)
                        nextNode = n // overwrite nextNode
                        lb += section
                      case "atomicEnd" => blockEnd = true
                      case "noReorderEnd" => blockEnd = true
                      case "nondet_int" => lb += new Statement(stmt)
                      case _ => lb += new FunctionCallStatement(stmt)
                    }
                  } else {
                    lb += new Statement(stmt)
                  }
              }
            case ret:CReturnStatement =>
              ret.getReturnValue match {
                case id:CIdExpression =>
                  if (edge.getSuccessor.isInstanceOf[FunctionExitNode]) new Exception("return must be last statement")
                  retVar = Some(id)
                case _ => new Exception("only variables may be returned")
              }
          }
        }
      }
    }

    (nextNode, lb.result(), retVar)
  }

  // level 1 makes functions
  def getFunction(name:String,node:FunctionEntryNode, declaration: CFunctionDeclaration, otherFunctions: Set[String]):Function = {
    //System.out.println("parsing " + name) // needed to detected stack-overflow
    val localVariables = mutable.Set[CVariableDeclaration]()
    val (_,l, ret) = getProcessNode(node, localVariables, otherFunctions)
    val fun = new Function(name,l, declaration, localVariables.toSet, name.contains("thread"),
      ret, childrenSortable = name!="main")
    fun
  }

  // here we collect the names of locks in the program
  /*def secondRound(p:Program) = {
    def processStructure(str:Structure):Boolean = {
      if (str.isInstanceOf[Statement]) {
        val stmt = str.asInstanceOf[Statement]
        ExpressionHelpers.getFunctionDef(stmt.getEdge) match {
          case None => ()
          case Some((name,arg1)) =>
            if (name == "lock") {
              ExpressionHelpers.getName(arg1) match {
                case None => null
                case Some(name) => stmt.getProgramLevel().addLockName(name)
            }
          }
        }
      }
      return true
    }
    p.processAllStructuresByOne(processStructure)
  }*/

  def postParse(parseResult: ParseResult , originalProgram:String):Program = {
    //val functions = otherFunctions.map(t => PostParser.getFunction(t._2, t._1)) ++ threads.map(t => if (t._2.endsWith("_ns")) PostParser.getFunctionNotSortable(t._2, t._1) else PostParser.getFunction(t._2, t._1))

    import collection.JavaConversions._

    val (functionDecls: mutable.Buffer[CDeclaration], otherDecls) = parseResult.getGlobalDeclarations.map(p => p.getFirst).partition { p => p.isInstanceOf[CFunctionDeclaration]}
    val functionDeclsMap : Map[String, CFunctionDeclaration] = functionDecls.map(
      {decl : CDeclaration =>
        val fdecl = decl.asInstanceOf[CFunctionDeclaration]
        (fdecl.getName, fdecl)
      }).toMap

    val otherFunctions = parseResult.getFunctions.filter {case (name,f) => name != "main"}
    val functionNames : Set[String] = otherFunctions.map {
      case (name, f) => name
    }.toSet

    val main: Function = getFunction("main", parseResult.getFunctions.get("main"), functionDeclsMap("main"), functionNames)

    val functions = main :: otherFunctions.map {
      case (name, f) => getFunction(name, f, functionDeclsMap(name), functionNames)
    }.toList

    val globalVariables = otherDecls.toList.filterNot(_.getName == "nondet") // nondet is not really a variable

    val p = new Program(functions, originalProgram, globalVariables)
    //secondRound(p)
    p
  }
}
