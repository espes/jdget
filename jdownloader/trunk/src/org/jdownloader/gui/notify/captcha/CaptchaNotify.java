package org.jdownloader.gui.notify.captcha;

import java.awt.event.MouseEvent;

import org.jdownloader.captcha.v2.AbstractResponse;
import org.jdownloader.captcha.v2.Challenge;
import org.jdownloader.captcha.v2.ChallengeSolver;
import org.jdownloader.captcha.v2.solver.gui.DialogBasicCaptchaSolver;
import org.jdownloader.captcha.v2.solver.gui.DialogClickCaptchaSolver;
import org.jdownloader.captcha.v2.solverjob.ChallengeSolverJobListener;
import org.jdownloader.captcha.v2.solverjob.SolverJob;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.notify.BasicContentPanel;
import org.jdownloader.gui.notify.BubbleNotify;
import org.jdownloader.gui.notify.gui.AbstractNotifyWindow;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class CaptchaNotify extends AbstractNotifyWindow<BasicContentPanel> implements ChallengeSolverJobListener {
    
    private SolverJob<?> job;
    
    public CaptchaNotify(CaptchaBubbleSupport captchaBubbleSupport, SolverJob<?> job) {
        super(captchaBubbleSupport, _GUI._.CaptchaNotify_CaptchaNotify_title_(Challenge.getHost(job.getChallenge())), new BasicContentPanel(_GUI._.CaptchaNotify_CaptchaNotifyPanel_text(), NewTheme.I().getIcon(IconKey.ICON_OCR, 32)));
        
        job.getEventSender().addListener(this, true);
        this.job = job;
        
    }
    
    @Override
    public void onSolverJobReceivedNewResponse(AbstractResponse<?> response) {
    }
    
    @Override
    public void onSolverDone(ChallengeSolver<?> solver) {
        // getContentComponent().update();
        if (job.isDone()) {
            close();
        }
    }
    
    @Override
    protected void onMouseClicked(MouseEvent m) {
        super.onMouseClicked(m);
        
        DialogBasicCaptchaSolver.getInstance().requestFocus(job.getChallenge());
        DialogClickCaptchaSolver.getInstance().requestFocus(job.getChallenge());
        close();
    }
    
    // protected int getTimeout() {
    // // don't autoclose
    // return 0;
    // }
    
    private void close() {
        BubbleNotify.getInstance().hide(this);
        job.getEventSender().removeListener(this);
    }
    
    @Override
    public void onSolverStarts(ChallengeSolver<?> parameter) {
        // getContentComponent().update();
    }
    
    @Override
    public void onSolverTimedOut(ChallengeSolver<?> parameter) {
        // getContentComponent().update();
        if (job.isDone()) {
            close();
        }
    }
    
}
