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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.appwork.controlling.State;
import org.appwork.controlling.StateConflictException;
import org.appwork.controlling.StateMachine;
import org.appwork.controlling.StateMachineInterface;

/**
 * @author daniel
 * 
 */
public class FtpConnection implements Runnable, StateMachineInterface {

    public static enum COMMAND {
        /* commands starting with X are experimental, see RFC1123 */
        RNTO(true, 1, -1),
        RNFR(true, 1, -1),
        DELE(true, 1, -1),
        XRMD(true, 1, -1),
        RMD(true, 1, -1),
        SIZE(true, 1, -1), /* rfc3659 */
        STRU(true, 1),
        MODE(true, 1),
        ALLO(true, 1, -1),
        APPE(true, 1, -1),
        STOR(true, 1, -1),
        XMKD(true, 1, -1),
        MKD(true, 1, -1),
        NLST(true, 1, -1),
        EPRT(true, 1, 1),
        RETR(true, 1, -1),
        TYPE(true, 1, 2),
        LIST(true, 0, 1),
        XCUP(true, 0),
        CDUP(true, 0),
        XCWD(true, 1, -1),
        CWD(true, 1, -1),
        XPWD(true, 0),
        PWD(true, 0),
        NOOP(false, 0),
        PASS(false, 1),
        QUIT(true, 0),
        SYST(true, 0),
        PORT(true, 1),
        USER(false, 1);

        private int     paramSize;
        private int     maxSize;
        private boolean needLogin;

        private COMMAND(boolean needLogin, final int paramSize) {
            this(needLogin, paramSize, paramSize);
        }

        private COMMAND(final boolean needLogin, final int paramSize, final int maxSize) {
            this.paramSize = paramSize;
            this.needLogin = needLogin;
            this.maxSize = maxSize;
        }

        public boolean match(final int length) {
            if (length == paramSize) { return true; }
            if (length == maxSize) { return true; }
            if (maxSize == -1) { return true; }
            return false;
        }

        public boolean needsLogin() {
            return needLogin;
        }
    }

    private static enum TYPE {
        ASCII,
        BINARY;
    }

    private static final State       IDLE         = new State("IDLE");
    private static final State       USER         = new State("USER");
    private static final State       PASS         = new State("USER");
    private static final State       LOGIN        = new State("USER");
    private static final State       LOGOUT       = new State("LOGOUT");
    private static final State       IDLEEND      = new State("IDLEEND");
    static {
        FtpConnection.IDLE.addChildren(FtpConnection.USER);
        FtpConnection.USER.addChildren(FtpConnection.PASS, FtpConnection.LOGIN, FtpConnection.LOGOUT);
        FtpConnection.PASS.addChildren(FtpConnection.LOGIN, FtpConnection.LOGOUT);
        FtpConnection.LOGIN.addChildren(FtpConnection.LOGOUT);
        FtpConnection.LOGOUT.addChildren(FtpConnection.IDLEEND);
    }

    private final FtpServer          ftpServer;
    private final Socket             controlSocket;
    private BufferedReader           reader;
    private BufferedWriter           writer;

    private StateMachine             stateMachine = null;

    private Thread                   thread       = null;

    private String                   passiveIP    = null;
    private int                      passivePort  = 0;
    private TYPE                     type         = TYPE.BINARY;
    private final FtpConnectionState connectionState;
    private Socket                   dataSocket   = null;

    /**
     * @param ftpServer
     * @param clientSocket
     * @throws IOException
     */
    public FtpConnection(final FtpServer ftpServer, final Socket clientSocket) throws IOException {
        stateMachine = new StateMachine(this, FtpConnection.IDLE, FtpConnection.IDLEEND);
        connectionState = ftpServer.getFtpCommandHandler().createNewConnectionState();
        this.ftpServer = ftpServer;
        controlSocket = clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
            thread = new Thread(this);
            thread.setName("FTPConnection " + this);
            thread.start();
        } catch (final IOException e) {
            try {
                controlSocket.close();
            } catch (final Throwable e2) {
            }
            throw e;
        }
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    /**
     * @param command
     * @throws IOException
     */
    private void handleCommand(final String command) throws IOException {
        try {
            final String commandParts[] = command.split(" ");
            COMMAND commandEnum = null;
            try {
                commandEnum = COMMAND.valueOf(commandParts[0]);
            } catch (final IllegalArgumentException e) {
                commandEnum = null;
            }
            try {
                if (commandEnum != null) {
                    if (commandEnum.needLogin) {
                        /* checks if this command needs valid login */
                        if (!stateMachine.isState(FtpConnection.LOGIN)) { throw new FtpNotLoginException(); }
                    }
                    if (!commandEnum.match(commandParts.length - 1)) {
                        /* checks if the parameter syntax is okay */
                        throw new FtpCommandSyntaxException();
                    }
                    /* this checks RNFR,RNTO command sequence */
                    if (connectionState.getRenameFile() != null && !commandEnum.equals(COMMAND.RNTO)) {
                        /* when renameFile is set, a RNTO command MUST follow */
                        connectionState.setRenameFile(null);
                        throw new FtpBadSequenceException();
                    }
                    switch (commandEnum) {
                    case RNTO:
                        onRNTO(commandParts);
                        break;
                    case RNFR:
                        onRNFR(commandParts);
                        break;
                    case XRMD:
                    case RMD:
                        onRMD(commandParts);
                        break;
                    case DELE:
                        onDELE(commandParts);
                        break;
                    case SIZE:
                        onSIZE(commandParts);
                        break;
                    case STRU:
                        onSTRU(commandParts);
                        break;
                    case MODE:
                        onMODE(commandParts);
                        break;
                    case ALLO:
                        onALLO();
                        break;
                    case APPE:
                        onSTOR(commandParts, true);
                        break;
                    case STOR:
                        onSTOR(commandParts, false);
                        break;
                    case XMKD:
                    case MKD:
                        onMKD(commandParts);
                        break;
                    case NLST:
                        onNLST(commandParts);
                        break;
                    case EPRT:
                        onEPRT(commandParts);
                        break;
                    case RETR:
                        onRETR(commandParts);
                        break;
                    case LIST:
                        onLIST(commandParts);
                        break;
                    case USER:
                        onUSER(commandParts);
                        break;
                    case PORT:
                        onPORT(commandParts);
                        break;
                    case SYST:
                        onSYST();
                        break;
                    case QUIT:
                        onQUIT();
                        break;
                    case PASS:
                        onPASS(commandParts);
                        break;
                    case NOOP:
                        onNOOP();
                        break;
                    case XPWD:
                    case PWD:
                        onPWD();
                        break;
                    case XCWD:
                    case CWD:
                        onCWD(commandParts);
                        break;
                    case XCUP:
                    case CDUP:
                        onCDUP();
                        break;
                    case TYPE:
                        onTYPE(commandParts);
                        break;
                    }
                } else {
                    throw new FtpCommandNotImplementedException();
                }
            } catch (final StateConflictException e) {
                throw new FtpBadSequenceException();
            }
        } catch (final FtpException e) {
            write(e.getCode(), e.getMessage());
        }
    }

    /**
     * @param commandParts
     * @throws FtpBadSequenceException
     * @throws FtpFileNotExistException 
     * @throws IOException 
     */
    private void onRNTO(String[] commandParts) throws FtpBadSequenceException, FtpFileNotExistException, IOException {
        if (connectionState.getRenameFile() == null) {
            /* a renameFile must exist, RNFR must be the command before RNTO */
            throw new FtpBadSequenceException();
        }
        ftpServer.getFtpCommandHandler().renameFile(connectionState, buildParameter(commandParts));
        write(250, "\"" + buildParameter(commandParts) + "\" rename successfull.");
    }

    /**
     * @param commandParts
     * @throws FtpBadSequenceException
     * @throws FtpFileNotExistException 
     * @throws IOException 
     */
    private void onRNFR(String[] commandParts) throws FtpBadSequenceException, FtpFileNotExistException, IOException {
        if (connectionState.getRenameFile() != null) {
            connectionState.setRenameFile(null);
            throw new FtpBadSequenceException();
        }
        ftpServer.getFtpCommandHandler().renameFile(connectionState, buildParameter(commandParts));
        write(350, "\"" + buildParameter(commandParts) + "\" rename pending.");
    }

    /**
     * @param commandParts
     * @throws FtpException
     * @throws FtpFileNotExistException
     * @throws IOException
     */
    private void onDELE(String[] commandParts) throws FtpFileNotExistException, FtpException, IOException {
        ftpServer.getFtpCommandHandler().removeFile(connectionState, buildParameter(commandParts));
        write(250, "\"" + buildParameter(commandParts) + "\" removed.");
    }

    /**
     * @param commandParts
     * @throws IOException
     * @throws FtpException
     * @throws FtpFileNotExistException
     */
    private void onRMD(String[] commandParts) throws IOException, FtpFileNotExistException, FtpException {
        ftpServer.getFtpCommandHandler().removeDirectory(connectionState, buildParameter(commandParts));
        write(250, "\"" + buildParameter(commandParts) + "\" removed.");
    }

    /**
     * @param commandParts
     * @throws IOException
     * @throws FtpFileNotExistException
     */
    private void onSIZE(String[] commandParts) throws FtpFileNotExistException, IOException {
        write(213, "" + ftpServer.getFtpCommandHandler().getSize(connectionState, buildParameter(commandParts)));
    }

    private void onSTRU(String[] commandParts) throws IOException, FtpCommandParameterException {
        if ("F".equalsIgnoreCase(commandParts[1])) {
            write(200, "Command okay.");
        } else {
            throw new FtpCommandParameterException();
        }
    }

    private void onMODE(String[] commandParts) throws IOException, FtpCommandParameterException {
        if ("S".equalsIgnoreCase(commandParts[1])) {
            write(200, "Command okay.");
        } else {
            throw new FtpCommandParameterException();
        }
    }

    /**
     * @param commandParts
     * @throws IOException
     * @throws FtpException
     */
    private void onNLST(String[] commandParts) throws IOException, FtpException {
        try {
            try {
                if (dataSocket == null || !dataSocket.isConnected()) {
                    dataSocket = new Socket(passiveIP, passivePort);
                }
            } catch (final IOException e) {
                throw new FtpException(425, "Can't open data connection");
            }
            write(150, "Opening XY mode data connection for file list");
            try {
                final ArrayList<? extends FtpFile> list = ftpServer.getFtpCommandHandler().getFileList(connectionState, buildParameter(commandParts));
                StringBuilder sb = new StringBuilder();
                for (FtpFile file : list) {
                    sb.append(file.getName());
                    sb.append("\r\n");
                }
                dataSocket.getOutputStream().write(sb.toString().getBytes("UTF-8"));
                dataSocket.getOutputStream().flush();
            } catch (final FtpFileNotExistException e) {
                /* need another error code here */
                throw new FtpException(450, "Requested file action not taken; File unavailable");
            } catch (final Exception e) {
                throw new FtpException(451, "Requested action aborted: local error in processing");
            }
            /* we close the passive port after command */
            write(226, "Transfer complete.");
        } finally {
            try {
                dataSocket.close();
            } catch (final Throwable e) {
            } finally {
                dataSocket = null;
            }
        }
    }

    /**
     * @param commandParts
     * @throws IOException
     */
    /**
     * RFC2428
     * 
     * @throws FtpException
     * 
     * @throws FtpNotLoginException
     **/
    private void onEPRT(String[] commandParts) throws IOException, FtpException {
        final String parts[] = commandParts[1].split("\\|");
        try {
            /* close old maybe existing data connection */
            dataSocket.close();
        } catch (final Throwable e) {
        } finally {
            dataSocket = null;
        }
        if (parts.length != 4) { throw new FtpCommandSyntaxException(); }
        if (!"1".equals(parts[1])) {
            /* 2 equals IPV6 */
            throw new FtpException(522, "Network protocol not supported, use (1)");
        }
        passiveIP = parts[2];
        passivePort = Integer.parseInt(parts[3]);
        write(200, "PORT command successful");
    }

    private void onCDUP() throws IOException, FtpFileNotExistException {
        ftpServer.getFtpCommandHandler().onDirectoryUp(connectionState);
        write(200, "Command okay.");
    }

    private void onCWD(final String params[]) throws IOException, FtpFileNotExistException {
        ftpServer.getFtpCommandHandler().setCurrentDirectory(connectionState, buildParameter(params));
        write(250, "\"" + connectionState.getCurrentDir() + "\" is cwd.");
    }

    /**
     * @param commandParts
     * @throws IOException
     * @throws FtpFileNotExistException
     */
    private void onMKD(String[] commandParts) throws IOException, FtpFileNotExistException {
        ftpServer.getFtpCommandHandler().makeDirectory(connectionState, buildParameter(commandParts));
        write(257, "\"" + buildParameter(commandParts) + "\" created.");
    }

    private void onLIST(final String params[]) throws IOException, FtpException {
        try {
            try {
                if (dataSocket == null || !dataSocket.isConnected()) {
                    dataSocket = new Socket(passiveIP, passivePort);
                }
            } catch (final IOException e) {
                throw new FtpException(425, "Can't open data connection");
            }
            write(150, "Opening XY mode data connection for file list");
            try {
                final ArrayList<? extends FtpFile> list = ftpServer.getFtpCommandHandler().getFileList(connectionState, buildParameter(params));
                dataSocket.getOutputStream().write(ftpServer.getFtpCommandHandler().formatFileList(list).getBytes("UTF-8"));
                dataSocket.getOutputStream().flush();
            } catch (final FtpFileNotExistException e) {
                /* need another error code here */
                throw new FtpException(450, "Requested file action not taken; File unavailable");
            } catch (final Exception e) {
                throw new FtpException(451, "Requested action aborted: local error in processing");
            }
            /* we close the passive port after command */
            write(226, "Transfer complete.");
        } finally {
            try {
                dataSocket.close();
            } catch (final Throwable e) {
            } finally {
                dataSocket = null;
            }
        }
    }

    private void onNOOP() throws IOException {
        write(200, "Command okay");
    }

    private void onALLO() throws IOException {
        write(200, "Command okay");
    }

    private void onPASS(final String params[]) throws IOException, FtpBadSequenceException, FtpNotLoginException {
        stateMachine.setStatus(FtpConnection.PASS);
        if (connectionState.getUser() == null) {
            throw new FtpBadSequenceException();
        } else {
            if (connectionState.getUser().getPassword() != null) {
                if (connectionState.getUser().getPassword().equals(params[1])) {
                    final String message = ftpServer.getFtpCommandHandler().onLoginSuccessRequest(connectionState);
                    if (message != null) {
                        write(230, message, true);
                    }
                    write(230, "User logged in, proceed");
                    stateMachine.setStatus(FtpConnection.LOGIN);
                } else {
                    final String message = ftpServer.getFtpCommandHandler().onLoginFailedMessage(connectionState);
                    if (message != null) {
                        write(530, message, true);
                    }
                    stateMachine.setStatus(FtpConnection.LOGOUT);
                    stateMachine.setStatus(FtpConnection.IDLEEND);
                    stateMachine.reset();
                    throw new FtpNotLoginException();
                }
            } else {
                throw new RuntimeException("THIS MUST NOT HAPPEN!");
            }
        }
    }

    private void onPORT(final String params[]) throws IOException, FtpCommandSyntaxException {
        try {
            /* close old maybe existing data connection */
            dataSocket.close();
        } catch (final Throwable e) {
        } finally {
            dataSocket = null;
        }
        final String parts[] = params[1].split(",");
        if (parts.length != 6) { throw new FtpCommandSyntaxException(); }
        passiveIP = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        passivePort = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
        write(200, "PORT command successful");
    }

    private void onPWD() throws IOException {
        write(257, "\"" + connectionState.getCurrentDir() + "\" is cwd.");
    }

    private void onQUIT() throws IOException {
        stateMachine.setStatus(FtpConnection.LOGOUT);
        write(221, ftpServer.getFtpCommandHandler().onLogoutRequest(connectionState));
        stateMachine.setStatus(FtpConnection.IDLEEND);
    }

    private void onSYST() throws IOException {
        write(215, "UNIX Type: L8");
    }

    private void onTYPE(final String[] commandParts) throws IOException, FtpCommandParameterException {
        final String type = commandParts[1];
        if ("A".equalsIgnoreCase(type)) {
            this.type = TYPE.ASCII;
        } else if ("I".equalsIgnoreCase(type)) {
            this.type = TYPE.BINARY;
        } else if ("L".equalsIgnoreCase(type)) {
            if (commandParts.length == 3 && "8".equals(commandParts[2])) {
                this.type = TYPE.BINARY;
            } else {
                throw new FtpCommandParameterException();
            }
        } else {
            throw new FtpCommandParameterException();
        }
        write(200, "Command okay");
    }

    private String buildParameter(final String[] commandParts) {
        if (commandParts == null) return null;
        String param = "";
        for (int index = 1; index < commandParts.length; index++) {
            if (param.length() > 0) {
                param += " ";
            }
            param += commandParts[index];
        }
        return param;
    }

    private void onRETR(final String[] commandParts) throws IOException, FtpException {
        try {
            try {
                if (dataSocket == null || !dataSocket.isConnected()) {
                    dataSocket = new Socket(passiveIP, passivePort);
                }
            } catch (final IOException e) {
                throw new FtpException(425, "Can't open data connection");
            }
            write(150, "Opening XY mode data connection for transfer");
            long bytesWritten = 0;
            try {
                bytesWritten = ftpServer.getFtpCommandHandler().onRETR(dataSocket.getOutputStream(), connectionState, buildParameter(commandParts));
                dataSocket.getOutputStream().flush();
            } catch (final FtpFileNotExistException e) {
                /* need another error code here */
                throw new FtpException(450, "Requested file action not taken; File unavailable");
            } catch (final IOException e) {
                throw new FtpException(426, "Requested action aborted: IOException");
            } catch (final Exception e) {
                throw new FtpException(451, "Requested action aborted: local error in processing");
            }
            /* we close the passive port after command */
            write(226, "Transfer complete. " + bytesWritten + " bytes transfered!");
        } finally {
            try {
                dataSocket.close();
            } catch (final Throwable e) {
            } finally {
                dataSocket = null;
            }
        }

    }

    private void onSTOR(final String[] commandParts, boolean append) throws IOException, FtpException {
        try {
            try {
                if (dataSocket == null || !dataSocket.isConnected()) {
                    dataSocket = new Socket(passiveIP, passivePort);
                }
            } catch (final IOException e) {
                throw new FtpException(425, "Can't open data connection");
            }
            write(150, "Opening XY mode data connection for transfer");
            long bytesRead = 0;
            try {
                bytesRead = ftpServer.getFtpCommandHandler().onSTOR(dataSocket.getInputStream(), connectionState, append, buildParameter(commandParts));
            } catch (final FtpFileNotExistException e) {
                /* need another error code here */
                throw new FtpException(450, "Requested file action not taken; File unavailable");
            } catch (final IOException e) {
                throw new FtpException(426, "Requested action aborted: IOException");
            } catch (final Exception e) {
                throw new FtpException(451, "Requested action aborted: local error in processing");
            }
            /* we close the passive port after command */
            write(226, "Transfer complete. " + bytesRead + " bytes received!");
        } finally {
            try {
                dataSocket.close();
            } catch (final Throwable e) {
            } finally {
                dataSocket = null;
            }
        }
    }

    private void onUSER(final String params[]) throws IOException, FtpNotLoginException {
        if (stateMachine.isFinal()) {
            stateMachine.reset();
        }
        stateMachine.setStatus(FtpConnection.USER);
        connectionState.setUser(ftpServer.getFtpCommandHandler().getUser(params[1]));
        if (connectionState.getUser() != null) {
            if (connectionState.getUser().getPassword() == null) {
                final String message = ftpServer.getFtpCommandHandler().onLoginSuccessRequest(connectionState);
                if (message != null) {
                    write(230, message, true);
                }
                write(230, "User logged in, proceed");
                stateMachine.setStatus(FtpConnection.LOGIN);
            } else {
                write(331, "User name okay, need password");
            }
        } else {
            final String message = ftpServer.getFtpCommandHandler().onLoginFailedMessage(connectionState);
            if (message != null) {
                write(530, message, true);
            }
            stateMachine.setStatus(FtpConnection.LOGOUT);
            stateMachine.setStatus(FtpConnection.IDLEEND);
            stateMachine.reset();
            throw new FtpNotLoginException();
        }
    }

    public void run() {
        try {
            write(220, ftpServer.getFtpCommandHandler().getWelcomeMessage(connectionState));
            while (true) {
                final String command = reader.readLine();
                if (command == null) {
                    break;
                }
                System.out.println(command);
                handleCommand(command);
            }
        } catch (final IOException e) {
        }
    }

    private void write(final int code, final String message) throws IOException {
        write(code, message, false);
    }

    private void write(final int code, final String message, final boolean multiLine) throws IOException {
        if (multiLine) {
            writer.write(code + "-" + message + "\r\n");
        } else {
            writer.write(code + " " + message + "\r\n");
        }
        writer.flush();
    }
}
