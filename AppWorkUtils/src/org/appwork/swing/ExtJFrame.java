/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.app.gui
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

import org.appwork.swing.event.PropertySetEvent;
import org.appwork.swing.event.PropertySetEventSender;
import org.appwork.utils.os.CrossSystem;

/**
 * @author Thomas
 * 
 */
public class ExtJFrame extends JFrame implements PropertyStateEventProviderInterface {

    public static final String     PROPERTY_LOCATION               = "location";
    public static final String     PROPERTY_EXTENDED_STATE         = "extendedState";
    public static final String     PROPERTY_FOCUSABLE              = "focusable";
    public static final String     PROPERTY_FOCUSABLE_WINDOW_STATE = "focusableWindowState";
    public static final String     PROPERTY_ALWAYS_ON_TOP          = "alwaysOnTop";
    public static final String     PROPERTY_VISIBLE                = "visible";

    private PropertySetEventSender propertySetEventSender;

    /**
     * @throws HeadlessException
     */
    public ExtJFrame() throws HeadlessException {
        super();
        macSpecials();

    }

    /**
     * 
     */
    private void macSpecials() {
        if (CrossSystem.isMac()) {
            addWindowStateListener(new WindowStateListener() {

                private boolean oldVisibleState = true;

                @Override
                public void windowStateChanged(WindowEvent e) {

                    if ((getExtendedState() & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
                        // there is a bug that caused MAC OS 10.9 under java
                        // 1.7.0_25-b15 to popup the iconified owner. the
                        // visible owner
                        // cannot be used or accessed in any way.
                        // workaround: setting the frame invisible in iconified
                        // state should do the job
                        oldVisibleState = isVisible();
                        setVisible(false);
                    } else {
                        setVisible(oldVisibleState);
                    }

                }
            });
        }

    }

    /**
     * @param graphicsconfiguration
     */
    public ExtJFrame(final GraphicsConfiguration graphicsconfiguration) {
        super(graphicsconfiguration);
        macSpecials();

    }

    /**
     * @param s
     * @throws HeadlessException
     */
    public ExtJFrame(final String s) throws HeadlessException {
        super(s);
        macSpecials();
    }

    /**
     * @param s
     * @param graphicsconfiguration
     */
    public ExtJFrame(final String s, final GraphicsConfiguration graphicsconfiguration) {
        super(s, graphicsconfiguration);
        macSpecials();

    }

    @Override
    protected void firePropertyChange(final String s, final boolean flag, final boolean flag1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, flag, flag1));
        }
        super.firePropertyChange(s, flag, flag1);
    }

    @Override
    public void firePropertyChange(final String s, final byte byte0, final byte byte1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, byte0, byte1));
        }
        super.firePropertyChange(s, byte0, byte1);
    }

    @Override
    public void firePropertyChange(final String s, final char c, final char c1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, c, c1));
        }

        super.firePropertyChange(s, c, c1);
    }

    @Override
    public void firePropertyChange(final String s, final double d, final double d1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, d, d1));
        }
        super.firePropertyChange(s, d, d1);
    }

    @Override
    public void firePropertyChange(final String s, final float f, final float f1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, f, f1));
        }
        super.firePropertyChange(s, f, f1);
    }

    @Override
    protected void firePropertyChange(final String s, final int i, final int j) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, i, j));
        }
        super.firePropertyChange(s, i, j);
    }

    @Override
    public void firePropertyChange(final String s, final long l, final long l1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, l, l1));
        }

        super.firePropertyChange(s, l, l1);
    }

    @Override
    protected void firePropertyChange(final String s, final Object obj, final Object obj1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, obj, obj));
        }
        super.firePropertyChange(s, obj, obj1);
    }

    @Override
    public void firePropertyChange(final String s, final short word0, final short word1) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, word0, word1));
        }

        super.firePropertyChange(s, word0, word1);
    }

    public PropertySetEventSender getPropertySetEventSender() {
        // no sync required. we are in edt
        if (this.propertySetEventSender == null) {
            this.propertySetEventSender = new PropertySetEventSender();
        }

        return this.propertySetEventSender;
    }

    @Override
    public void setExtendedState(final int i) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, ExtJFrame.PROPERTY_EXTENDED_STATE, this.getExtendedState(), i));
        }
        super.setExtendedState(i);
    }

    @Override
    public void setLocation(final int x, final int y) {
        if (this.propertySetEventSender != null) {
            this.propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, ExtJFrame.PROPERTY_LOCATION, this.getLocation(), new Point(x, y)));
        }
        super.setLocation(x, y);
    }

}
