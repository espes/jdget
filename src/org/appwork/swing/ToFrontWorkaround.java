package org.appwork.swing;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.appwork.utils.swing.EDTHelper;

public class ToFrontWorkaround implements ActionListener {

    private Window window;
    private Timer  disableAlwaysonTop;

    /**
     * @param updateGui
     */
    public ToFrontWorkaround(Window window) {
        this.window = window;
        disableAlwaysonTop = new Timer(2000, this);
        disableAlwaysonTop.setInitialDelay(2000);
        disableAlwaysonTop.setRepeats(false);
    }

    /**
     * 
     */
    public void start() {

        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {

                window.setAlwaysOnTop(true);
                disableAlwaysonTop.restart();
                window.toFront();
                return null;
            }
        }.start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (window != null) window.setAlwaysOnTop(false);

    }

}
