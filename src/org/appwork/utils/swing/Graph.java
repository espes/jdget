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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.components.tooltips.TooltipTextDelegateFactory;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.swing.graph.Limiter;

/**
 * @author thomas
 * 
 */
abstract public class Graph extends JPanel implements ToolTipHandler {

    private static final long          serialVersionUID = 6943108941655020136L;
    private int                        i;
    private int[]                      cache;
    private transient Thread           fetcherThread;
    private int                        interval         = 1000;
    private Timer                      painter;

    private final Object               LOCK             = new Object();

    private Color                      currentColorTop;
    private Color                      currentColorBottom;

    public long                        average;

    private int[]                      averageCache;

    private Color                      averageColor     = new Color(0x333333);
    private Color                      averageTextColor = new Color(0);
    private int                        capacity         = 0;
    private Color                      textColor        = new Color(0);
    private boolean                    running          = false;

    protected int                      value;

    private Font                       textFont;

    private int                        all;

    private Limiter[]                  limiter;
    private TooltipTextDelegateFactory tooltipFactory;
    private boolean                    antiAliasing     = false;

    public Graph() {
        this(60, 1000);
    }

    public Graph(final int capacity, final int interval) {
        tooltipFactory = new TooltipTextDelegateFactory(this);
        // ToolTipController.getInstance().
        currentColorTop = new Color(100, 100, 100, 40);
        currentColorBottom = new Color(100, 100, 100, 80);
        average = 0;
        setInterval(interval);
        setCapacity(capacity);

        setOpaque(false);

    }

    public ExtTooltip createExtTooltip(final Point mousePosition) {
        return getTooltipFactory().createTooltip();
    }

    /**
     * @return
     */
    protected String createTooltipText() {

        return getAverageSpeedString() + "  " + getSpeedString();
    }

    /**
     * @return the averageColor
     */
    public Color getAverageColor() {
        return averageColor;
    }

    public long getAverageSpeed() {
        if (all == 0) { return 0; }
        return average / all;
    }

    /**
     * @return
     */
    public String getAverageSpeedString() {
        // TODO Auto-generated method stub
        if (all <= 0) { return null; }
        return _AWU.T.AppWorkUtils_Graph_getAverageSpeedString(SizeFormatter.formatBytes(average / all));
    }

    /**
     * @return the averageTextColor
     */
    public Color getAverageTextColor() {
        return averageTextColor;
    }

    /**
     * @return the colorA
     */
    public Color getCurrentColorTop() {
        return currentColorTop;
    }

    /**
     * @return the colorB
     */
    public Color getCurrentColorBottom() {
        return currentColorBottom;
    }

    public int getInterval() {
        return interval;
    }

    /**
     * @return
     */
    public Limiter[] getLimiter() {
        return limiter;
    }

    /**
     * @return
     */
    protected int getPaintHeight() {

        return getHeight();
    }

    /**
     * @return
     */
    public String getSpeedString() {
        // TODO Auto-generated method stub
        if (all <= 0) { return null; }
        return _AWU.T.AppWorkUtils_Graph_getSpeedString(SizeFormatter.formatBytes(value));
    }

    /**
     * @return the textColor
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * @return the textFont
     */
    public Font getTextFont() {
        return textFont;
    }

    public int getTooltipDelay(final Point mousePositionOnScreen) {
        return 0;
    }

    public TooltipTextDelegateFactory getTooltipFactory() {
        return tooltipFactory;
    }

    /**
     * @return
     */
    abstract public int getValue();

    /**
     * @return the antiAliasing
     */
    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public boolean isTooltipDisabledUntilNextRefocus() {
        return false;
    }

    @Override
    public boolean isTooltipWithoutFocusEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);
        this.paintComponent(g, true);
    }

    /**
     * @param g
     * @param b
     */
    public void paintComponent(final Graphics g, final boolean paintText) {
        if (fetcherThread != null) {
            final Graphics2D g2 = (Graphics2D) g;
            if (!antiAliasing) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g2.setStroke(new BasicStroke(1));

            int id = i;
            int max = 10;
            for (final int element : cache) {
                max = Math.max(element, max);
            }
            for (final int element : averageCache) {
                max = Math.max(element, max);
            }
            Limiter[] limitertmp = null;
            if ((limitertmp = getLimiter()) != null) {

                for (final Limiter l : limitertmp) {
                    max = Math.max(l.getValue(), max);
                }
            }
            final int height = getPaintHeight();

            final Polygon poly = new Polygon();
            poly.addPoint(0, getHeight());
            final Polygon apoly = new Polygon();
            apoly.addPoint(0, getHeight());
            final int[] lCache = cache;
            final int[] laverageCache = averageCache;
            for (int x = 0; x < lCache.length; x++) {

                poly.addPoint(x * getWidth() / (lCache.length - 1), getHeight() - (int) (height * lCache[id] * 0.9) / max);
                if (averageColor != null) {
                    apoly.addPoint(x * getWidth() / (lCache.length - 1), getHeight() - (int) (height * laverageCache[id] * 0.9) / max);
                }

                id++;

                id = id % lCache.length;
            }

            poly.addPoint(getWidth(), getHeight());
            apoly.addPoint(getWidth(), getHeight());

            g2.setPaint(new GradientPaint(getWidth() / 2, getHeight() - getPaintHeight(), currentColorTop, getWidth() / 2, getHeight(), currentColorBottom));

            g2.fill(poly);
            g2.setColor(currentColorBottom);
            g2.draw(poly);

            if (averageColor != null) {
                ((Graphics2D) g).setColor(averageColor);

                final AlphaComposite ac5 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                g2.setComposite(ac5);
                g2.fill(apoly);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2.draw(apoly);
            }

            if (limitertmp != null) {
                int h;
                for (final Limiter l : limitertmp) {
                    if (l.getValue() > 0) {

                        h = getHeight() - (int) (height * l.getValue() * 0.9) / max;
                        g2.setPaint(new GradientPaint(getWidth() / 2, h, l.getColorA(), getWidth() / 2, h + height / 10, l.getColorB()));
                        g2.fillRect(0, h, getWidth(), height / 10);
                        // g2.drawRect(0, h, this.getWidth(), height / 5);
                    }
                }
            }

            // Draw speed string
            if (paintText) {
                int xText = getWidth();

                if (textFont != null) {
                    g2.setFont(textFont);
                }
                // current speed
                String speedString = getSpeedString();
                if (speedString != null && running) {
                    g2.setColor(getTextColor());
                    // align right. move left
                    xText = xText - 3 - g2.getFontMetrics().stringWidth(speedString);
                    g2.drawString(speedString, xText, 12);
                }
                // average speed
                if (averageColor != null) {
                    speedString = getAverageSpeedString();
                    if (speedString != null && running) {
                        g2.setColor(getAverageTextColor());
                        xText = xText - 3 - g2.getFontMetrics().stringWidth(speedString);
                        g2.drawString(speedString, xText, 12);
                    }
                }
            }
        }

    }

    /**
     * resets the average cache and makes sure, that the average recalculates
     * within a few cycles
     */
    protected void resetAverage() {
        final int tmp = all;
        if (tmp == 0) {
            average = 0;
        } else {
            average /= tmp;
        }
        average *= 3;
        all = 3;
    }

    /**
     * @param antiAliasing
     *            the antiAliasing to set
     */
    public void setAntiAliasing(final boolean antiAliasing) {
        this.antiAliasing = antiAliasing;
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
    protected void setCapacity(final int cap) {
        if (fetcherThread != null) { throw new IllegalStateException("Already started"); }
        capacity = cap;
        final int[] lcache = new int[cap];
        for (int x = 0; x < cap; x++) {
            lcache[x] = 0;
        }
        final int[] laverageCache = new int[cap];
        for (int x = 0; x < cap; x++) {
            laverageCache[x] = 0;
        }
        averageCache = laverageCache;
        cache = lcache;
    }

    /**
     * @param colorA
     *            the colorA to set
     */
    public void setCurrentColorTop(final Color colorA) {
        currentColorTop = colorA;
    }

    /**
     * @param colorB
     *            the colorB to set
     */
    public void setCurrentColorBottom(final Color colorB) {
        currentColorBottom = colorB;
    }

    public void setInterval(final int interval) {
        this.interval = interval;
    }

    public void setLimiter(final Limiter[] limiter) {
        this.limiter = limiter;
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

    public void setTooltipFactory(final TooltipTextDelegateFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
    }

    @Override
    public void setToolTipText(final String text) {

        putClientProperty(JComponent.TOOL_TIP_TEXT_KEY, text);

        if (text == null || text.length() == 0) {
            ToolTipController.getInstance().unregister(this);
        } else {
            ToolTipController.getInstance().register(this);
        }
    }

    public void start() {
        synchronized (LOCK) {
            if (fetcherThread != null) {
                // already running
                return;
            }
            
            running = true;
            painter = new Timer(getInterval(), new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    synchronized (LOCK) {

                        Graph.this.setToolTipText(Graph.this.createTooltipText());
                        Graph.this.repaint();
                    }
                }
            });
            painter.setRepeats(true);
            painter.setInitialDelay(0);

            i = 0;

            fetcherThread = new Thread("Speedmeter updater") {

                @Override
                public void run() {
                    all = 0;
                    average = 0;
                    while (!isInterrupted()) {
                        synchronized (LOCK) {
                            value = Graph.this.getValue();

                            if (all == cache.length) {
                                average = average - cache[i] + value;

                            } else {
                                average = average + value;

                            }

                            all = Math.min(all + 1, cache.length);
                            averageCache[i] = (int) (average / all);
                            cache[i] = value;

                            i++;

                            i = i % cache.length;
                        }
                        if (isInterrupted()) { return; }
                        try {
                            Thread.sleep(Graph.this.getInterval());
                        } catch (final InterruptedException e) {
                            return;
                        }
                    }
                }
            };
            fetcherThread.start();
            painter.start();
        }
    }

    public void stop() {
        synchronized (LOCK) {
            running = false;
            if (fetcherThread != null) {
                fetcherThread.interrupt();
                fetcherThread = null;
            }
            if (painter != null) {
                painter.stop();
                painter = null;
            }
            Graph.this.repaint();
            setCapacity(capacity);
        }
    }

    @Override
    public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {
        return false;
    }

}