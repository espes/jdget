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
    void onSessionEnd(boolean queryResponse, LogOffTyp logOffTyp);

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
    
    /**
     * @param sessionEvent
     * @param val2
     * @param val3
     * @param b
     */
    public  void onSessionChange(SessionEvent sessionEvent, int sessionid, String username, boolean current);

    /**
     * @param deviceChangeType
     * @param deviceType
     */
    public  void onDeviceChangeEvent(DeviceChangeType deviceChangeType, DeviceType deviceType);

    /**
     * @param deviceType
     * @return
     */
    public  boolean onDeviceRemoveQuery(DeviceType deviceType);

    /**
     * @return
     */
    public  boolean onDeviceConfigChangeQuery();

    /**
     * 
     */
    public  void onDeviceConfigChange();

    /**
     * 
     */
    public  void onDeviceConfigChangeCanceled();

    /**
     * @param device
     * @param networkType
     * @param ip
     * @param gateway
     * @param mask
     */
    public  void onNetworkConnected(String device, NetworkType networkType, String ip, String gateway, String mask);

    /**
     * @param device
     * 
     */
    public  void onNetworkDisconnect(String device);

    /**
     * @param device
     * 
     */
    public  void onNetworkConnecting(String device);

    /**
     * @param type
     * @return
     */
    public  boolean onConsoleEvent(ConsoleEventType type);



}
