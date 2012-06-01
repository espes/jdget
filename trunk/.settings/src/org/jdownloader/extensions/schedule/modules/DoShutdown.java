package org.jdownloader.extensions.schedule.modules;

import org.jdownloader.extensions.schedule.SchedulerModule;
import org.jdownloader.extensions.schedule.SchedulerModuleInterface;
import org.jdownloader.extensions.schedule.translate.T;

@SchedulerModule
public class DoShutdown implements SchedulerModuleInterface {

    private static final long serialVersionUID = 7232503485324370368L;

    public boolean checkParameter(String parameter) {
        return false;
    }

    public void execute(String parameter) {

        // OptionalPluginWrapper addon =
        // JDUtilities.getOptionalPlugin("shutdown");
        // if (addon == null) {
        // JDLogger.getLogger().info("JDShutdown addon not loaded! Cannot shutdown!");
        // return;
        // }
        // addon.getPlugin().interact("shutdown", null);
    }

    public String getTranslation() {
        return T._.jd_plugins_optional_schedule_modules_doShutdown();
    }

    public boolean needParameter() {
        return false;
    }

}