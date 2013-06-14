package org.jdownloader.captcha.v2.solver.gui;

import jd.controlling.captcha.BasicCaptchaDialogHandler;
import jd.controlling.captcha.CaptchaSettings;
import jd.controlling.captcha.SkipException;
import jd.controlling.captcha.SkipRequest;
import jd.gui.swing.jdgui.JDGui;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.StringUtils;
import org.jdownloader.captcha.v2.AbstractResponse;
import org.jdownloader.captcha.v2.ChallengeSolver;
import org.jdownloader.captcha.v2.challenge.stringcaptcha.BasicCaptchaChallenge;
import org.jdownloader.captcha.v2.challenge.stringcaptcha.CaptchaResponse;
import org.jdownloader.captcha.v2.solver.CBSolver;
import org.jdownloader.captcha.v2.solver.Captcha9kwSettings;
import org.jdownloader.captcha.v2.solver.Captcha9kwSolver;
import org.jdownloader.captcha.v2.solver.CaptchaBrotherHoodSettings;
import org.jdownloader.captcha.v2.solver.CaptchaResolutorCaptchaSettings;
import org.jdownloader.captcha.v2.solver.CaptchaResolutorCaptchaSolver;
import org.jdownloader.captcha.v2.solver.jac.JACSolver;
import org.jdownloader.captcha.v2.solverjob.ChallengeSolverJobListener;
import org.jdownloader.captcha.v2.solverjob.ResponseList;
import org.jdownloader.captcha.v2.solverjob.SolverJob;
import org.jdownloader.settings.staticreferences.CFG_SILENTMODE;

public class DialogBasicCaptchaSolver extends ChallengeSolver<String> {
    private CaptchaSettings                       config;
    private Captcha9kwSettings                    config9kw;
    private CaptchaBrotherHoodSettings            configcbh;
    private CaptchaResolutorCaptchaSettings       configresolutor;
    private static final DialogBasicCaptchaSolver INSTANCE = new DialogBasicCaptchaSolver();

    public static DialogBasicCaptchaSolver getInstance() {
        return INSTANCE;
    }

    @Override
    public Class<String> getResultType() {
        return String.class;
    }

    public void enqueue(SolverJob<String> job) {
        if (job.getChallenge() instanceof BasicCaptchaChallenge) {
            super.enqueue(job);
        }
    }

    private DialogBasicCaptchaSolver() {
        super(1);
        config = JsonConfig.create(CaptchaSettings.class);
        config9kw = JsonConfig.create(Captcha9kwSettings.class);
        configcbh = JsonConfig.create(CaptchaBrotherHoodSettings.class);
        configresolutor = JsonConfig.create(CaptchaResolutorCaptchaSettings.class);
    }

    @Override
    public void solve(final SolverJob<String> job) throws InterruptedException, SkipException {
        synchronized (this) {

            if (job.getChallenge() instanceof BasicCaptchaChallenge) {
                job.getLogger().info("Waiting for JAC");
                job.waitFor(config.getCaptchaDialogJAntiCaptchaTimeout(), JACSolver.getInstance());

                if (config9kw.isEnabled() && config.getCaptchaDialog9kwTimeout() > 0) job.waitFor(config.getCaptchaDialog9kwTimeout(), Captcha9kwSolver.getInstance());
                if (configcbh.isEnabled() && config.getCaptchaDialogCaptchaBroptherhoodTimeout() > 0) job.waitFor(config.getCaptchaDialogCaptchaBroptherhoodTimeout(), CBSolver.getInstance());
                if (configresolutor.isEnabled() && config.getCaptchaDialogResolutorCaptchaTimeout() > 0) job.waitFor(config.getCaptchaDialogResolutorCaptchaTimeout(), CaptchaResolutorCaptchaSolver.getInstance());

                job.getLogger().info("JAC is done. Response so far: " + job.getResponse());
                ChallengeSolverJobListener jacListener = null;
                if (JDGui.getInstance().isSilentModeActive()) {
                    switch (CFG_SILENTMODE.CFG.getonCaptchaDuringSilentModeAction()) {
                    case DEFAULT_DIALOG_HANDLING:
                        break;
                    case DISABLE_DIALOG_SOLVER:
                        return;
                    case SKIP_LINK:
                        throw new SkipException(SkipRequest.SINGLE);
                    }
                }
                checkInterruption();
                BasicCaptchaChallenge captchaChallenge = (BasicCaptchaChallenge) job.getChallenge();
                // we do not need another queue
                final BasicCaptchaDialogHandler handler = new BasicCaptchaDialogHandler(captchaChallenge);
                job.getEventSender().addListener(jacListener = new ChallengeSolverJobListener() {

                    @Override
                    public void onSolverTimedOut(ChallengeSolver<?> parameter) {
                    }

                    @Override
                    public void onSolverStarts(ChallengeSolver<?> parameter) {
                    }

                    @Override
                    public void onSolverJobReceivedNewResponse(AbstractResponse<?> response) {
                        ResponseList<String> resp = job.getResponse();
                        handler.setSuggest(resp.getValue());
                        job.getLogger().info("Received Suggestion: " + resp);

                    }

                    @Override
                    public void onSolverDone(ChallengeSolver<?> solver) {

                    }
                });
                try {
                    ResponseList<String> resp = job.getResponse();
                    if (resp != null) {
                        handler.setSuggest(resp.getValue());
                    }
                    checkInterruption();
                    if (!captchaChallenge.getImageFile().exists()) {

                        job.getLogger().info("Cannot solve. image does not exist");
                        return;
                    }

                    handler.run();

                    if (StringUtils.isNotEmpty(handler.getCaptchaCode())) {
                        job.addAnswer(new CaptchaResponse(captchaChallenge, this, handler.getCaptchaCode(), 100));
                    }
                } finally {
                    job.getLogger().info("Dialog closed. Response far: " + job.getResponse());
                    if (jacListener != null) job.getEventSender().removeListener(jacListener);
                }
            }
        }

    }

}
