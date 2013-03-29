package org.vaadin.multiprobe.data

import java.text.SimpleDateFormat
import scala.collection.mutable.HashMap
import java.io.BufferedWriter
import java.io.FileNotFoundException
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader
import scala.io.Source
import java.util.Date
import java.text.ParseException
import java.io.FileWriter

/**
 * Contains the data from all probes.
 *
 * This is a singleton object, to which the collector writes data and UIs read from.
 */
object ProbeData {
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
  var probes: HashMap[Int, Probe] = new HashMap[Int, Probe]
  var journal: BufferedWriter = _

  {
    val datafile: String = System.getenv("HOME") + "/analprobe.dat"
    println("Opening data journal " + datafile)

    try {
      val reader: FileReader = new FileReader(datafile)
      val in: BufferedReader = new BufferedReader(reader)
      var readLines: Int = 0

      for (line: String <- Source.fromFile(datafile).getLines) {
        val trimmed: String = line.trim()
        val split = trimmed.split(" ")
        if (split.length == 3) {
          try {
            // Probe events include a date, probe ID, and measurement
            val date: Date = dateFormat.parse(split(0))
            val probeId: Int = Integer.parseInt(split(1))
            val value: Int = Integer.parseInt(split(2))

            getOrCreateProbe(probeId).add(new ProbeEntry(date, value), null)
          } catch {
            case e: ParseException =>
              System.err.println("Invalid format '" +
                e.getMessage() + " in " + line)
          }
        } else
          System.err.println("Invalid format: " + line)
      }

      in.close()
      println(readLines + " entries read")
    } catch {
      case e: FileNotFoundException =>
        println("Journal not found, creating one.")
      case e: IOException =>
        System.err.println(e.getMessage())
    }

    // Start writing more
    try {
      val writer: FileWriter = new FileWriter(datafile, true);
      journal = new BufferedWriter(writer);
    } catch {
      case e: IOException =>
        System.err.println(e.getMessage());
    }
  }

  def getOrCreateProbe(probeId: Int): Probe = {
    if (probes.contains(probeId))
      probes(probeId)
    else {
      val newProbe = new Probe(probes.size)
      probes.put(newProbe.id, newProbe)
      newProbe
    }
  }
}
