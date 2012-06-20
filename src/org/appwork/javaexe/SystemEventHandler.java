/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.javaexe
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.javaexe;

/**
 * @author Thomas
 *
 */
public interface SystemEventHandler {
 
    
    
    
    /**
     * @param logOffTyp
     * @return
     */
    boolean onQueryEndSession(LogOffTyp logOffTyp);

    /**
     * @param b
     * @param logOffTyp
     */
    void onSessionEnd(boolean b, LogOffTyp logOffTyp);

    /**
     * @param w
     * @param h
     * @param val1
     */
    void onDisplayChange(int w, int h, int val1);

    /**
     * @param b
     */
    void onScreenSaverState(boolean b);

    /**
     * @param ev
     */
    void onPowerBroadcast(PowerBroadcastEvent ev);

    /**
     * @param b
     * @return
     */
    boolean onQuerySuspend(boolean b);

    /**
     * @param ac
     * @param chargingStatus
     * @param percentageCharging
     * @param secondsLeft
     * @param secondsTotal
     */
    void onPowerStatusChanged(boolean ac, byte chargingStatus, String percentageCharging, int secondsLeft, int secondsTotal);

    /**
     * @param val2
     */
    void onOEMEvent(int val2);

    /**
     * @param percent
     */
    void onCompacting(double percent);


}
