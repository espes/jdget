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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @author daniel
 * 
 */
public abstract class FtpConnectionHandler<E extends FtpFile> {

    /**
     * @return
     */
    public FtpConnectionState createNewConnectionState() {

        return new FtpConnectionState();
    }

    public String formatFileList(final ArrayList<? extends FtpFile> list) {
        final String DEL = " ";
        final StringBuilder sb = new StringBuilder();
        final SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        for (final FtpFile f : list) {
            // directory or not
            sb.append(f.isDirectory() ? "d" : "-");
            // rights
            sb.append("rwxrwxrwx");
            sb.append(DEL);
            sb.append("0");
            sb.append(DEL);
            // group
            sb.append(f.getGroup());
            sb.append(DEL);
            // user
            sb.append(f.getOwner());
            sb.append(DEL);
            sb.append(f.getSize());
            sb.append(DEL);
            sb.append(df.format(new Date(f.getLastModified())));
            sb.append(DEL);
            sb.append(f.getName());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * @param string
     * @return TODO
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public abstract ArrayList<E> getFileList(FtpConnectionState connectionState, String string) throws UnsupportedEncodingException, IOException, FtpException;

    /**
     * @param connectionState
     * @param buildParameter
     * @return
     */
    public abstract long getSize(FtpConnectionState connectionState, String buildParameter) throws FtpException;

    public abstract FTPUser getUser(final String user);

    public abstract String getWelcomeMessage(FtpConnectionState ftpConnection);

    /**
     * @param connectionState
     * @param buildParameter
     */
    public abstract void makeDirectory(FtpConnectionState connectionState, String buildParameter) throws FtpException;

    /**
     * @param dir
     * @param connectionState
     * @return
     */
    public abstract void onDirectoryUp(FtpConnectionState connectionState) throws FtpException;

    public abstract String onLoginFailedMessage(FtpConnectionState ftpConnection) throws FtpException;

    public abstract String onLoginSuccessRequest(FtpConnectionState connectionState) throws FtpException;

    public abstract String onLogoutRequest(FtpConnectionState connectionState) throws FtpException;

    /**
     * @param outputStream
     * @param connectionState
     * @param param
     */
    public abstract long onRETR(OutputStream outputStream, FtpConnectionState connectionState, String param) throws IOException, FtpException;

    /**
     * @param inputStream
     * @param connectionState
     * @param buildParameter
     * @return
     * @throws FtpFileNotExistException
     * @throws IOException
     */
    public abstract long onSTOR(InputStream inputStream, FtpConnectionState connectionState, boolean append, String buildParameter) throws FtpException, IOException;

    /**
     * @param connectionState
     * @param buildParameter
     * @throws FtpFileNotExistException
     * @throws FtpException
     */
    public abstract void removeDirectory(FtpConnectionState connectionState, String buildParameter) throws FtpException;

    /**
     * @param connectionState
     * @param buildParameter
     */
    public abstract void removeFile(FtpConnectionState connectionState, String buildParameter) throws FtpException;

    /**
     * @param connectionState
     * @param buildParameter
     */
    /*
     * this function gets called twice:
     * 
     * 1.) by RNFR, which should set the renameFile in connectionState if
     * renaming is possible
     * 
     * 2.) by RNTO, which should do the actual renaming
     */
    public abstract void renameFile(FtpConnectionState connectionState, String buildParameter) throws FtpException;

    /**
     * @param connectionState
     * @param cwd
     * @return
     */
    public abstract void setCurrentDirectory(FtpConnectionState connectionState, String cwd) throws FtpException;

}
