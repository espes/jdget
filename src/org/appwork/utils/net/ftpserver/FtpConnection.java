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
        TYPE(1),
        LIST(0, 1),
        CDUP(0),
        CWD(1),
        PWD(0),
        NOOP(0),
        PASS(1),
        QUIT(0),
        SYST(0),
        PORT(1),
        USER(1);

        private int paramSize;
        private int maxSize;

        public int getParamSize() {
            return paramSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        private COMMAND(final int paramSize) {
            this(paramSize, -1);
        }

        private COMMAND(final int paramSize, final int maxSize) {
            this.paramSize = paramSize;
            this.maxSize = maxSize;
        }
    }

    private static enum TYPE {
        ASCII,
        BINARY;
    }

    private static final State IDLE         = new State("IDLE");
    private static final State USER         = new State("USER");
    private static final State PASS         = new State("USER");
    private static final State LOGIN        = new State("USER");
    private static final State LOGOUT       = new State("LOGOUT");
    private static final State IDLEEND      = new State("IDLEEND");
    static {
        IDLE.addChildren(USER);
        USER.addChildren(PASS, LOGIN, LOGOUT);
        PASS.addChildren(LOGIN, LOGOUT);
        LOGIN.addChildren(LOGOUT);
        LOGOUT.addChildren(IDLEEND);
    }

    private FtpServer          ftpServer;
    private Socket             controlSocket;
    private BufferedReader     reader;
    private BufferedWriter     writer;
    private FTPUser            user         = null;

    private StateMachine       stateMachine = null;

    private Thread             thread       = null;
    private String             passiveIP    = null;
    private int                passivePort  = 0;
    private TYPE               type         = TYPE.BINARY;

    /**
     * @param ftpServer
     * @param clientSocket
     * @throws IOException
     */
    public FtpConnection(FtpServer ftpServer, Socket clientSocket) throws IOException {
        this.stateMachine = new StateMachine(this, IDLE, IDLEEND);
        this.ftpServer = ftpServer;
        this.controlSocket = clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
            thread = new Thread(this);
            thread.setName("FTPConnection");
            thread.start();
        } catch (IOException e) {
            try {
                controlSocket.close();
            } catch (final Throwable e2) {
            }
            throw e;
        }
    }

    public void run() {
        try {
            write(220, ftpServer.getFtpCommandHandler().getWelcomeMessage());
            while (true) {
                String command = reader.readLine();
                if (command == null) break;
                System.out.println(command);
                handleCommand(command);
            }
        } catch (IOException e) {
        }
    }

    private void write(int code, String message) throws IOException {
        write(code, message, false);
    }

    private void write(int code, String message, boolean multiLine) throws IOException {
        if (multiLine) {
            writer.write(code + "-" + message + "\r\n");
        } else {
            writer.write(code + " " + message + "\r\n");
        }
        writer.flush();
    }

    /**
     * @param command
     * @throws IOException
     */
    private void handleCommand(String command) throws IOException {
        try {
            String commandParts[] = command.split(" ");
            COMMAND commandEnum = null;
            try {
                commandEnum = COMMAND.valueOf(commandParts[0]);
            } catch (IllegalArgumentException e) {
                commandEnum = null;
            }
            if (commandEnum != null) {
                if (commandEnum.paramSize != commandParts.length - 1 && commandEnum.maxSize != commandParts.length - 1) throw new FtpCommandSyntaxException();
                switch (commandEnum) {
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
        } catch (FtpCommandNotImplementedException e) {
            write(502, "Command not implemented");
        } catch (FtpCommandSyntaxException e) {
            write(501, "Syntax error in parameters or arguments");
        } catch (FtpBadSequenceException e) {
            write(503, "Bad sequence of commands");
        } catch (StateConflictException e) {
            e.printStackTrace();
            write(503, "Bad sequence of commands");
        }
    }

    private void onSYST() throws IOException {
        write(215, "UNIX Type: L8");
    }

    private void onUSER(String params[]) throws IOException {
        if (this.stateMachine.isFinal()) this.stateMachine.reset();
        this.stateMachine.setStatus(USER);
        this.user = ftpServer.getFtpCommandHandler().onLogin(params[1]);
        if (this.user != null) {
            if (this.user.getPassword() == null) {
                String message = ftpServer.getFtpCommandHandler().onLoginSuccess();
                if (message != null) write(230, message, true);
                write(230, "User logged in, proceed");
                this.stateMachine.setStatus(LOGIN);
            } else {
                write(331, "User name okay, need password");
            }
        } else {
            String message = ftpServer.getFtpCommandHandler().onLoginFailed();
            if (message != null) write(530, message, true);
            write(530, "Not logged in");
            this.stateMachine.setStatus(LOGOUT);
            this.stateMachine.setStatus(IDLEEND);
            this.stateMachine.reset();
        }
    }

    private void onPASS(String params[]) throws IOException {
        this.stateMachine.setStatus(PASS);
        if (this.user == null) {
            throw new FtpBadSequenceException();
        } else {
            if (user.getPassword() != null) {
                if (user.getPassword().equals(params[1])) {
                    String message = ftpServer.getFtpCommandHandler().onLoginSuccess();
                    if (message != null) write(230, message, true);
                    write(230, "User logged in, proceed");
                    this.stateMachine.setStatus(LOGIN);
                } else {
                    String message = ftpServer.getFtpCommandHandler().onLoginFailed();
                    if (message != null) write(530, message, true);
                    write(530, "Not logged in");
                    this.stateMachine.setStatus(LOGOUT);
                    this.stateMachine.setStatus(IDLEEND);
                    this.stateMachine.reset();
                }
            } else {
                throw new RuntimeException("THIS MUST NOT HAPPEN!");
            }
        }
    }

    private void onQUIT() throws IOException {
        this.stateMachine.setStatus(LOGOUT);
        write(221, ftpServer.getFtpCommandHandler().onLogout());
        this.stateMachine.setStatus(IDLEEND);
    }

    private void onNOOP() throws IOException {
        write(200, "Command okay");
    }

    private void onPWD() throws IOException {
        this.stateMachine.setStatus(LOGIN);
        write(257, "\"" + ftpServer.getFtpCommandHandler().getPWD() + "\" is cwd.");
    }

    private void onCWD(String params[]) throws IOException {
        if (!stateMachine.isState(LOGIN)) {
            write(530, "Not logged in");
        } else {
            try {
                ftpServer.getFtpCommandHandler().CWD(params[1]);
                write(250, "\"" + ftpServer.getFtpCommandHandler().getPWD() + "\" is cwd.");
            } catch (FtpFileNotExistException e) {
                write(550, "No such directory.");
            }
        }
    }

    private void onCDUP() throws IOException {
        if (!stateMachine.isState(LOGIN)) {
            write(530, "Not logged in");
        } else {
            try {
                ftpServer.getFtpCommandHandler().onCDUP();
                write(200, "Command okay.");
            } catch (FtpFileNotExistException e) {
                write(550, "No such directory.");
            }
        }

    }

    private void onPORT(String params[]) throws IOException {
        if (!stateMachine.isState(LOGIN)) {
            write(530, "Not logged in");
        } else {
            String parts[] = params[1].split(",");
            if (parts.length != 6) throw new FtpCommandSyntaxException();
            passiveIP = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
            passivePort = (Integer.parseInt(parts[4]) * 256) + Integer.parseInt(parts[5]);
            write(200, "PORT command successful");
        }
    }

    private void onLIST(String params[]) throws IOException {
        if (!stateMachine.isState(LOGIN)) {
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
                } catch (IOException e) {
                    write(425, "Can't open data connection");
                    return;
                }
                write(150, "Opening XY mode data connection for file list");
                try {
                    ftpServer.getFtpCommandHandler().onLIST(socket.getOutputStream(), file);
                    socket.getOutputStream().flush();
                } catch (FtpFileNotExistException e) {
                    write(450, "Requested file action not taken; File unavailable");
                    return;
                } catch (Exception e) {
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

    private void onTYPE(String[] commandParts) throws IOException {
        if (!stateMachine.isState(LOGIN)) {
            write(530, "Not logged in");
        } else {
            String type = commandParts[1];
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

    @Override
    public StateMachine getStateMachine() {
        return stateMachine;
    }
}
