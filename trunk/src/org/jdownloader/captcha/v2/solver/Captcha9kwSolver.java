package org.jdownloader.captcha.v2.solver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jd.controlling.captcha.CaptchaSettings;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.IO;
import org.appwork.utils.StringUtils;
import org.jdownloader.captcha.v2.AbstractResponse;
import org.jdownloader.captcha.v2.Challenge;
import org.jdownloader.captcha.v2.ChallengeResponseValidation;
import org.jdownloader.captcha.v2.ChallengeSolver;
import org.jdownloader.captcha.v2.challenge.stringcaptcha.BasicCaptchaChallenge;
import org.jdownloader.captcha.v2.solver.jac.JACSolver;
import org.jdownloader.captcha.v2.solver.jac.SolverException;
import org.jdownloader.captcha.v2.solverjob.SolverJob;
import org.jdownloader.logging.LogController;
import org.jdownloader.settings.advanced.AdvancedConfigManager;
import org.jdownloader.settings.staticreferences.CFG_CAPTCHA;

public class Captcha9kwSolver extends ChallengeSolver<String> implements ChallengeResponseValidation {
    private Captcha9kwSettings            config;
    private static final Captcha9kwSolver INSTANCE   = new Captcha9kwSolver();
    private ThreadPoolExecutor            threadPool = new ThreadPoolExecutor(0, 1, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory());

    public static Captcha9kwSolver getInstance() {
        return INSTANCE;
    }

    @Override
    public Class<String> getResultType() {
        return String.class;
    }

    private Captcha9kwSolver() {
        super(1);
        config = JsonConfig.create(Captcha9kwSettings.class);
        AdvancedConfigManager.getInstance().register(config);
        threadPool.allowCoreThreadTimeOut(true);

    }

    @Override
    public boolean canHandle(Challenge<?> c) {
        return CFG_CAPTCHA.CAPTCHA_EXCHANGE_SERVICES_ENABLED.isEnabled() && config.isEnabled() && super.canHandle(c);
    }

    public String getAPIROOT() {
        if (config.ishttps()) {
            return "https://www.9kw.eu/";
        } else {
            return "http://www.9kw.eu/";
        }
    }

    @Override
    public void solve(final SolverJob<String> job) throws InterruptedException, SolverException {
        if (StringUtils.isEmpty(config.getApiKey())) {
            job.getLogger().info("No ApiKey for 9kw.eu found.");
            return;
        }
        if (job.getChallenge() instanceof BasicCaptchaChallenge && CFG_CAPTCHA.CAPTCHA_EXCHANGE_SERVICES_ENABLED.isEnabled()) {
            job.waitFor(JsonConfig.create(CaptchaSettings.class).getCaptchaDialogJAntiCaptchaTimeout(), JACSolver.getInstance());
            checkInterruption();
            BasicCaptchaChallenge challenge = (BasicCaptchaChallenge) job.getChallenge();

            job.getLogger().info("Start Captcha to 9kw.eu. Timeout: " + JsonConfig.create(CaptchaSettings.class).getCaptchaDialogJAntiCaptchaTimeout() + " - getTypeID: " + challenge.getTypeID());
            if (config.getwhitelist() != null) {
                if (config.getwhitelist().length() > 5) {
                    if (config.getwhitelist().contains(challenge.getTypeID())) {
                        job.getLogger().info("Hoster on whitelist for 9kw.eu. - " + challenge.getTypeID());
                    } else {
                        job.getLogger().info("Hoster not on whitelist for 9kw.eu. - " + challenge.getTypeID());
                        return;
                    }
                }
            }
            if (config.getblacklist() != null) {
                if (config.getblacklist().length() > 5) {
                    if (config.getblacklist().contains(challenge.getTypeID())) {
                        job.getLogger().info("Hoster on blacklist for 9kw.eu. - " + challenge.getTypeID());
                        return;
                    } else {
                        job.getLogger().info("Hoster not on blacklist for 9kw.eu. - " + challenge.getTypeID());
                    }
                }
            }

            try {
                byte[] data = IO.readFile(challenge.getImageFile());
                Browser br = new Browser();
                br.setAllowedResponseCodes(new int[] { 500 });
                String ret = "";
                for (int i = 0; i <= 5; i++) {
                    ret = br.postPage(getAPIROOT() + "index.cgi", "action=usercaptchaupload&jd=2&source=jd2&captchaperhour=" + config.gethour() + "&prio=" + config.getprio() + "&confirm=" + config.isconfirm() + "&oldsource=" + Encoding.urlEncode(challenge.getTypeID()) + "&apikey=" + Encoding.urlEncode(config.getApiKey()) + "&captchaSource=jdPlugin&timeout=" + JsonConfig.create(CaptchaSettings.class).getCaptchaDialogJAntiCaptchaTimeout() + "&version=1.1&base64=1&file-upload-01=" + Encoding.urlEncode(org.appwork.utils.encoding.Base64.encodeToString(data, false)));
                    if (ret.startsWith("OK-")) {
                        break;
                    } else {
                        Thread.sleep(3000);
                    }
                }
                job.getLogger().info("Send Captcha to 9kw.eu. - Answer: " + ret);
                if (!ret.startsWith("OK-")) throw new SolverException(ret);
                // Error-No Credits
                String captchaID = ret.substring(3);
                data = null;

                int count9kw = 5;
                Thread.sleep(5000);
                while (true) {
                    count9kw += 2;
                    job.getLogger().info("9kw.eu Ask " + captchaID);
                    ret = br.getPage(getAPIROOT() + "index.cgi?action=usercaptchacorrectdata&jd=2&source=jd2&apikey=" + Encoding.urlEncode(config.getApiKey()) + "&id=" + Encoding.urlEncode(captchaID) + "&version=1.1");
                    job.getLogger().info("9kw.eu Answer " + count9kw + "s: " + ret);
                    if (ret.startsWith("OK-answered-")) {
                        job.addAnswer(new Captcha9kwResponse(challenge, this, ret.substring("OK-answered-".length()), 100, captchaID));
                        return;
                    }
                    checkInterruption();
                    Thread.sleep(2000);
                }

            } catch (IOException e) {
                job.getLogger().log(e);
            }
        } else {
            job.getLogger().info("Problem with Captcha9kwSolver.");
        }

    }

    @Override
    public void setValid(final AbstractResponse<?> response) {
        if (config.isfeedback()) {
            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        String captchaID = ((Captcha9kwResponse) response).getCaptcha9kwID();
                        Browser br = new Browser();
                        String ret = "";
                        br.setAllowedResponseCodes(new int[] { 500 });
                        for (int i = 0; i <= 5; i++) {
                            ret = br.getPage(getAPIROOT() + "index.cgi?action=usercaptchacorrectback&source=jd2&correct=1&id=" + captchaID + "&apikey=" + Encoding.urlEncode(config.getApiKey()));
                            if (ret.startsWith("OK")) {
                                break;
                            }
                        }
                    } catch (final Throwable e) {
                        LogController.CL(true).log(e);
                    }
                }
            });
        }
    }

    @Override
    public void setUnused(AbstractResponse<?> response) {
    }

    @Override
    public void setInvalid(final AbstractResponse<?> response) {
        if (config.isfeedback()) {
            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        String captchaID = ((Captcha9kwResponse) response).getCaptcha9kwID();
                        Browser br = new Browser();
                        String ret = "";
                        br.setAllowedResponseCodes(new int[] { 500 });
                        for (int i = 0; i <= 5; i++) {
                            ret = br.getPage(getAPIROOT() + "index.cgi?action=usercaptchacorrectback&source=jd2&correct=2&id=" + captchaID + "&apikey=" + Encoding.urlEncode(config.getApiKey()));
                            if (ret.startsWith("OK")) {
                                break;
                            }
                        }

                    } catch (final Throwable e) {
                        LogController.CL(true).log(e);
                    }
                }
            });
        }
    }
}
