package org.jdownloader.extensions.vlcstreaming;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jd.Launcher;
import jd.nutils.Executer;
import jd.parser.Regex;
import jd.plugins.AddonPanel;
import jd.plugins.DownloadLink;

import org.appwork.utils.Application;
import org.appwork.utils.StringUtils;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.actions.AppAction;
import org.jdownloader.api.RemoteAPIController;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.gui.menu.MenuContext;
import org.jdownloader.gui.menu.eventsender.MenuFactoryEventSender;
import org.jdownloader.gui.menu.eventsender.MenuFactoryListener;
import org.jdownloader.gui.views.downloads.table.DownloadTableContext;
import org.jdownloader.images.NewTheme;
import org.jdownloader.logging.LogController;

public class VLCStreamingExtension extends AbstractExtension<VLCStreamingConfig, VLCStreamingTranslation> implements MenuFactoryListener {

    private VLCStreamingAPIImpl vlcstreamingAPI;
    private String              vlcBinary;

    @Override
    protected void stop() throws StopException {
        try {
            MenuFactoryEventSender.getInstance().removeListener(this);
            if (vlcstreamingAPI != null) {
                RemoteAPIController.getInstance().unregister(vlcstreamingAPI);
            }
        } finally {
            vlcstreamingAPI = null;
        }
    }

    @Override
    protected void start() throws StartException {
        vlcstreamingAPI = new VLCStreamingAPIImpl();
        RemoteAPIController.getInstance().register(vlcstreamingAPI);
        MenuFactoryEventSender.getInstance().addListener(this, true);
    }

    @Override
    protected void initExtension() throws StartException {
        vlcBinary = getVLCBinary();
        if (StringUtils.isEmpty(vlcBinary)) { throw new StartException("Could not find vlc binary"); }
    }

    @Override
    public ExtensionConfigPanel<?> getConfigPanel() {
        return null;
    }

    @Override
    public boolean hasConfigPanel() {
        return false;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ImageIcon getIcon(int size) {
        return NewTheme.I().getIcon("vlc-ico", size);
    }

    @Override
    public AddonPanel<? extends AbstractExtension<VLCStreamingConfig, VLCStreamingTranslation>> getGUI() {
        return null;
    }

    protected String getVLCBinary() {
        String ret = getSettings().getVLCCommand();
        if (!StringUtils.isEmpty(ret)) return ret;
        if (CrossSystem.isWindows()) {
            return getVLCBinaryFromWindowsRegistry();
        } else {
            return "vlc";
        }
    }

    @Override
    public void onExtendPopupMenu(final MenuContext<?> context) {
        if (context instanceof DownloadTableContext) {

            final JMenu menu = new JMenu(T._.popup_streaming()) {
                /**
                 * 
                 */
                private static final long serialVersionUID = -5156294142768994122L;

                protected JMenuItem createActionComponent(Action a) {
                    if (((AppAction) a).isToggle()) { return new JCheckBoxMenuItem(a); }
                    return super.createActionComponent(a);
                }
            };

            // menu.setEnabled(false);
            ((DownloadTableContext) context).getMenu().add(menu);
            menu.setIcon(getIcon(18));
            menu.setEnabled(((DownloadTableContext) context).getSelectionInfo().isLinkContext());
            menu.add(new AppAction() {
                DownloadLink              link             = ((DownloadTableContext) context).getSelectionInfo().getContextLink();

                private static final long serialVersionUID = 1375146705091555054L;

                {
                    setName(T._.popup_streaming_playvlc());
                    Image front = NewTheme.I().getImage("media-playback-start", 20, true);
                    setSmallIcon(new ImageIcon(ImageProvider.merge(getIcon(20).getImage(), front, 0, 0, 5, 5)));
                    setEnabled(isDirectPlaySupported(link));
                }

                public boolean isDirectPlaySupported(DownloadLink link) {
                    String filename = link.getName().toLowerCase(Locale.ENGLISH);
                    if (filename.endsWith("mkv")) return true;
                    if (filename.endsWith("mov")) return true;
                    if (filename.endsWith("avi")) return true;
                    if (filename.endsWith("mp4")) return true;
                    return false;
                }

                public void actionPerformed(ActionEvent e) {
                    Executer exec = new Executer(getVLCBinary());
                    exec.setLogger(LogController.CL());
                    exec.addParameters(new String[] { "http://127.0.0.1:" + RemoteAPIController.getInstance().getApiPort() + "/vlcstreaming/play?id=" + link.getUniqueID() });
                    exec.setRunin(Application.getRoot(Launcher.class));
                    exec.setWaitTimeout(0);
                    exec.start();
                }
            });

        }
    }

    private String getVLCBinaryFromWindowsRegistry() {
        // Retrieve a reference to the root of the user preferences tree
        Method closeKey = null;
        int hKey = -1;
        try {
            final Preferences systemRoot = Preferences.systemRoot();
            final Class clz = systemRoot.getClass();
            final int KEY_READ = 0x20019;
            Class[] params1 = { byte[].class, int.class, int.class };
            final Method openKey = clz.getDeclaredMethod("openKey", params1);
            openKey.setAccessible(true);
            Class[] params2 = { int.class };
            closeKey = clz.getDeclaredMethod("closeKey", params2);
            closeKey.setAccessible(true);
            final Method winRegQueryValue = clz.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
            winRegQueryValue.setAccessible(true);

            String key = "SOFTWARE\\Classes\\Applications\\vlc.exe\\shell\\Open\\command";
            hKey = (Integer) openKey.invoke(systemRoot, toByteEncodedString(key), KEY_READ, KEY_READ);

            byte[] valb = (byte[]) winRegQueryValue.invoke(systemRoot, hKey, toByteEncodedString(""));

            String vals = (valb != null ? new String(valb).trim() : null);
            return new Regex(vals, "\"(.*?\\.exe)\"").getMatch(0);
        } catch (Throwable e) {
            LogController.CL().log(e);
            return null;
        } finally {
            try {
                if (hKey != -1) closeKey.invoke(Preferences.userRoot(), hKey);
            } catch (final Throwable e) {
            }
        }
    }

    private static byte[] toByteEncodedString(String str) {
        byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
}
