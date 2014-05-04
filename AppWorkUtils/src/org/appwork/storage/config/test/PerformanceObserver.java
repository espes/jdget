/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.appwork.storage.config.handler.StorageHandler;

/**
 * @author Thomas
 * 
 */
public class PerformanceObserver extends Thread {
public PerformanceObserver(){
    StorageHandler.PROFILER_MAP = new HashMap<String, Long>();
    StorageHandler.PROFILER_CALLNUM_MAP=new HashMap<String, Long>();
   
}

    private boolean profileMethods    = true;
    private long interval=30000;

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }



    public boolean isProfileMethods() {
        return profileMethods;
    }

    public void setProfileMethods(boolean profileMethods) {
        this.profileMethods = profileMethods;
    }

    public void run() {

        while (true) {
     
            print();

        }
    }

    /**
     * 
     */
    public void print() {
        if(profileMethods){
            // /
            java.util.List<Entry<String, Long>> entries = new ArrayList<Entry<String, Long>>();
            for (Iterator<Entry<String, Long>> it = StorageHandler.PROFILER_MAP.entrySet().iterator(); it.hasNext();) {
                entries.add(it.next());
            }
            Collections.sort(entries, new Comparator<Entry<String, Long>>() {

                @Override
                public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {

                    return o1.getValue().compareTo(o2.getValue());
                }

            });

            for (Entry<String, Long> i : entries) {
              Long invocations = StorageHandler.PROFILER_CALLNUM_MAP.get(i.getKey());
              
              
                System.out.println((i.getValue() / 1000) / 1000f + "ms \t"+invocations+"#\t"+(i.getValue()/invocations)+"ns/i  " + i.getKey());
            }
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public void start() {
       super.start();
    }
}
