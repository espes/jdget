package org.appwork.javaexe;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.logging.Log;

public abstract class JavaExe_SystemEventManagement {

    static final int WM_QUERYENDSESSION = 0x0011;
    static final int WM_ENDSESSION      = 0x0016;
    static final int WM_DEVMODECHANGE   = 0x001B;
    static final int WM_TIMECHANGE      = 0x001E;
    static final int WM_COMPACTING      = 0x0041;
    static final int WM_USERCHANGED     = 0x0054;
    static final int WM_DISPLAYCHANGE   = 0x007E;
    static final int WM_SYSCOMMAND      = 0x0112;
    static final int WM_POWERBROADCAST  = 0x0218;
    static final int WM_DEVICECHANGE    = 0x0219;
    static final int WM_SESSION_CHANGE  = 0x02B1;
    static final int WM_NETWORK         = 0x0401;
    static final int WM_CONSOLE         = 0x0402;

    static final int ENDSESSION_LOGOFF  = 0x80000000;

    static final int SC_SCREENSAVE      = 0xF140;

    final static String getIPstr(int[] bufIP, int offset) {
        return "" + bufIP[offset] + "." + bufIP[offset + 1] + "." + bufIP[offset + 2] + "." + bufIP[offset + 3];
    }

    protected int onEvent(int msg, int val1, int val2, String val3, int[] arr1, byte[] arr2) {

        switch (msg) {
        case WM_QUERYENDSESSION:
            if (getHandler() == null) return 1;
            return getHandler().onQueryEndSession((val2 & ENDSESSION_LOGOFF) == 0 ? LogOffTyp.SHUTDOWN : LogOffTyp.LOGOUT) ? 1 : 0;
        case WM_ENDSESSION:
            if (getHandler() == null) return 0;
            getHandler().onSessionEnd(val1 != 0, (val2 & ENDSESSION_LOGOFF) == 0 ? LogOffTyp.SHUTDOWN : LogOffTyp.LOGOUT);
            return 0;
        case WM_DISPLAYCHANGE:
            if (getHandler() == null) return 0;
            int w = (val2 & 0x0000FFFF);
            int h = ((val2 >> 16) & 0x0000FFFF);
            getHandler().onDisplayChange(w, h, val1);
            return 0;
        case WM_SYSCOMMAND:
            if (getHandler() == null) return 0;
            // currently only SCREENSAVE supported
            getHandler().onScreenSaverState(val2 == 1);
            return 0;
        case WM_COMPACTING:
            if (getHandler() == null) return 0;
            double percent = (100.0 * ((double) val1)) / 65536.0;
            getHandler().onCompacting(percent);
            return 0;
        case WM_POWERBROADCAST:
            if (getHandler() == null) return 1;
            PowerBroadcastEvent ev = PowerBroadcastEvent.get(val1);
            switch (ev) {
            case OEMEVENT:
                getHandler().onOEMEvent(val2);
                break;
            case POWERSTATUSCHANGE:
                boolean ac = arr2[0] == 1;
                byte chargingStatus = arr2[1];
                String percentageCharging = (arr2[2] == 255 ? "?" : "" + arr2[2]);
                int secondsLeft = arr1[0];
                int secondsTotal = arr1[1];
                getHandler().onPowerStatusChanged(ac, chargingStatus, percentageCharging, secondsLeft, secondsTotal);
                break;
            case QUERYSUSPEND:
                return getHandler().onQuerySuspend((val2 & 1) != 0) ? 1 : 0;
            default:

                getHandler().onPowerBroadcast(ev);
                break;
            }
            return 1;
        case WM_CONSOLE:
            if (getHandler() == null) return 1;
            ConsoleEventType ctrlType = ConsoleEventType.get(val1);
            return getHandler().onConsoleEvent(ctrlType) ? 1 : 0;

        case WM_NETWORK:
            if (getHandler() == null) return 0;
            NetworkStatus status = NetworkStatus.get(val1);
            String device = val3;

            switch (status) {
            case CONNECTED:
                NetworkType networkType = NetworkType.get(arr1[0]);
                String ip = getIPstr(arr1, 1);
                String gateway = getIPstr(arr1, 5);
                String mask = getIPstr(arr1, 9);
                getHandler().onNetworkConnected(device, networkType, ip, gateway, mask);
                break;
            case CONNECTING:
                getHandler().onNetworkConnecting(device);
                break;
            case DISCONNECT:
                getHandler().onNetworkDisconnect(device);
                break;
            }
            return 0;
        case WM_DEVICECHANGE:

            DeviceChangeType deviceChangeType = DeviceChangeType.get(val1);

            switch (deviceChangeType) {
            case CONFIGCHANGECANCELED:
                getHandler().onDeviceConfigChangeCanceled();
                break;
            case CONFIGCHANGED:
                getHandler().onDeviceConfigChange();
                break;
            case QUERYCHANGECONFIG:
                // if (isPrompt) ret =
                // Examples_UtilsGUI.showConfirmDialog("System Event",
                // "The system has requested to dock or undock this computer.\nDo you accept ?");
                if (getHandler() == null) return 1;
                return getHandler().onDeviceConfigChangeQuery() ? 1 : 0;

            case DEVICEQUERYREMOVE:
                if (getHandler() == null) return 1;
                DeviceType deviceType = getDevice(val3, arr1);
                return getHandler().onDeviceRemoveQuery(deviceType) ? 1 : 0;

            default:
                if (getHandler() == null) return 0;
                deviceType = getDevice(val3, arr1);
                getHandler().onDeviceChangeEvent(deviceChangeType, deviceType);

            }
            return 0;
        case WM_SESSION_CHANGE:
            if (getHandler() == null) return 0;
            getHandler().onSessionChange(SessionEvent.get(val1), val2, val3, ((arr1 != null && arr1.length > 0 && arr1[0] != 0)));
            return 0;

        default:
            Log.exception(new WTFException("Not supported " + msg));
            return 0;

        }
    }

    /**
     * @param i
     * @return
     */
    private DeviceType getDevice(String val3, int[] arr1) {

        switch (arr1[1]) {
        case DeviceType.OEM:

            return new OEMDevice(arr1[3], arr1[4]);

        case DeviceType.PORT:

            return new PortDevice(val3);

        case DeviceType.VOLUME:
            return VolumeDevice.create(arr1[3], arr1[4]);

        }

        return null;

    }

    /**
     * @return
     */
    protected abstract SystemEventHandler getHandler();

}
