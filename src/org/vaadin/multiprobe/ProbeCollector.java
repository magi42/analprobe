package org.vaadin.multiprobe;

import java.io.IOException;
import java.util.Date;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.vaadin.multiprobe.data.ProbeData;
import org.vaadin.multiprobe.data.ProbeEntry;


public class ProbeCollector extends GenericServlet {
    private static final long serialVersionUID = -672276421418002033L;
    
    ProbeData data = ProbeData.get();
    
    @Override
    public void service(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
        String strValue = request.getParameter("value");
        if (strValue == null) {
            response.getWriter().append("ERROR: NO VALUE");
            return;
        }
        int value = Integer.valueOf(strValue);
        if (value < 0 || value > 1024) {
            response.getWriter().append("ERROR: INVALID VALUE");
            return;
        }

        String probeIdStr = request.getParameter("probe");
        if (probeIdStr == null) {
            response.getWriter().append("ERROR: NO PROBE");
            return;
        }
        int probeId = Integer.valueOf(probeIdStr);
        
        ProbeEntry entry = new ProbeEntry(new Date(), value);
        synchronized (data) {
            data.getProbe(probeId).add(entry, data.getJournalWriter());
        }
        
        response.getWriter().append("ACK");
    }
}
