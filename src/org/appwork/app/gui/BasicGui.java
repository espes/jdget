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
import org.appwork.storage.Storage;
import org.appwork.swing.trayicon.AWTrayIcon;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.LockPanel;
import org.appwork.utils.swing.dialog.Dialog;

public abstract class BasicGui {

    /**
     * The Mainframe
     */
    private final JFrame  frame;

    private LockPanel     lockPanel;

    private AWTrayIcon    ti;

    private final Storage storage;

    protected BasicGui(final String title) {

        this.frame = new JFrame(title);

        // dilaog init
        this.storage = JSonStorage.getPlainStorage("BasicGui");
        Dialog.getInstance().setParentOwner(this.frame);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                if (!CrossSystem.isMac()) {
                    new Thread("Closer") {
                        @Override
                        public void run() {
                            BasicGui.this.requestExit();

                        }

                    }.start();
                } else {
                    if (BasicGui.this.getFrame().isVisible()) {
                        BasicGui.this.getFrame().setVisible(false);
                    }
                }
            }
        });
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // set appicon
        final ArrayList<Image> list = new ArrayList<Image>();

        try {
            list.add(ImageProvider.getBufferedImage("appicon", true, false));
        } catch (final Throwable e) {
            Log.exception(e);
        }
        try {
            this.lockPanel = LockPanel.create(this.frame);
        } catch (final AWTException e1) {

            org.appwork.utils.logging.Log.exception(e1);
        }
        this.frame.setIconImages(list);
        // Set Application dimensions and locations

        // set extended state

        this.frame.setExtendedState(JSonStorage.getPlainStorage("Interface").get("EXTENDEDSTATE", Frame.NORMAL));
        final Dimension dim = new Dimension(JSonStorage.getPlainStorage("Interface").get("DIMENSION_WIDTH", 1000), JSonStorage.getPlainStorage("Interface").get("DIMENSION_HEIGHT", 600));
        // restore size
        this.frame.setSize(dim);
        this.frame.setPreferredSize(dim);

        this.frame.setMinimumSize(new Dimension(100, 100));
        //

        this.layoutPanel();
        // setGlasPane();

        // restore location. use center of screen as default.
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = screenSize.width / 2 - this.frame.getSize().width / 2;
        final int y = screenSize.height / 2 - this.frame.getSize().height / 2;

        this.frame.setLocation(JSonStorage.getPlainStorage("Interface").get("LOCATION_X", x), JSonStorage.getPlainStorage("Interface").get("LOCATION_Y", y));

        this.frame.pack();

        this.frame.setVisible(true);
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new CopyCutPasteHandler());

    }

    public void dispose() {
        if (this.frame.getExtendedState() == Frame.NORMAL && this.frame.isShowing()) {

            JSonStorage.getPlainStorage("Interface").put("LOCATION_X", this.frame.getLocationOnScreen().x);
            JSonStorage.getPlainStorage("Interface").put("LOCATION_Y", this.frame.getLocationOnScreen().y);
            JSonStorage.getPlainStorage("Interface").put("DIMENSION_WIDTH", this.frame.getSize().width);
            JSonStorage.getPlainStorage("Interface").put("DIMENSION_HEIGHT", this.frame.getSize().height);

        }

        JSonStorage.getPlainStorage("Interface").put("EXTENDEDSTATE", this.frame.getExtendedState());
        if (this.ti != null) {
            this.ti.dispose();
        }
        this.frame.setVisible(false);
        this.frame.dispose();
    }

    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * @return the {@link GUI#lockPanel}
     * @see GUI#lockPanel
     */
    protected LockPanel getLockPanel() {
        return this.lockPanel;
    }

    public Storage getStorage() {
        return this.storage;
    }

    /**
     * Creates the whole mainframework panel
     * 
     * @throws IOException
     */
    protected abstract void layoutPanel();

    protected abstract void requestExit();
}
