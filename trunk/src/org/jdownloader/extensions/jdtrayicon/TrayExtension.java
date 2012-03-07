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

package org.jdownloader.extensions.jdtrayicon;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jd.Main;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.ConfigGroup;
import jd.controlling.JDLogger;
import jd.controlling.JSonWrapper;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorEvent;
import jd.controlling.linkcollector.LinkCollectorListener;
import jd.controlling.linkcrawler.CrawledLink;
import jd.gui.UserIO;
import jd.gui.swing.SwingGui;
import jd.gui.swing.jdgui.JDGui;
import jd.plugins.AddonPanel;

import org.appwork.utils.Application;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.extensions.jdtrayicon.translate.T;
import org.jdownloader.images.NewTheme;

public class TrayExtension extends AbstractExtension<TrayConfig> implements MouseListener, MouseMotionListener, WindowListener, WindowStateListener, ActionListener, LinkCollectorListener {

    @Override
    protected void stop() throws StopException {
        removeTrayIcon();
        if (guiFrame != null) {
            guiFrame.removeWindowListener(this);
            guiFrame.removeWindowStateListener(this);
            miniIt(false, false);
            guiFrame.setAlwaysOnTop(false);
            guiFrame = null;
        }
        LinkCollector.getInstance().getEventsender().removeListener(this);
    }

    @Override
    protected void start() throws StartException {
        if (Application.getJavaVersion() < Application.JAVA16) {
            logger.severe("Error initializing SystemTray: Tray is supported since Java 1.6. your Version: " + Application.getJavaVersion());
            throw new StartException("Tray is supported since Java 1.6. your Version: " + Application.getJavaVersion());
        }
        if (!SystemTray.isSupported()) {
            logger.severe("Error initializing SystemTray: Tray isn't supported jet");
            throw new StartException("Tray isn't supported!");
        }
        Main.GUI_COMPLETE.executeWhenReached(new Runnable() {

            public void run() {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        try {
                            if (SwingGui.getInstance() != null) {
                                guiFrame = SwingGui.getInstance().getMainFrame();
                                if (guiFrame != null) {
                                    guiFrame.removeWindowListener(TrayExtension.this);
                                    guiFrame.addWindowListener(TrayExtension.this);
                                    guiFrame.removeWindowStateListener(TrayExtension.this);
                                    guiFrame.addWindowStateListener(TrayExtension.this);
                                    logger.info("Systemtray OK");
                                    initGUI();
                                    LinkCollector.getInstance().getEventsender().addListener(TrayExtension.this);
                                }
                            }

                        } catch (Exception e) {
                            Log.exception(e);
                        }
                    }
                };
            }
        });
    }

    protected void initSettings(ConfigContainer config) {
        ConfigEntry ce, cond;
        config.setGroup(new ConfigGroup(getName(), getIconKey()));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_CLOSE_TO_TRAY, T._.plugins_optional_JDLightTray_closetotray()).setDefaultValue(true));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_START_MINIMIZED, T._.plugins_optional_JDLightTray_startMinimized()).setDefaultValue(false));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_SINGLE_CLICK, T._.plugins_optional_JDLightTray_singleClick()).setDefaultValue(false));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_TOOLTIP, T._.plugins_optional_JDLightTray_tooltip()).setDefaultValue(true));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_SHOW_ON_LINKGRAB, T._.plugins_optional_JDLightTray_linkgrabber_intray()).setDefaultValue(true));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_SHOW_ON_LINKGRAB2, T._.plugins_optional_JDLightTray_linkgrabber_always()).setDefaultValue(false));
        config.addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        config.addEntry(cond = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, subConfig, PROPERTY_PASSWORD_REQUIRED, T._.plugins_optional_JDLightTray_passwordRequired()).setDefaultValue(false));
        config.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_PASSWORDFIELD, subConfig, PROPERTY_PASSWORD, T._.plugins_optional_JDLightTray_password()));
        ce.setEnabledCondidtion(cond, true);
    }

    @Override
    public String getConfigID() {
        return "trayicon";
    }

    @Override
    public boolean isQuickToggleEnabled() {
        return true;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getDescription() {
        return T._.jd_plugins_optional_jdtrayicon_jdlighttray_description();
    }

    @Override
    public AddonPanel<TrayExtension> getGUI() {
        return null;
    }

    @Override
    public boolean isDefaultEnabled() {
        return true;
    }

    @Override
    public java.util.ArrayList<JMenuItem> getMenuAction() {
        return null;
    }

    private JSonWrapper                         subConfig                  = null;

    private static final String                 PROPERTY_START_MINIMIZED   = "PROPERTY_START_MINIMIZED";

    private static final String                 PROPERTY_CLOSE_TO_TRAY     = "PROPERTY_CLOSE_TO_TRAY";

    private static final String                 PROPERTY_SINGLE_CLICK      = "PROPERTY_SINGLE_CLICK";

    private static final String                 PROPERTY_TOOLTIP           = "PROPERTY_TOOLTIP";

    private static final String                 PROPERTY_SHOW_ON_LINKGRAB  = "PROPERTY_SHOW_ON_LINKGRAB";

    private static final String                 PROPERTY_SHOW_ON_LINKGRAB2 = "PROPERTY_SHOW_ON_LINKGRAB2";

    private static final String                 PROPERTY_PASSWORD_REQUIRED = "PROPERTY_PASSWORD_REQUIRED";

    private static final String                 PROPERTY_PASSWORD          = "PROPERTY_PASSWORD";

    private TrayIconPopup                       trayIconPopup;

    private TrayIcon                            trayIcon;

    private JFrame                              guiFrame;

    private TrayIconTooltip                     trayIconTooltip;

    private TrayMouseAdapter                    ma;

    private boolean                             iconified                  = false;

    private Timer                               disableAlwaysonTop;

    private ExtensionConfigPanel<TrayExtension> configPanel;

    public ExtensionConfigPanel<TrayExtension> getConfigPanel() {
        return configPanel;
    }

    public boolean hasConfigPanel() {
        return true;
    }

    public TrayExtension() throws StartException {
        super(T._.jd_plugins_optional_jdtrayicon_jdlighttray());
        subConfig = JSonWrapper.get("ADDONS_JDLIGHTTRAY");

        disableAlwaysonTop = new Timer(2000, this);
        disableAlwaysonTop.setInitialDelay(2000);
        disableAlwaysonTop.setRepeats(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == disableAlwaysonTop) {
            if (guiFrame != null) guiFrame.setAlwaysOnTop(false);
        }
        return;
    }

    private void initGUI() {
        SystemTray systemTray = SystemTray.getSystemTray();
        Image img = IconIO.getScaledInstance(NewTheme.I().getImage("logo/jd_logo_64_64", -1), (int) systemTray.getTrayIconSize().getWidth(), (int) systemTray.getTrayIconSize().getHeight());
        /*
         * trayicon message must be set, else windows cannot handle icon right
         * (eg autohide feature)
         */
        trayIcon = new TrayIcon(img, "JDownloader");
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(this);

        ma = new TrayMouseAdapter(this, trayIcon);
        trayIcon.addMouseListener(ma);
        trayIcon.addMouseMotionListener(ma);

        trayIconTooltip = new TrayIconTooltip();

        try {
            systemTray.add(trayIcon);
        } catch (Exception e) {
            JDLogger.exception(e);
        }
        Main.GUI_COMPLETE.executeWhenReached(new Runnable() {

            public void run() {
                logger.info("JDLightTrayIcon Init complete");
                guiFrame = JDGui.getInstance().getMainFrame();
                if (guiFrame != null) {
                    guiFrame.removeWindowListener(TrayExtension.this);
                    guiFrame.addWindowListener(TrayExtension.this);
                    guiFrame.removeWindowStateListener(TrayExtension.this);
                    guiFrame.addWindowStateListener(TrayExtension.this);
                }
                if (subConfig.getBooleanProperty(PROPERTY_START_MINIMIZED, false)) {
                    miniIt(true, true);
                }
            }

        });

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        trayIconTooltip.hideTooltip();
    }

    public void mousePressed(MouseEvent e) {
        trayIconTooltip.hideTooltip();
        if (e.getSource() instanceof TrayIcon) {
            if (!CrossSystem.isMac()) {
                if (e.getClickCount() >= (subConfig.getBooleanProperty(PROPERTY_SINGLE_CLICK, false) ? 1 : 2) && !SwingUtilities.isRightMouseButton(e)) {
                    miniIt(guiFrame.isVisible(), true);
                } else {
                    if (trayIconPopup != null && trayIconPopup.isShowing()) {
                        trayIconPopup.dispose();
                        trayIconPopup = null;
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        if (!checkPassword()) return;
                        trayIconPopup = new TrayIconPopup();
                        calcLocation(trayIconPopup, e.getPoint());
                        trayIconPopup.setVisible(true);
                        trayIconPopup.startAutoHide();
                    }
                }
            } else {
                if (e.getClickCount() >= (subConfig.getBooleanProperty(PROPERTY_SINGLE_CLICK, false) ? 1 : 2) && !SwingUtilities.isLeftMouseButton(e)) {
                    miniIt(guiFrame.isVisible() & guiFrame.getState() != Frame.ICONIFIED, true);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (trayIconPopup != null && trayIconPopup.isShowing()) {
                        trayIconPopup.dispose();
                        trayIconPopup = null;
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (!checkPassword()) return;
                        trayIconPopup = new TrayIconPopup();
                        Point pointOnScreen = e.getLocationOnScreen();
                        if (e.getX() > 0) pointOnScreen.x -= e.getPoint().x;
                        calcLocation(trayIconPopup, pointOnScreen);
                        trayIconPopup.setVisible(true);
                        trayIconPopup.startAutoHide();
                    }
                }
            }
        }
    }

    private boolean checkPassword() {
        if (subConfig.getBooleanProperty(PROPERTY_PASSWORD_REQUIRED, false) && !subConfig.getStringProperty(PROPERTY_PASSWORD, "").equals("")) {
            String password = UserIO.getInstance().requestInputDialog(UserIO.STYLE_PASSWORD, T._.plugins_optional_JDLightTray_enterPassword(), null);
            if (password == null || !password.equals(subConfig.getStringProperty(PROPERTY_PASSWORD, ""))) {
                UserIO.getInstance().requestMessageDialog(T._.plugins_optional_JDLightTray_enterPassword_wrong());
                return false;
            }
        }
        return true;
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    private static void calcLocation(final TrayIconPopup window, final Point p) {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int limitX = (int) screenSize.getWidth() / 2;
                int limitY = (int) screenSize.getHeight() / 2;
                if (!CrossSystem.isMac()) {
                    if (p.x <= limitX) {
                        if (p.y <= limitY) {
                            // top left
                            window.setLocation(p.x, p.y);
                        } else {
                            // bottom left
                            window.setLocation(p.x, p.y - window.getHeight());
                        }
                    } else {
                        if (p.y <= limitY) {
                            // top right
                            window.setLocation(p.x - window.getWidth(), p.y);
                        } else {
                            // bottom right
                            window.setLocation(p.x - window.getWidth(), p.y - window.getHeight());
                        }
                    }
                } else {
                    if (p.getX() <= (screenSize.getWidth() - window.getWidth())) {
                        window.setLocation((int) p.getX(), 22);
                    } else {
                        window.setLocation(p.x - window.getWidth(), 22);
                    }
                }

                return null;
            }
        }.waitForEDT();
    }

    private void miniIt(final boolean minimize, boolean checkPW) {
        if (!minimize) {
            if (checkPW && !checkPassword()) return;

        }
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                /* set visible state */
                guiFrame.setVisible(!minimize);
                return null;
            }
        }.start();
        if (minimize == false) {
            /* workaround for : toFront() */
            new EDTHelper<Object>() {
                @Override
                public Object edtRun() {
                    guiFrame.setAlwaysOnTop(true);
                    disableAlwaysonTop.restart();
                    guiFrame.toFront();
                    return null;
                }
            }.start();
        }
    }

    /**
     * gets called if mouse stays over the tray. Edit delay in
     * {@link TrayMouseAdapter}
     */
    public void mouseStay(MouseEvent e) {
        if (!subConfig.getBooleanProperty(PROPERTY_TOOLTIP, true)) return;
        if (trayIconPopup != null && trayIconPopup.isVisible()) return;
        trayIconTooltip.showTooltip(((TrayMouseAdapter) e.getSource()).getEstimatedTopLeft());
    }

    private void removeTrayIcon() {
        try {
            if (trayIcon != null) {
                trayIcon.removeActionListener(this);
                SystemTray.getSystemTray().remove(trayIcon);
                if (ma != null) {
                    trayIcon.removeMouseListener(ma);
                    trayIcon.removeMouseMotionListener(ma);
                }
            }
        } catch (Throwable e) {
        }
    }

    // TODO
    // @Override
    // public Object interact(String command, Object parameter) {
    // if (command == null) return null;
    // if (command.equalsIgnoreCase("closetotray")) return
    // subConfig.getBooleanProperty(PROPERTY_CLOSE_TO_TRAY, true);
    // if (command.equalsIgnoreCase("refresh")) {
    // new EDTHelper<Object>() {
    // @Override
    // public Object edtRun() {
    // removeTrayIcon();
    // initGUI();
    // return null;
    // }
    // }.start();
    // }
    // return null;
    // }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if (subConfig.getBooleanProperty(PROPERTY_CLOSE_TO_TRAY, true)) {
            miniIt(true, true);
        }
    }

    public void windowDeactivated(WindowEvent e) {
        /* workaround for : toFront() */
        if (guiFrame != null) guiFrame.setAlwaysOnTop(false);
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowStateChanged(WindowEvent evt) {
        int oldState = evt.getOldState();
        int newState = evt.getNewState();

        if ((oldState & JFrame.ICONIFIED) == 0 && (newState & JFrame.ICONIFIED) != 0) {
            iconified = true;
            // Frame was not iconified
        } else if ((oldState & JFrame.ICONIFIED) != 0 && (newState & JFrame.ICONIFIED) == 0) {
            iconified = false;
            // Frame was iconified
        }
    }

    @Override
    protected void initExtension() throws StartException {
        ConfigContainer cc = new ConfigContainer(getName());
        initSettings(cc);
        configPanel = createPanelFromContainer(cc);
    }

    public void onLinkCollectorAbort(LinkCollectorEvent event) {
    }

    public void onLinkCollectorFilteredLinksAvailable(LinkCollectorEvent event) {
    }

    public void onLinkCollectorFilteredLinksEmpty(LinkCollectorEvent event) {
    }

    public void onLinkCollectorDataRefresh(LinkCollectorEvent event) {
    }

    public void onLinkCollectorStructureRefresh(LinkCollectorEvent event) {
        if ((!guiFrame.isVisible() && subConfig.getBooleanProperty(PROPERTY_SHOW_ON_LINKGRAB, true)) || subConfig.getBooleanProperty(PROPERTY_SHOW_ON_LINKGRAB2, false)) {
            /* dont try to restore jd if password required */
            if (subConfig.getBooleanProperty(PROPERTY_PASSWORD_REQUIRED, false)) return;
            if (!guiFrame.isVisible()) {
                /* set visible */
                new EDTHelper<Object>() {
                    @Override
                    public Object edtRun() {
                        guiFrame.setVisible(true);
                        return null;
                    }
                }.start();
            }
            /* workaround for : toFront() */
            new EDTHelper<Object>() {
                @Override
                public Object edtRun() {
                    guiFrame.toFront();
                    return null;
                }
            }.start();
            if (iconified) {
                /* restore normale state,if windows was iconified */
                new EDTHelper<Object>() {
                    @Override
                    public Object edtRun() {
                        /* after this normal, its back to iconified */
                        guiFrame.setState(JFrame.NORMAL);
                        return null;
                    }
                }.start();
            }
        }
    }

    public void onLinkCollectorLinksRemoved(LinkCollectorEvent event) {
    }

    public void onLinkCollectorLinkAdded(LinkCollectorEvent event, CrawledLink parameter) {
    }

}