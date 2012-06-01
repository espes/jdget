package jd.gui.swing.jdgui.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import jd.gui.UserIO;

import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.extensions.LazyExtension;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.images.NewTheme;
import org.jdownloader.translate._JDT;

public class ExtensionEnableAction extends AbstractAction {

    private static final long serialVersionUID = 6997360773808826159L;
    private LazyExtension     plg;
    private ImageIcon         icon16Enabled;
    private ImageIcon         icon16Disabled;

    public ExtensionEnableAction(LazyExtension plg) {
        super(plg.getName());
        this.plg = plg;

        putValue(SELECTED_KEY, plg._isEnabled());
        icon16Enabled = getCheckBoxImage(20, true);
        icon16Disabled = getCheckBoxImage(20, false);
        updateIcon();
    }

    private void updateIcon() {
        if (isSelected()) {
            putValue(AbstractAction.SMALL_ICON, icon16Enabled);
        } else {
            putValue(AbstractAction.SMALL_ICON, icon16Disabled);
        }
    }

    public void actionPerformed(ActionEvent e) {

        updateIcon();

        if (!plg._isEnabled()) {
            try {
                plg._setEnabled(true);

                if (plg._getExtension().getGUI() != null) {
                    int ret = UserIO.getInstance().requestConfirmDialog(UserIO.DONT_SHOW_AGAIN, plg.getName(), _JDT._.gui_settings_extensions_show_now(plg.getName()));

                    if (UserIO.isOK(ret)) {
                        // activate panel
                        plg._getExtension().getGUI().setActive(true);
                        // bring panel to front
                        plg._getExtension().getGUI().toFront();

                    }
                }
            } catch (StartException e1) {
                Dialog.getInstance().showExceptionDialog(_JDT._.dialog_title_exception(), e1.getMessage(), e1);
            } catch (StopException e1) {
                e1.printStackTrace();
            }
        } else {
            try {

                plg._setEnabled(false);
            } catch (StartException e1) {
                e1.printStackTrace();
            } catch (StopException e1) {
                Dialog.getInstance().showExceptionDialog(_JDT._.dialog_title_exception(), e1.getMessage(), e1);
            }
        }
        AddonsMenu.getInstance().onUpdated();

    }

    public boolean isSelected() {
        final Object value = getValue(SELECTED_KEY);
        return (value == null) ? false : (Boolean) value;
    }

    public void setSelected(final boolean selected) {
        putValue(SELECTED_KEY, selected);
        updateIcon();
    }

    public ImageIcon getCheckBoxImage(int size, boolean selected) {

        if (selected) {
            return plg._getIcon(size);
        } else {
            return NewTheme.I().getDisabledIcon(plg._getIcon(size));
        }

    }
}
