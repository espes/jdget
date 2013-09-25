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
public class CopyAction extends AbstractAction {
    /**
     * 
     */
    private static final long    serialVersionUID = -7350323932196046071L;
    private final JTextComponent text;

    public CopyAction(final JTextComponent c) {
        super(_AWU.T.COPYCUTPASTE_COPY());
        text = c;

        putValue(Action.SMALL_ICON, AWUTheme.I().getIcon("copy", 16));

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    }
    public Object getValue(String key) {
        return super.getValue(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */

    public void actionPerformed(final ActionEvent e) {
       
        text.copy();

    }

    @Override
    public boolean isEnabled() {
        return !(text instanceof JPasswordField) && text.isEnabled() && text.getSelectedText() != null;
    }
}
