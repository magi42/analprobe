package org.vaadin.analprobe

import java.util.Date

import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedList
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.vaadin.analprobe.data.ListWindow
import org.vaadin.analprobe.data.Probe
import org.vaadin.analprobe.data.ProbeData
import org.vaadin.analprobe.data.ProbeEntry

import com.vaadin.addon.charts.Chart
import com.vaadin.addon.charts.model.AxisType
import com.vaadin.addon.charts.model.ChartType
import com.vaadin.addon.charts.model.Configuration
import com.vaadin.addon.charts.model.DataSeries
import com.vaadin.addon.charts.model.DataSeriesItem
import com.vaadin.addon.charts.model.Marker
import com.vaadin.addon.charts.model.PlotOptionsArea
import com.vaadin.addon.charts.model.Title
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.data.util.BeanItemContainer
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Button.ClickListener
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.VerticalLayout

class ProbeChart extends CustomComponent {
  var timescale: Int = _
  var windows: HashMap[Int, ProbeWindow] = new HashMap[Int, ProbeWindow]

  class TimeScale(val caption: String, val duration: Int) extends Serializable

  class ProbeWindow(val probe: Probe) extends ListWindow[ProbeEntry](probe.data) {
    val series: DataSeries = new DataSeries
    var currentValue: DataSeriesItem = _

    override def clear = {
      super.clear
      series.clear()
      println("Cleared window")
    }

    /** Forwards the beginning of the time window to the given time */
    def forwardStart(windowStart: Date) {
      if (!probe.data.isEmpty) {
        val startSecond: Long = windowStart.getTime();

        if (first == null) {
          first = probe.data

          // Forward until we reach the start of the window
          while (!first.tail.isEmpty && first.head.time.getTime() > startSecond)
            if (first.head.time.getTime() <= startSecond) {
              // System.out.println("Forwarding over " + first.getTime());
              first = first.tail
            }
        }

        // Remove old items until we reach current time
        // System.out.println("Forwarding start time");
        while (series.size() > 0 && series.get(0).getX().longValue() < startSecond) {
          val seriesCurrent: DataSeriesItem = series.get(0);
          // System.out.println("Checking if " + seriesCurrent.getX().longValue() + " >= " + startSecond);
          series.remove(series.get(0));
          // System.out.println("Removed an old item from window");
        }
      }
    }

    def forwardEnd(windowEnd: Date, updateChart: Boolean) = {
      var init: Boolean = false
      if (last == null) {
        last = first
        init = true;
      }

      // Remove old items until we reach current time
      val endTime: Long = windowEnd.getTime()
      var added: Boolean = false
      breakable {
        while (init || !last.tail.isEmpty) {
          if (!init)
            last = last.tail
          init = false

          if (series.size() > 0) {
            // Check if the series is up-to-date
            val seriesLast: DataSeriesItem = series.get(series.size() - 1)
            // System.out.println("Is " + seriesLast.getX().longValue() + " later than " +endTime);
            if (seriesLast.getX().longValue() > endTime)
              break
          }

          // If the chart is empty
          if (currentValue == null) {
            currentValue = new DataSeriesItem(last.head.time, last.head.value)
            series.add(currentValue, updateChart, false)
          } else {
            // Replace the current level with the new data point value
            currentValue.setX(last.head.time.getTime())
            currentValue.setY(last.head.value)
            series.update(currentValue)
          }

          // boolean shift = (series.size() > 2 && currentValue.getX().longValue() - series.get(0).getX().longValue() >= timescale*1000);

          // Create the new current point that holds the last level
          currentValue = new DataSeriesItem(last.head.time, last.head.value)
          series.add(currentValue, updateChart, false);
          // System.out.println("Added series item " + first.getTime() + " value=" + last.getValue());

          added = true;
        }
      }

      // If no points were added, move the current level point to current time
      if (!added) {
        currentValue.setX(windowEnd.getTime());
        series.update(currentValue);
      }
    }
  }

  def setScale(seconds: Int) = {
    timescale = seconds

    // Reset all data series.
    // The series will be refilled on next request
    ProbeData.synchronized {
      for (probe <- ProbeData.probes.values) {
        // Creates a new window if none exists for the probe
        val window: ProbeWindow = getProbeWindow(probe.id)

        window.clear

        // Go to the beginning of the window
        val startTime: Date = new Date((new Date().getTime() - timescale * 1000));
        window.forwardStart(startTime);
      }
    }

    // TODO refreshData(false)
    chart.drawChart(conf);
  }

  def refreshData(updateChart: Boolean) = {
    ProbeData.synchronized {
      // Check if there is new data from any of the available probes
      for (probe: Probe <- ProbeData.probes.values) {
        val window: ProbeWindow = getProbeWindow(probe.id)
        val currentTime: Date = new Date()

        // Wind the beginning of the window forward if it has
        // been left far beyond
        val criticalTime: Date = new Date((currentTime.getTime() - 4 * timescale * 1000));
        val startTime: Date = new Date((currentTime.getTime() - timescale * 1000));
        if (window.series.size() > 2 && window.series.get(0).getX().longValue() < criticalTime.getTime())
          window.forwardStart(startTime);

        // Go forward with the end of the window (to the end of data)
        window.forwardEnd(currentTime, updateChart);
      }
    }
  }

  private def getProbeWindow(probeId: Int): ProbeWindow = {
    if (!windows.contains(probeId))
      newProbe(probeId)
    windows(probeId)
  }

  private def newProbe(probeId: Int) = {
    // Put the data in a Charts DataSeries which displays a window of the all data
    val window: ProbeWindow = new ProbeWindow(ProbeData.getOrCreateProbe(probeId));
    val series: DataSeries = window.series
    series.setName("Probe " + probeId)
    conf.addSeries(series);

    // Mapping from the probe ID to the object
    windows.put(probeId, window);
  }

  val content: VerticalLayout = new VerticalLayout
  content.setSizeFull()
  setSizeFull()

  var chart: Chart = new Chart(ChartType.AREA)
  chart.setSizeFull()
  content.addComponent(chart)
  content.setExpandRatio(chart, 1.0f)

  // Configure chart  
  val conf: Configuration = chart.getConfiguration()
  conf.setTitle("Probe Data")

  // Set general plot options
  val plotOptions: PlotOptionsArea = new PlotOptionsArea()
  plotOptions.setMarker(new Marker(false))
  conf.setPlotOptions(plotOptions)

  // Configure X axis
  conf.getxAxis().setType(AxisType.DATETIME)
  conf.getxAxis().setTitle(new Title(""))

  val controls: HorizontalLayout = new HorizontalLayout
  content.addComponent(controls)

  // Time scale selection
  val timescales: Array[TimeScale] = Array(
    new TimeScale("1 year", 60 * 60 * 24 * 365),
    new TimeScale("1 day", 60 * 60 * 24),
    new TimeScale("6 hours", 60 * 60 * 6),
    new TimeScale("1 hour", 60 * 60),
    new TimeScale("10 minutes", 60 * 10),
    new TimeScale("1 minute", 60),
    new TimeScale("15 seconds", 15))
  val scalecontainer: BeanItemContainer[TimeScale] =
    new BeanItemContainer[TimeScale](classOf[TimeScale])

  for (timescale <- timescales)
    scalecontainer.addBean(timescale)
  val scaleselect: NativeSelect = new NativeSelect(null, scalecontainer)
  scaleselect.setItemCaptionPropertyId("caption");
  scaleselect.addValueChangeListener(new ValueChangeListener() {
    override def valueChange(event: ValueChangeEvent) = {
      val itemId = event.getProperty().getValue()
      if (itemId != null) {
        val scale: TimeScale = scalecontainer.getItem(itemId).getBean()
        setScale(scale.duration)
      }
    }
  });
  scaleselect.setImmediate(true);
  scaleselect.setValue(timescales(5));
  scaleselect.setNullSelectionAllowed(false);
  controls.addComponent(scaleselect);

  // Resets the time window
  val reset: Button = new Button("Reset");
  reset.addClickListener(new ClickListener() {
    @Override
    def buttonClick(event: ClickEvent) {
      setScale(timescale);
    }
  });
  controls.addComponent(reset);

  // This sets the initial scale and refreshes all graphs
  setScale(timescales(5).duration)
  chart.drawChart(conf)

  setCompositionRoot(content)

}