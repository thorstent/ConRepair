/*
 * Copyright 2013-2014, IST Austria
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

import java.io.BufferedReader
import java.lang.InterruptedException
import at.ac.ist.con_repair.Main

/**
 * This will monitor stdin and process commands it reads there
 */
object CommandReader {
  val monitor = new Thread(new Runnable {
    def run() {
      try {
        while(true) {
          val line = Console.readLine()
          processCommand(line)
        }
      } catch {
        case _:InterruptedException =>
      }
    }
  })

  def start() = {
    monitor.setDaemon(true)
    monitor.start()
  }

  private def processCommand(cmd:String):Unit = {
    cmd match {
      case "cancel" =>
        Console.println("Cancelling as soon as possible")
        Main.cancel = true
      case _ => Console.println("Command not understood")
    }
  }
}
