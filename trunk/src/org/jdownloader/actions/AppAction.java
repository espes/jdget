package org.jdownloader.actions;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import org.appwork.swing.action.BasicAction;
import org.jdownloader.actions.event.AppActionEvent;
import org.jdownloader.actions.event.AppActionEventSender;
import org.jdownloader.images.NewTheme;

/**
 * This abstract class is the parent class for all actions in JDownloader
 * 
 * @author thomas
 * 
 */
public abstract class AppAction extends BasicAction {

    protected String             iconKey;

    protected int                size;

    private AppActionEventSender eventSender;

    private boolean              visible = true;

    public AppAction() {
        super();

    }

    /**
     * e.g. KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK
     * 
     * @param vkQ
     * @param ctrlDownMask
     */
    public void setAccelerator(int vkQ, int ctrlDownMask) {
        setAccelerator(KeyStroke.getKeyStroke(vkQ, ctrlDownMask));
    }

    /**
     * KeyEvent.VK_Q
     * 
     * @param vkQ
     */
    public void setAccelerator(int vkQ) {
        setAccelerator(vkQ, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    }

    public synchronized AppActionEventSender getEventSender() {
        if (eventSender == null) {
            eventSender = new AppActionEventSender();
            addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    eventSender.fireEvent(new AppActionEvent(AppAction.this, AppActionEvent.Type.PROPERTY_CHANGE, evt));
                }
            });
        }
        return eventSender;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
        setIconSizes(18);

    }

    public String getIconKey() {
        return iconKey;
    }

    public AppAction setIconSizes(int size) {
        this.size = size;
        return this;
    }

    public Object getValue(String key) {
        if (iconKey != null && LARGE_ICON_KEY.equalsIgnoreCase(key)) {
            return NewTheme.I().getIcon(iconKey, size);
        } else if (iconKey != null && SMALL_ICON.equalsIgnoreCase(key)) {
            //
            return NewTheme.I().getIcon(iconKey, size);
        }
        return super.getValue(key);
    }

    /**
     * invalid actions should not be used.
     * 
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean newValue) {
        if (visible == newValue) return;
        boolean oldValue = visible;
        this.visible = newValue;

        firePropertyChange("visible", Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }

}
