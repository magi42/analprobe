package org.vaadin.multiprobe;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
public class MultiProbeUI extends UI {
    ProbeChart chart;
    
    @Override
    protected void init(VaadinRequest request) {
        Page.getCurrent().setTitle("Analog Data Collector");

        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        setContent(layout);
        
        chart = new ProbeChart();
        layout.addComponent(chart);
		
        // Set up polling for more data
        new Refresher() {
            {
            extend(UI.getCurrent());
            setRefreshInterval(1000);
            addListener(new RefreshListener() {
                @Override
                public void refresh(Refresher source) {
                    chart.refreshData(true);
                }
            });
        }};
    }
}
