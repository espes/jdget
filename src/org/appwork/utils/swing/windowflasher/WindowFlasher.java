package org.appwork.utils.swing.windowflasher;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.appwork.utils.swing.EDTRunner;

public class WindowFlasher {

    private final JFrame           window;

    private final java.util.List<Image> flashIcons;
    private List<Image>            icons;
    private Timer                  iconFlashTimer;

    private boolean                running = false;

    public WindowFlasher(final JFrame frame, final java.util.List<Image> list) {
        this.window = frame;
        this.flashIcons = list;

        final WindowAdapter windowWindowAdapter = new WindowAdapter() {

            @Override
            public void windowGainedFocus(final WindowEvent e) {
                if (WindowFlasher.this.running) {
                    WindowFlasher.this.stop();
                }
            }

        };

        this.window.addWindowFocusListener(windowWindowAdapter);
    }

    /**
     * @return
     */
    public boolean hasFocus() {
        if (this.window.isFocused()) { return true; }

        return false;
    }

    public boolean isRunning() {
        return this.running;
    }

    /**
     * @param flashy
     */
    protected void set(final boolean flashy) {
        if (flashy) {

            WindowFlasher.this.window.setIconImages(WindowFlasher.this.flashIcons);
        } else {
            WindowFlasher.this.window.setIconImages(WindowFlasher.this.icons);
        }
    }

    public synchronized void start() {
        if (!this.hasFocus()) {
            this.running = true;
            if (this.flashIcons != null) {
                if (this.iconFlashTimer == null) {
                    this.icons = this.window.getIconImages();
                    this.iconFlashTimer = new Timer(500, new ActionListener() {
                        private boolean flashy = false;

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            this.flashy = !this.flashy;
                            WindowFlasher.this.set(this.flashy);

                        }
                    });
                    this.iconFlashTimer.setRepeats(true);
                    this.iconFlashTimer.start();
                }
            }
        }
    }

    /**
     * 
     */
    public void stop() {
        this.running = false;
        new EDTRunner() {

            @Override
            protected void runInEDT() {

                if (WindowFlasher.this.iconFlashTimer != null) {

                    WindowFlasher.this.iconFlashTimer.stop();
                    WindowFlasher.this.iconFlashTimer = null;
                    new EDTRunner() {

                        @Override
                        protected void runInEDT() {
                            WindowFlasher.this.set(false);
                        }
                    };

                }
            }
        };
    }

}
