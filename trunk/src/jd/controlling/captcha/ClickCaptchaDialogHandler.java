package jd.controlling.captcha;

import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import jd.gui.swing.dialog.ClickCaptchaDialog;
import jd.gui.swing.dialog.DialogType;

import org.appwork.uio.UserIODefinition.CloseReason;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.DomainInfo;
import org.jdownloader.captcha.v2.challenge.clickcaptcha.ClickCaptchaChallenge;
import org.jdownloader.captcha.v2.challenge.clickcaptcha.ClickedPoint;

public class ClickCaptchaDialogHandler extends ChallengeDialogHandler<ClickCaptchaChallenge> {

    private ClickedPoint       result;
    private ClickCaptchaDialog dialog;

    public ClickedPoint getPoint() {
        return result;
    }

    public ClickCaptchaDialogHandler(ClickCaptchaChallenge captchaChallenge) {
        super(DomainInfo.getInstance(captchaChallenge.getPlugin().getHost()), captchaChallenge);

    }

    @Override
    protected void showDialog(DialogType dialogType, int flag, Image[] images) throws DialogClosedException, DialogCanceledException, HideCaptchasByHostException, HideCaptchasByPackageException, StopCurrentActionException, HideAllCaptchasException, RefreshException {

        ClickCaptchaDialog d = new ClickCaptchaDialog(flag, dialogType, getHost(), images, captchaChallenge.getExplain());
        d.setPlugin(captchaChallenge.getPlugin());
        d.setTimeout(getTimeoutInMS());
        if (getTimeoutInMS() == captchaChallenge.getTimeout()) {
            // no reason to let the user stop the countdown if the result cannot be used after the countdown anyway
            d.setCountdownPausable(false);
        }

        dialog = d;

        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                dialog.displayDialog();
                dialog.getDialog().addWindowListener(new WindowListener() {

                    @Override
                    public void windowOpened(WindowEvent e) {
                    }

                    @Override
                    public void windowIconified(WindowEvent e) {
                    }

                    @Override
                    public void windowDeiconified(WindowEvent e) {
                    }

                    @Override
                    public void windowDeactivated(WindowEvent e) {
                    }

                    @Override
                    public void windowClosing(WindowEvent e) {
                        synchronized (ClickCaptchaDialogHandler.this) {
                            boolean v = dialog.getDialog().isVisible();
                            ClickCaptchaDialogHandler.this.notifyAll();
                        }

                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        synchronized (ClickCaptchaDialogHandler.this) {
                            boolean v = dialog.getDialog().isVisible();
                            ClickCaptchaDialogHandler.this.notifyAll();
                        }
                    }

                    @Override
                    public void windowActivated(WindowEvent e) {
                    }
                });
                return null;
            }
        }.waitForEDT();
        try {
            while (dialog.getDialog().isVisible()) {
                synchronized (this) {

                    this.wait();

                }
            }

        } catch (InterruptedException e) {
            throw new DialogClosedException(Dialog.RETURN_INTERRUPT);
        }
        result = dialog.getResult();
        try {
            if (dialog.getCloseReason() != CloseReason.OK) {

                if (dialog.isHideCaptchasForHost()) throw new HideCaptchasByHostException();
                if (dialog.isHideCaptchasForPackage()) throw new HideCaptchasByPackageException();
                if (dialog.isStopDownloads()) throw new StopCurrentActionException();
                if (dialog.isHideAllCaptchas()) throw new HideAllCaptchasException();
                if (dialog.isStopCrawling()) throw new StopCurrentActionException();
                if (dialog.isStopShowingCrawlerCaptchas()) throw new HideAllCaptchasException();
                if (dialog.isRefresh()) throw new RefreshException();
                dialog.throwCloseExceptions();
                throw new DialogClosedException(Dialog.RETURN_CLOSED);
            }
        } catch (IllegalStateException e) {
            // Captcha has been solved externally

        }

    }

}