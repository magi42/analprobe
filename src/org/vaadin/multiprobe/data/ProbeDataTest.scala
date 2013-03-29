package org.vaadin.multiprobe.data

import junit.framework.Assert._
import junit.framework.TestCase

class ProbeDataTest extends TestCase {
  def testProbeData() {
    val data = ProbeData
    println ("Probes = " + data.probes.size)
    // assertEquals(network.units.size, 5)
  }
}