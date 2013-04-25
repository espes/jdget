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
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
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
    private static final long                       serialVersionUID = -2262534550090971819L;
    private final JFrame                            frame;
    private final Robot                             robot;
    private BufferedImage                           screen;
    private BufferedImage                           gray;
    private Timer                                   fadeTimer;
    private int                                     fadeCounter;
    private double                                  steps;
    private final JWindow                           waitingPanel;
    private JTextArea                               text;

    // if there are different lockpanels for the same frame, the fade animations
    // may lock
    private static final HashMap<JFrame, LockPanel> CACHE            = new HashMap<JFrame, LockPanel>();

    /**
     * @param parentOwner
     * @throws AWTException
     */
    public synchronized static LockPanel create(final JFrame parentOwner) throws AWTException {
        LockPanel ret = LockPanel.CACHE.get(parentOwner);
        if (ret == null) {
            ret = new LockPanel(parentOwner);
            LockPanel.CACHE.put(parentOwner, ret);
        }
        return ret;

    }

    private float alpha = 0.1f;

    /**
     * @param frame
     * @throws AWTException
     */
    private LockPanel(final JFrame frame) throws AWTException {
        this.frame = frame;

        this.robot = new Robot();
        this.waitingPanel = new JWindow();

        frame.addWindowListener(new WindowListener() {

            public void windowActivated(final WindowEvent e) {
                if (LockPanel.this.waitingPanel.isVisible()) {
                    LockPanel.this.waitingPanel.toFront();
                }

            }

            public void windowClosed(final WindowEvent e) {

            }

            public void windowClosing(final WindowEvent e) {

            }

            public void windowDeactivated(final WindowEvent e) {

            }

            public void windowDeiconified(final WindowEvent e) {

            }

            public void windowIconified(final WindowEvent e) {

            }

            public void windowOpened(final WindowEvent e) {

            }

        });

        final JPanel p;
        this.waitingPanel.setContentPane(p = new JPanel());
        p.setLayout(new MigLayout("ins 10", "[][fill,grow]", "[fill,grow]"));
        p.add(new JLabel(AWUTheme.I().getIcon("wait", 32)));

        p.add(this.text = new JTextArea(), "spanx,aligny center");
        p.setBorder(BorderFactory.createLineBorder(p.getBackground().darker().darker()));
        JProgressBar bar;
        p.add(bar = new JProgressBar(), "growx,pushx,spanx,newline");
        bar.setIndeterminate(true);
        this.text.setBorder(null);
        this.text.setBackground(null);

        this.addMouseListener(new MouseAdapter() {
        });
    }

    /**
     * @return
     */
    private BufferedImage createScreenShot() {
        this.frame.toFront();
        final boolean top = this.frame.isAlwaysOnTop();
        try {
            return new EDTHelper<BufferedImage>() {
                @Override
                public BufferedImage edtRun() {
                    try {
                        if (LockPanel.this.frame.isShowing()) {
                            LockPanel.this.frame.setAlwaysOnTop(true);
                            final Rectangle captureSize = new Rectangle(LockPanel.this.frame.getContentPane().getSize());
                            final Point loc = LockPanel.this.frame.getContentPane().getLocationOnScreen();
                            captureSize.x = loc.x;
                            captureSize.y = loc.y;

                            return LockPanel.this.robot.createScreenCapture(captureSize);
                        } else {
                            return null;
                        }
                    } catch (final Throwable e) {
                        /*
                         * to catch component must be showing on the screen to
                         * determine its location
                         */
                        Log.exception(Level.WARNING, e);
                        return null;
                    }

                }
            }.getReturnValue();

        } finally {
            this.frame.setAlwaysOnTop(top);
        }
    }

    public synchronized void fadeIn(final int time) {
        this.fadeCounter++;
        this.steps = 50 * 1.0 / time;
        if (this.fadeTimer != null) {
            this.fadeTimer.stop();
            this.fadeTimer = null;
        }

        this.fadeTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                final Timer timer = LockPanel.this.fadeTimer;
                LockPanel.this.alpha += LockPanel.this.steps;

                if (LockPanel.this.alpha >= 1.0) {
                    LockPanel.this.alpha = 1.0f;
                    if (timer != null) {
                        LockPanel.this.fadeTimer.stop();
                    }
                }

                LockPanel.this.repaint();

            }
        });
        this.fadeTimer.setRepeats(true);
        this.fadeTimer.setInitialDelay(0);
        this.fadeTimer.start();

    }

    public synchronized void fadeOut(final int time) {

        this.screen = this.createScreenShot();
        this.fadeCounter--;
        this.steps = 50 * 1.0 / time;
        if (this.fadeCounter > 0) { return; }
        if (this.fadeTimer != null) {
            this.fadeTimer.stop();
            this.fadeTimer = null;
        }

        this.fadeTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                final Timer timer = LockPanel.this.fadeTimer;
                LockPanel.this.alpha -= LockPanel.this.steps;
                System.out.println(LockPanel.this.alpha);
                if (LockPanel.this.alpha <= 0.0) {
                    LockPanel.this.alpha = 0.0f;
                    if (timer != null) {
                        timer.stop();
                    }
                    LockPanel.this.setWaitingPanelText(null);

                    LockPanel.this.frame.getGlassPane().setVisible(false);
                }

                LockPanel.this.repaint();
            }
        });
        this.fadeTimer.setRepeats(true);
        this.fadeTimer.setInitialDelay(0);
        this.fadeTimer.start();

    }

    /**
     * @return the text
     */
    public JTextArea getText() {
        return this.text;
    }

    /**
     * @param time
     */
    public void lock(final int time) {
        this.frame.getGlassPane().setVisible(false);
        this.screen = this.createScreenShot();
        this.frame.getGlassPane().setVisible(true);
        if (this.screen != null) {
            this.gray = ImageProvider.convertToGrayScale(this.screen);

            final float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f };
            final Kernel kernel = new Kernel(3, 3, data);
            final ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            final BufferedImage dest = new BufferedImage(this.gray.getWidth(), this.gray.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            this.gray = convolve.filter(this.gray, dest);
            this.frame.setGlassPane(this);

            this.frame.getGlassPane().setVisible(true);

            this.fadeIn(time);
        }

    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Composite comp = ((Graphics2D) g).getComposite();
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        g.drawImage(this.screen, 0, 0, null);
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha));
        g.drawImage(this.gray, 0, 0, null);
        ((Graphics2D) g).setComposite(comp);

    }

    public void setWaitingPanelText(final String wait) {
        if (wait == null) {
            this.waitingPanel.setVisible(false);
        } else {

            this.text.setText(wait);
            this.waitingPanel.pack();
            this.waitingPanel.setLocation(SwingUtils.getCenter(this.frame, this.waitingPanel));
            this.waitingPanel.setVisible(true);

        }

    }

    /**
     * @param i
     */
    public void unlock(final int i) {
        this.fadeOut(i);

    }

}
