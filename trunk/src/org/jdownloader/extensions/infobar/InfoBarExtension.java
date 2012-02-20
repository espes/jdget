package org.jdownloader.extensions.infobar;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.ConfigGroup;
import jd.plugins.AddonPanel;

import org.appwork.utils.Application;
import org.appwork.utils.swing.EDTHelper;
import org.jdownloader.actions.AppAction;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.extensions.infobar.translate.T;

public class InfoBarExtension extends AbstractExtension<InfoBarConfig> {

    @Override
    public boolean isDefaultEnabled() {
        return true;
    }

    private static final String                    PROPERTY_OPACITY      = "PROPERTY_OPACITY";

    private static final String                    PROPERTY_DROPLOCATION = "PROPERTY_DROPLOCATION";

    private static final String                    PROPERTY_DOCKING      = "PROPERTY_DOCKING";

    private AppAction                              activateAction;

    private InfoDialog                             infoDialog;

    private ExtensionConfigPanel<InfoBarExtension> configPanel;

    public ExtensionConfigPanel<InfoBarExtension> getConfigPanel() {
        return configPanel;
    }

    public boolean hasConfigPanel() {
        return true;
    }

    public InfoBarExtension() throws StartException {
        super(T._.jd_plugins_optional_infobar_jdinfobar());

    }

    private void updateOpacity(Object value) {
        if (infoDialog == null) return;
        final int newValue;
        if (value == null) {
            newValue = getPluginConfig().getIntegerProperty(PROPERTY_OPACITY, 100);
        } else {
            newValue = Integer.parseInt(value.toString());
        }
        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                try {
                    com.sun.awt.AWTUtilities.setWindowOpacity(infoDialog, (float) (newValue / 100.0));
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.start();
    }

    public void setGuiEnable(boolean b) {
        if (b) {
            if (infoDialog == null) {
                infoDialog = InfoDialog.getInstance(activateAction);
                infoDialog.setEnableDropLocation(getPluginConfig().getBooleanProperty(PROPERTY_DROPLOCATION, true));
                infoDialog.setEnableDocking(getPluginConfig().getBooleanProperty(PROPERTY_DOCKING, true));
                if (Application.getJavaVersion() >= 16000000) updateOpacity(null);
            }
            infoDialog.showDialog();
        } else {
            if (infoDialog != null) infoDialog.hideDialog();
        }
        if (activateAction != null && activateAction.isSelected() != b) activateAction.setSelected(b);
    }

    @Override
    public String getIconKey() {
        return "info";
    }

    @Override
    protected void stop() throws StopException {
        if (infoDialog != null) infoDialog.hideDialog();
    }

    @Override
    protected void start() throws StartException {
        activateAction = new AppAction() {
            {
                setName("Show Infobar");
                setIconKey(this.getIconKey());
                setSelected(false);
            }

            public void actionPerformed(ActionEvent e) {
            }
        };

        logger.info("InfoBar: OK");
    }

    protected void initSettings(ConfigContainer config) {
        config.setGroup(new ConfigGroup(getName(), getIconKey()));
        if (Application.getJavaVersion() >= 16000000) {
            ConfigEntry ce = new ConfigEntry(ConfigContainer.TYPE_SPINNER, getPluginConfig(), PROPERTY_OPACITY, T._.jd_plugins_optional_infobar_opacity(), 1, 100, 10) {
                private static final long serialVersionUID = 1L;

                @Override
                public void valueChanged(Object newValue) {
                    updateOpacity(newValue);
                    super.valueChanged(newValue);
                }
            };
            ce.setDefaultValue(100);
            config.addEntry(ce);
            config.addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        }
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), PROPERTY_DROPLOCATION, T._.jd_plugins_optional_infobar_dropLocation2()) {

            private static final long serialVersionUID = 1L;

            @Override
            public void valueChanged(Object newValue) {
                if (infoDialog != null) infoDialog.setEnableDropLocation((Boolean) newValue);
                super.valueChanged(newValue);
            }
        }.setDefaultValue(true));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), PROPERTY_DOCKING, T._.jd_plugins_optional_infobar_docking()) {

            private static final long serialVersionUID = 1L;

            @Override
            public void valueChanged(Object newValue) {
                if (infoDialog != null) infoDialog.setEnableDocking((Boolean) newValue);
                super.valueChanged(newValue);
            }
        }.setDefaultValue(true));
    }

    @Override
    public String getConfigID() {
        return "infobar";
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getDescription() {
        return T._.jd_plugins_optional_infobar_jdinfobar_description();
    }

    @Override
    public AddonPanel<InfoBarExtension> getGUI() {
        return null;
    }

    @Override
    public java.util.ArrayList<JMenuItem> getMenuAction() {
        ArrayList<JMenuItem> menu = new ArrayList<JMenuItem>();

        menu.add(new JCheckBoxMenuItem(activateAction));

        return menu;
    }

    @Override
    protected void initExtension() throws StartException {

        ConfigContainer cc = new ConfigContainer(getName());
        initSettings(cc);
        configPanel = createPanelFromContainer(cc);
    }

}