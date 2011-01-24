/**
 * 
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.appwork.utils.ClipboardUtils;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;

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
        super(APPWORKUTILS.COPYCUTPASTE_PASTE.s());
        text = c;

        try {
            putValue(Action.SMALL_ICON, ImageProvider.getImageIcon("paste", 16, 16, true));
        } catch (final IOException e) {
            Log.exception(e);
        }

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        text.paste();

    }

    @Override
    public boolean isEnabled() {
        if (text.isEditable() && text.isEnabled()) {
            return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(ClipboardUtils.stringFlavor);
        } else {
            return false;
        }
    }
}
