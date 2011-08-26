package org.appwork.app.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.appwork.swing.components.BadgePainter;
import org.appwork.swing.components.Badgeable;

public class MigPanel extends JPanel implements BadgePainter {

    /**
     * 
     */
    ArrayList<Badgeable>      badgeables       = new ArrayList<Badgeable>();
    private boolean           badgesEnabled    = false;
    private static final long serialVersionUID = 5744502853913432797L;

    /**
     * 
     * @param constraints
     * @param columns
     * @param rows
     */
    public MigPanel(final String constraints, final String columns, final String rows) {
        super(new MigLayout(constraints, columns, rows));

    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);
        if (this.isBadgesEnabled()) {
            if (comp instanceof JComponent) {
                this.lookupBadges((JComponent) comp);
            }

        }
    }

    public boolean isBadgesEnabled() {
        return this.badgesEnabled;
    }

    /**
     * @param comp
     */
    private void lookupBadges(final JComponent comp) {
        if (comp instanceof Badgeable) {
            this.badgeables.add((Badgeable) comp);
        }
        for (final Component c : comp.getComponents()) {
            if (c instanceof JComponent) {

                this.lookupBadges((JComponent) c);

            }
        }

    }

    @Override
    protected void paintChildren(final Graphics g) {
        super.paintChildren(g);
        for (final Badgeable b : this.badgeables) {
            if (((JComponent) b).isShowing()) {
                final Point p = SwingUtilities.convertPoint(((JComponent) b), new Point(0, 0), this);
                g.translate(p.x, p.y);
                b.paintBadge(this, g);
                g.translate(-p.x, -p.y);
            }
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

    }

    @Override
    public void paintComponents(final Graphics g) {
        super.paintComponents(g);
        // if (this.isShowing()) {

        // }
    }

    @Override
    public void repaint(final long tm, final int x, final int y, final int width, final int height) {
        super.repaint(tm, x, y, width, height);
    }

    public void setBadgesEnabled(final boolean badgesEnabled) {
        this.badgesEnabled = badgesEnabled;
    }

}
