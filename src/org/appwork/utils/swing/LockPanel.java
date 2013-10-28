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
import java.awt.Color;
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
import org.appwork.utils.swing.windowmanager.WindowManager;
import org.appwork.utils.swing.windowmanager.WindowManager.FrameState;

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

        robot = new Robot();
        waitingPanel = new JWindow();

        frame.addWindowListener(new WindowListener() {

            public void windowActivated(final WindowEvent e) {
                if (waitingPanel.isVisible()) {
                    WindowManager.getInstance().setZState(waitingPanel,FrameState.TO_FRONT);

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
        waitingPanel.setContentPane(p = new JPanel());
        p.setLayout(new MigLayout("ins 10", "[][fill,grow]", "[fill,grow]"));
        JLabel lbl;
        p.add(lbl = new JLabel(AWUTheme.I().getIcon("wait", 32)));

        p.add(text = new JTextArea(), "spanx,aligny center");

        Color bg = p.getBackground();
        if (bg == null) {
            bg = lbl.getBackground();
        }

        if (bg == null) {
            bg = Color.LIGHT_GRAY;
        }
        p.setBorder(BorderFactory.createLineBorder(bg.darker().darker()));
        JProgressBar bar;
        p.add(bar = new JProgressBar(), "growx,pushx,spanx,newline");
        bar.setIndeterminate(true);
        text.setBorder(null);
        text.setBackground(null);

        addMouseListener(new MouseAdapter() {
        });
    }

    /**
     * @return
     */
    private BufferedImage createScreenShot() {
        WindowManager.getInstance().setZState(frame,FrameState.TO_FRONT);
        final boolean top = frame.isAlwaysOnTop();
        try {
            return new EDTHelper<BufferedImage>() {
                @Override
                public BufferedImage edtRun() {
                    try {
                        if (frame.isShowing()) {
                            frame.setAlwaysOnTop(true);
                            final Rectangle captureSize = new Rectangle(frame.getContentPane().getSize());
                            final Point loc = frame.getContentPane().getLocationOnScreen();
                            captureSize.x = loc.x;
                            captureSize.y = loc.y;

                            return robot.createScreenCapture(captureSize);
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
            frame.setAlwaysOnTop(top);
        }
    }

    public synchronized void fadeIn(final int time) {
        fadeCounter++;
        steps = 50 * 1.0 / time;
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }

        fadeTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                final Timer timer = fadeTimer;
                alpha += steps;

                if (alpha >= 1.0) {
                    alpha = 1.0f;
                    if (timer != null) {
                        fadeTimer.stop();
                    }
                }

                LockPanel.this.repaint();

            }
        });
        fadeTimer.setRepeats(true);
        fadeTimer.setInitialDelay(0);
        fadeTimer.start();

    }

    public synchronized void fadeOut(final int time) {

        screen = createScreenShot();
        fadeCounter--;
        steps = 50 * 1.0 / time;
        if (fadeCounter > 0) { return; }
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }

        fadeTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                final Timer timer = fadeTimer;
                alpha -= steps;
                System.out.println(alpha);
                if (alpha <= 0.0) {
                    alpha = 0.0f;
                    if (timer != null) {
                        timer.stop();
                    }
                    LockPanel.this.setWaitingPanelText(null);

                    frame.getGlassPane().setVisible(false);
                }

                LockPanel.this.repaint();
            }
        });
        fadeTimer.setRepeats(true);
        fadeTimer.setInitialDelay(0);
        fadeTimer.start();

    }

    /**
     * @return the text
     */
    public JTextArea getText() {
        return text;
    }

    /**
     * @param time
     */
    public void lock(final int time) {
        frame.getGlassPane().setVisible(false);
        screen = createScreenShot();
        frame.getGlassPane().setVisible(true);
        if (screen != null) {
            gray = ImageProvider.convertToGrayScale(screen);

            final float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f };
            final Kernel kernel = new Kernel(3, 3, data);
            final ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            final BufferedImage dest = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            gray = convolve.filter(gray, dest);
            frame.setGlassPane(this);

            frame.getGlassPane().setVisible(true);

            fadeIn(time);
        }

    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Composite comp = ((Graphics2D) g).getComposite();
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        g.drawImage(screen, 0, 0, null);
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(gray, 0, 0, null);
        ((Graphics2D) g).setComposite(comp);

    }

    public void setWaitingPanelText(final String wait) {
        if (wait == null) {
            WindowManager.getInstance().setVisible(waitingPanel, false,FrameState.OS_DEFAULT);
        } else {

            text.setText(wait);
            waitingPanel.pack();
            waitingPanel.setLocation(SwingUtils.getCenter(frame, waitingPanel));
            WindowManager.getInstance().setVisible(waitingPanel, true,FrameState.OS_DEFAULT);

        }

    }

    /**
     * @param i
     */
    public void unlock(final int i) {
        fadeOut(i);

    }

}
