package org.appwork.swing.components;

import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.appwork.utils.ImageProvider.ImageProvider;

public class ExtMergedIcon implements Icon {
    private class Entry {

        private Icon      icon;
        private int       x;
        private int       y;
        private Composite composite;
        private int       z;

        public Entry(final Icon icon, final int x, final int y, final int z, final Composite c) {
            this.icon = icon;
            this.x = x;
            this.y = y;
            composite = c;
            this.z = z;
        }

    }

    public ExtMergedIcon() {

    }

    public void add(final Icon icon, final int x, final int y, final int z, final Composite c) {
        addEntry(new Entry(icon, x, y, z, c));
    }

    public ExtMergedIcon(final Icon icon, final int x, final int y, final int z, final Composite c) {
        addEntry(new Entry(icon, x, y, z, c));
    }

    private TreeSet<Entry> entries = new TreeSet<Entry>(new Comparator<Entry>() {

                                       @Override
                                       public int compare(final Entry o1, final Entry o2) {
                                           return new Integer(o1.z).compareTo(new Integer(o2.z));
                                       }
                                   });
    private int            width   = 0;
    private int            height  = 0;
    private ImageIcon      internalIcon;
    private boolean        caching;

    private void addEntry(final Entry entry) {
        if (internalIcon != null) { throw new IllegalStateException("internalIcon is set"); }
        width = Math.max(width, entry.x + entry.icon.getIconWidth());
        height = Math.max(height, entry.y + entry.icon.getIconHeight());
        entries.add(entry);
    }

    public void cache() {
        caching = true;
        try {
            internalIcon = ImageProvider.toImageIcon(this);
            entries.clear();
        } finally {
            caching = false;
        }
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {

        final Graphics2D g2 = (Graphics2D) g;

        if (internalIcon == null && !caching) {
            cache();

        }

        if (internalIcon != null) {
            g2.drawImage(internalIcon.getImage(), x, y, null);
//            internalIcon.paintIcon(c, g2, x, y);
            return;
        }

        for (final Entry e : entries) {
            final Composite com = g2.getComposite();
            try {
                if(e.composite!=null) {
                    g2.setComposite(e.composite);
                }
                e.icon.paintIcon(c, g2, x + e.x, y + e.y);
            } finally {
               if(com!=null) {
                g2.setComposite(com);
            }
            }

        }
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

}