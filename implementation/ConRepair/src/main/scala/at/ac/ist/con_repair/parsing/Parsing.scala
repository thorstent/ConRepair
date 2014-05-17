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

import org.sosy_lab.cpachecker.cfa.{DOTBuilder, ParseResult}
import org.sosy_lab.cpachecker.cfa.CParser.Factory
import java.io.File
import at.ac.ist.con_repair.helpers.IO
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode

object Parsing {
  private val declarationString = "void noReorderBegin();" +
    "void noReorderEnd();" +
    "void atomicBegin();" +
    "void atomicEnd();" +
    "void assume(int);" +
    "void assert(int);" +
    "void unlock(int);" +
    "void up(int);" +
    "void down(int);" +
    "void lock(int);" +
    "int nondet;" +
    "int nondet_int();" +
    "void asserta(int);" +
    "void assertd(int);"

  def parse(originalProgram:String)(implicit debugOutDir: Option[File]): (ParseResult) =
  {
    val program = originalProgram + declarationString
    val parser = Factory.getParser(Helpers.logManager, Factory.getDefaultOptions)
    val prog: ParseResult = parser.parseString(program)

    import scala.collection.JavaConversions._
    debugOutDir match { case None =>  case Some(debugOutDir2) =>
      for(x: (String, FunctionEntryNode) <- prog.getFunctions) {
        val dot = DOTBuilder.generateDOT(prog.getFunctions.values(),x._2)
        val dotFile = new File(debugOutDir2, "0-" + x._1 + ".dot")
        IO.writeToFile(dotFile, dot)
      }

    }

    prog
  }

}
