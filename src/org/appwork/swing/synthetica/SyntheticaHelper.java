package org.appwork.swing.synthetica;

import java.io.UnsupportedEncodingException;

import javax.swing.UIManager;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.components.ExtPasswordField;
import org.appwork.utils.os.CrossSystem;

public class SyntheticaHelper {


    /**
     * @throws UnsupportedEncodingException
     * 
     */
    public static void init() throws UnsupportedEncodingException {
        init("de.javasoft.plaf.synthetica.SyntheticaSimple2DLookAndFeel");

    }

    /**
     * @param string
     * @throws UnsupportedEncodingException
     */
    public static void init(String laf) throws UnsupportedEncodingException {

        SyntheticaSettings config = JsonConfig.create(SyntheticaSettings.class);
        UIManager.put("Synthetica.window.decoration", false);
        UIManager.put("Synthetica.text.antialias", config.isTextAntiAliasEnabled());
        /* http://www.jyloo.com/news/?pubId=1297681728000 */
        /* we want our own FontScaling, not SystemDPI */
        UIManager.put("Synthetica.font.respectSystemDPI", config.isFontRespectsSystemDPI());
        UIManager.put("Synthetica.font.scaleFactor", config.getFontScaleFactor());
        if (config.isFontRespectsSystemDPI() && config.getFontScaleFactor() != 100) {

        }
        UIManager.put("Synthetica.animation.enabled", config.isAnimationEnabled());
        if (CrossSystem.isWindows()) {
            /* only windows opaque works fine */
            UIManager.put("Synthetica.window.opaque", config.isWindowOpaque());
        } else {
            /* must be true to disable it..strange world ;) */
            UIManager.put("Synthetica.window.opaque", true);
        }
        /*
         * NOTE: This Licensee Information may only be used by AppWork UG. If
         * you like to create derived creation based on this sourcecode, you
         * have to remove this license key. Instead you may use the FREE Version
         * of synthetica found on javasoft.de
         */

        /* we save around x-400 ms here by not using AES */
        String key = new String(new byte[] { 67, 49, 52, 49, 48, 50, 57, 52, 45, 54, 49, 66, 54, 52, 65, 65, 67, 45, 52, 66, 55, 68, 51, 48, 51, 57, 45, 56, 51, 52, 65, 56, 50, 65, 49, 45, 51, 55, 69, 53, 68, 54, 57, 53 }, "UTF-8");
        if (key != null) {
            String[] li = { "Licensee=AppWork UG", "LicenseRegistrationNumber=289416475", "Product=Synthetica", "LicenseType=Small Business License", "ExpireDate=--.--.----", "MaxVersion=2.999.999" };
            UIManager.put("Synthetica.license.info", li);
            UIManager.put("Synthetica.license.key", key);
        }

        de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.setLookAndFeel(laf);
        de.javasoft.plaf.synthetica.SyntheticaLookAndFeel.setExtendedFileChooserEnabled(false);

        ExtPasswordField.MASK = "*******";

    }

}
