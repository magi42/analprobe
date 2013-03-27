package org.vaadin.multiprobe.data;

import java.io.IOException;
import java.io.Writer;

public class Probe {
    private final ProbeData analData;

    private ProbeEntry data;
    private ProbeEntry tail;
    private int count = 0;
    
    private int id;
    public Probe(ProbeData analData, int id) {
        this.analData = analData;
        this.id = id;
    }
    
    public void add(ProbeEntry entry, Writer journal) {
        if (data == null)
            data = tail = entry;
        else {
            tail.setNext(entry);
            tail = entry;
        }
        count++;

        if (journal != null)
            try {
                entry.write(id, journal);
            } catch (IOException e) {
                System.err.println("Could not write entry to journal");
            }
    }
    
    public int getId() {
        return id;
    }
    
    public ProbeEntry getData() {
        return data;
    }
    
    public int size() {
        return count;
    }
}