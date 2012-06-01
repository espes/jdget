package jd.controlling.reconnect.pluginsinc.liveheader;

import org.appwork.utils.swing.dialog.UserIODefinition;

public interface DataCompareDialogInterface extends UserIODefinition {

    String getUsername();

    String getPassword();

    String getManufactor();

    String getRouterName();

    String getFirmware();

    String getHostName();

}
