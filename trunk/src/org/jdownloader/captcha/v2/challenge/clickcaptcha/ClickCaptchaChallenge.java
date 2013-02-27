package org.jdownloader.captcha.v2.challenge.clickcaptcha;

import java.io.File;

import jd.controlling.IOPermission;
import jd.plugins.Plugin;

import org.jdownloader.captcha.v2.challenge.stringcaptcha.ImageCaptchaChallenge;

public class ClickCaptchaChallenge extends ImageCaptchaChallenge<ClickedPoint> {

    public ClickCaptchaChallenge(IOPermission ioPermission, File imagefile, String explain, Plugin plugin) {
        super(imagefile, plugin.getHost(), explain, plugin, ioPermission);

    }

    @Override
    public boolean isSolved() {
        return this.getResult() != null;
    }

}
