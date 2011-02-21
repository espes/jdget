/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.locale.APPWORKUTILS;

/**
 * @author thomas
 * 
 */
abstract public class Graph extends JPanel {

    private static final long serialVersionUID = 6943108941655020136L;
    private int               i;
    private int[]             cache;
    private transient Thread  fetcherThread;
    private final int         interval         = 1000;
    private Timer             painter;
    private final Object      LOCK             = new Object();

    private Color             colorA;

    private Color             colorB;

    private long              average;
    private int[]             averageCache;
    private Color             averageColor     = new Color(0x333333);
    private Color             averageTextColor = new Color(0);
    private int               capacity         = 0;

    private Color             textColor        = new Color(0);

    private boolean           running          = false;

    protected int             value;

    private Font              textFont;
    private int               all;

    public Graph() {
        this.colorA = new Color(100, 100, 100, 40);
        this.colorB = new Color(100, 100, 100, 80);
        this.average = 0;
        this.setCapacity(60);

        this.setOpaque(false);

    }

    /**
     * @return the averageColor
     */
    public Color getAverageColor() {
        return this.averageColor;
    }

    /**
     * @return
     */
    public String getAverageSpeedString() {
        // TODO Auto-generated method stub
        if (this.all <= 0) { return null; }
        return APPWORKUTILS.T.AppWorkUtils_Graph_getAverageSpeedString(SizeFormatter.formatBytes(this.average / this.all));
    }

    /**
     * @return the averageTextColor
     */
    public Color getAverageTextColor() {
        return this.averageTextColor;
    }

    /**
     * @return the colorA
     */
    public Color getColorA() {
        return this.colorA;
    }

    /**
     * @return the colorB
     */
    public Color getColorB() {
        return this.colorB;
    }

    /**
     * @return
     */
    public String getSpeedString() {
        // TODO Auto-generated method stub
        if (this.all <= 0) { return null; }
        return APPWORKUTILS.T.AppWorkUtils_Graph_getSpeedString(SizeFormatter.formatBytes(this.value));
    }

    /**
     * @return the textColor
     */
    public Color getTextColor() {
        return this.textColor;
    }

    /**
     * @return the textFont
     */
    public Font getTextFont() {
        return this.textFont;
    }

    /**
     * @return
     */
    abstract public int getValue();

    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);
        if (this.fetcherThread != null) {
            final Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1));

            int id = this.i;
            int max = 10;
            for (final int element : this.cache) {
                max = Math.max(element, max);
            }
            for (final int element : this.averageCache) {
                max = Math.max(element, max);
            }

            final int height = this.getHeight();

            final Polygon poly = new Polygon();
            poly.addPoint(0, this.getHeight());
            final Polygon apoly = new Polygon();
            apoly.addPoint(0, this.getHeight());

            for (int x = 0; x < this.cache.length; x++) {

                poly.addPoint(x * this.getWidth() / (this.cache.length - 1), this.getHeight() - (int) (this.getHeight() * this.cache[id] * 0.9) / max);
                if (this.averageColor != null) {
                    apoly.addPoint(x * this.getWidth() / (this.cache.length - 1), this.getHeight() - (int) (this.getHeight() * this.averageCache[id] * 0.9) / max);
                }

                id++;

                id = id % this.cache.length;
            }

            poly.addPoint(this.getWidth(), height);
            apoly.addPoint(this.getWidth(), height);

            g2.setPaint(new GradientPaint(this.getWidth() / 2, 0, this.colorA, this.getWidth() / 2, height, this.colorB));

            g2.fill(poly);
            g2.setColor(this.colorB);
            g2.draw(poly);
            String speedString = this.getSpeedString();
            int xText = this.getWidth();

            if (this.textFont != null) {
                g2.setFont(this.textFont);
            }
            if (speedString != null && this.running) {
                g2.setColor(this.getTextColor());
                g2.drawString(speedString, xText = xText - 3 - g2.getFontMetrics().stringWidth(speedString), 12);
            }
            if (this.averageColor != null) {
                ((Graphics2D) g).setColor(this.averageColor);

                final AlphaComposite ac5 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                g2.setComposite(ac5);
                g2.fill(apoly);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2.draw(apoly);

                speedString = this.getAverageSpeedString();
                if (speedString != null && this.running) {
                    g2.setColor(this.getAverageTextColor());
                    g2.drawString(speedString, xText - 3 - g2.getFontMetrics().stringWidth(speedString), 12);
                }
            }
        }
    }

    /**
     * @param averageColor
     *            the averageColor to set
     */
    public void setAverageColor(final Color averageColor) {
        this.averageColor = averageColor;
    }

    /**
     * @param averageTextColor
     *            the averageTextColor to set
     */
    public void setAverageTextColor(final Color averageTextColor) {
        this.averageTextColor = averageTextColor;
    }

    /**
     * @param j
     */
    private void setCapacity(final int cap) {
        if (this.fetcherThread != null) { throw new IllegalStateException("Already started"); }
        capacity = cap;
        this.cache = new int[cap];
        for (int x = 0; x < cap; x++) {
            this.cache[x] = 0;
        }

        this.averageCache = new int[cap];
        for (int x = 0; x < cap; x++) {
            this.averageCache[x] = 0;
        }
    }

    /**
     * @param colorA
     *            the colorA to set
     */
    public void setColorA(final Color colorA) {
        this.colorA = colorA;
    }

    /**
     * @param colorB
     *            the colorB to set
     */
    public void setColorB(final Color colorB) {
        this.colorB = colorB;
    }

    /**
     * @param textColor
     *            the textColor to set
     */
    public void setTextColor(final Color textColor) {
        this.textColor = textColor;
    }

    /**
     * @param textFont
     *            the textFont to set
     */
    public void setTextFont(final Font textFont) {
        this.textFont = textFont;
    }

    public void start() {
        synchronized (this.LOCK) {
            if (this.fetcherThread != null) {
                // already running
                return;
            }
            this.running = true;
            this.painter = new Timer(1000, new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    synchronized (Graph.this.LOCK) {
                        Graph.this.repaint();
                    }
                }
            });
            this.painter.setRepeats(true);
            this.painter.setInitialDelay(0);

            this.i = 0;

            this.fetcherThread = new Thread("Speedmeter updater") {

                @Override
                public void run() {
                    Graph.this.all = 0;
                    Graph.this.average = 0;
                    while (!this.isInterrupted()) {
                        synchronized (Graph.this.LOCK) {
                            Graph.this.value = Graph.this.getValue();

                            if (Graph.this.all == Graph.this.cache.length) {
                                Graph.this.average = Graph.this.average - Graph.this.cache[Graph.this.i] + Graph.this.value;

                            } else {
                                Graph.this.average = Graph.this.average + Graph.this.value;

                            }

                            Graph.this.all = Math.min(Graph.this.all + 1, Graph.this.cache.length);
                            Graph.this.averageCache[Graph.this.i] = (int) (Graph.this.average / Graph.this.all);
                            Graph.this.cache[Graph.this.i] = Graph.this.value;

                            Graph.this.i++;

                            Graph.this.i = Graph.this.i % Graph.this.cache.length;
                        }
                        if (this.isInterrupted()) { return; }
                        try {
                            Thread.sleep(Graph.this.interval);
                        } catch (final InterruptedException e) {
                            return;
                        }
                    }
                }
            };
            this.fetcherThread.start();
            this.painter.start();
        }
    }

    public void stop() {
        synchronized (this.LOCK) {
            this.running = false;
            if (this.fetcherThread != null) {
                this.fetcherThread.interrupt();
                this.fetcherThread = null;
            }
            if (this.painter != null) {
                this.painter.stop();
                this.painter = null;
            }
            Graph.this.repaint();
            setCapacity(this.capacity);
        }
    }

}