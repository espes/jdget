/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.sms
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.sms;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import org.appwork.utils.logging.Log;
import org.appwork.utils.net.SimpleHTTP;

/**
 * @author daniel
 * API-DOCS: https://www.sms77.de/api.pdf
 */
public class SMS77Gateway {

    public static final String API_URL  = "http://gateway.sms77.de/";
    private boolean            useHTTPS = false;

    public boolean isUseHTTPS() {
        return useHTTPS;
    }

    public void setUseHTTPS(boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    private SimpleHTTP br;

    public void setBrowser(SimpleHTTP br) {
        this.br = br;
    }

    public SimpleHTTP getBrowser() {
        return br;
    }

    private String userName;
    private String userPass;

    public SMS77Gateway(final String userName, final String userPass) {
        this.br = new SimpleHTTP();
        this.userName = userName;
        this.userPass = userPass;
    }

    private synchronized String[] callAPI(final String cmd, final SMS77GatewayParameter... parameters) throws SMS77GatewayException {
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append("?u=" + URLEncoder.encode(userName, "UTF-8") + "&p=" + URLEncoder.encode(userPass, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.exception(e);
            sb.append("?u=" + userName + "&p=" + userPass);
        }

        for (final SMS77GatewayParameter param : parameters) {
            if (param != null) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(param.toString());
            }
        }
        try {
            String url = API_URL;
            if (this.useHTTPS) url = url.replaceFirst("http:", "https:");
            /* post seems not to work */
            String rets[] = br.getPage(new URL(url + cmd + sb.toString())).split("[\r\n]+");
            String ret = rets[0];
            if ("900".equals(ret)) throw new SMS77GatewayException("Benutzer/Passwort-Kombination falsch");
            if ("902".equals(ret)) throw new SMS77GatewayException("http API für diesen Account deaktiviert");
            if ("903".equals(ret)) throw new SMS77GatewayException("Server IP ist falsch");
            if ("700".equals(ret)) throw new SMS77GatewayException("Unbekannter Fehler");
            return rets;
        } catch (final Throwable e) {
            if (e instanceof SMS77GatewayException) throw (SMS77GatewayException) e;
            throw new SMS77GatewayException(e.getMessage(), e);
        }
    }

    /*
     * returns current balance of the account
     */
    public double getBalance() throws SMS77GatewayException {
        String ret = callAPI("balance.php")[0];
        return Double.parseDouble(ret);
    }

    /*
     * sends a message to given receiver and returns the messageID
     */
    public String sendSMS(final SMS77Message message, final String receiver) throws SMS77GatewayException {
        if (message == null) throw new NullPointerException("message is null");
        if (receiver == null) throw new NullPointerException("receiver is null");
        if (message.getMessage().length() > 1555) throw new SMS77GatewayException("Message too long");
        ArrayList<SMS77GatewayParameter> params = new ArrayList<SMS77GatewayParameter>();
        params.add(SMS77GatewayParameter.create("to", receiver));
        params.add(SMS77GatewayParameter.create("text", message.getMessage()));
        /* TODO: is basicplus really the right type here */
        if (message.getSender() != null && !message.getType().equals(SMS77Message.TYPE.BASICPLUS)) {
            params.add(SMS77GatewayParameter.create("from", message.getSender()));
        }
        params.add(SMS77GatewayParameter.create("type", message.getType().name().toLowerCase(Locale.ENGLISH)));
        params.add(SMS77GatewayParameter.create("return_msg_id", "1"));
        String[] rets = this.callAPI("", params.toArray(new SMS77GatewayParameter[params.size()]));
        String ret = rets[0];
        if ("400".equals(ret)) throw new SMS77GatewayException("type ungültig");
        if ("402".equals(ret)) throw new SMS77GatewayException("Reloadsperre");
        if ("306".equals(ret)) throw new SMS77GatewayException("Absendernummer ungültig");
        if ("202".equals(ret)) throw new SMS77GatewayException("Empfängernummer ungültig");
        if ("201".equals(ret)) throw new SMS77GatewayException("Ländercode für diesen SMS-Typ nicht gültig. Bitte als Basic SMS verschicken.");
        if ("101".equals(ret)) throw new SMS77GatewayException("Versand an mindestens einen Empfänger fehlgeschlagen.");
        return rets[1];
    }

    /*
     * returns status of given messageID
     */
    public SMS77MsgStatus getSMSStatus(final String messageID) throws SMS77GatewayException {
        if (messageID == null) throw new NullPointerException("messageID is null");
        String ret[] = this.callAPI("status.php", SMS77GatewayParameter.create("msg_id", messageID));
        if ("901".equals(ret[0])) throw new SMS77GatewayException("Ungültige Msg ID");
        try {
            return new SMS77MsgStatus(ret);
        } catch (final Throwable e) {
            throw new SMS77GatewayException(e.getMessage(), e);
        }
    }
}
