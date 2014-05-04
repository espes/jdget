package org.appwork.swing.synthetica;

import java.awt.Font;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.UIManager;

import org.appwork.exceptions.WTFException;
import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.components.ExtPasswordField;
import org.appwork.txtresource.TranslationFactory;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

public class SyntheticaHelper {

    public static String getDefaultFont() {
        switch (CrossSystem.getOS()) {
        case WINDOWS_7:
        case WINDOWS_8:
        case WINDOWS_VISTA:
            return "Segoe UI";
        }

        return null;
    }

    /**
     * @param config
     * @param locale
     * @return
     */
    public static String getFontName(final SyntheticaSettings config, final LanguageFileSetup locale) {
        final String fontName = config.getFontName();

        final String fontFromTranslation = locale.config_fontname();

        String newFontName = null;

        if (fontFromTranslation != null && !"default".equalsIgnoreCase(fontFromTranslation)) {
            /* we have customized fontName in translation */
            /* lower priority than fontName in settings */
            newFontName = fontFromTranslation;
        }
        if (fontName != null && !"default".equalsIgnoreCase(fontName)) {
            /* we have customized fontName in settings, it has highest priority */
            newFontName = fontName;
        }
        if (newFontName == null) {
            newFontName = SyntheticaHelper.getDefaultFont();
        }
        return newFontName;
    }

    public static int getFontScaleFaktor(final SyntheticaSettings config, final LanguageFileSetup translationFileConfig) {
        int fontScale = -1;
        try {
            fontScale = Integer.parseInt(translationFileConfig.config_fontscale_faktor());
        } catch (final Exception e) {
        }
        if (config.getFontScaleFactor() != 100 || fontScale <= 0) {
            fontScale = config.getFontScaleFactor();
        }
        return fontScale;
    }

    /**
     * @throws IOException
     * 
     */
    public static void init() throws IOException {
        SyntheticaHelper.init("de.javasoft.plaf.synthetica.SyntheticaSimple2DLookAndFeel");

    }

    public static void init(final String laf) throws IOException {

        init(laf, readLicense());
    }

    /**
     * @return
     * @throws IOException
     */
    private static String readLicense() throws IOException {
        final URL url = Application.getRessourceURL("cfg/synthetica-license.key");
        if (url == null) {

            Log.L.warning("Missing Look And Feel License. Reverted to your System Look And Feel!");

            Log.L.warning("Missing Look And Feel License. Reverted to your System Look And Feel!");

            Log.L.warning("Missing Look And Feel License.");
            Log.L.warning("You can only use Synthetica Look and Feel in official JDownloader versions.");
            Log.L.warning("Reverted to your System Look And Feel!");
            Log.L.warning("If you are a developer, and want to do some gui work on the offical JDownloader Look And Feel, write e-mail@appwork.org to get a developer Look And Feel Key");
            throw new WTFException("No Synthetica License Found!");
        }
        return IO.readURLToString(url);
    }

    /**
     * @param string
     * @throws IOException
     */
    public static void init(final String laf, String license) throws IOException {

  
        if (CrossSystem.isMac()) {

            if (checkIfMacInitWillFail()) {
                System.runFinalization();
                System.gc();
                if (checkIfMacInitWillFail()) { throw new IOException("Cannot Init LookAndFeel. Windows Are Open"); }
            }
        }
        if (StringUtils.isEmpty(license)) {
            license = readLicense();
        }
        Log.L.info("LaF init: " + laf);
        final long start = System.currentTimeMillis();
        try {
            /* we save around x-400 ms here by not using AES */
            if (license == null) {

                Log.L.warning("Missing Look And Feel License. Reverted to your System Look And Feel!");

                Log.L.warning("Missing Look And Feel License. Reverted to your System Look And Feel!");

                Log.L.warning("Missing Look And Feel License.");
                Log.L.warning("You can only use Synthetica Look and Feel in official JDownloader versions.");
                Log.L.warning("Reverted to your System Look And Feel!");
                Log.L.warning("If you are a developer, and want to do some gui work on the offical JDownloader Look And Feel, write e-mail@appwork.org to get a developer Look And Feel Key");
                throw new WTFException("No Synthetica License Found!");
            }
            String[] licenseLines = Regex.getLines(license);
            // final String[] li = { };
            final ArrayList<String> valids = new ArrayList<String>();
            for (final String s : licenseLines) {
                if (!s.trim().startsWith("#") && !s.trim().startsWith("//")) {
                    valids.add(s);
                }
            }

            JFrame.setDefaultLookAndFeelDecorated(false);

            JDialog.setDefaultLookAndFeelDecorated(false);
            final LanguageFileSetup locale = TranslationFactory.create(LanguageFileSetup.class);
            final SyntheticaSettings config = JsonConfig.create(SyntheticaSettings.class);
            de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.setWindowsDecorated(false);
            // final HashMap<String, String> dummy = new HashMap<String,
            // String>();
            // dummy.put("defaultlaf","BlaBlaLeberLAF");
            // AppContext.getAppContext().put("swing.lafdata", dummy);
            UIManager.put("Synthetica.window.decoration", false);
            UIManager.put("Synthetica.text.antialias", config.isTextAntiAliasEnabled());
            UIManager.put("Synthetica.extendedFileChooser.rememberPreferences", Boolean.FALSE);
            UIManager.put("Synthetica.extendedFileChooser.rememberLastDirectory", Boolean.FALSE);

            // /* http://www.jyloo.com/news/?pubId=1297681728000 */
            // /* we want our own FontScaling, not SystemDPI */
            UIManager.put("Synthetica.font.respectSystemDPI", config.isFontRespectsSystemDPI());
            final int fontScale = SyntheticaHelper.getFontScaleFaktor(config, locale);
            UIManager.put("Synthetica.font.scaleFactor", fontScale);
            if (config.isFontRespectsSystemDPI() && fontScale != 100) {
                Log.L.warning("SystemDPI might interfere with JD's FontScaling");
            }
            UIManager.put("Synthetica.animation.enabled", config.isAnimationEnabled());
            if (CrossSystem.isWindows()) {
                /* only windows opaque works fine */
                UIManager.put("Synthetica.window.opaque", config.isWindowOpaque());
            } else {
                /* must be true to disable it..strange world ;) */
                UIManager.put("Synthetica.window.opaque", true);
            }
            UIManager.put("Synthetica.menu.toolTipEnabled", true);
            UIManager.put("Synthetica.menuItem.toolTipEnabled", true);
            /*
             * NOTE: This Licensee Information may only be used by AppWork UG.
             * If you like to create derived creation based on this sourcecode,
             * you have to remove this license key. Instead you may use the FREE
             * Version of synthetica found on javasoft.de
             */

            licenseLines = valids.toArray(new String[] {});
            final String key = licenseLines[0];
            final String[] li = new String[licenseLines.length - 1];
            System.arraycopy(licenseLines, 1, li, 0, li.length);
            if (key != null) {
                UIManager.put("Synthetica.license.info", li);
                UIManager.put("Synthetica.license.key", key);
            }

            de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.setLookAndFeel(laf);
            de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.setExtendedFileChooserEnabled(false);

            final String fontName = SyntheticaHelper.getFontName(config, locale);

            int fontSize = de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.getFont().getSize();
            fontSize = fontScale * fontSize / 100;

            if (fontName != null) {

                /* change Font */
                final int oldStyle = de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.getFont().getStyle();
                final Font newFont = new Font(fontName, oldStyle, fontSize);
                de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.setFont(newFont, false);
            }

            ExtPasswordField.MASK = "*******";

        } finally {
            final long time = System.currentTimeMillis() - start;
            Log.L.info("LAF Init duration: " + time + "ms");

        }
    }

    protected static boolean checkIfMacInitWillFail() {

        // synthetica init fails on mac if there are already active windows
        Window awindow[];
        final int j = (awindow = Window.getWindows()).length;

        for (int i = 0; i < j; i++) {
            final Window window = awindow[i];

            final boolean flag = !(window instanceof JWindow) && !(window instanceof JFrame) && !(window instanceof JDialog);
            if (!window.getClass().getName().contains("Popup$HeavyWeightWindow") && !flag) { return true; }

        }
        return false;
    }

}
