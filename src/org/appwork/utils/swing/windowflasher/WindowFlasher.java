package org.appwork.utils.swing.windowflasher;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.appwork.utils.swing.EDTRunner;

public class WindowFlasher {

    private final JFrame           window;

    private final ArrayList<Image> flashIcons;
    private final List<Image>      icons;
    private Timer                  iconFlashTimer;

    public WindowFlasher(final JFrame frame, final ArrayList<Image> list) {
        this.window = frame;
        this.flashIcons = list;

        this.icons = frame.getIconImages();

        final WindowAdapter windowWindowAdapter = new WindowAdapter() {

            @Override
            public void windowGainedFocus(final WindowEvent e) {

                if (WindowFlasher.this.iconFlashTimer != null) {
                    WindowFlasher.this.iconFlashTimer.stop();
                    new EDTRunner() {

                        @Override
                        protected void runInEDT() {
                            WindowFlasher.this.window.setIconImages(WindowFlasher.this.icons);

                        }
                    };
                    WindowFlasher.this.iconFlashTimer = null;
                }
            }

        };

        this.window.addWindowFocusListener(windowWindowAdapter);
    }

    public synchronized void start() {
        if (!this.window.isFocused()) {

            if (this.flashIcons != null) {
                if (this.iconFlashTimer == null) {
                    this.iconFlashTimer = new Timer(600, new ActionListener() {
                        private boolean flashy = false;

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            this.flashy = !this.flashy;
                            if (this.flashy) {
                                WindowFlasher.this.window.setIconImages(WindowFlasher.this.flashIcons);
                            } else {
                                WindowFlasher.this.window.setIconImages(WindowFlasher.this.icons);
                            }
                        }
                    });
                    this.iconFlashTimer.setRepeats(true);
                    this.iconFlashTimer.start();
                }
            }
        }
    }

}
