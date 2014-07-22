package org.vaadin.analprobe

import javax.servlet.GenericServlet
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import java.util.Date
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable
import javax.servlet.ServletException
import java.io.IOException
import org.vaadin.analprobe.data.ProbeData
import org.vaadin.analprobe.data.ProbeEntry

class ProbeCollector extends GenericServlet {
  @throws(classOf[ServletException])
  @throws(classOf[IOException])
  override def service(request: ServletRequest, response: ServletResponse) = {
    breakable {
      val strValue: String = request.getParameter("value")
      if (strValue == null) {
        response.getWriter().append("ERROR: NO VALUE");
        break
      }

      val value: Int = Integer.valueOf(strValue);
      if (value < 0 || value > 1024) {
        response.getWriter().append("ERROR: INVALID VALUE");
        break
      }

      val probeIdStr: String = request.getParameter("probe")
      if (probeIdStr == null) {
        response.getWriter().append("ERROR: NO PROBE");
        break
      }
      val probeId: Int = Integer.valueOf(probeIdStr)

      val entry: ProbeEntry = new ProbeEntry(new Date(), value)
      ProbeData.synchronized {
        ProbeData.getOrCreateProbe(probeId).add(entry, ProbeData.journal);
      }

      response.getWriter().append("ACK");
    }
  }
}