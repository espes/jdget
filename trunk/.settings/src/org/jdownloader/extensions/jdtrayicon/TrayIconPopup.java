//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.jdownloader.extensions.jdtrayicon;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.gui.swing.jdgui.components.toolbar.actions.AutoReconnectToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ClipBoardToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ExitToolbarAction;
import jd.gui.swing.jdgui.components.toolbar.actions.GlobalPremiumSwitchToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.OpenDefaultDownloadFolderAction;
import jd.gui.swing.jdgui.components.toolbar.actions.PauseDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ReconnectAction;
import jd.gui.swing.jdgui.components.toolbar.actions.StartDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.StopDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.UpdateAction;
import jd.gui.swing.jdgui.menu.ChunksEditor;
import jd.gui.swing.jdgui.menu.ParalellDownloadsEditor;
import jd.gui.swing.jdgui.menu.ParallelDownloadsPerHostEditor;
import jd.gui.swing.jdgui.menu.SpeedlimitEditor;
import jd.utils.JDUtilities;
import net.miginfocom.swing.MigLayout;

import org.appwork.utils.swing.EDTHelper;
import org.jdownloader.actions.AppAction;
import org.jdownloader.extensions.jdtrayicon.translate.T;
import org.jdownloader.images.NewTheme;

//final, because the constructor calls Thread.start(),
//see http://findbugs.sourceforge.net/bugDescriptions.html#SC_START_IN_CTOR
public final class TrayIconPopup extends JFrame implements MouseListener {

    private static final long         serialVersionUID  = 2623190748929934409L;

    private JPanel                    entryPanel;
    private JPanel                    quickConfigPanel;
    private JPanel                    bottomPanel;
    private boolean                   enteredPopup;

    private boolean                   hideThreadrunning = false;

    private JPanel                    exitPanel;
    private ArrayList<AbstractButton> resizecomps;

    private transient Thread          hideThread;

    public TrayIconPopup() {
        super();
        resizecomps = new ArrayList<AbstractButton>();
        setVisible(false);
        setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        addMouseListener(this);
        this.setUndecorated(true);
        initEntryPanel();
        initQuickConfigPanel();
        initBottomPanel();
        initExitPanel();
        JPanel content = new JPanel(new MigLayout("ins 5, wrap 1", "[]", "[]5[]5[]5[]5[]"));
        add(content);
        content.add(new JLabel("<html><b>" + JDUtilities.getJDTitle(0) + "</b></html>"), "align center");
        content.add(new JSeparator(), "growx, spanx");
        content.add(entryPanel);
        content.add(new JSeparator(), "growx, spanx");
        content.add(quickConfigPanel);
        content.add(new JSeparator(), "growx, spanx");
        content.add(bottomPanel, "pushx,growx");
        content.add(new JSeparator(), "growx, spanx");
        content.add(exitPanel);
        content.setBorder(BorderFactory.createLineBorder(content.getBackground().darker()));
        Dimension size = new Dimension(getPreferredSize().width, resizecomps.get(0).getPreferredSize().height);
        for (AbstractButton c : resizecomps) {
            c.setPreferredSize(size);
            c.setMinimumSize(size);
            c.setMaximumSize(size);
        }
        setAlwaysOnTop(true);
        pack();
        hideThread = new Thread() {
            /*
             * this thread handles closing of popup because enter/exit/move
             * events are too slow and can miss the exitevent
             */
            public void run() {
                while (true && hideThreadrunning) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                    }
                    if (enteredPopup && hideThreadrunning) {
                        PointerInfo mouse = MouseInfo.getPointerInfo();
                        Point current = TrayIconPopup.this.getLocation();
                        if (mouse.getLocation().x < current.x || mouse.getLocation().x > current.x + TrayIconPopup.this.getSize().width) {
                            dispose();
                            break;
                        } else if (mouse.getLocation().y < current.y || mouse.getLocation().y > current.y + TrayIconPopup.this.getSize().height) {
                            dispose();
                            break;
                        }
                    }
                }
            }
        };
        hideThreadrunning = true;
        hideThread.start();
    }

    /**
     * start autohide in 3 secs if mouse did not enter popup before
     */
    public void startAutoHide() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                if (!enteredPopup) {
                    new EDTHelper<Object>() {
                        @Override
                        public Object edtRun() {
                            hideThreadrunning = false;
                            dispose();
                            return null;
                        }
                    }.start();
                }
            }
        }.start();
    }

    private void initEntryPanel() {
        entryPanel = new JPanel(new MigLayout("ins 0, wrap 1", "[]", "[]0[]0[]0[]0[]0[]0[]"));
        if (DownloadWatchDog.getInstance().getStateMachine().isState(DownloadWatchDog.RUNNING_STATE)) {
            addMenuEntry(entryPanel, new TrayAction(StopDownloadsAction.getInstance().init()));
            addMenuEntry(entryPanel, new TrayAction(PauseDownloadsAction.getInstance().init(), T._.popup_pause()));
        } else if (DownloadWatchDog.getInstance().getStateMachine().isState(DownloadWatchDog.IDLE_STATE, DownloadWatchDog.STOPPED_STATE)) {
            addMenuEntry(entryPanel, new TrayAction(StartDownloadsAction.getInstance().init()));
            addMenuEntry(entryPanel, new TrayAction(PauseDownloadsAction.getInstance().init(), T._.popup_pause()));
        }

        // addMenuEntry(entryPanel, "action.addurl");
        // addMenuEntry(entryPanel, "action.load");
        addMenuEntry(entryPanel, new TrayAction(UpdateAction.getInstance().init(), T._.popup_update()));
        addMenuEntry(entryPanel, new TrayAction(ReconnectAction.getInstance().init(), T._.popup_reconnect()));
        addMenuEntry(entryPanel, new TrayAction(OpenDefaultDownloadFolderAction.getInstance().init(), T._.popup_downloadfolder()));
    }

    private void initQuickConfigPanel() {
        quickConfigPanel = new JPanel(new MigLayout("ins 0, wrap 1", "[]", "[]0[]0[]"));
        addMenuEntry(quickConfigPanel, new TrayAction(GlobalPremiumSwitchToggleAction.getInstance().init(), T._.popup_premiumtoggle()));
        addMenuEntry(quickConfigPanel, new TrayAction(ClipBoardToggleAction.getInstance().init(), T._.popup_clipboardtoggle()));
        addMenuEntry(quickConfigPanel, new TrayAction(AutoReconnectToggleAction.getInstance().init(), T._.popup_reconnecttoggle()));
    }

    private void initExitPanel() {
        exitPanel = new JPanel(new MigLayout("ins 0, wrap 1", "[]", "[]"));
        addMenuEntry(exitPanel, new TrayAction(ExitToolbarAction.getInstance().init(), T._.popup_exit()));
    }

    private void initBottomPanel() {

        bottomPanel = new JPanel(new MigLayout("ins 0, wrap 1", "[grow, fill]", "[]2[]2[]2[]"));
        bottomPanel.add(new ChunksEditor());
        bottomPanel.add(new ParalellDownloadsEditor());
        bottomPanel.add(new ParallelDownloadsPerHostEditor());
        bottomPanel.add(new SpeedlimitEditor());

    }

    private void addMenuEntry(JPanel panel, AppAction actionId) {
        AbstractButton ret = getMenuEntry(actionId);
        if (ret == null) return;
        panel.add(ret, "growx, pushx");
    }

    private AbstractButton getMenuEntry(AppAction action) {

        AbstractButton b = createButton(action);
        if (action == ExitToolbarAction.getInstance()) {
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    hideThreadrunning = false;
                    TrayIconPopup.this.dispose();
                }
            });
        }
        resizecomps.add(b);
        return b;
    }

    private AbstractButton createButton(AppAction action) {
        if (action.isToggle()) {
            JToggleButton bt = new JToggleButton(action);
            bt.setOpaque(false);
            bt.setContentAreaFilled(false);
            bt.setBorderPainted(false);

            bt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    hideThreadrunning = false;
                    TrayIconPopup.this.dispose();
                }
            });
            bt.setIcon(NewTheme.I().getCheckBoxImage(action.getIconKey(), false, 24));
            bt.setSelectedIcon(NewTheme.I().getCheckBoxImage(action.getIconKey(), true, 24));
            bt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bt.setFocusPainted(false);
            bt.setHorizontalAlignment(JButton.LEFT);
            bt.setIconTextGap(5);
            bt.addMouseListener(new HoverEffect(bt));
            return bt;
        } else {
            JToggleButton bt = new JToggleButton(action);
            bt.setOpaque(false);
            bt.setContentAreaFilled(false);
            bt.setBorderPainted(false);
            bt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    hideThreadrunning = false;
                    TrayIconPopup.this.dispose();
                }
            });

            bt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bt.setFocusPainted(false);
            bt.setHorizontalAlignment(JButton.LEFT);
            bt.setIconTextGap(5);
            bt.addMouseListener(new HoverEffect(bt));
            return bt;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        enteredPopup = true;
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

}