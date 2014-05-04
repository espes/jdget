package org.jdownloader.myjdownloader.client.json;

public class CaptchaChallenge {
    private String captchaResponse;
    
    private String captchaChallenge;
    
    private String image;
    
    public CaptchaChallenge(/* STorable */) {
    }
    
    public String getCaptchaChallenge() {
        return this.captchaChallenge;
    }
    
    public String getCaptchaResponse() {
        return this.captchaResponse;
    }
    
    public String getImage() {
        return this.image;
    }
    
    public void setCaptchaChallenge(final String captchaChallenge) {
        this.captchaChallenge = captchaChallenge;
    }
    
    public void setCaptchaResponse(final String captchaResponse) {
        this.captchaResponse = captchaResponse;
    }
    
    public void setImage(final String image) {
        this.image = image;
    }
    
}
