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
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;

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
        super(APPWORKUTILS.COPYCUTPASTE_SELECT.s());
        text = c;

        try {
            putValue(Action.SMALL_ICON, ImageProvider.getImageIcon("select", 16, 16, true));
        } catch (final IOException e) {
            Log.exception(e);
        }

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.rapidshare.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        text.selectAll();

    }

    @Override
    public boolean isEnabled() {
        return text.isEnabled() && text.getText().length() > 0;
    }
}
