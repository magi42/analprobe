package org.vaadin.multiprobe.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;


public class ProbeData implements Serializable {
    private static final long serialVersionUID = 3502338010463752172L;

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
    
    HashMap<Integer, Probe> probes = new HashMap<Integer, Probe>(); 
    
    BufferedWriter out;

    static ProbeData instance = new ProbeData();
    
    public static ProbeData get() {
        return instance;
    }

    private ProbeData() {
        String datafile = System.getenv("HOME") + "/analprobe.dat";
        System.out.println("Opening data journal " + datafile);
        
        // Read data collected so far, but no data older than a day
        try {
            FileReader reader = new FileReader(datafile);
            BufferedReader in = new BufferedReader(reader);
            int readLines = 0;
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                line = line.trim();
                String split[] = line.split(" ");
                if (split.length == 3)
                    try {
                        Date date = dateFormat.parse(split[0]);
                        int probeId = Integer.parseInt(split[1]);
                        int value = Integer.parseInt(split[2]);
                        getProbe(probeId).add(new ProbeEntry(date, value), null);
                    } catch (ParseException e) {
                        System.err.println("Invalid format '" +
                            e.getMessage() + " in " + line);
                    }
                else
                    System.err.println("Invalid format: " + line);
            }
            in.close();
            System.out.println(readLines + " entries read");
        } catch (FileNotFoundException e) {
            System.out.println("Journal not found, creating one.");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        
        // Start writing more
        try {
            FileWriter writer = new FileWriter(datafile, true);
            out = new BufferedWriter(writer);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public Collection<Probe> getProbes() {
        return probes.values();
    }
    
    public Probe getProbe(int probeId) {
        if (! probes.containsKey(probeId))
            probes.put(probeId, new Probe(this, probeId));
        return probes.get(probeId);
    }
    
    public Writer getJournalWriter() {
        return out;
    }
}
