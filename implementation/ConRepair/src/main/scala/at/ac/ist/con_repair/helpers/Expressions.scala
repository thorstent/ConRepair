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

import java.math.BigInteger
import scala.Some
import org.sosy_lab.cpachecker.cfa.ast.c._
import org.sosy_lab.cpachecker.cfa.types.c.{CStorageClass, CBasicType, CSimpleType, CType}


object Expressions {
  /*def dummyNode(functionName:String) = {
    new CStatement(0, functionName)
  }*/

  /*val dummyFunctionExitNode = new CFAFunctionExitNode(0, "")
  val dummyFunctionDefNode = new FunctionDefinitionNode(0, "", null, dummyFunctionExitNode, new java.util.ArrayList[IASTParameterDeclaration](), new java.util.ArrayList[String]()) {}
*/

  def syntheticLocation(line:Int, appendix:Char) : CFileLocation = {
    new CFileLocation(line,appendix.toString,0,0,0)
  }

  def isSyntheticLocation(location:CFileLocation) = {
    location.getFileName.length == 1 &&
      location.getEndingLineNumber != 0 &&
      location.getNodeLength == 0 &&
      location.getNodeOffset == 0 &&
      location.getStartingLineNumber == 0
  }

  def getSyntheticLocation(location:CFileLocation) : (Int,Char) = {
    (location.getEndingLineNumber, location.getFileName()(0))
  }

  val syntheticLocation = new CFileLocation(0,"",0,0,0)

  def isGlobal(exp:CIdExpression):Boolean = {
    exp.getDeclaration match {
      case d:CVariableDeclaration => d.isGlobal
      case d:CParameterDeclaration => false
      case d:CFunctionDeclaration => d.isGlobal // function pointer variable
      case _ => assert(false); false
    }
  }

  def getChangedVariables(stmt: CStatement): Set[Variable] = {
    stmt match {
      case assignment: CAssignment =>
        val exp: CExpression = assignment.getLeftHandSide
        exp match {
          case exp:CIdExpression => return Set((exp.getName,isGlobal(exp)))
          case _ =>
        }
      case _ =>
    }
    Set.empty
  }

  def getUsedVariables(stmt: CStatement): Set[Variable] = {
    import scala.collection.JavaConversions._
    stmt match {
      case assignment: CAssignment =>
        val exp: CRightHandSide = assignment.getRightHandSide
        return getUsedVariables(exp)
      case funCall : CFunctionCall =>
        val exp = stmt.asInstanceOf[CFunctionCall].getFunctionCallExpression
        return exp.getParameterExpressions.foldLeft(Set.empty:Set[Variable])((list, ast) => getUsedVariables(ast) ++ list)
      case _ =>
    }
    Set.empty
  }

  def getUsedVariables(exp: CRightHandSide) : Set[Variable] = {
    exp match {
      case expression: CBinaryExpression =>
        getUsedVariables(expression.getOperand1) ++ getUsedVariables(expression.getOperand2)
      case expression: CUnaryExpression =>
        getUsedVariables(expression.getOperand)
      case exp:CIdExpression => Set((exp.getName,isGlobal(exp)))
      case _ => Set()
    }
  }

  def isNondet(condition:CExpression):Boolean = {
    condition match {
      case c:CBinaryExpression => c.getOperand1 match  {
        case i:CIdExpression => i.getName == "nondet"
        case _ => false
      }
      case c:CUnaryExpression => isNondet(c.getOperand)
      case _ => false
    }
  }

  // get the node where the two join again
  /*def getUnifyingNode(edge: CFAEdge) : (CFANode,List[CFAEdge]) = {
    var edges = List(edge)
    var currEdge = edge
    var nextnode = edge.getSuccessor
    while (nextnode.getNumEnteringEdges == 1)
    {
      currEdge = nextnode.getLeavingEdge(0)
      edges ++= List(currEdge)
      nextnode = currEdge.getSuccessor
    }
    (nextnode, edges)
  }

  // gives us the next node and the edges
  // if current node is join node then we go from there (if ignoreFirstIsJoin)
  // if current node is split node we return immediatelly
  def getLongestLine(node: CFANode, ignoreFirstIsJoin: Boolean = false) : (CFANode,List[CFAEdge]) = {
    var nextNode = node
    val res = new ListBuffer[CFAEdge]
    var first = ignoreFirstIsJoin
    while ((first || nextNode.getNumEnteringEdges <= 1) && (nextNode.getNumLeavingEdges == 1)) {
      val edge = nextNode.getLeavingEdge(0)
      if (edge.getEdgeType eq CFAEdgeType.FunctionReturnEdge)
        return (nextNode, res.toList)
      res += edge
      nextNode = edge.getSuccessor
      first = false
    }
    return (nextNode, res.toList)
  }

  def printEdge(edge: CFAEdge) : String = {
    if (edge.getEdgeType eq CFAEdgeType.AssumeEdge)
    {
      return "assume (" + edge.getRawAST.toASTString.trim.stripPrefix("[").stripSuffix("]") + ")"
    }
    else
      return edge.getRawAST.toASTString.stripSuffix(";")
  }

  // if function defition returns the name and the first argument
  def getFunctionDef(edge: CFAEdge): Option[(String,IASTExpression)] = {
    if (edge.getRawAST.isInstanceOf[IASTFunctionCallStatement]) {
      val stmt = edge.getRawAST.asInstanceOf[IASTFunctionCallStatement]
      val expr = stmt.getFunctionCallExpression.getFunctionNameExpression
      if (expr.isInstanceOf[IASTIdExpression]) {
        val name = expr.asInstanceOf[IASTIdExpression].getName
        var arg1 : IASTExpression = null
        if (stmt.getFunctionCallExpression.getParameterExpressions().size() > 0)
          arg1 = stmt.getFunctionCallExpression.getParameterExpressions().get(0)
        return Some((name,arg1))
      }
    }
    return None;
  }*/

  def getName(expr: CExpression) : Option[String] = {
    if (expr.isInstanceOf[CIdExpression])
      Some(expr.asInstanceOf[CIdExpression].getName)
    else None
  }

  //def getFunctionName(edge:CFAEdge) = edge.getPredecessor.getFunctionName

  def makeDeclaration(name:String, global:Boolean, typ : CType=new CSimpleType(false,false,CBasicType.INT,false,false,true,false,false,false,false)) =
    new CVariableDeclaration(syntheticLocation, global, CStorageClass.AUTO, typ, name, name, null)

  def makeName(name:String,decl:CSimpleDeclaration) = new CIdExpression(syntheticLocation, null, name, decl)
  def makeName(name:String,global:Boolean=true):CIdExpression = makeName(name, makeDeclaration(name, global))
  def makeName(decl:CParameterDeclaration):CIdExpression = makeName(decl.getName, decl)

  def makeBinaryOperation(op1: CExpression, op2: CExpression, operator: CBinaryExpression.BinaryOperator) =
    new CBinaryExpression(syntheticLocation, null, op1, op2, operator)

  def makeUnaryOperation(op1: CExpression, operator: CUnaryExpression.UnaryOperator) =
    new CUnaryExpression(op1.getFileLocation, null, op1, operator)

  def makeIntConst(number:Long, location: CFileLocation = syntheticLocation) = new CIntegerLiteralExpression(location, null, new BigInteger(number.toString))

  def makeAssume(condition: CExpression, location: CFileLocation = syntheticLocation) : CFunctionCallStatement = {
    val param = new java.util.ArrayList[CExpression]()
    param.add(condition)
    val exp = new CFunctionCallExpression(location, null, makeName("assume"), param, null)
    new CFunctionCallStatement(location, exp)
  }

  def makeFunctionCall(name:String, args: List[CExpression] = List()): CFunctionCallExpression = {
    import scala.collection.JavaConversions._
    new CFunctionCallExpression(syntheticLocation, null, makeName(name), args, null)
  }

  def makeAssignment(left:CIdExpression, right:CExpression, location:CFileLocation = syntheticLocation) : CExpressionAssignmentStatement = {
    new CExpressionAssignmentStatement(location, left, right)
  }

  /*def makeFunctionEdge(name:String, args: List[IASTExpression] = List(), functionName:String): CFAEdge = {
    import scala.collection.JavaConversions._
    val ast:IASTFunctionCallStatement = new IASTFunctionCallStatement(name + "(" + args.map(_.toASTString).mkString(",") + ")",null,makeFunctionCall(name, args, functionName))
    val edge = new FunctionCallEdge(name + "(" + args.map(_.toASTString).mkString(",") + ")", ast, 0, dummyNode(functionName), dummyFunctionDefNode, args, null)
    return edge
  }

  def makeFunctionEdgeT(name:String, args: List[IASTExpression] = List(), functionName:String): CFAEdge with OtherLock = {
    import scala.collection.JavaConversions._
    val ast:IASTFunctionCallStatement = new IASTFunctionCallStatement(name + "(" + args.map(_.toASTString).mkString(",") + ")",null,makeFunctionCall(name, args, functionName))
    val edge = new FunctionCallEdge(name + "(" + args.map(_.toASTString).mkString(",") + ")", ast, 0, dummyNode(functionName), dummyFunctionDefNode, args, null) with OtherLock
    return edge
  }

  def makeAssertEdge(assert:IASTExpression, functionName:String):CFAEdge = {
    makeFunctionEdge("assert", List(assert), functionName)
  }

  def makeAssertEdgeT(assert:IASTExpression, functionName:String):CFAEdge with OtherLock = {
    makeFunctionEdgeT("assert", List(assert), functionName)
  }

  def makeAssignmentEdge(variable:String, value:IASTExpression, functionName:String, global:Boolean = true): CFAEdge = {
    val stmt: IASTStatement = new IASTExpressionAssignmentStatement(variable + " = " + value.toASTString, null, makeName(variable, global), value)
    return new StatementEdge(stmt, 0, dummyNode(functionName), dummyNode(functionName))
  }

  def makeAssignmentEdge(variable:String, value:IASTFunctionCallExpression, functionName:String): CFAEdge = {
    val stmt: IASTStatement = new IASTFunctionCallAssignmentStatement(variable + " = " + value.toASTString(), null, makeName(variable), value)
    return new StatementEdge(stmt, 0, dummyNode(functionName), dummyNode(functionName))
  }

  def makeAssumeEdge(assumption: IASTExpression, functionName:String): CFAEdge = {
    new AssumeEdge(assumption.toASTString, 0, dummyNode(functionName), dummyNode(functionName), assumption, true)
  }*/
}