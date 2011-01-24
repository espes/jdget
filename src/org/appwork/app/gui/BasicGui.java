package org.appwork.app.gui;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.appwork.app.gui.copycutpaste.CopyCutPasteHandler;
import org.appwork.storage.JSonStorage;
import org.appwork.swing.trayicon.AWTrayIcon;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.LockPanel;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.windowflasher.WindowFlasher;

public abstract class BasicGui {

    /**
     * The Mainframe
     */
    private final JFrame        frame;

    private LockPanel           lockPanel;

    private AWTrayIcon          ti;

    private final WindowFlasher flasher;

    protected BasicGui(final String title) {

        frame = new JFrame(title);

        // dilaog init

        Dialog.getInstance().setParentOwner(frame);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                if (!CrossSystem.isMac()) {
                    new Thread("Closer") {
                        @Override
                        public void run() {
                            requestExit();

                        }

                    }.start();
                } else {
                    if (getFrame().isVisible()) {
                        getFrame().setVisible(false);
                    }
                }
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // set appicon
        final ArrayList<Image> list = new ArrayList<Image>();

        try {
            list.add(ImageProvider.getBufferedImage("appicon", true));
        } catch (final IOException e) {
            Log.exception(e);
        }
        try {
            lockPanel = LockPanel.create(frame);
        } catch (final AWTException e1) {

            org.appwork.utils.logging.Log.exception(e1);
        }
        frame.setIconImages(list);
        // Set Application dimensions and locations

        // set extended state

        frame.setExtendedState(JSonStorage.getPlainStorage("Interface").get("EXTENDEDSTATE", Frame.NORMAL));
        final Dimension dim = new Dimension(JSonStorage.getPlainStorage("Interface").get("DIMENSION_WIDTH", 1000), JSonStorage.getPlainStorage("Interface").get("DIMENSION_HEIGHT", 600));
        // restore size
        frame.setSize(dim);
        frame.setPreferredSize(dim);

        frame.setMinimumSize(new Dimension(100, 100));
        //

        layoutPanel();
        // setGlasPane();

        // restore location. use center of screen as default.
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = screenSize.width / 2 - frame.getSize().width / 2;
        final int y = screenSize.height / 2 - frame.getSize().height / 2;

        frame.setLocation(JSonStorage.getPlainStorage("Interface").get("LOCATION_X", x), JSonStorage.getPlainStorage("Interface").get("LOCATION_Y", y));

        frame.pack();

        frame.setVisible(true);
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new CopyCutPasteHandler());

        flasher = new WindowFlasher(frame);

    }

    public void dispose() {
        if (frame.getExtendedState() == Frame.NORMAL && frame.isShowing()) {

            JSonStorage.getPlainStorage("Interface").put("LOCATION_X", frame.getLocationOnScreen().x);
            JSonStorage.getPlainStorage("Interface").put("LOCATION_Y", frame.getLocationOnScreen().y);
            JSonStorage.getPlainStorage("Interface").put("DIMENSION_WIDTH", frame.getSize().width);
            JSonStorage.getPlainStorage("Interface").put("DIMENSION_HEIGHT", frame.getSize().height);

        }

        JSonStorage.getPlainStorage("Interface").put("EXTENDEDSTATE", frame.getExtendedState());
        if (ti != null) {
            ti.dispose();
        }
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * @return the {@link GUI#flasher}
     * @see GUI#flasher
     */
    public WindowFlasher getFlasher() {
        return flasher;
    }

    public JFrame getFrame() {
        return frame;
    }

    /**
     * @return the {@link GUI#lockPanel}
     * @see GUI#lockPanel
     */
    protected LockPanel getLockPanel() {
        return lockPanel;
    }

    /**
     * Creates the whole mainframework panel
     * 
     * @throws IOException
     */
    protected abstract void layoutPanel();

    protected abstract void requestExit();
}
