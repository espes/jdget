/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver;

/**
 * @author thomas
 * 
 */
public class FtpConnectionState {
    private String  currentDir = "/";
    private FTPUser user       = null;

    public String getCurrentDir() {
        return currentDir;
    }

    public FTPUser getUser() {
        return user;
    }

    public void setCurrentDir(final String currentDir) {
        this.currentDir = currentDir;
    }

    public void setUser(final FTPUser user) {
        this.user = user;
    }
}
