package org.vaadin.multiprobe

import com.github.wolfie.refresher.Refresher
import com.github.wolfie.refresher.Refresher.RefreshListener
import com.vaadin.annotations.Title
import com.vaadin.annotations.Widgetset
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout

@Title("Analog Data Collector")
@Widgetset("org.vaadin.multiprobe.widgetset.MultiprobeWidgetset")
class MultiProbeUI extends UI {
  var chart: ProbeChart = _

  def init(request: VaadinRequest) = {
    val layout: VerticalLayout = new VerticalLayout
    layout.setSizeFull
    layout.setMargin(true)
    setContent(layout)

    chart = new ProbeChart
    layout addComponent chart

    // Set up polling for more data
    new Refresher() {
      extend(UI.getCurrent())
      setRefreshInterval(1000)
      addListener(new RefreshListener {
        override def refresh(source: Refresher) {
          chart.refreshData(true)
        }
      })
    }
  }
}
