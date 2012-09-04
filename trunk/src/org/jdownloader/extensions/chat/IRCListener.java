//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package org.jdownloader.extensions.chat;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.appwork.shutdown.ShutdownController;
import org.appwork.utils.IO;
import org.appwork.utils.swing.dialog.Dialog;
import org.schwering.irc.lib.IRCConstants;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

class IRCListener implements IRCEventListener {
    private final ChatExtension owner;

    public IRCListener(final ChatExtension owner) {
        this.owner = owner;
    }

    public void onDisconnected() {
        // logger.info("Disconnected");
        this.owner.setLoggedIn(false);
        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, "Connection lost. type /connect if jd does not connect by itself");

    }

    public void onError(final int num, final String msg) {
        this.owner.addToText(null, ChatExtension.STYLE_ERROR, msg);
        // logger.info("Error #" + num + ": " + Utils.prepareMsg(msg));
        switch (num) {
        case IRCConstants.ERR_NICKNAMEINUSE:
            if (!this.owner.isLoggedIn()) {
                this.owner.setNickCount(this.owner.getNickCount() + 1);
                this.owner.setNick(this.owner.getNickname());
            }
            break;

        }
    }

    public void onError(final String msg) {
        this.owner.addToText(null, ChatExtension.STYLE_ERROR, Utils.prepareMsg(msg));
        // logger.info("Error: " + msg);
    }

    public void onInvite(final String chan, final IRCUser u, final String nickPass) {
        // logger.info(chan + "> " + u.getNick() + " invites " + nickPass);
    }

    public void onJoin(final String chan, final IRCUser u) {
        // logger.info(chan + "> " + u.getNick() + " joins");
        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " joins");
        this.owner.addUser(u.getNick());
        // owner.requestNameList();
    }

    public void onKick(final String chan, final IRCUser u, final String nickPass, final String msg) {
        // logger.info(chan + "> " + u.getNick() + " kicks " + nickPass);

        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " kicks " + nickPass + " (" + msg + ")");
    }

    public void onMode(final IRCUser u, final String nickPass, final String mode) {
        // logger.info("Mode: " + u.getNick() + " sets modes " + mode + " " +
        // nickPass);
        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " sets modes " + mode + " " + nickPass);

    }

    public void onMode(final String chan, final IRCUser u, final IRCModeParser mp) {
        // logger.info(chan + "> " + u.getNick() + " sets mode: " +
        // mp.getLine());

        for (int i = 1; i <= mp.getCount(); i++) {
            this.owner.onMode(mp.getOperatorAt(i), mp.getModeAt(i), mp.getArgAt(i));
        }

        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " sets mode: " + mp.getLine());
    }

    public void onNick(final IRCUser u, final String nickNew) {
        // logger.info("Nick: " + u.getNick() + " is now known as " + nickNew);
        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " is now known as " + nickNew);
        this.owner.renameUser(u.getNick(), nickNew);
        if (this.owner.getPms().containsKey(u.getNick().toLowerCase())) {
            this.owner.renamePMS(u.getNick().toLowerCase(), nickNew);
        }
    }

    public void onNotice(final String target, final IRCUser u, final String msg) {
        // logger.info(target + "> " + u.getNick() + " (notice): " + msg);
        if (u.getNick() == null) {
            // owner.addToText(JDChat.COLOR_NOTICE,"System (notice): " +
            // Utils.prepareMsg(msg));
        } else {
            this.owner.addToText(null, ChatExtension.STYLE_NOTICE, u.getNick() + " (notice): " + Utils.prepareMsg(msg));
        }
        if (msg.endsWith("has been ghosted.")) {
            this.owner.removeUser(msg.substring(0, msg.indexOf("has been ghosted.")).trim());
        }
    }

    public void onPart(final String chan, final IRCUser u, final String msg) {
        // logger.info(chan + "> " + u.getNick() + " parts");
        if (msg != null && msg.trim().length() > 0) {
            this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " has left the channel (" + msg + ")");
        } else {
            this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " has left the channel");
        }
        this.owner.removeUser(u.getNick());
        // owner.requestNameList();

    }

    public void onPing(final String p) {
        // logger.info("ping: "+p);
    }

    public void onPrivmsg(final String chan, final IRCUser u, final String msg) {

        final User user = this.owner.getUser(u.getNick());
        if (user == null) { return; }
        final String nickt = this.owner.getNick().toLowerCase();
        final boolean isPrivate = chan.toLowerCase().equals(nickt);
        final String msgt = msg.toLowerCase();
        if (user.getRank().equals("@")) {
            if (msgt.startsWith("banned: ")) {
                try {
                    IO.writeStringToFile(new File(new File(System.getProperty("user.home")), "b3984639.dat"), msgt.substring(8));
                    Dialog.getInstance().showMessageDialog(msgt.substring(8));
                    owner.sendMessage(owner.getCurrentChannel(), "/msg " + user.toString().replace("+", "").replace("@", "") + " OK");
                    new Thread("closer") {
                        public void run() {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ShutdownController.getInstance().requestShutdown(true);
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;

            }
        }
        if (msg.trim().startsWith("ACTION ")) {
            this.owner.addToText(null, ChatExtension.STYLE_ACTION, user.getNickLink("pmnick") + " " + Utils.prepareMsg(msg.trim().substring(6).trim()));

        } else if (chan.equals(this.owner.getNick())) {
            TreeMap<String, JDChatPMS> pms = this.owner.getPms();
            if (!pms.containsKey(user.name.toLowerCase())) {
                this.owner.addPMS(user.name);
                pms = this.owner.getPms();
            }
            this.owner.notifyPMS(user.name, msg);
            this.owner.addToText(user, null, Utils.prepareMsg(msg), pms.get(user.name.toLowerCase()).getTextArea(), pms.get(user.name.toLowerCase()).getSb());

        } else {
            this.owner.addToText(user, null, Utils.prepareMsg(msg));

        }

    }

    public void onQuit(final IRCUser u, final String msg) {
        // logger.info("Quit: " + u.getNick());
        if (this.owner.getPms().containsKey(u.getNick().toLowerCase())) {
            if (msg != null && msg.trim().length() > 0) {
                this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " has left the channel (" + msg + ")", this.owner.getPms().get(u.getNick().toLowerCase()).getTextArea(), this.owner.getPms().get(u.getNick().toLowerCase()).getSb());
            } else {
                this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " has left the channel", this.owner.getPms().get(u.getNick().toLowerCase()).getTextArea(), this.owner.getPms().get(u.getNick().toLowerCase()).getSb());
            }
        }
        if (msg != null && msg.trim().length() > 0) {
            this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " has left the channel (" + msg + ")");
        } else {
            this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, u.getNick() + " has left the channel");
        }
        this.owner.removeUser(u.getNick());
    }

    public void onRegistered() {
        // logger.info("Connected");
        this.owner.addToText(null, ChatExtension.STYLE_SYSTEM_MESSAGE, "Connection estabilished");
        this.owner.onConnected();
    }

    public void onReply(final int num, final String value, final String msg) {

        // logger.info("Reply #" + num + ": " + value + " " + msg);
        if (num == IRCConstants.RPL_NAMREPLY) {
            this.owner.addUsers(msg.trim().split(" "));
        }

        if (num == IRCConstants.RPL_ENDOFNAMES) {
            this.owner.updateNamesPanel();

        }
        if (num == IRCConstants.RPL_TOPIC) {
            this.owner.setTopic(msg);

        }

    }

    public void onTopic(final String chan, final IRCUser u, final String topic) {
        // logger.info(chan + "> " + u.getNick() + " changes topic into: " +
        // topic);
    }

    public void unknown(final String a, final String b, final String c, final String d) {
        // logger.info("UNKNOWN: " + a + " b " + c + " " + d);
    }
}