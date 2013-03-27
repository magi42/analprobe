package org.vaadin.multiprobe.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;


/**
 * The entries form a linked list.
 * 
 * @author magi
 */
public class ProbeEntry {
    protected Date time;
    protected int  value;
    ProbeEntry next;
    
    public ProbeEntry(Date time, int value) {
        this.time = time;
        this.value = value;
    }
    
    public Date getTime() {
        return time;
    }
    public int getValue() {
        return value;
    }
    
    public void write(int probeId, Writer writer) throws IOException {
        writer.write(ProbeData.dateFormat.format(time) + " " + probeId + " " + value);
        writer.append("\n");
        writer.flush();
    }
    
    public void setNext(ProbeEntry next) {
        this.next = next;
    }
    
    public ProbeEntry getNext() {
        return next;
    }
}