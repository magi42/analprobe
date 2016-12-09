package org.vaadin.analprobe.data

import java.io.Writer
import java.util.Date

class ProbeEntry(val time: Date, val value: Double) {
  def write(probeId: Int, writer: Writer) = {
    writer.write(ProbeData.dateFormat.format(time) + " " + probeId + " " + value)
    writer.append("\n")
    writer.flush()
  }
}
