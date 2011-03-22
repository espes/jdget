package org.appwork.utils.swing.windowflasher;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class WindowFlasher {

    private final JFrame window;
    private final Dialog d;

    /**
     * @param frame
     */
    public WindowFlasher(final JFrame frame) {
        this.window = frame;
        this.d = new java.awt.Dialog(this.window);
        this.d.setUndecorated(true);
        this.d.setSize(54, 54);
        this.d.setModal(false);

        final WindowAdapter dWindowListener = new WindowAdapter() {

            @Override
            public void windowGainedFocus(final WindowEvent e) {
                WindowFlasher.this.window.requestFocus();
                WindowFlasher.this.d.setVisible(false);

            }
        };
        final WindowAdapter windowWindowAdapter = new WindowAdapter() {

            @Override
            public void windowGainedFocus(final WindowEvent e) {
                WindowFlasher.this.d.setVisible(false);

            }
        };
        this.d.addWindowFocusListener(dWindowListener);
        this.window.addWindowFocusListener(windowWindowAdapter);

    }

    /**
     * 
     */
    public void start() {

        if (!this.window.isFocused()) {
            this.d.setVisible(false);
            this.d.setLocation(0, 0);
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.d.setLocationRelativeTo(this.window);
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.d.setVisible(true);
        }
    }

}
