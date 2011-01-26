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
        RETR(1, -1),
        TYPE(1),
        LIST(0, 1),
        CDUP(0),
        CWD(1, -1),
        PWD(0),
        NOOP(0),
        PASS(1),
        QUIT(0),
        SYST(0),
        PORT(1),
        USER(1);

        private int paramSize;
        private int maxSize;

        private COMMAND(final int paramSize) {
            this(paramSize, paramSize);
        }

        private COMMAND(final int paramSize, final int maxSize) {
            this.paramSize = paramSize;
            this.maxSize = maxSize;
        }

        public boolean match(final int length) {
            if (length == paramSize) { return true; }
            if (length == maxSize) { return true; }
            if (maxSize == -1) { return true; }
            return false;
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

    @Override
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
            if (commandEnum != null) {
                if (!commandEnum.match(commandParts.length - 1)) { throw new FtpCommandSyntaxException(); }
                switch (commandEnum) {
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
                case PWD:
                    onPWD();
                    break;
                case CWD:
                    onCWD(commandParts);
                    break;
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
        } catch (final FtpCommandNotImplementedException e) {
            write(502, "Command not implemented");
        } catch (final FtpCommandSyntaxException e) {
            write(501, "Syntax error in parameters or arguments");
        } catch (final FtpBadSequenceException e) {
            write(503, "Bad sequence of commands");
        } catch (final StateConflictException e) {
            e.printStackTrace();
            write(503, "Bad sequence of commands");
        }
    }

    private void onCDUP() throws IOException {
        if (!stateMachine.isState(FtpConnection.LOGIN)) {
            write(530, "Not logged in");
        } else {
            try {

                ftpServer.getFtpCommandHandler().onDirectoryUp(connectionState);
                write(200, "Command okay.");
            } catch (final FtpFileNotExistException e) {
                write(550, "No such directory.");
            }
        }

    }

    private void onCWD(final String params[]) throws IOException {
        if (!stateMachine.isState(FtpConnection.LOGIN)) {
            write(530, "Not logged in");
        } else {
            try {
                String param = "";
                for (int index = 1; index < params.length; index++) {
                    if (param.length() > 0) {
                        param += " ";
                    }
                    param += params[index];
                }
                ftpServer.getFtpCommandHandler().setCurrentDirectory(connectionState, param);
                write(250, "\"" + connectionState.getCurrentDir() + "\" is cwd.");
            } catch (final FtpFileNotExistException e) {
                write(550, "No such directory.");

            }
        }
    }

    private void onLIST(final String params[]) throws IOException {
        if (!stateMachine.isState(FtpConnection.LOGIN)) {
            write(530, "Not logged in");
        } else {
            String file = null;
            if (params.length == 2) {
                file = params[1];
            }
            Socket socket = null;
            try {
                try {
                    socket = new Socket(passiveIP, passivePort);
                } catch (final IOException e) {
                    write(425, "Can't open data connection");
                    return;
                }
                write(150, "Opening XY mode data connection for file list");
                try {
                    final ArrayList<FtpFile> list = ftpServer.getFtpCommandHandler().getFileList(connectionState, file);

                    socket.getOutputStream().write(ftpServer.getFtpCommandHandler().getFilelistFormatter().format(list).getBytes("UTF-8"));
                    socket.getOutputStream().flush();
                } catch (final FtpFileNotExistException e) {
                    write(450, "Requested file action not taken; File unavailable");
                    return;
                } catch (final Exception e) {
                    write(451, "Requested action aborted: local error in processing");
                    return;
                }
                /* we close the passive port after command */
                write(226, "Transfer complete.");
            } finally {
                try {
                    socket.close();
                } catch (final Throwable e) {
                }
            }
        }

    }

    private void onNOOP() throws IOException {
        write(200, "Command okay");
    }

    private void onPASS(final String params[]) throws IOException {
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
                    write(530, "Not logged in");
                    stateMachine.setStatus(FtpConnection.LOGOUT);
                    stateMachine.setStatus(FtpConnection.IDLEEND);
                    stateMachine.reset();
                }
            } else {
                throw new RuntimeException("THIS MUST NOT HAPPEN!");
            }
        }
    }

    private void onPORT(final String params[]) throws IOException {
        if (!stateMachine.isState(FtpConnection.LOGIN)) {
            write(530, "Not logged in");
        } else {
            final String parts[] = params[1].split(",");
            if (parts.length != 6) { throw new FtpCommandSyntaxException(); }
            passiveIP = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
            passivePort = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
            write(200, "PORT command successful");
        }
    }

    private void onPWD() throws IOException {
        stateMachine.setStatus(FtpConnection.LOGIN);
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

    private void onTYPE(final String[] commandParts) throws IOException {
        if (!stateMachine.isState(FtpConnection.LOGIN)) {
            write(530, "Not logged in");
        } else {
            final String type = commandParts[1];
            if (type.equalsIgnoreCase("A")) {
                this.type = TYPE.ASCII;
            } else if (type.equalsIgnoreCase("I")) {
                this.type = TYPE.BINARY;
            } else {
                write(504, "Command not implemented for that parameter");
                return;
            }
            write(200, "Command okay");
        }
    }

    private void onRETR(final String[] commandParts) throws IOException {
        if (!stateMachine.isState(FtpConnection.LOGIN)) {
            write(530, "Not logged in");
        } else {
            Socket socket = null;
            try {
                try {
                    socket = new Socket(passiveIP, passivePort);
                } catch (final IOException e) {
                    write(425, "Can't open data connection");
                    return;
                }
                write(150, "Opening XY mode data connection for transfer");
                try {
                    String param = "";
                    for (int index = 1; index < commandParts.length; index++) {
                        if (param.length() > 0) {
                            param += " ";
                        }
                        param += commandParts[index];
                    }
                    ftpServer.getFtpCommandHandler().onRETR(socket.getOutputStream(), connectionState, param);
                    socket.getOutputStream().flush();
                } catch (final FtpFileNotExistException e) {
                    write(450, "Requested file action not taken; File unavailable");
                    return;
                } catch (final IOException e) {
                    write(426, "Requested action aborted: IOException");
                    return;
                } catch (final Exception e) {
                    write(451, "Requested action aborted: local error in processing");
                    return;
                }
                /* we close the passive port after command */
                write(226, "Transfer complete.");
            } finally {
                try {
                    socket.close();
                } catch (final Throwable e) {
                }
            }
        }

    }

    private void onUSER(final String params[]) throws IOException {
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
            write(530, "Not logged in");
            stateMachine.setStatus(FtpConnection.LOGOUT);
            stateMachine.setStatus(FtpConnection.IDLEEND);
            stateMachine.reset();
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
