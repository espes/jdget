//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

/* 
 Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

 This file is part of SimpleFTP.

 This software is dual-licensed, allowing you to choose between the GNU
 General Public License (GPL) and the www.jibble.org Commercial License.
 Since the GPL may be too restrictive for use in a proprietary application,
 a commercial license is also provided. Full license information can be
 found at http://www.jibble.org/licenses/

 $Author: pjm2 $
 $Id: SimpleFTP.java,v 1.2 2004/05/29 19:27:37 pjm2 Exp $

 */
package jd.nutils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.rmi.ConnectException;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import jd.controlling.authentication.AuthenticationController;

import org.appwork.utils.Regex;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.logging.LogController;

/**
 * SimpleFTP is a simple package that implements a Java FTP client. With SimpleFTP, you can connect to an FTP server and upload multiple files.
 * 
 * Based on Work of Paul Mutton http://www.jibble.org/
 */
public class SimpleFTP {
    private static final int TIMEOUT            = 20 * 1000;
    private boolean          binarymode         = false;
    private BufferedReader   reader             = null;
    private Socket           socket             = null;
    private BufferedWriter   writer             = null;
    private String           dir                = "/";
    private String           host;
    private Logger           logger             = LogController.CL();
    private String           latestResponseLine = null;
    
    public Logger getLogger() {
        return logger;
    }
    
    public void setLogger(Logger logger) {
        if (logger == null) logger = LogController.CL();
        this.logger = logger;
    }
    
    /**
     * Create an instance of SimpleFTP.
     */
    public SimpleFTP() {
    }
    
    /**
     * Enter ASCII mode for sending text files. This is usually the default mode. Make sure you use binary mode if you are sending images or other binary data,
     * as ASCII mode is likely to corrupt them.
     */
    public boolean ascii() throws IOException {
        sendLine("TYPE A");
        try {
            readLines(new int[] { 200 }, "could not enter ascii mode");
            if (binarymode) binarymode = false;
            return true;
        } catch (IOException e) {
            LogController.CL().log(e);
            if (e.getMessage().contains("ascii")) { return false; }
            throw e;
        }
    }
    
    /**
     * Enter binary mode for sending binary files.
     */
    public boolean bin() throws IOException {
        sendLine("TYPE I");
        try {
            readLines(new int[] { 200 }, "could not enter binary mode");
            if (!binarymode) binarymode = true;
            return true;
        } catch (IOException e) {
            LogController.CL().log(e);
            if (e.getMessage().contains("binary")) { return false; }
            throw e;
        }
    }
    
    /**
     * returns current value of 'binarymode'.
     * 
     * @since JD2
     * */
    public boolean isBinary() {
        if (this.binarymode)
            return true;
        else
            return false;
    }
    
    /**
     * Connects to the default port of an FTP server and logs in as anonymous/anonymous.
     */
    public void connect(String host) throws IOException {
        connect(host, 21);
    }
    
    /**
     * Connects to an FTP server and logs in as anonymous/anonymous.
     */
    public void connect(String host, int port) throws IOException {
        connect(host, port, "anonymous", "anonymous");
    }
    
    /**
     * Connects to an FTP server and logs in with the supplied username and password.
     */
    public void connect(String host, int port, String user, String pass) throws IOException {
        if (socket != null) { throw new IOException("SimpleFTP is already connected. Disconnect first."); }
        socket = new Socket(host, port);
        this.host = host;
        socket.setSoTimeout(TIMEOUT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String response = readLines(new int[] { 220 }, "SimpleFTP received an unknown response when connecting to the FTP server: ");
        sendLine("USER " + user);
        response = readLines(new int[] { 230, 331 }, "SimpleFTP received an unknown response after sending the user: ");
        sendLine("PASS " + pass);
        response = readLines(new int[] { 230 }, "SimpleFTP was unable to log in with the supplied password: ");
        sendLine("PWD");
        while ((response = readLine()).startsWith("230") || response.charAt(0) >= '9' || response.charAt(0) <= '0') {
            
        }
        //
        if (!response.startsWith("257 ")) { throw new IOException("PWD COmmand not understood " + response); }
        
        // Response: 257 "/" is the current directory
        dir = new Regex(response, "\"(.*)\"").getMatch(0);
        // dir = dir;
        // Now logged in.
    }
    
    /**
     * Changes the working directory (like cd). Returns true if successful.RELATIVE!!!
     */
    public boolean cwd(String dir) throws IOException {
        dir = dir.replaceAll("[\\\\|//]+?", "/");
        if (dir.equals(this.dir)) return true;
        sendLine("CWD " + dir);
        try {
            readLines(new int[] { 250 }, "SimpleFTP was unable to change directory");
            if (!dir.endsWith("/") && !dir.endsWith("\\")) dir += "/";
            if (dir.startsWith("/")) {
                this.dir = dir;
            } else {
                this.dir += dir;
            }
            return true;
        } catch (IOException e) {
            LogController.CL().log(e);
            if (e.getMessage().contains("was unable to change")) { return false; }
            throw e;
        }
    }
    
    /**
     * Disconnects from the FTP server.
     */
    public void disconnect() throws IOException {
        Socket lsocket = socket;
        try {
            /* avoid stackoverflow for io-exception during sendLine */
            socket = null;
            if (lsocket != null) sendLine("QUIT");
        } finally {
            try {
                lsocket.close();
            } catch (final Throwable e) {
            }
        }
    }
    
    /**
     * Returns the working directory of the FTP server it is connected to.
     */
    public String pwd() throws IOException {
        sendLine("PWD");
        String dir = null;
        String response = readLines(new int[] { 257 }, null);
        if (response.startsWith("257 ")) {
            int firstQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', firstQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(firstQuote + 1, secondQuote);
            }
        }
        return dir;
    }
    
    public String readLine() throws IOException {
        String line = reader.readLine();
        logger.info(host + " < " + line);
        return line;
    }
    
    public boolean wasLatestOperationNotPermitted() {
        String latest = getLastestResponseLine();
        if (latest != null) return latest.toLowerCase(Locale.ENGLISH).contains("operation not permitted");
        return false;
    }
    
    public String getLastestResponseLine() {
        return latestResponseLine;
    }
    
    /* read response and check if it matches expectcode */
    public String readLines(int expectcodes[], String errormsg) throws IOException {
        StringBuilder sb = new StringBuilder();
        String response = null;
        boolean multilineResponse = false;
        boolean error = true;
        int endCodeMultiLine = 0;
        while (true) {
            response = readLine();
            latestResponseLine = response;
            if (response == null) {
                if (sb.length() == 0) throw new IOException("no response received, EOF?");
                return sb.toString();
            }
            sb.append(response + "\r\n");
            error = true;
            for (int expectcode : expectcodes) {
                if (response.startsWith("" + expectcode + "-")) {
                    /* multiline response, RFC 640 */
                    endCodeMultiLine = expectcode;
                    error = false;
                    multilineResponse = true;
                    break;
                }
                if (multilineResponse == true && response.startsWith("" + endCodeMultiLine + " ")) {
                    /* end of response of multiline */
                    return sb.toString();
                }
                if (multilineResponse == false && response.startsWith("" + expectcode + " ")) {
                    /* end of response */
                    return sb.toString();
                }
                if (response.startsWith("" + expectcode)) {
                    error = false;
                    break;
                }
            }
            if (error && !multilineResponse) throw new IOException((errormsg != null ? errormsg : "revieved unexpected responsecode ") + sb.toString());
        }
    }
    
    public boolean remove(String string) throws IOException {
        sendLine("DELE " + string);
        try {
            readLines(new int[] { 250 }, "could not remove file");
            return true;
        } catch (IOException e) {
            LogController.CL().log(e);
            if (e.getMessage().contains("could not remove file")) { return false; }
            throw e;
        }
    }
    
    public boolean rename(String from, String to) throws IOException {
        sendLine("RNFR " + from);
        try {
            readLines(new int[] { 350 }, "RNFR failed");
        } catch (IOException e) {
            LogSource.exception(logger, e);
            if (e.getMessage().contains("RNFR")) { return false; }
        }
        sendLine("RNTO " + to);
        try {
            readLines(new int[] { 250 }, "RNTO failed");
        } catch (IOException e) {
            LogSource.exception(logger, e);
            if (e.getMessage().contains("RNTO")) { return false; }
        }
        return true;
    }
    
    public long getSize(String file) throws IOException {
        sendLine("SIZE " + file);
        String Size = null;
        try {
            Size = readLines(new int[] { 213 }, "SIZE failed");
        } catch (IOException e) {
            LogSource.exception(logger, e);
            if (e.getMessage().contains("SIZE") || e.getMessage().contains("550")) { return -1; }
        }
        String[] split = Size.split(" ");
        return Long.parseLong(split[1].trim());
    }
    
    /**
     * Sends a raw command to the FTP server.
     */
    public void sendLine(String line) throws IOException {
        try {
            logger.info(host + " > " + line);
            writer.write(line + "\r\n");
            writer.flush();
        } catch (IOException e) {
            LogSource.exception(logger, e);
            if (socket != null) disconnect();
            throw e;
        }
    }
    
    /**
     * Sends a file to be stored on the FTP server. Returns true if the file transfer was successful. The file is sent in passive mode to avoid NAT or firewall
     * problems at the client end.
     */
    public boolean stor(File file) throws IOException {
        if (file.isDirectory()) { throw new IOException("SimpleFTP cannot upload a directory."); }
        String filename = file.getName();
        return stor(new FileInputStream(file), filename);
    }
    
    /**
     * Sends a file to be stored on the FTP server. Returns true if the file transfer was successful. The file is sent in passive mode to avoid NAT or firewall
     * problems at the client end.
     */
    public boolean stor(InputStream input, String filename) throws IOException {
        Socket dataSocket = null;
        BufferedOutputStream output = null;
        try {
            InetSocketAddress pasv = pasv();
            sendLine("STOR " + filename);
            String response = null;
            try {
                dataSocket = new Socket(pasv.getHostName(), pasv.getPort());
                response = readLine();
                if (!response.startsWith("150 ") && !response.startsWith("125 ")) { throw new IOException("SimpleFTP was not allowed to send the file: " + response); }
                output = new BufferedOutputStream(dataSocket.getOutputStream());
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                input.close();
            } finally {
                try {
                    output.flush();
                } catch (final Throwable e) {
                }
                try {
                    output.close();
                } catch (final Throwable e) {
                }
                this.shutDownSocket(dataSocket);
            }
            response = readLine();
            return response.startsWith("226 ");
        } catch (ConnectException e) {
            LogSource.exception(logger, e);
            cancelTransfer();
            return stor(input, filename);
        } catch (SocketException e) {
            LogSource.exception(logger, e);
            cancelTransfer();
            return stor(input, filename);
        } finally {
            input.close();
        }
        
    }
    
    public void cancelTransfer() {
        try {
            this.sendLine("ABOR");
            readLine();
        } catch (IOException e) {
            LogSource.exception(logger, e);
        }
    }
    
    public InetSocketAddress pasv() throws IOException {
        sendLine("PASV");
        String response = readLines(new int[] { 227 }, "SimpleFTP could not request passive mode:");
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
                return new InetSocketAddress(ip, port);
            } catch (Exception e) {
                LogSource.exception(logger, e);
                throw new IOException("SimpleFTP received bad data link information: " + response);
            }
        }
        throw new IOException("SimpleFTP received bad data link information: " + response);
    }
    
    /**
     * creates directories
     * 
     * @param cw
     * @return
     * @throws IOException
     */
    public boolean mkdir(String cw2) throws IOException {
        String tmp = this.dir;
        String cw = cw2;
        try {
            cw = cw.replace("\\", "/");
            
            String[] cwdirs = cw.split("[\\\\|/]{1}");
            String[] dirdirs = dir.split("[\\\\|/]{1}");
            int i;
            int length = 0;
            String root = "";
            for (i = 0; i < Math.min(cwdirs.length, dirdirs.length); i++) {
                if (cwdirs[i].equals(dirdirs[i])) {
                    length += cwdirs[i].length() + 1;
                    root += cwdirs[i] + "/";
                }
            }
            // cw=cw;
            cw = cw.substring(length);
            String[] dirs = cw.split("[\\\\|/]{1}");
            if (root.length() > 0) cwd(root);
            for (String d : dirs) {
                if (d == null || d.trim().length() == 0) {
                    cwd("/");
                    continue;
                }
                
                sendLine("MKD " + d);
                String response = readLine();
                if (!response.startsWith("257 ") && !response.startsWith("550 ")) {
                
                return false;
                
                }
                
                cwd(d);
            }
            return true;
        } finally {
            
            this.cwd(tmp);
        }
        
    }
    
    public boolean cwdAdd(String cw) throws IOException {
        if (cw.startsWith("/") || cw.startsWith("\\")) cw = cw.substring(1);
        return cwd(dir + cw);
        
    }
    
    public String getDir() {
        return dir;
    }
    
    public void download(String filename, File file, boolean restart) throws IOException {
        long resumePosition = 0;
        if (!binarymode) logger.info("Warning: Download in ASCII mode may fail!");
        InetSocketAddress pasv = pasv();
        if (restart) {
            resumePosition = file.length();
            if (resumePosition > 0) {
                sendLine("REST " + resumePosition);
                readLines(new int[] { 350 }, "Resume not supported");
            }
        }
        InputStream input = null;
        RandomAccessFile fos = null;
        Socket dataSocket = null;
        try {
            final long resumeAmount = resumePosition;
            dataSocket = new Socket();
            dataSocket.setSoTimeout(30 * 1000);
            dataSocket.connect(new InetSocketAddress(pasv.getHostName(), pasv.getPort()), 30 * 1000);
            sendLine("RETR " + filename);
            input = dataSocket.getInputStream();
            fos = new RandomAccessFile(file, "rw");
            if (resumePosition > 0) {
                /* in case we do resume, reposition the writepointer */
                fos.seek(resumePosition);
            }
            String response = readLines(new int[] { 150, 125 }, null);
            byte[] buffer = new byte[32767];
            int bytesRead = 0;
            long counter = resumePosition;
            while ((bytesRead = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    /* max 10 seks wait for buggy servers */
                    socket.setSoTimeout(TIMEOUT);
                    shutDownSocket(dataSocket);
                    input.close();
                    try {
                        response = readLine();
                    } catch (SocketTimeoutException e) {
                        LogSource.exception(logger, e);
                        response = "SocketTimeout because of buggy Server";
                    }
                    this.shutDownSocket(dataSocket);
                    input.close();
                    throw new InterruptedIOException();
                }
                counter += bytesRead;
                if (bytesRead > 0) fos.write(buffer, 0, bytesRead);
            }
            /* max 10 seks wait for buggy servers */
            socket.setSoTimeout(TIMEOUT);
            shutDownSocket(dataSocket);
            input.close();
            try {
                response = readLine();
            } catch (SocketTimeoutException e) {
                LogSource.exception(logger, e);
                response = "SocketTimeout because of buggy Server";
            }
            if (!response.startsWith("226")) { throw new IOException("Download failed: " + response); }
        } catch (SocketTimeoutException e) {
            LogSource.exception(logger, e);
            sendLine("ABOR");
            readLine();
            download(filename, file);
            return;
        } catch (SocketException e) {
            LogSource.exception(logger, e);
            sendLine("ABOR");
            readLine();
            download(filename, file);
            return;
        } catch (ConnectException e) {
            LogSource.exception(logger, e);
            sendLine("ABOR");
            readLine();
            download(filename, file);
            return;
        } finally {
            try {
                input.close();
            } catch (Throwable e) {
            }
            try {
                fos.close();
            } catch (Throwable e) {
            }
            shutDownSocket(dataSocket);
        }
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public void download(String filename, File file) throws IOException {
        download(filename, file, false);
    }
    
    public void shutDownSocket(Socket dataSocket) {
        try {
            dataSocket.shutdownOutput();
        } catch (Throwable e) {
        }
        try {
            dataSocket.shutdownInput();
        } catch (Throwable e) {
        }
        try {
            dataSocket.close();
        } catch (Throwable e) {
        }
    }
    
    /**
     * returns permissionmask, ?, user?, group?, size?, date, name
     * 
     * @return
     * @throws IOException
     */
    public String[][] list() throws IOException {
        InetSocketAddress pasv = pasv();
        sendLine("LIST");
        Socket dataSocket = null;
        InputStreamReader input = null;
        StringBuilder sb = new StringBuilder();
        try {
            dataSocket = new Socket(pasv.getHostName(), pasv.getPort());
            input = new InputStreamReader(dataSocket.getInputStream(), "UTF8");
            readLines(new int[] { 125, 150 }, null);
            char[] buffer = new char[4096];
            int bytesRead = 0;
            while ((bytesRead = input.read(buffer)) != -1) {
                if (bytesRead > 0) sb.append(buffer, 0, bytesRead);
            }
        } finally {
            try {
                input.close();
            } catch (final Throwable e) {
            }
            shutDownSocket(dataSocket);
        }
        readLines(new int[] { 226 }, null);
        /* permission,type,user,group,size,date,filename */
        String[][] matches = new Regex(sb.toString(), "([-dxrw]+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+\\s+\\S+\\s+\\S+)\\s+(.*?)[$\r\n]+").getMatches();
        if (matches == null || matches.length == 0) {
            /* date,time,size,name */
            matches = new Regex(sb.toString(), "(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(.*?)[$\r\n]+").getMatches();
        }
        return matches;
    }
    
    /**
     * permissionmask, ?, user?, group?, size?, date, name
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public String[] getFileInfo(String path) throws IOException {
        String name = path.substring(path.lastIndexOf("/") + 1);
        path = path.substring(0, path.lastIndexOf("/"));
        if (!this.cwd(path)) return null;
        for (String[] file : list()) {
            if (file.length == 4 && file[3].equals(name)) return file;
            if (file.length == 7 && file[6].equals(name)) return file;
        }
        return null;
    }
    
    /**
     * COnnect to the url.does not change directory
     * 
     * @param url
     * @throws IOException
     */
    public void connect(URL url) throws IOException {
        String host = url.getHost();
        int port = url.getPort();
        if (port <= 0) port = 21;
        boolean autoTry = false;
        try {
            if (url.getUserInfo() != null) {
                String[] auth = url.getUserInfo().split(":");
                connect(host, port, auth[0], auth[1]);
            } else {
                String[] ret = AuthenticationController.getInstance().getLogins("ftp://" + url.getHost());
                if (ret != null) {
                    autoTry = true;
                    connect(host, port, ret[0], ret[1]);
                } else {
                    connect(host, port);
                }
            }
        } catch (IOException e) {
            if (e.getMessage().contains("was unable to log in with the supplied")) {
                disconnect();
                if (autoTry) {
                    connect(host, port);
                } else {
                    String[] ret = AuthenticationController.getInstance().getLogins("ftp://" + url.getHost());
                    if (ret == null) throw e;
                    connect(host, port, ret[0], ret[1]);
                }
                return;
            }
            throw e;
        }
    }
    
}
