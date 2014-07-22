package org.vaadin.analprobe

import com.vaadin.ui.VerticalLayout
import com.vaadin.annotations.Widgetset
import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.UI
import com.vaadin.annotations.Title

@Title("Analog Data Collector")
@Widgetset("org.vaadin.analprobe.widgetset.AnalprobeWidgetset")
@Push
class AnalProbeUI extends UI {
  var chart: ProbeChart = _

  def init(request: VaadinRequest): Unit = {
    val layout: VerticalLayout = new VerticalLayout
    layout.setSizeFull
    layout.setMargin(true)
    setContent(layout)

    chart = new ProbeChart
    layout addComponent chart
  }
}
