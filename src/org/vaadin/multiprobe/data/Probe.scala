package org.vaadin.multiprobe.data

import java.util.Date
import scala.collection.mutable.LinkedList
import java.io.Writer
import java.io.IOException
import java.util.Observable

/**
 * Describes one instrument attached to the collector device.
 */
class Probe(val id: Int) extends Observable {
  var data: LinkedList[ProbeEntry] = LinkedList()
  var afterLast: LinkedList[ProbeEntry] = data
  var last : LinkedList[ProbeEntry] = null

  def add(entry: ProbeEntry, journal: Writer) = {
    afterLast.elem = entry
    afterLast.next = LinkedList()
    last = afterLast
    afterLast = afterLast.next
    
    if (journal != null)
      try {
        entry.write(id, journal)
      } catch {
        case e: IOException =>
          System.err.println("Could not write entry to journal");
      }
  }
  
  override def toString: String = {
    if (data.isEmpty)
      "{}"
    else
      "{" + data.map(entry => "(" + entry.time + "," + entry.value + ")").reduceLeft((a,b) => a + ", " + b) + "}"
  }
}
