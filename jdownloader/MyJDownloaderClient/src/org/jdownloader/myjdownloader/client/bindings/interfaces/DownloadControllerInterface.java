package org.jdownloader.myjdownloader.client.bindings.interfaces;

import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;

@ClientApiNameSpace("downloadcontroller")
public interface DownloadControllerInterface extends Linkable{

    void forceDownload(long[] linkIds, long[] packageIds);

    String getCurrentState();

    boolean pause(boolean value);

    /*
     * Info
     */
    int getSpeedInBps();

    /*
     * Controlls
     */
    boolean start();

    boolean stop();
}
