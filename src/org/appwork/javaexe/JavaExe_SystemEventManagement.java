package org.appwork.javaexe;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.logging.Log;

public abstract class JavaExe_SystemEventManagement {

    static final int WM_QUERYENDSESSION          = 0x0011;
    static final int WM_ENDSESSION               = 0x0016;
    static final int WM_DEVMODECHANGE            = 0x001B;
    static final int WM_TIMECHANGE               = 0x001E;
    static final int WM_COMPACTING               = 0x0041;
    static final int WM_USERCHANGED              = 0x0054;
    static final int WM_DISPLAYCHANGE            = 0x007E;
    static final int WM_SYSCOMMAND               = 0x0112;
    static final int WM_POWERBROADCAST           = 0x0218;
    static final int WM_DEVICECHANGE             = 0x0219;
    static final int WM_SESSION_CHANGE           = 0x02B1;
    static final int WM_NETWORK                  = 0x0401;
    static final int WM_CONSOLE                  = 0x0402;

    static final int DBT_QUERYCHANGECONFIG       = 0x0017;
    static final int DBT_CONFIGCHANGED           = 0x0018;
    static final int DBT_CONFIGCHANGECANCELED    = 0x0019;
    static final int DBT_DEVICEARRIVAL           = 0x8000;
    static final int DBT_DEVICEQUERYREMOVE       = 0x8001;
    static final int DBT_DEVICEQUERYREMOVEFAILED = 0x8002;
    static final int DBT_DEVICEREMOVECOMPLETE    = 0x8004;
    static final int DBT_DEVICEREMOVEPENDING     = 0x8003;
    static final int DBT_DEVICETYPESPECIFIC      = 0x8005;
    static final int DBT_CUSTOMEVENT             = 0x8006;
    static final int DBT_USERDEFINED             = 0xFFFF;

    static final int DBT_DEVTYP_OEM              = 0x00000000;
    static final int DBT_DEVTYP_VOLUME           = 0x00000002;
    static final int DBT_DEVTYP_PORT             = 0x00000003;

    static final int ENDSESSION_LOGOFF           = 0x80000000;

    static final int SC_SCREENSAVE               = 0xF140;

    static final int NET_DISCONNECT              = 0;
    static final int NET_CONNECTING              = 1;
    static final int NET_CONNECTED               = 2;

    static final int MIB_IF_TYPE_OTHER           = 1;
    static final int MIB_IF_TYPE_ETHERNET        = 6;
    static final int MIB_IF_TYPE_TOKENRING       = 9;
    static final int MIB_IF_TYPE_FDDI            = 15;
    static final int MIB_IF_TYPE_PPP             = 23;
    static final int MIB_IF_TYPE_LOOPBACK        = 24;
    static final int MIB_IF_TYPE_SLIP            = 28;

    static final int WTS_SESSION_LOGGED          = 0;
    static final int WTS_CONSOLE_CONNECT         = 1;
    static final int WTS_CONSOLE_DISCONNECT      = 2;
    static final int WTS_REMOTE_CONNECT          = 3;
    static final int WTS_REMOTE_DISCONNECT       = 4;
    static final int WTS_SESSION_LOGON           = 5;
    static final int WTS_SESSION_LOGOFF          = 6;
    static final int WTS_SESSION_LOCK            = 7;
    static final int WTS_SESSION_UNLOCK          = 8;
    static final int WTS_SESSION_REMOTE_CONTROL  = 9;

    static final int CTRL_C_EVENT                = 0;
    static final int CTRL_BREAK_EVENT            = 1;
    static final int CTRL_CLOSE_EVENT            = 2;
    static final int CTRL_LOGOFF_EVENT           = 5;
    static final int CTRL_SHUTDOWN_EVENT         = 6;

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
        default:
            Log.exception(new WTFException("Not supported " + msg));
            return 0;

        }

    }

    /**
     * @return
     */
    protected abstract SystemEventHandler getHandler();

}
