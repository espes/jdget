package org.jdownloader.myjdownloader.client;

public class RegisterPayload {
    public RegisterPayload() {

    }

    public RegisterPayload(String email, String serverSecret, String captchaChallenge, String captchaResponse) {
        this.secretServer = serverSecret;
        this.captchaChallenge = captchaChallenge;
        this.captchaResponse = captchaResponse;
        this.email = email;
    }

    private String secretServer;

    public String getSecretServer() {
        return secretServer;
    }

    public void setSecretServer(String secretServer) {
        this.secretServer = secretServer;
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
