/**
 * 
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.locale._AWU;

/**
 * @author $Author: unknown$
 * 
 */
public class CutAction extends AbstractAction {
    /**
     * 
     */
    private static final long    serialVersionUID = -7350323932196046071L;
    private final JTextComponent text;

    public CutAction(final JTextComponent c) {
        super(_AWU.T.COPYCUTPASTE_CUT());
        text = c;

        putValue(Action.SMALL_ICON, AWUTheme.I().getIcon("cut", 16));

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */

    public void actionPerformed(final ActionEvent e) {
        if (text == null) {
            if (e.getSource() instanceof JTextComponent) {
                final JTextComponent text = (JTextComponent) e.getSource();
                text.cut();
            }
        } else {
            text.cut();
        }

    }

    @Override
    public boolean isEnabled() {
        if (text == null) { return true; }
        return !(text instanceof JPasswordField) && text.isEnabled() && text.getSelectedText() != null;
    }
}
