package org.jdownloader.captcha.blacklist;

import java.util.ArrayList;

import org.jdownloader.captcha.v2.Challenge;

public class CaptchaBlackList {
    private static final CaptchaBlackList INSTANCE = new CaptchaBlackList();
    private ArrayList<BlacklistEntry>     entries;

    private CaptchaBlackList() {
        entries = new ArrayList<BlacklistEntry>();
    }

    public static CaptchaBlackList getInstance() {
        return INSTANCE;
    }

    public void add(BlacklistEntry entry) {
        synchronized (entries) {
            entries.add(entry);
        }
    }

    public boolean matches(Challenge<?> c) {
        synchronized (entries) {
            ArrayList<BlacklistEntry> cleanups = new ArrayList<BlacklistEntry>();
            for (BlacklistEntry e : entries) {
                if (e.canCleanUp()) {
                    cleanups.add(e);
                    continue;
                }
                if (e.matches(c)) return true;
            }
            entries.removeAll(cleanups);
        }
        return false;
    }
}
