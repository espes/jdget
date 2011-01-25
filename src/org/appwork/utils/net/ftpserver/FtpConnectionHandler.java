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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author daniel
 * 
 */
public interface FtpConnectionHandler {

    public String getWelcomeMessage();

    public FTPUser onLogin(final String user);

    public String onLoginSuccess();

    public String onLoginFailed();

    public String onLogout();

    /**
     * @return
     */
    public String getPWD();

    /**
     * @param cwd
     * @return
     */
    public void CWD(String cwd) throws FtpFileNotExistException;

    /**
     * @return
     */
    public void onCDUP() throws FtpFileNotExistException;

    /**
     * @param outputStream
     * @param string
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void onLIST(OutputStream outputStream, String string) throws UnsupportedEncodingException, IOException, FtpFileNotExistException;
}
