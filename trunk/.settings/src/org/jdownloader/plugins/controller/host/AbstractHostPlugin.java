package org.jdownloader.plugins.controller.host;

import org.appwork.storage.Storable;

public class AbstractHostPlugin implements Storable {
    public AbstractHostPlugin(/* STorable */) {
    }

    private boolean premium;
    private boolean hasConfig;
    private long    version;
    private int     interfaceVersion = 0;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isHasConfig() {
        return hasConfig;
    }

    public void setHasConfig(boolean hasConfig) {
        this.hasConfig = hasConfig;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    private String premiumUrl;

    public String getPremiumUrl() {
        return premiumUrl;
    }

    public void setPremiumUrl(String premiumUrl) {
        this.premiumUrl = premiumUrl;
    }

    private String classname;
    private String displayName;

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    private String pattern;

    public AbstractHostPlugin(String className) {
        this.classname = className;
    }

    /**
     * @return the interfaceVersion
     */
    public int getInterfaceVersion() {
        return interfaceVersion;
    }

    /**
     * @param interfaceVersion
     *            the interfaceVersion to set
     */
    public void setInterfaceVersion(int interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }

}
