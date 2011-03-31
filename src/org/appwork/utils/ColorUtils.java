package org.appwork.utils;

import java.awt.Color;

public class ColorUtils {

    /**
     * Returns a color instance which has the given alpha value
     * 
     * @param background
     * @param alpha
     *            0-255
     * @return
     */
    public static Color getAlphaInstance(final Color background, final int alpha) {
        final Color ret = new Color(background.getRGB() & 0x00FFFFFF | (alpha & 0xFF) << 24, true);
        return ret;
    }

}
