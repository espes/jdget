package jd.gui.swing.jdgui.components.toolbar.actions;

import java.awt.event.ActionEvent;

import jd.controlling.downloadcontroller.DownloadLinkCandidate;
import jd.controlling.downloadcontroller.DownloadLinkCandidateResult;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.DownloadWatchDogProperty;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.downloadcontroller.event.DownloadWatchdogListener;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.controlling.contextmenu.ActionContext;
import org.jdownloader.controlling.contextmenu.Customizer;
import org.jdownloader.gui.toolbar.action.AbstractToolBarAction;
import org.jdownloader.gui.translate._GUI;

public class PauseDownloadsAction extends AbstractToolBarAction implements DownloadWatchdogListener, GenericConfigEventListener<Integer>, ActionContext {
    
    public PauseDownloadsAction() {
        setIconKey("media-playback-pause");
        setSelected(false);
        setEnabled(false);
        setTooltipText(_GUI._.gui_menu_action_break2_desc(org.jdownloader.settings.staticreferences.CFG_GENERAL.PAUSE_SPEED.getValue() / 1024));
        
        DownloadWatchDog.getInstance().getEventSender().addListener(this, true);
        
        org.jdownloader.settings.staticreferences.CFG_GENERAL.PAUSE_SPEED.getEventSender().addListener(this, true);
        DownloadWatchDog.getInstance().notifyCurrentState(this);
        
    }
    
    protected void initContextDefaults(ActionContext actionContext) {
        if (actionContext == this) {
            setHideIfDownloadsAreStopped(false);
        }
        
    }
    
    public void actionPerformed(ActionEvent e) {
        DownloadWatchDog.getInstance().pauseDownloadWatchDog(!DownloadWatchDog.getInstance().isPaused());
    }
    
    @Override
    public String createTooltip() {
        return _GUI._.gui_menu_action_break2_desc(org.jdownloader.settings.staticreferences.CFG_GENERAL.PAUSE_SPEED.getValue() / 1024);
    }
    
    @Override
    public void onDownloadWatchdogDataUpdate() {
    }
    
    private boolean            hideIfDownloadsAreStopped     = false;
    public static final String HIDE_IF_DOWNLOADS_ARE_STOPPED = "HideIfDownloadsAreStopped";
    
    @Customizer(name = "Hide if downloads are not running")
    public boolean isHideIfDownloadsAreStopped() {
        return hideIfDownloadsAreStopped;
    }
    
    public void setHideIfDownloadsAreStopped(boolean showIfDownloadsAreRunning) {
        this.hideIfDownloadsAreStopped = showIfDownloadsAreRunning;
        if (isHideIfDownloadsAreStopped() && !DownloadWatchDog.getInstance().isRunning()) {
            setVisible(false);
        } else {
            setVisible(true);
        }
    }
    
    @Override
    public void onDownloadWatchdogStateIsIdle() {
        new EDTRunner() {
            
            @Override
            protected void runInEDT() {
                setEnabled(false);
                setSelected(false);
            }
        };
        
    }
    
    @Override
    public void onDownloadWatchdogStateIsPause() {
        
        new EDTRunner() {
            
            @Override
            protected void runInEDT() {
                setEnabled(true);
                setSelected(true);
            }
        };
        
    }
    
    @Override
    public void onDownloadWatchdogStateIsRunning() {
        new EDTRunner() {
            
            @Override
            protected void runInEDT() {
                setEnabled(true);
                setSelected(false);
                setVisible(true);
            }
        };
        
    }
    
    @Override
    public void onDownloadWatchdogStateIsStopped() {
        new EDTRunner() {
            
            @Override
            protected void runInEDT() {
                setEnabled(false);
                setSelected(false);
                if (isHideIfDownloadsAreStopped()) {
                    setVisible(false);
                }
            }
        };
        
    }
    
    @Override
    public void onDownloadWatchdogStateIsStopping() {
    }
    
    @Override
    public void onConfigValidatorError(KeyHandler<Integer> keyHandler, Integer invalidValue, ValidationException validateException) {
    }
    
    @Override
    public void onConfigValueModified(KeyHandler<Integer> keyHandler, Integer newValue) {
        new EDTRunner() {
            
            @Override
            protected void runInEDT() {
                
                setTooltipText(_GUI._.gui_menu_action_break2_desc(org.jdownloader.settings.staticreferences.CFG_GENERAL.PAUSE_SPEED.getValue() / 1024));
                
            }
        };
    }
    
    @Override
    public void onDownloadControllerStart(SingleDownloadController downloadController, DownloadLinkCandidate candidate) {
    }
    
    @Override
    public void onDownloadControllerStopped(SingleDownloadController downloadController, DownloadLinkCandidate candidate, DownloadLinkCandidateResult result) {
    }
    
    @Override
    public void onDownloadWatchDogPropertyChange(DownloadWatchDogProperty propertyChange) {
    }
    
}
