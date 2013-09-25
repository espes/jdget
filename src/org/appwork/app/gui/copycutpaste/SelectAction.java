/**
 * 
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.locale._AWU;

/**
 * @author $Author: unknown$
 * 
 */
public class SelectAction extends AbstractAction {

    /**
     * 
     */
    private static final long    serialVersionUID = -4042641089773394231L;
    private final JTextComponent text;

    public SelectAction(final JTextComponent c) {
        super(_AWU.T.COPYCUTPASTE_SELECT());
        text = c;

        putValue(Action.SMALL_ICON, AWUTheme.I().getIcon("select", 16));

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */

    public void actionPerformed(final ActionEvent e) {
        text.selectAll();

    }

    @Override
    public boolean isEnabled() {
        return text.isEnabled() && text.getText().length() > 0;
    }
}
