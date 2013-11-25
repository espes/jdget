package org.appwork.swing.components;

import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.appwork.utils.ImageProvider.ImageProvider;

public class ExtMergedIcon implements Icon {
    private class Entry {

        private final Icon      icon;
        private final int       x;
        private final int       y;
        private final Composite composite;
        private final int       z;

        public Entry(final Icon icon, final int x, final int y, final int z, final Composite c) {
            this.icon = icon;
            this.x = x;
            this.y = y;
            this.composite = c;
            this.z = z;
        }

    }

    private int                  cropedWidth  = -1;
    private int                  cropedHeight = -1;

    private final TreeSet<Entry> entries      = new TreeSet<Entry>(new Comparator<Entry>() {

                                                  @Override
                                                  public int compare(final Entry o1, final Entry o2) {
                                                      return new Integer(o1.z).compareTo(new Integer(o2.z));
                                                  }
                                              });

    private int                  width        = 0;
    private int                  height       = 0;
    private ImageIcon            internalIcon;

    private boolean              caching;

    public ExtMergedIcon() {

    }

    /**
     * @param abstractIcon
     */
    public ExtMergedIcon(final Icon icon) {
        this(icon, 0, 0, 0, null);
    }

    /**
     * @param abstractIcon
     * @param i
     * @param j
     */
    public ExtMergedIcon(final Icon icon, final int x, final int y) {
        this.addEntry(new Entry(icon, x, y, 0, null));
    }

    public ExtMergedIcon(final Icon icon, final int x, final int y, final int z, final Composite c) {
        this.addEntry(new Entry(icon, x, y, z, c));
    }

    public ExtMergedIcon add(final Icon icon) {
        synchronized (this.entries) {
            return this.add(icon, 0, 0, this.entries.size(), null);
        }
    }

    /**
     * @param abstractIcon
     * @param i
     * @param j
     * @return
     */
    public ExtMergedIcon add(final Icon icon, final int x, final int y) {
        synchronized (this.entries) {
            return this.add(icon, x, y, this.entries.size(), null);
        }

    }

    public ExtMergedIcon add(final Icon icon, final int x, final int y, final int z, final Composite c) {
        this.addEntry(new Entry(icon, x, y, z, c));
        return this;
    }

    private void addEntry(final Entry entry) {
        if (this.internalIcon != null) { throw new IllegalStateException("internalIcon is set"); }
        this.width = Math.max(this.width, entry.x + entry.icon.getIconWidth());
        this.height = Math.max(this.height, entry.y + entry.icon.getIconHeight());
        this.entries.add(entry);
    }

    public void cache() {
        this.caching = true;
        try {
            this.internalIcon = ImageProvider.toImageIcon(this);
            this.entries.clear();
        } finally {
            this.caching = false;
        }
    }

    /**
     * @return
     */
    public Icon crop() {
        // TODO Auto-generated method stub
        return null;
    }

    public ExtMergedIcon crop(final int width, final int height) {
        this.cropedWidth = width;
        this.cropedHeight = height;

        return this;
    }

    @Override
    public int getIconHeight() {
        if (this.cropedHeight > 0) { return this.cropedHeight; }
        return this.height;
    }

    @Override
    public int getIconWidth() {
        if (this.cropedWidth > 0) { return this.cropedWidth; }
        return this.width;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {

        final Graphics2D g2 = (Graphics2D) g;

        if (this.internalIcon == null && !this.caching) {
            this.cache();

        }

        if (this.internalIcon != null) {
            g2.drawImage(this.internalIcon.getImage(), x, y, null);
            // internalIcon.paintIcon(c, g2, x, y);
            return;
        }
        final Shape oldClip = g2.getClip();
        // Rectangle rec = new Rectangle( );
        g2.setClip(x, y, this.getIconWidth(), this.getIconHeight());
        for (final Entry e : this.entries) {
            final Composite com = g2.getComposite();
            try {
                if (e.composite != null) {
                    g2.setComposite(e.composite);
                }
                e.icon.paintIcon(c, g2, x + e.x, y + e.y);
            } finally {
                if (com != null) {
                    g2.setComposite(com);
                }
            }

        }
        g2.setClip(oldClip);
    }

}