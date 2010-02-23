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

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;

/**
 * @author Unknown
 * 
 */
public class LockPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -2262534550090971819L;
    private JFrame frame;
    private Robot robot;
    private BufferedImage screen;
    private BufferedImage gray;
    private Timer fadeTimer;
    private int fadeCounter;
    private double steps;
    private JWindow waitingPanel;
    private JTextArea text;

    // if there are different lockpanels for the same frame, the fade animations
    // may lock
    private static final HashMap<JFrame, LockPanel> CACHE = new HashMap<JFrame, LockPanel>();

    /**
     * @param frame
     * @throws AWTException
     */
    private LockPanel(JFrame frame) throws AWTException {
        this.frame = frame;

        robot = new Robot();
        waitingPanel = new JWindow();

        frame.addWindowListener(new WindowListener() {

            public void windowActivated(WindowEvent e) {
                if (waitingPanel.isVisible()) waitingPanel.toFront();

            }

            public void windowClosed(WindowEvent e) {

            }

            public void windowClosing(WindowEvent e) {

            }

            public void windowDeactivated(WindowEvent e) {

            }

            public void windowDeiconified(WindowEvent e) {

            }

            public void windowIconified(WindowEvent e) {

            }

            public void windowOpened(WindowEvent e) {

            }

        });

        final JPanel p;
        waitingPanel.setContentPane(p = new JPanel());
        p.setLayout(new MigLayout("ins 10", "[][fill,grow]", "[fill,grow]"));
        try {
            p.add(new JLabel(ImageProvider.getImageIcon("wait", 32, 32)));
        } catch (IOException e) {
            Log.exception(e);
        }
        p.add(text = new JTextArea(), "spanx,aligny center");
        p.setBorder(BorderFactory.createLineBorder(p.getBackground().darker().darker()));
        JProgressBar bar;
        p.add(bar = new JProgressBar(), "growx,pushx,spanx,newline");
        bar.setIndeterminate(true);
        text.setBorder(null);
        text.setBackground(null);

        this.addMouseListener(new MouseAdapter() {
        });
    }

    /**
     * @return the text
     */
    protected JTextArea getText() {
        return text;
    }

    /**
     * @param i
     */
    public void lock(int time) {

        screen = createScreenShot();
        gray = ImageProvider.convertToGrayScale(screen);

        float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f };
        Kernel kernel = new Kernel(3, 3, data);
        ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dest = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray = convolve.filter(gray, dest);
        frame.setGlassPane(this);

        frame.getGlassPane().setVisible(true);

        fadeIn(time);

    }

    public void setWaitingPanelText(String wait) {
        if (wait == null) {
            this.waitingPanel.setVisible(false);
        } else {

            text.setText(wait);
            waitingPanel.pack();
            waitingPanel.setLocation(SwingUtils.getCenter(frame, waitingPanel));
            waitingPanel.setVisible(true);

        }

    }

    public synchronized void fadeOut(int time) {
        fadeCounter--;
        steps = (50 * 1.0) / (float) time;
        if (fadeCounter > 0) return;
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }

        fadeTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                alpha -= steps;
                if (alpha <= 0.0) {
                    alpha = 0.0f;
                    if (fadeTimer != null) fadeTimer.stop();
                    fadeTimer = null;
                    setWaitingPanelText(null);

                    frame.getGlassPane().setVisible(false);
                }

                LockPanel.this.repaint();
            }
        });
        fadeTimer.setRepeats(true);
        fadeTimer.setInitialDelay(0);
        fadeTimer.start();

    }

    public synchronized void fadeIn(int time) {
        fadeCounter++;
        steps = (50 * 1.0) / (float) time;
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }

        fadeTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                alpha += steps;

                if (alpha >= 1.0) {
                    alpha = 1.0f;
                    if (fadeTimer != null) fadeTimer.stop();
                }

                LockPanel.this.repaint();

            }
        });
        fadeTimer.setRepeats(true);
        fadeTimer.setInitialDelay(0);
        fadeTimer.start();

    }

    protected void paintComponent(Graphics g) {
        Composite comp = ((Graphics2D) g).getComposite();
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        g.drawImage(screen, 0, 0, null);
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
        g.drawImage(gray, 0, 0, null);
        ((Graphics2D) g).setComposite(comp);

    }

    private float alpha = 0.1f;

    /**
     * @return
     */
    private BufferedImage createScreenShot() {
        frame.toFront();
        boolean top = frame.isAlwaysOnTop();
        try {
            frame.setAlwaysOnTop(true);
            Rectangle captureSize = new Rectangle(frame.getContentPane().getSize());
            Point loc = frame.getContentPane().getLocationOnScreen();
            captureSize.x = loc.x;
            captureSize.y = loc.y;

            return robot.createScreenCapture(captureSize);
        } finally {
            frame.setAlwaysOnTop(top);
        }
    }

    /**
     * @param parentOwner
     * @throws AWTException
     */
    public synchronized static LockPanel create(JFrame parentOwner) throws AWTException {
        LockPanel ret = CACHE.get(parentOwner);
        if (ret == null) {
            ret = new LockPanel(parentOwner);
            CACHE.put(parentOwner, ret);
        }
        return ret;

    }

    /**
     * @param i
     */
    public void unlock(int i) {
        fadeOut(i);

    }

}
