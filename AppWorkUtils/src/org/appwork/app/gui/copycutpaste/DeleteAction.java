/**
 * 
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.event.ActionEvent;

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
public class DeleteAction extends AbstractAction {
    /**
     * 
     */
    private static final long    serialVersionUID = -7350323932196046071L;
    private final JTextComponent text;

    public DeleteAction(final JTextComponent c) {
        super(_AWU.T.COPYCUTPASTE_DELETE());
        this.text = c;

        this.putValue(Action.SMALL_ICON, AWUTheme.I().getIcon("delete", 16));

        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */

    public void actionPerformed(final ActionEvent e) {
        this.text.replaceSelection(null);

    }

    @Override
    public boolean isEnabled() {
        return this.text.isEnabled() && this.text.getSelectedText() != null;
    }
}
