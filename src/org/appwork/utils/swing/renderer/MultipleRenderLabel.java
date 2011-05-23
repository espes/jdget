/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.renderer
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.renderer;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.ImageProvider.ImageProvider;

/**
 * @author daniel
 * 
 */
public class MultipleRenderLabel extends JPanel {

    private static final long serialVersionUID = -378709535509849986L;
    private RenderLabel       left;
    private RenderLabel[]     rights           = null;
    private int               ICONCOUNT        = 0;

    public MultipleRenderLabel(final int size) {
        super(new MigLayout("ins 0", "[]0[fill,grow,align right]"));
        this.rights = new RenderLabel[size];
        this.ICONCOUNT = size;
        this.add(this.left = new RenderLabel());
        this.left.setOpaque(false);
        for (int i = 0; i < this.ICONCOUNT; i++) {
            this.add(this.rights[i] = new RenderLabel(), "dock east");
            this.rights[i].setOpaque(false);
        }
        this.setOpaque(false);
    }

    /**
     * Remember, that its always the same panel instance. so we have to reset to
     * defaults before each cellrenderer call.
     */
    public void clearIcons(final int counter) {
        for (int i = counter; i < this.ICONCOUNT; i++) {
            this.rights[i].setIcon(null);
            this.rights[i].setText(null);
            this.rights[i].setToolTipText(null);
        }
    }

    @Override
    public String getToolTipText() {
        final StringBuilder sb = new StringBuilder();
        if (this.left.getToolTipText() != null) {
            sb.append(this.left.getToolTipText());
        }
        for (int i = this.rights.length - 1; i >= 0; --i) {
            if (this.rights[i].getToolTipText() != null) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(this.rights[i].getToolTipText());
            }
        }
        if (sb.length() > 0) { return sb.toString(); }
        return null;
    }

    @Override
    public void setEnabled(final boolean b) {
        if (this.left == null) { return; }
        if (b == false) {
            this.left.setDisabledIcon(ImageProvider.getDisabledIcon(this.left.getIcon()));
        }
        this.left.setEnabled(b);
        for (int i = 0; i < this.ICONCOUNT; i++) {
            if (b == false) {
                this.rights[i].setDisabledIcon(ImageProvider.getDisabledIcon(this.rights[i].getIcon()));
            }
            this.rights[i].setEnabled(b);
        }
    }

    @Override
    public void setForeground(final Color fg) {
        super.setForeground(fg);
        if (this.left == null) { return; }
        this.left.setForeground(fg);
        for (final RenderLabel right : this.rights) {
            right.setForeground(fg);
        }
    }

    public void setIcon(final int i, final Icon icon, final String text, final String tooltip) {
        if (i < 0 && this.ICONCOUNT > 0) {
            this.left.setIcon(icon);
            // left.setText(text);
            this.left.setToolTipText(tooltip);
        } else {
            if (i < 0 || i >= this.ICONCOUNT) { return; }
            this.rights[i].setIcon(icon);
            this.rights[i].setText(text);
            this.rights[i].setToolTipText(tooltip);
        }
    }

    /**
     * clears the icon for left, setIcon AFTER setText
     */
    public void setText(final String text, final Icon icon) {
        this.left.setIcon(icon);
        this.left.setText(text);
        this.left.setToolTipText(text);
    }

}