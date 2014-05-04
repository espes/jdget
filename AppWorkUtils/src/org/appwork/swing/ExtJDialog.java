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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Window;

import javax.swing.JDialog;

import org.appwork.swing.event.PropertySetEvent;
import org.appwork.swing.event.PropertySetEventSender;

/**
 * @author Thomas
 * 
 */
public class ExtJDialog extends JDialog implements PropertyStateEventProviderInterface{

    private PropertySetEventSender propertySetEventSender;



  

    /**
     * 
     */
    public ExtJDialog() {
        super();
        
    }

    /**
     * @param dialog
     * @param flag
     */
    public ExtJDialog(final Dialog dialog, final boolean flag) {
        super(dialog, flag);
        
    }

    /**
     * @param dialog
     * @param s
     * @param flag
     * @param graphicsconfiguration
     */
    public ExtJDialog(final Dialog dialog, final String s, final boolean flag, final GraphicsConfiguration graphicsconfiguration) {
        super(dialog, s, flag, graphicsconfiguration);
        
    }

    /**
     * @param dialog
     * @param s
     * @param flag
     */
    public ExtJDialog(final Dialog dialog, final String s, final boolean flag) {
        super(dialog, s, flag);
        
    }

    /**
     * @param dialog
     * @param s
     */
    public ExtJDialog(final Dialog dialog, final String s) {
        super(dialog, s);
        
    }

    /**
     * @param dialog
     */
    public ExtJDialog(final Dialog dialog) {
        super(dialog);
        
    }

    /**
     * @param frame
     * @param flag
     */
    public ExtJDialog(final Frame frame, final boolean flag) {
        super(frame, flag);
        
    }

    /**
     * @param frame
     * @param s
     * @param flag
     * @param graphicsconfiguration
     */
    public ExtJDialog(final Frame frame, final String s, final boolean flag, final GraphicsConfiguration graphicsconfiguration) {
        super(frame, s, flag, graphicsconfiguration);
        
    }

    /**
     * @param frame
     * @param s
     * @param flag
     */
    public ExtJDialog(final Frame frame, final String s, final boolean flag) {
        super(frame, s, flag);
        
    }

    /**
     * @param frame
     * @param s
     */
    public ExtJDialog(final Frame frame, final String s) {
        super(frame, s);
        
    }

    /**
     * @param frame
     */
    public ExtJDialog(final Frame frame) {
        super(frame);
        
    }

    /**
     * @param window
     * @param modalitytype
     */
    public ExtJDialog(final Window window, final ModalityType modalitytype) {
        super(window, modalitytype);
        
    }

    /**
     * @param window
     * @param s
     * @param modalitytype
     * @param graphicsconfiguration
     */
    public ExtJDialog(final Window window, final String s, final ModalityType modalitytype, final GraphicsConfiguration graphicsconfiguration) {
        super(window, s, modalitytype, graphicsconfiguration);
        
    }

    /**
     * @param window
     * @param s
     * @param modalitytype
     */
    public ExtJDialog(final Window window, final String s, final ModalityType modalitytype) {
        super(window, s, modalitytype);
        
    }

    /**
     * @param window
     * @param s
     */
    public ExtJDialog(final Window window, final String s) {
        super(window, s);
        
    }

    /**
     * @param window
     */
    public ExtJDialog(final Window window) {
        super(window);
        
    }

    public PropertySetEventSender getPropertySetEventSender() {
        // no sync required. we are in edt
        if (propertySetEventSender == null) {
            propertySetEventSender = new PropertySetEventSender();
        }
        return propertySetEventSender;
    }
    public void setLocation(final int x, final int y) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, "location", getLocation(), new Point(x,y)));
        } 
        super.setLocation(x, y);
    }
    @Override
    protected void firePropertyChange(final String s, final Object obj, final Object obj1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, obj, obj));
        }
        super.firePropertyChange(s, obj, obj1);
    }

    @Override
    protected void firePropertyChange(final String s, final boolean flag, final boolean flag1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, flag, flag1));
        }
        super.firePropertyChange(s, flag, flag1);
    }

    @Override
    protected void firePropertyChange(final String s, final int i, final int j) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, i, j));
        }
        super.firePropertyChange(s, i, j);
    }

    @Override
    public void firePropertyChange(final String s, final byte byte0, final byte byte1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, byte0, byte1));
        }
        super.firePropertyChange(s, byte0, byte1);
    }

    @Override
    public void firePropertyChange(final String s, final char c, final char c1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, c, c1));
        }

        super.firePropertyChange(s, c, c1);
    }

    @Override
    public void firePropertyChange(final String s, final short word0, final short word1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, word0, word1));
        }

        super.firePropertyChange(s, word0, word1);
    }

    @Override
    public void firePropertyChange(final String s, final long l, final long l1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, l, l1));
        }

        super.firePropertyChange(s, l, l1);
    }

    @Override
    public void firePropertyChange(final String s, final float f, final float f1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, f, f1));
        }
        super.firePropertyChange(s, f, f1);
    }

    @Override
    public void firePropertyChange(final String s, final double d, final double d1) {
        if (propertySetEventSender != null) {
            propertySetEventSender.fireEvent(new PropertySetEvent(this, PropertySetEvent.Type.SET, s, d, d1));
        }
        super.firePropertyChange(s, d, d1);
    }

}
