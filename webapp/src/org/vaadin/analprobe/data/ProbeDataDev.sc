package org.vaadin.multiprobe.data
import scala.collection.mutable.LinkedList
import java.util.Date
import java.util.GregorianCalendar
import java.math

object ProbeDataDev {
  val probe: Probe = new Probe(0)                 //> probe  : org.vaadin.multiprobe.data.Probe = {}
  
  // Put in some test data
  var time: Long = new GregorianCalendar(2013,0,1,12,0,0).getTime().getTime()
                                                  //> time  : Long = 1357034400000
  for (i <- 0 until 100) {
    val value = Math.random()
    time += (3000*Math.random()).toInt
    probe.add(new ProbeEntry(new Date(time), value), null)
  }
  
  var window = new ListWindow(probe.data)         //> window  : org.vaadin.multiprobe.data.ListWindow[org.vaadin.multiprobe.data.P
                                                  //| robeEntry] = org.vaadin.multiprobe.data.ListWindow@502cb49d
  
  // Find the time window
  val endTime: Date = probe.last.elem.time        //> endTime  : java.util.Date = Tue Jan 01 12:02:34 EET 2013
  val startTime: Date = new Date(endTime.getTime() - 1000*100)
                                                  //> startTime  : java.util.Date = Tue Jan 01 12:00:54 EET 2013
  window.init(probe => probe.time.after(startTime) && probe.time.before(endTime))
}