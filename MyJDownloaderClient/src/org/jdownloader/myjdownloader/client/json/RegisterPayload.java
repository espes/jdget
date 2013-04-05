package org.jdownloader.myjdownloader.client.json;

public class RegisterPayload {
    public RegisterPayload() {

    }

    public RegisterPayload(String email, String serverSecret, String captchaChallenge, String captchaResponse) {
        this.loginSecret = serverSecret;
        this.captchaChallenge = captchaChallenge;
        this.captchaResponse = captchaResponse;
        this.email = email;
    }

    private String loginSecret;

    public String getLoginSecret() {
        return loginSecret;
    }

    public void setLoginSecret(String secretServer) {
        this.loginSecret = secretServer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaptchaResponse() {
        return captchaResponse;
    }

    public void setCaptchaResponse(String captchaResponse) {
        this.captchaResponse = captchaResponse;
    }

    public String getCaptchaChallenge() {
        return captchaChallenge;
    }

    public void setCaptchaChallenge(String captchaChallenge) {
        this.captchaChallenge = captchaChallenge;
    }

    private String email;
    private String captchaResponse;
    private String captchaChallenge;
}
