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

    public static int charToMnemonic(final char mnemonic) {
        try {

            final Field f = KeyEvent.class.getField("VK_" + Character.toUpperCase(mnemonic));
            return (Integer) f.get(null);

        } catch (final Exception e) {

        }
        return 0;
    }

    private TooltipFactory tooltipFactory;

    private boolean        toggle = false;

    public BasicAction() {
        super();
    }

    /**
     * @param routerSendAction_RouterSendAction_
     */
    public BasicAction(final String name) {
        super(name);
    }

    public String getName() {
        return (String) this.getValue(Action.NAME);
    }

    public String getShortCutString() {
        final Object value = this.getValue(Action.ACCELERATOR_KEY);

        return value == null ? null : KeyUtils.getShortcutString((KeyStroke) this.getValue(Action.ACCELERATOR_KEY), true);
    }

    public Icon getSmallIcon() {
        return (Icon) this.getValue(Action.SMALL_ICON);
    }

    /**
     * @return
     */
    public TooltipFactory getTooltipFactory() {
        return this.tooltipFactory;
    }

    /**
     * Returns the actions description
     */
    public String getTooltipText() {
        try {
            final Object ret = this.getValue(Action.SHORT_DESCRIPTION);
            return ret == null ? null : ret.toString();
        } catch (final Exception e) {
            return null;
        }
    }

    // /**
    // * Sets the shortcut fort this action. a System dependend behaviour is
    // * choosen. e,g. WIndows+ Strg+ Acceleratir
    // *
    // * example: action.setAccelerator("ENTER"); defines a Enter shortcut
    // *
    // * @param accelerator
    // * @depcreated. use {@link #setAccelerator(KeyStroke)}
    // */
    // @Deprecated
    // public void setAccelerator(final String accelerator) {
    // KeyStroke ks;
    // if (accelerator != null && accelerator.length() > 0 &&
    // !accelerator.equals("-")) {
    // final Class<?> b = KeyEvent.class;
    // final String[] split = accelerator.split("\\+");
    // int mod = 0;
    // try {
    // final int splitLength = split.length;
    // for (int i = 0; i < splitLength - 1; ++i) {
    // if (new Regex(split[i], "^CTRL$").matches()) {
    // mod = mod | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // } else if (new Regex(split[i], "^SHIFT$").matches()) {
    // mod = mod | KeyEvent.SHIFT_DOWN_MASK;
    // } else if (new Regex(split[i], "^ALTGR$").matches()) {
    // mod = mod | KeyEvent.ALT_GRAPH_DOWN_MASK;
    // } else if (new Regex(split[i], "^ALT$").matches()) {
    // mod = mod | KeyEvent.ALT_DOWN_MASK;
    // } else if (new Regex(split[i], "^META$").matches()) {
    // mod = mod | KeyEvent.META_DOWN_MASK;
    // } else {
    // Log.L.info(getName() + " Shortcuts: skipping wrong modifier " + mod +
    // " in " + accelerator);
    // }
    // }
    //
    // final Field f = b.getField("VK_" + split[splitLength - 1].toUpperCase());
    // final int m = (Integer) f.get(null);
    // putValue(AbstractAction.ACCELERATOR_KEY, ks = KeyStroke.getKeyStroke(m,
    // mod));
    // Log.L.finest(getName() + " Shortcuts: mapped " + accelerator + " to " +
    // ks);
    // } catch (final Exception e) {
    // // JDLogger.exception(e);
    // putValue(AbstractAction.ACCELERATOR_KEY, ks =
    // KeyStroke.getKeyStroke(accelerator.charAt(accelerator.length() - 1),
    // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    // Log.L.finest(getName() + " Shortcuts: mapped " + accelerator + " to " +
    // ks + " (Exception)");
    // }
    // }
    // }

    /**
     * For toggle actions, this method returns if it is currently selected
     * 
     * @return
     */
    public boolean isSelected() {
        final Object value = this.getValue(Action.SELECTED_KEY);
        return value == null ? false : (Boolean) value;
    }

    public boolean isToggle() {
        return this.toggle;
    }

    public BasicAction setAccelerator(final KeyStroke stroke) {
        this.putValue(Action.ACCELERATOR_KEY, stroke);
        return this;
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
        if (this.getName() == null) { throw new IllegalStateException("First set Name"); }
        if (key == null) {
            key = "-";
        }
        final char mnemonic = key.charAt(0);

        if (mnemonic != 0 && !key.contentEquals("-")) {

            final int m = BasicAction.charToMnemonic(mnemonic);
            this.putValue(Action.MNEMONIC_KEY, m);
            this.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, this.getName().indexOf(m));
        }
    }

    public void setName(final String name) {
        this.putValue(Action.NAME, name);

    }

    /**
     * Sets the action selected. WARNING. Swing usualy handles the selection
     * state
     * 
     * @param selected
     */
    public void setSelected(final boolean selected) {
        this.putValue(Action.SELECTED_KEY, selected);
        this.toggle = true;
    }

    public void setSmallIcon(final Icon icon) {
        this.putValue(Action.SMALL_ICON, icon);
    }

    public void setTooltipFactory(final TooltipFactory factory) {
        this.tooltipFactory = factory;
    }

    public void setTooltipText(final String text) {
        this.putValue(Action.SHORT_DESCRIPTION, text);
    }

}
