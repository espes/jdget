package jd.controlling.captcha;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.gui.swing.dialog.CaptchaDialog;
import jd.gui.swing.dialog.CaptchaDialogInterface;
import jd.gui.swing.dialog.DialogType;
import jd.gui.swing.jdgui.JDGui;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;

import org.appwork.exceptions.WTFException;
import org.appwork.storage.config.JsonConfig;
import org.appwork.uio.UIOManager;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.DomainInfo;
import org.jdownloader.captcha.v2.challenge.stringcaptcha.ImageCaptchaChallenge;
import org.jdownloader.controlling.UniqueAlltimeID;
import org.jdownloader.logging.LogController;

public abstract class ChallengeDialogHandler<T extends ImageCaptchaChallenge<?>> {
    private CaptchaDialogInterface textDialog;

    private DomainInfo             host;
    protected T                    captchaChallenge;
    private CaptchaSettings        config;
    private final UniqueAlltimeID  id = new UniqueAlltimeID();

    public ChallengeDialogHandler(DomainInfo instance, T captchaChallenge2) {
        this.host = instance;
        this.captchaChallenge = captchaChallenge2;
        config = JsonConfig.create(CaptchaSettings.class);

    }

    public DomainInfo getHost() {
        return host;
    }

    public void run() throws InterruptedException, SkipException {

        viaGUI();

    }

    private Logger getLogger() {
        Logger logger = null;
        if (captchaChallenge.getPlugin() instanceof PluginForHost) {
            logger = captchaChallenge.getPlugin().getLogger();
        } else if (captchaChallenge.getPlugin() instanceof PluginForDecrypt) {
            logger = captchaChallenge.getPlugin().getLogger();
        }
        if (logger == null) logger = LogController.GL;
        return logger;
    }

    private void viaGUI() throws InterruptedException, SkipException {
        DialogType dialogType = null;
        try {
            int f = 0;

            int countdown = getTimeoutInMS();
            if (captchaChallenge.getPlugin() instanceof PluginForHost) {
                dialogType = DialogType.HOSTER;
                if (countdown > 0) {
                    f = f | UIOManager.LOGIC_COUNTDOWN;
                }
            } else if (captchaChallenge.getPlugin() instanceof PluginForDecrypt) {
                dialogType = DialogType.CRAWLER;

                if (countdown > 0) {
                    f = f | UIOManager.LOGIC_COUNTDOWN;
                }
            }

            Image[] images = CaptchaDialog.getGifImages(captchaChallenge.getImageFile().toURI().toURL());
            if (images == null || images.length == 0) {
                BufferedImage img = IconIO.getImage(captchaChallenge.getImageFile().toURI().toURL(), false);
                if (img != null) images = new Image[] { img };
            }

            if (images == null || images.length == 0 || images[0] == null) {
                getLogger().severe("Could not load CaptchaImage! " + captchaChallenge.getImageFile().getAbsolutePath());
                return;
            }

            // }

            final ModalExclusionType orgEx = new EDTHelper<ModalExclusionType>() {

                @Override
                public ModalExclusionType edtRun() {
                    return JDGui.getInstance().getMainFrame().getModalExclusionType();
                }
            }.getReturnValue();

            try {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        JDGui.getInstance().getMainFrame().setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);
                        /**
                         * This may have no effect. we have to set the frame invisble and visible again
                         * 
                         */

                        // this brings our frame to top..
                        // JDGui.getInstance().getMainFrame().setVisible(!JDGui.getInstance().getMainFrame().isVisible());
                        // JDGui.getInstance().getMainFrame().setVisible(!JDGui.getInstance().getMainFrame().isVisible());

                    }
                }.waitForEDT();

                showDialog(dialogType, f, images);
            } finally {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        JDGui.getInstance().getMainFrame().setModalExclusionType(orgEx);
                    }
                }.waitForEDT();

            }

            return;
        } catch (DialogNoAnswerException e) {

            /* no external response available */
            if (e.isCausedByInterrupt()) throw new InterruptedException("Dialog Interrupted");

            if (e.isCausedByTimeout()) { throw new SkipException(SkipRequest.TIMEOUT); }
            throw new SkipException(SkipRequest.SINGLE);

        } catch (HideCaptchasByHostException e) {

            throw new SkipException(SkipRequest.BLOCK_HOSTER);

        } catch (HideCaptchasByPackageException e) {

            throw new SkipException(SkipRequest.BLOCK_PACKAGE);

        } catch (StopCurrentActionException e) {
            switch (dialogType) {
            case CRAWLER:

                break;
            case HOSTER:

                break;
            }

            DownloadWatchDog.getInstance().stopDownloads();

            throw new SkipException(SkipRequest.STOP_CURRENT_ACTION);
        } catch (HideAllCaptchasException e) {
            throw new SkipException(SkipRequest.BLOCK_ALL_CAPTCHAS);

        } catch (MalformedURLException e) {
            throw new WTFException();
        } catch (RuntimeException e) {
            LogSource.exception(getLogger(), e);
        } catch (RefreshException e) {
            throw new SkipException(SkipRequest.REFRESH);
        }
    }

    protected int getTimeoutInMS() {

        int countdown = -1;

        if (captchaChallenge.getPlugin() instanceof PluginForHost) {

            if (config.isDialogCountdownForDownloadsEnabled()) {
                countdown = config.getDialogCountdown();
            }
        } else if (captchaChallenge.getPlugin() instanceof PluginForDecrypt) {

            if (config.isDialogCountdownForCrawlerEnabled()) {
                countdown = config.getDialogCountdown();
            }

        }
        int pluginTimeout = captchaChallenge.getTimeout();
        if (pluginTimeout > 0) {

            if (countdown <= 0 || pluginTimeout < countdown) {
                countdown = pluginTimeout;
            }
        }
        return countdown;
    }

    /**
     * @param dialogType
     * @param f
     * @param images
     * @throws DialogClosedException
     * @throws DialogCanceledException
     * @throws HideCaptchasByHostException
     * @throws StopCurrentActionException
     * @throws HideCaptchasByPackageException
     * @throws HideAllCaptchasException
     * @throws RefreshException
     */
    abstract protected void showDialog(DialogType dialogType, int flag, Image[] images) throws DialogClosedException, DialogCanceledException, HideCaptchasByHostException, HideCaptchasByPackageException, StopCurrentActionException, HideAllCaptchasException, RefreshException;

    /**
     * @return the iD
     */
    public UniqueAlltimeID getID() {
        return id;
    }
}
