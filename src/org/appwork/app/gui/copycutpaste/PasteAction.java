/**
 * 
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.ClipboardUtils;
import org.appwork.utils.locale._AWU;

/**
 * @author $Author: unknown$
 * 
 */
public class PasteAction extends AbstractAction {
    /**
     * 
     */
    private static final long    serialVersionUID = -7917978502085788063L;
    private final JTextComponent text;

    public PasteAction(final JTextComponent c) {
        super(_AWU.T.COPYCUTPASTE_PASTE());
        this.text = c;

        this.putValue(Action.SMALL_ICON, AWUTheme.I().getIcon("paste", 16));

        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */

    public void actionPerformed(final ActionEvent e) {
        this.text.paste();

    }

    @Override
    public boolean isEnabled() {
        if (this.text.isEditable() && this.text.isEnabled()) {
            return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(ClipboardUtils.stringFlavor);
        } else {
            return false;
        }
    }
}
