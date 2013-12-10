package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.CaptchaChallenge;

@ApiNamespace("captcha")
public interface CaptchaAPI {

    public CaptchaChallenge getCaptcha();

}
