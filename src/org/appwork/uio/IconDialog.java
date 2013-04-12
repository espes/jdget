package org.appwork.uio;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.appwork.swing.MigPanel;
import org.appwork.utils.swing.dialog.ConfirmDialog;
import org.appwork.utils.swing.dialog.Dialog;

public class IconDialog extends ConfirmDialog implements MessageDialogInterface {

 

    private ImageIcon bigIcon;

    public IconDialog(final int flag, final String title, final String msg, final ImageIcon icon, final String okText) {
        super(flag|Dialog.STYLE_HIDE_ICON|Dialog.BUTTONS_HIDE_CANCEL, title, msg, null, okText, null);
        bigIcon=icon;

    }
    
    @Override
    public JComponent layoutDialogContent() {
        final MigPanel p = new MigPanel("wrap 1", "[grow,fill]", "[][]");       
        
        p.add(new JLabel(bigIcon));
        p.add(new JLabel(getMessage(),SwingConstants.CENTER));
        return p;
    }

}
