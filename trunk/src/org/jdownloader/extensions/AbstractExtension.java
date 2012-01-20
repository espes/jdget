//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.jdownloader.extensions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import jd.config.ConfigContainer;
import jd.config.SubConfiguration;
import jd.controlling.JSonWrapper;
import jd.gui.swing.jdgui.menu.MenuAction;
import jd.gui.swing.jdgui.views.settings.sidebar.AddonConfig;
import jd.plugins.AddonPanel;
import jd.plugins.ExtensionConfigInterface;
import jd.utils.locale.JDL;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.jdownloader.images.NewTheme;
import org.jdownloader.logging.LogController;
import org.jdownloader.settings.advanced.AdvancedConfigManager;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Superclass for all extensions
 * 
 * @author thomas
 * 
 */
public abstract class AbstractExtension<T extends ExtensionConfigInterface> {

    public static final int ADDON_INTERFACE_VERSION = 8;

    private boolean         enabled                 = false;

    /**
     * true if the extension is currently running.
     * 
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * start/stops the extension.
     * 
     * @param enabled
     * @throws StartException
     * @throws StopException
     */
    public synchronized void setEnabled(boolean enabled) throws StartException, StopException {
        if (enabled == this.enabled) return;
        this.enabled = enabled;
        if (enabled) {
            start();
            store.setEnabled(true);
        } else {
            stop();
            store.setEnabled(false);
            if (getGUI() != null) {
                getGUI().setActive(false);
            }
        }

    }

    /**
     * Converts an ExtensionConfigPanel from the old config containers
     * 
     * @param initSettings
     * @return
     */
    @Deprecated
    protected ExtensionConfigPanel createPanelFromContainer(ConfigContainer initSettings) {

        final AddonConfig cp = AddonConfig.getInstance(initSettings, "", false);
        ExtensionConfigPanel<AbstractExtension<T>> ret = new ExtensionConfigPanel<AbstractExtension<T>>(this, false) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onShow() {
            }

            @Override
            protected void onHide() {
            }

            @Override
            public void save() {
                cp.setHidden();
            }

            @Override
            public void updateContents() {
                cp.setShown();
            }
        };

        ret.add(cp, "gapleft 25,spanx,growx,pushx,growy,pushy");
        return ret;
    }

    /**
     * Returns the internal storage. Most of the configvalues are for internal
     * use only. This config only contains values which are valid for all
     * extensions
     * 
     * @return
     */
    public T getSettings() {
        return store;
    }

    /**
     * use {@link #setProxyRotationEnabled(false)} to stop the extension.
     * 
     * @throws StopException
     */
    protected abstract void stop() throws StopException;

    protected abstract void start() throws StartException;

    protected Logger    logger;

    private String      name;

    private int         version = -1;
    @Deprecated
    private JSonWrapper classicConfig;

    private T           store;

    public String getName() {
        return name;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * converts old dynamic getConfigName entries to static getID entries, WE
     * MUST USE STATIC getID to access db
     * 
     * @deprecated Use {@link #getSettings()}
     */

    @Deprecated
    public synchronized JSonWrapper getPluginConfig() {
        if (classicConfig != null) return classicConfig;
        classicConfig = JSonWrapper.get(this.getConfigID());
        if (classicConfig.size() == 0 && SubConfiguration.hasConfig(getName())) {
            /* convert old to new */
            SubConfiguration oldConfig = SubConfiguration.getConfig(getName());
            if (oldConfig != null) {
                /* put old values into new db and delete old one then */
                oldConfig.copyTo(classicConfig);
                SubConfiguration.removeConfig(getName());
                classicConfig.save();
            }
        }
        return classicConfig;
    }

    /**
     * 
     * @param name
     *            name of this plugin. Until JD 2.* we should use null here to
     *            use the old defaultname. we used to sue this localized name as
     *            config key.
     * @throws
     * @throws StartException
     */
    public AbstractExtension(String name) {
        this.name = name == null ? JDL.L(getClass().getName(), getClass().getSimpleName()) : name;
        logger = createLogger(getClass());
        version = readVersion(getClass());
        store = buildStore();
        AdvancedConfigManager.getInstance().register(store);
        logger.info("Loaded");
    }

    /**
     * Creates the correct config based on the Extensions supertype
     * 
     * @param class1
     * @return
     */
    private T buildStore() {
        return JsonConfig.create(Application.getResource("cfg/" + getClass().getName()), getConfigClass());
    }

    /**
     * returns the config interface class for this extension
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> getConfigClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedTypeImpl) {
            return (Class<T>) ((ParameterizedTypeImpl) type).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("Bad Extension Definition. Please add Generic ConfigClass: class " + getClass().getSimpleName() + " extends AbstractExtension<" + getClass().getSimpleName() + "Config>{... with 'public interface " + getClass().getSimpleName() + "Config extends ExtensionConfigInterface{...");
        }
    }

    public static ExtensionConfigInterface createStore(String className, Class<? extends ExtensionConfigInterface> interfaceClass) {
        return JsonConfig.create(Application.getResource("cfg/" + className), interfaceClass);
    }

    /**
     * Gets called once per session as soon as the extension gets loaded the
     * first time
     * 
     * @throws StartException
     */
    protected abstract void initExtension() throws StartException;

    /**
     * Reads the version.dat in the same directory as class1
     * 
     * @param class1
     * @return
     */
    public static int readVersion(Class<? extends AbstractExtension> class1) {

        try {
            return Integer.parseInt(IO.readURLToString(class1.getResource("version.dat")).trim());
        } catch (Throwable e) {
            return -1;
        }

    }

    public abstract ExtensionConfigPanel<?> getConfigPanel();

    public abstract boolean hasConfigPanel();

    private Logger createLogger(Class<? extends AbstractExtension> class1) {
        return LogController.getInstance().createLogger(class1);
    }

    /**
     * @deprecated Use {@link #getSettings()}
     */
    @Deprecated
    public abstract String getConfigID();

    public abstract String getAuthor();

    public abstract String getDescription();

    public boolean isLinuxRunnable() {
        return true;
    }

    @Deprecated
    public String getIconKey() {
        return "settings";
    }

    public ImageIcon getIcon(int size) {
        return NewTheme.I().getIcon(getIconKey(), size);
    }

    public boolean isWindowsRunnable() {
        return true;
    }

    public boolean isMacRunnable() {
        return true;
    }

    public abstract AddonPanel<? extends AbstractExtension<T>> getGUI();

    public boolean isDefaultEnabled() {
        return false;
    }

    public int getVersion() {
        return version;
    }

    public ArrayList<MenuAction> getMenuAction() {
        return null;
    }

    public ExtensionGuiEnableAction getShowGuiAction() {
        return getGUI() != null ? getGUI().getEnabledAction() : null;

    }

    public void init() throws StartException {
        initExtension();

        if (store.isFreshInstall()) {
            store.setEnabled(this.isDefaultEnabled());
            store.setFreshInstall(false);
        }
        if (store.isEnabled()) {
            try {
                setEnabled(true);
            } catch (StopException e) {
                // cannot happen
            }
        }
    }

    public boolean isQuickToggleEnabled() {
        return false;
    }

}