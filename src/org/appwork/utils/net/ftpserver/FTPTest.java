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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author daniel
 * 
 */
public class FTPTest {

    public static void main(String[] args) throws IOException {
        FtpConnectionHandler handler = new FtpConnectionHandler() {
            File current = new File("/home/daniel");

            @Override
            public String getWelcomeMessage() {
                return "Hallo";
            }

            @Override
            public FTPUser onLogin(String user) {
                if ("test".equals(user)) {
                    return new FTPUser("test", "test");
                } else {
                    return null;
                }
            }

            @Override
            public String onLogout() {
                return "Bye";
            }

            @Override
            public String onLoginSuccess() {
                return "OK";
            }

            @Override
            public String onLoginFailed() {
                return "NÄ";
            }

            @Override
            public String getPWD() {
                return current.getAbsolutePath();
            }

            @Override
            public void CWD(String cwd) throws FtpFileNotExistException {
                File newcur = null;
                if (cwd.startsWith("/")){
                    newcur=new File(cwd);
                }else{
                    newcur=new File(current, cwd);
                }
                if (newcur.exists() && newcur.isDirectory()) {
                    current = newcur;
                    return;
                }
                throw new FtpFileNotExistException();
            }

            @Override
            public void onCDUP() throws FtpFileNotExistException {
                File newcur = current.getParentFile();
                if (newcur != null) {
                    current = newcur; 
                    return;
                }
                throw new FtpFileNotExistException();
            }

            @Override
            public void onLIST(OutputStream outputStream, String string) throws UnsupportedEncodingException, IOException {
                if (string == null) {
                    File[] list = current.listFiles();
                    if (list != null) {
                        for (File item : list) {
                            String ret = "";
                            if (item.isDirectory()) {
                                ret = ret + "D   "+item.getName();
                            } else {
                                ret = ret + "F   ";
                                ret = ret + item.getName() + "   " + item.length();
                            }
                            ret = ret + "\r\n";
                            outputStream.write(ret.getBytes("UTF-8"));
                        }
                    } else {
                        throw new FtpFileNotExistException();
                    }
                } else {
                    File file = new File(string);
                    if (!file.exists()) throw new FtpFileNotExistException();
                    String ret = "";
                    if (file.isDirectory()) {
                        ret = ret + "   ";
                    } else {
                        ret = ret + "   ";
                        ret = ret + file.getName() + "   " + file.length();
                    }
                    ret = ret + "\r\n";
                    outputStream.write(ret.getBytes("UTF-8"));
                }

            }

        };
        FtpServer server = new FtpServer(handler, 8080);
        server.start();
    }
}
