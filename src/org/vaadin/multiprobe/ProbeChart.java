package org.vaadin.multiprobe;

import java.util.Date;
import java.util.HashMap;

import org.vaadin.multiprobe.data.Probe;
import org.vaadin.multiprobe.data.ProbeData;
import org.vaadin.multiprobe.data.ProbeEntry;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

class ProbeChart extends CustomComponent {
    private static final long serialVersionUID = -7190363198957976267L;

    Chart chart;
    Configuration conf; // Chart configuration
    HashMap<Integer, ProbeWindow> windows = new HashMap<Integer, ProbeWindow>();
    int timescale; // Time window in seconds
    
    public class TimeScale {
        private String caption;
        private int    duration;
        
        public TimeScale(String caption, int duration) {
            this.caption = caption;
            this.duration = duration;
        }
        
        public String getCaption() {
            return caption;
        }
        public int getDuration() {
            return duration;
        }
    }

    /** A window inside probe data, manages a graph data series. */
    class ProbeWindow {
        Probe probe;
        ProbeEntry first;
        ProbeEntry last;
        DataSeries series;
        DataSeriesItem currentValue;

        public ProbeWindow(Probe probe) {
            this.probe = probe;
            this.first = null;
            this.last  = null;
            this.series = new DataSeries();
            this.currentValue = null;
        }
        
        public Probe getProbe() {
            return probe;
        }
        
        public DataSeries getSeries() {
            return series;
        }
        
        public void clear() {
            first = null;
            last  = null;
            series.clear();
            System.out.println("Cleared window");
        }
        
        /** Goes forward with the beginning of the window. */
        public void forwardStart(Date windowStart) {
            long startSecond = windowStart.getTime();

            if (first == null) {
                first = probe.getData();
                
                // Forward until we reach the start of the window
                while (first.getNext() != null) {
                    if (first.getTime().getTime() < startSecond) {
                        // System.out.println("Forwarding over " + first.getTime());
                        first = first.getNext();
                    } else
                        break;
                }
            }

            // Remove old items until we reach current time
            // System.out.println("Forwarding start time");
            while (series.size() > 0) {
                DataSeriesItem seriesCurrent = series.get(0);
                // System.out.println("Checking if " + seriesCurrent.getX().longValue() + " >= " + startSecond);
                if (seriesCurrent.getX().longValue() >= startSecond)
                    break;
                series.remove(series.get(0));
                // System.out.println("Removed an old item from window");
            }
        }

        public void forwardEnd(Date windowEnd, boolean updateChart) {
            boolean init = false;
            if (last == null) {
                last = first;
                init = true;
            }

            // Remove old items until we reach current time
            long endTime = windowEnd.getTime();
            boolean added = false;
            while (init || last.getNext() != null) {
                if (!init)
                    last = last.getNext();
                init = false;

                if (series.size() > 0) {
                    // Check if the series is up-to-date
                    DataSeriesItem seriesLast = series.get(series.size()-1);
                    // System.out.println("Is " + seriesLast.getX().longValue() + " later than " +endTime);
                    if (seriesLast.getX().longValue() > endTime)
                        break;
                }
                
                // If the chart is empty
                if (currentValue == null) {
                    currentValue = new DataSeriesItem(last.getTime(), last.getValue());
                    series.add(currentValue, updateChart, false);
                } else {
                    // Replace the current level with the new data point value
                    currentValue.setX(last.getTime().getTime());
                    currentValue.setY(last.getValue());
                    series.update(currentValue);
                }
                
                // boolean shift = (series.size() > 2 && currentValue.getX().longValue() - series.get(0).getX().longValue() >= timescale*1000);

                // Create the new current point that holds the last level
                currentValue = new DataSeriesItem(last.getTime(), last.getValue());
                series.add(currentValue, updateChart, false);
                // System.out.println("Added series item " + first.getTime() + " value=" + last.getValue());
                
                added = true;
            }
            
            // If no points were added, move the current level point to current time
            if (!added) {
                currentValue.setX(windowEnd.getTime());
                series.update(currentValue);
            }
        }
    }
    
    public ProbeChart() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setSizeFull();
        
        chart = new Chart(ChartType.AREA);
        chart.setSizeFull();
        content.addComponent(chart);
        content.setExpandRatio(chart, 1.0f);
        
        // Configure chart
        conf = chart.getConfiguration();
        conf.setTitle("Probe Data");
        
        // Set general plot options
        PlotOptionsArea plotOptions = new PlotOptionsArea();
        plotOptions.setMarker(new Marker(false));
        conf.setPlotOptions(plotOptions);

        // Configure X axis
        conf.getxAxis().setType(AxisType.DATETIME);
        conf.getxAxis().setTitle(new Title(""));        
        
        HorizontalLayout controls = new HorizontalLayout();
        content.addComponent(controls);

        // Time scale selection
        TimeScale timescales[] = {
                new TimeScale("1 year", 60*60*24*365),
                new TimeScale("1 day", 60*60*24),
                new TimeScale("6 hours", 60*60*6),
                new TimeScale("1 hour", 60*60),
                new TimeScale("10 minutes", 60*10),
                new TimeScale("1 minute", 60),
                new TimeScale("15 seconds", 15),
        };
        final BeanItemContainer<TimeScale> scalecontainer =
                new BeanItemContainer<TimeScale> (TimeScale.class);
        for (TimeScale timescale: timescales)
            scalecontainer.addBean(timescale);
        final NativeSelect scaleselect = new NativeSelect(null, scalecontainer);
        scaleselect.setItemCaptionPropertyId("caption");
        scaleselect.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 3163922754918964604L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                Object itemId = event.getProperty().getValue();
                if (itemId != null) {
                    TimeScale scale = scalecontainer.getItem(itemId).getBean();
                    setScale(scale.getDuration());
                }
            }
        });
        scaleselect.setImmediate(true);
        scaleselect.setValue(timescales[5]);
        scaleselect.setNullSelectionAllowed(false);
        controls.addComponent(scaleselect);
        
        // Resets the time window
        Button reset = new Button("Reset");
        reset.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 9174143542237298751L;

            @Override
            public void buttonClick(ClickEvent event) {
                setScale(timescale);
            }
        });
        controls.addComponent(reset);

        // This sets the initial scale and refreshes all graphs
        setScale(timescales[5].getDuration());
        chart.drawChart(conf);
       
        setCompositionRoot(content);
    }
    
    void setScale(int seconds) {
        timescale = seconds;

        // Reset all data series.
        // The series will be refilled on next request
        ProbeData data = ProbeData.get();
        synchronized (data) {
            for (Probe probe: data.getProbes()) {
                // Creates a new window if none exists for the probe
                ProbeWindow window = getProbeWindow(probe.getId());

                window.clear();

                // Go to the beginning of the window
                Date startTime = new Date ((new Date().getTime() - timescale*1000));
                window.forwardStart(startTime);
            }
        }

        refreshData(false);
        chart.drawChart(conf);
    }
    
    public void refreshData(boolean updateChart) {
        ProbeData data = ProbeData.get();
        synchronized (data) {
            // Check if there is new data from any of the available probes
            for (Probe probe: data.getProbes()) {
                ProbeWindow window = getProbeWindow(probe.getId());

                //
                Date currentTime = new Date();

                // Wind the beginning of the window forward if it has
                // been left far beyond
                Date criticalTime = new Date ((currentTime.getTime() - 4*timescale*1000));
                Date startTime    = new Date ((currentTime.getTime() - timescale*1000));
                if (window.getSeries().size() > 2 && window.getSeries().get(0).getX().longValue() < criticalTime.getTime())
                    window.forwardStart(startTime);
                
                // Go forward with the end of the window (to the end of data)
                window.forwardEnd(currentTime, updateChart);
                
                // conf.getxAxis().setExtremes(startTime.getTime(), currentTime.getTime());
                // conf.getxAxis().setExtremes(min, max)
                //conf.getxAxis().setStartOnTick(false);
            }
        }
    }

    private ProbeWindow getProbeWindow(int probeId) {
        if (! windows.containsKey(probeId))
            newProbe(probeId);
        return windows.get(probeId);
    }

    private void newProbe(int probeId) {
        // Put the data in a Charts DataSeries which displays a window of the all data
        ProbeWindow window = new ProbeWindow(ProbeData.get().getProbe(probeId));
        DataSeries series = window.getSeries();
        series.setName("Probe " + probeId);
        conf.addSeries(series);
        
        // Mapping from the probe ID to the object
        windows.put(probeId, window);
    }
}