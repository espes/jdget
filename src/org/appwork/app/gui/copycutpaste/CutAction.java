/**
 * 
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;

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
        super(APPWORKUTILS.T.COPYCUTPASTE_CUT());
        text = c;

        try {
            putValue(Action.SMALL_ICON, ImageProvider.getImageIcon("cut", 16, 16, true));
        } catch (final IOException e) {
            Log.exception(e);
        }

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */
    
    public void actionPerformed(final ActionEvent e) {
        text.cut();

    }

    @Override
    public boolean isEnabled() {
        return !(text instanceof JPasswordField) && text.isEnabled() && text.getSelectedText() != null;
    }
}
