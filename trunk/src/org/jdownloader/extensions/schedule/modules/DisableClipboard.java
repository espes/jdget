package org.jdownloader.extensions.schedule.modules;

import org.jdownloader.extensions.schedule.SchedulerModule;
import org.jdownloader.extensions.schedule.SchedulerModuleInterface;
import org.jdownloader.extensions.schedule.translate.T;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;

@SchedulerModule
public class DisableClipboard implements SchedulerModuleInterface {

    private static final long serialVersionUID = -8088061436921363098L;

    public boolean checkParameter(String parameter) {
        return false;
    }

    public void execute(String parameter) {
        org.jdownloader.settings.staticreferences.GUI.CLIPBOARD_MONITORED.setValue(false);
    }

    public String getTranslation() {
        return T._.jd_plugins_optional_schedule_modules_disableClipboard();
    }

    public boolean needParameter() {
        return false;
    }

}