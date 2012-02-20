package jd.gui.swing.jdgui.components.toolbar.actions;

import java.awt.event.ActionEvent;

import jd.controlling.IOEQ;
import jd.controlling.downloadcontroller.DownloadWatchDog;

import org.appwork.controlling.StateEvent;
import org.appwork.controlling.StateEventListener;
import org.jdownloader.gui.translate._GUI;

public class StopDownloadsAction extends AbstractToolbarAction {
    private static final StopDownloadsAction INSTANCE = new StopDownloadsAction();

    /**
     * get the only existing instance of StopDownloadsAction. This is a
     * singleton
     * 
     * @return
     */
    public static StopDownloadsAction getInstance() {
        return StopDownloadsAction.INSTANCE;
    }

    /**
     * Create a new instance of StopDownloadsAction. This is a singleton class.
     * Access the only existing instance by using {@link #getInstance()}.
     */
    private StopDownloadsAction() {

    }

    public void actionPerformed(ActionEvent e) {
        IOEQ.add(new Runnable() {

            public void run() {
                if (DownloadWatchDog.getInstance().getStateMachine().hasPassed(DownloadWatchDog.STOPPING_STATE)) return;
                DownloadWatchDog.getInstance().stopDownloads();
            }

        });
    }

    @Override
    public String createIconKey() {
        return "media-playback-stop";
    }

    @Override
    protected String createMnemonic() {
        return _GUI._.action_stop_downloads_mnemonic();
    }

    @Override
    protected String createAccelerator() {
        return _GUI._.action_stop_downloads_accelerator();
    }

    @Override
    public String createTooltip() {
        return _GUI._.action_stop_downloads_tooltip();
    }

    @Override
    protected void doInit() {
        this.setEnabled(false);
        DownloadWatchDog.getInstance().getStateMachine().addListener(new StateEventListener() {

            public void onStateUpdate(StateEvent event) {
            }

            public void onStateChange(StateEvent event) {
                if (DownloadWatchDog.IDLE_STATE == event.getNewState() || DownloadWatchDog.STOPPED_STATE == event.getNewState()) {
                    setEnabled(false);
                } else if (DownloadWatchDog.RUNNING_STATE == event.getNewState()) {
                    setEnabled(true);
                }
            }
        });

    }

}
