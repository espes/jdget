package org.appwork.swing.synthetica;

import java.awt.Font;
import java.io.UnsupportedEncodingException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.components.ExtPasswordField;
import org.appwork.txtresource.TranslationFactory;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

public class SyntheticaHelper {

    public static String getDefaultFont() {
        switch (CrossSystem.OS_ID) {
        case CrossSystem.OS_WINDOWS_7:
        case CrossSystem.OS_WINDOWS_8:
        case CrossSystem.OS_WINDOWS_VISTA:
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
     * @throws UnsupportedEncodingException
     * 
     */
    public static void init() throws UnsupportedEncodingException {
        SyntheticaHelper.init("de.javasoft.plaf.synthetica.SyntheticaSimple2DLookAndFeel");

    }

    /**
     * @param string
     * @throws UnsupportedEncodingException
     */
    public static void init(final String laf) throws UnsupportedEncodingException {
        Log.L.info("LaF init: " + laf);
        final long start = System.currentTimeMillis();
        try {
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
            UIManager.put("Synthetica.menu.toolTipEnabled", true); UIManager.put("Synthetica.menuItem.toolTipEnabled", true);
            /*
             * NOTE: This Licensee Information may only be used by AppWork UG.
             * If you like to create derived creation based on this sourcecode,
             * you have to remove this license key. Instead you may use the FREE
             * Version of synthetica found on javasoft.de
             */

            /* we save around x-400 ms here by not using AES */
            final String key = new String(new byte[] { 67, 49, 52, 49, 48, 50, 57, 52, 45, 54, 49, 66, 54, 52, 65, 65, 67, 45, 52, 66, 55, 68, 51, 48, 51, 57, 45, 56, 51, 52, 65, 56, 50, 65, 49, 45, 51, 55, 69, 53, 68, 54, 57, 53 }, "UTF-8");
            if (key != null) {
                final String[] li = { "Licensee=AppWork UG", "LicenseRegistrationNumber=289416475", "Product=Synthetica", "LicenseType=Small Business License", "ExpireDate=--.--.----", "MaxVersion=2.999.999" };
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

}
