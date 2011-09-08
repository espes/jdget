/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.appwork.utils.locale._AWU;

/**
 * @author thomas
 * 
 */

public class Shortcuts {

    public static String getAcceleratorString(KeyStroke ks) {
        if (ks == null) return null;
        final StringBuilder builder = new StringBuilder();
        builder.append(getModifiersText(ks.getModifiers()));
        if (builder.length() > 0) builder.append('+');
        if (ks.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            return builder.append(ks.getKeyChar()).toString();
        } else {
            return builder.append(KeyEvent.getKeyText(ks.getKeyCode())).toString();
        }
    }

    private static String getModifiersText(final int modifiers) {
        final StringBuilder buf = new StringBuilder();

        if ((modifiers & KeyEvent.CTRL_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_ctrl());
        }
        if ((modifiers & KeyEvent.META_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_meta());
        }
        if ((modifiers & KeyEvent.ALT_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_alt());
        }
        if ((modifiers & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_altGr());
        }
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_shift());
        }
        if ((modifiers & KeyEvent.BUTTON1_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_button1());
        }
        if ((modifiers & KeyEvent.BUTTON2_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_button2());
        }
        if ((modifiers & KeyEvent.BUTTON3_DOWN_MASK) != 0) {
            if (buf.length() > 0) buf.append('+');
            buf.append(_AWU.T.ShortCuts_key_button3());
        }

        return buf.toString();
    }

}
