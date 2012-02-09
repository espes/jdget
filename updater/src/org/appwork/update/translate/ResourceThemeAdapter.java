package org.appwork.update.translate;

import org.appwork.storage.config.JsonConfig;
import org.appwork.txtresource.AbstractResourcePath;
import org.appwork.update.updateclient.Setup;

public class ResourceThemeAdapter extends AbstractResourcePath {

    @Override
    public String getPath() {
        return "themes/"+JsonConfig.create(Setup.class).getIconTheme()+"/org/appwork/updater/translation/Translation";
    }

}
