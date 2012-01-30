package org.jdownloader.jdserv.stats;

import org.appwork.remotecall.RemoteCallInterface;

public interface StatisticsInterface extends RemoteCallInterface {
    /**
     * Will be called when JD has been started
     * 
     * @param id
     * @param startTime
     * @param revision
     * @param branch
     * @param jared
     * @param javaVersion
     * @param os
     * @param string
     * @return
     */
    String onStartup(String id, long startTime, String timeZone, String os, long javaVersion, boolean jared, String branch, long revision);

    //
    /**
     * Will be called once the first time jd started
     * 
     * @param id
     * @param time
     */
    void onFreshInstall(String id, long time);

    /**
     * JD has been closed
     * 
     * @param id
     * @param time
     * @param l
     */
    void onExit(String id, long time, long runtime);

}
