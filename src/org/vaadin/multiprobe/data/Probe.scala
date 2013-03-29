package org.vaadin.multiprobe.data

import java.util.Date
import scala.collection.mutable.LinkedList
import java.io.Writer
import java.io.IOException

/**
 * Describes one instrument attached to the collector device.
 */
class Probe(val id: Int) {
  var data: LinkedList[ProbeEntry] = new LinkedList[ProbeEntry]

  def add(entry: ProbeEntry, journal: Writer) = {
    if (journal != null)
      try {
        entry.write(id, journal)
      } catch {
        case e: IOException =>
          System.err.println("Could not write entry to journal");
      }
  }
}
