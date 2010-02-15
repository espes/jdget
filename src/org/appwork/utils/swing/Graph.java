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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author thomas
 * 
 */

abstract public class Graph extends JPanel {

    private int i;
    private int[] cache;
    private transient Thread fetcherThread;
    private int interval = 1000;
    private Timer painter;

    private Color colorA;

    /**
     * @return the colorA
     */
    public Color getColorA() {
        return colorA;
    }

    /**
     * @param colorA
     *            the colorA to set
     */
    public void setColorA(Color colorA) {
        this.colorA = colorA;
    }

    /**
     * @return the colorB
     */
    public Color getColorB() {
        return colorB;
    }

    /**
     * @param colorB
     *            the colorB to set
     */
    public void setColorB(Color colorB) {
        this.colorB = colorB;
    }

    private Color colorB;
    private int max;

    public Graph() {
        colorA = new Color(100, 100, 100, 40);
        colorB = new Color(100, 100, 100, 80);
        setCapacity(60);

        setOpaque(false);

    }

    /**
     * @param j
     */
    private void setCapacity(int cap) {
        cache = new int[cap];
        for (int x = 0; x < cap; x++) {
            cache[x] = 0;
        }
    }

    public void start() {

        painter = new Timer(1000, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        painter.setRepeats(true);
        painter.setInitialDelay(0);

        i = 0;
        fetcherThread = new Thread("Speedmeter updater") {

            @Override
            public void run() {

                while (!this.isInterrupted()) {

                    cache[i] = getValue();
                    i++;
                    i = i % cache.length;

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        fetcherThread.start();
        painter.start();

    }

    /**
     * @return
     */
    abstract public int getValue();

    public void stop() {

        if (fetcherThread != null) {
            fetcherThread.interrupt();
            fetcherThread = null;
        }
        if (painter != null) {
            painter.stop();
            painter = null;
        }

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1));

        int id = i;
        this.max = 10;
        for (int element : cache) {
            max = Math.max(element, max);
        }

        int height = getHeight();

        Polygon poly = new Polygon();
        poly.addPoint(0, getWidth());

        for (int x = 0; x < cache.length; x++) {
            poly.addPoint((x * getWidth()) / (cache.length), getHeight() - (int) (getHeight() * cache[id] * 0.9) / max);
            id++;
            id = id % cache.length;
        }
        poly.addPoint(getWidth(), height);

        ((Graphics2D) g).setPaint(new GradientPaint(getWidth() / 2, 0, colorA, getWidth() / 2, height, colorB.darker()));

        g2.fill(poly);

    }

}