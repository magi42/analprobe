package org.vaadin.multiprobe.data

import junit.framework.Assert._
import junit.framework.TestCase
import java.util.Date

class ProbeDataTest extends TestCase {
  def testProbeAdd() {
    var probe: Probe = new Probe(1)
    probe.add(new ProbeEntry(new Date(), 1), null)
    probe.add(new ProbeEntry(new Date(), 2), null)
    probe.add(new ProbeEntry(new Date(), 3), null)
    probe.add(new ProbeEntry(new Date(), 4), null)
    probe.add(new ProbeEntry(new Date(), 5), null)
    assertEquals(5, probe.data.length)
  }

  def testProbeData() {
    val data = ProbeData
    // println ("Probes = " + data.probes.size)
    // assertEquals(network.units.size, 5)
  }
}