package org.vaadin.multiprobe

import com.vaadin.annotations.Title
import com.vaadin.annotations.Widgetset
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.annotations.Push

@Title("Analog Data Collector")
@Widgetset("org.vaadin.multiprobe.widgetset.MultiprobeWidgetset")
@Push
class MultiProbeUI extends UI {
  var chart: ProbeChart = _

  def init(request: VaadinRequest) = {
    val layout: VerticalLayout = new VerticalLayout
    layout.setSizeFull
    layout.setMargin(true)
    setContent(layout)

    chart = new ProbeChart
    layout addComponent chart
  }
}
