/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.action;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.appwork.swing.components.tooltips.TooltipFactory;
import org.appwork.utils.KeyUtils;

/**
 * @author thomas
 * 
 */
public abstract class BasicAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -198177718803470771L;
    private TooltipFactory    tooltipFactory;
    private boolean           toggle           = false;

    /**
     * @param routerSendAction_RouterSendAction_
     */
    public BasicAction(final String name) {
        super(name);
    }

    public boolean isToggle() {
        return toggle;
    }

    public BasicAction() {
        super();
    }

    public void setSmallIcon(final Icon icon) {
        putValue(SMALL_ICON, icon);
    }

    public Icon getSmallIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    public String getName() {
        return (String) getValue(NAME);
    }

    public void setName(final String name) {
        putValue(NAME, name);

    }

    public BasicAction setAccelerator(final KeyStroke stroke) {
        putValue(AbstractAction.ACCELERATOR_KEY, stroke);
  
        return this;
    }

//    /**
//     * Sets the shortcut fort this action. a System dependend behaviour is
//     * choosen. e,g. WIndows+ Strg+ Acceleratir
//     * 
//     * example: action.setAccelerator("ENTER"); defines a Enter shortcut
//     * 
//     * @param accelerator
//     * @depcreated. use {@link #setAccelerator(KeyStroke)}
//     */
//    @Deprecated
//    public void setAccelerator(final String accelerator) {
//        KeyStroke ks;
//        if (accelerator != null && accelerator.length() > 0 && !accelerator.equals("-")) {
//            final Class<?> b = KeyEvent.class;
//            final String[] split = accelerator.split("\\+");
//            int mod = 0;
//            try {
//                final int splitLength = split.length;
//                for (int i = 0; i < splitLength - 1; ++i) {
//                    if (new Regex(split[i], "^CTRL$").matches()) {
//                        mod = mod | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
//                    } else if (new Regex(split[i], "^SHIFT$").matches()) {
//                        mod = mod | KeyEvent.SHIFT_DOWN_MASK;
//                    } else if (new Regex(split[i], "^ALTGR$").matches()) {
//                        mod = mod | KeyEvent.ALT_GRAPH_DOWN_MASK;
//                    } else if (new Regex(split[i], "^ALT$").matches()) {
//                        mod = mod | KeyEvent.ALT_DOWN_MASK;
//                    } else if (new Regex(split[i], "^META$").matches()) {
//                        mod = mod | KeyEvent.META_DOWN_MASK;
//                    } else {
//                        Log.L.info(getName() + " Shortcuts: skipping wrong modifier " + mod + " in " + accelerator);
//                    }
//                }
//
//                final Field f = b.getField("VK_" + split[splitLength - 1].toUpperCase());
//                final int m = (Integer) f.get(null);
//                putValue(AbstractAction.ACCELERATOR_KEY, ks = KeyStroke.getKeyStroke(m, mod));
//                Log.L.finest(getName() + " Shortcuts: mapped " + accelerator + " to " + ks);
//            } catch (final Exception e) {
//                // JDLogger.exception(e);
//                putValue(AbstractAction.ACCELERATOR_KEY, ks = KeyStroke.getKeyStroke(accelerator.charAt(accelerator.length() - 1), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//                Log.L.finest(getName() + " Shortcuts: mapped " + accelerator + " to " + ks + " (Exception)");
//            }
//        }
//    }

    /**
     * Sets the action selected. WARNING. Swing usualy handles the selection
     * state
     * 
     * @param selected
     */
    public void setSelected(final boolean selected) {
        putValue(SELECTED_KEY, selected);
        toggle = true;
    }

    public String getShortCutString() {
        final Object value = getValue(Action.ACCELERATOR_KEY);

        return (value == null) ? null : KeyUtils.getShortcutString((KeyStroke) getValue(Action.ACCELERATOR_KEY), true);
    }

    /**
     * For toggle actions, this method returns if it is currently selected
     * 
     * @return
     */
    public boolean isSelected() {
        final Object value = getValue(SELECTED_KEY);
        return (value == null) ? false : (Boolean) value;
    }

    /**
     * Returns the actions description
     */
    public String getTooltipText() {
        try {
            return getValue(AbstractAction.SHORT_DESCRIPTION).toString();
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Sets the Mnemonic for this icon. Mnemonics are used to activate actions
     * using the keyboard (ALT + Mnemonic) usualy the mnemonic is part of the
     * name, and thus gets underlined in menus.
     * 
     * Always set the Mnemonic AFTER! setting the title
     * 
     * @param key
     */

    public void setMnemonic(String key) {
        if (getName() == null) { throw new IllegalStateException("First set Name"); }
        if (key == null) {
            key = "-";
        }
        final char mnemonic = key.charAt(0);

        if (mnemonic != 0 && !key.contentEquals("-")) {

            final int m = charToMnemonic(mnemonic);
            putValue(AbstractAction.MNEMONIC_KEY, m);
            putValue(AbstractAction.DISPLAYED_MNEMONIC_INDEX_KEY, getName().indexOf(m));
        }
    }

    public static int charToMnemonic(final char mnemonic) {
        try {

            final Field f = KeyEvent.class.getField("VK_" + Character.toUpperCase(mnemonic));
            return (Integer) f.get(null);

        } catch (final Exception e) {

        }
        return 0;
    }

    public void setTooltipText(final String text) {
        putValue(SHORT_DESCRIPTION, text);
    }

    /**
     * @return
     */
    public TooltipFactory getTooltipFactory() {
        return tooltipFactory;
    }

    public void setTooltipFactory(final TooltipFactory factory) {
        tooltipFactory = factory;
    }

}
