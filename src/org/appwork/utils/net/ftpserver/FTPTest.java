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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.appwork.utils.Files;

/**
 * @author daniel
 * 
 */
public class FTPTest {

    protected static final File ROOT = new File("c:/test");

    public static void main(final String[] args) throws IOException {
        final FtpConnectionHandler handler = new FtpConnectionHandler() {

            /**
             * @return
             */
            @Override
            public FtpConnectionState createNewConnectionState() {
                final FtpConnectionState state = new FtpConnectionState();
                state.setCurrentDir(FTPTest.ROOT.getAbsolutePath());
                return state;
            }

            @Override
            public ArrayList<FtpFile> getFileList(final FtpConnectionState connectionState, final String item) throws UnsupportedEncodingException, IOException {

                if (item == null) {
                    return list(new File(connectionState.getCurrentDir()));

                } else {
                    final File file = new File(item);
                    return list(file);
                }

            }

            @Override
            public FTPUser getUser(final String user) {
                if ("test".equals(user)) {
                    return new FTPUser("test", "test");
                } else {
                    return null;
                }
            }

            @Override
            public String getWelcomeMessage(final FtpConnectionState connectionState) {
                return "Hallo " + connectionState;
            }

            /**
             * @param file
             * @return
             * @throws FtpFileNotExistException
             */
            protected ArrayList<FtpFile> list(final File file) throws FtpFileNotExistException {
                final ArrayList<FtpFile> ret = new ArrayList<FtpFile>();
                if (!file.exists()) { throw new FtpFileNotExistException();

                }
                if (file.isDirectory()) {
                    final File[] list = file.listFiles();
                    if (list != null) {
                        for (final File item : list) {
                            ret.add(new FtpFile(item.getName(), item.length(), item.isDirectory(), item.lastModified()));
                        }
                    } else {
                        throw new FtpFileNotExistException();
                    }
                } else {
                    ret.add(new FtpFile(file.getName(), file.length(), file.isDirectory(), file.lastModified()));
                }
                return ret;
            }

            @Override
            public void onDirectoryUp(final FtpConnectionState connectionState) throws FtpFileNotExistException {
                if (new File(connectionState.getCurrentDir()).equals(FTPTest.ROOT)) { throw new FtpFileNotExistException(); }
                final File newcur = new File(connectionState.getCurrentDir()).getParentFile();
                if (newcur != null) {

                    connectionState.setCurrentDir(newcur.getAbsolutePath());
                    return;
                }
                throw new FtpFileNotExistException();
            }

            @Override
            public String onLoginFailedMessage(final FtpConnectionState con) {
                return "NÄ " + con.getUser();
            }

            @Override
            public String onLoginSuccessRequest(final FtpConnectionState con) {
                return "OK " + con.getUser();
            }

            @Override
            public String onLogoutRequest(final FtpConnectionState connectionState) {
                return "Bye";
            }

            @Override
            public void setCurrentDirectory(final FtpConnectionState connectionState, final String cwd) throws FtpFileNotExistException {
                File newcur = null;

                if (new File(cwd).isAbsolute()) {
                    newcur = new File(cwd);
                } else {
                    newcur = new File(connectionState.getCurrentDir(), cwd);
                }
                final String rel = Files.getRelativePath(FTPTest.ROOT, newcur);
                if (rel == null) { throw new FtpFileNotExistException(); }
                if (newcur.exists() && newcur.isDirectory()) {

                    connectionState.setCurrentDir(newcur.getAbsolutePath());
                    return;
                }
                throw new FtpFileNotExistException();
            }

        };
        final FtpServer server = new FtpServer(handler, 8080);
        server.start();
    }

}
