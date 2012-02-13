/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdownloader.extensions.neembuu;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import jd.controlling.JDLogger;
import jd.http.Browser;
import jd.http.Request;
import jd.http.URLConnectionAdapter;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.download.DownloadInterface;
import org.appwork.utils.Exceptions;
import org.appwork.utils.net.httpconnection.HTTPConnection.RequestMethod;

/**
 *
 * @author Shashank Tulsyan
 */
public final class JD_Utils {
    public static URLConnectionAdapter copyConnection(
                DownloadLink downloadLink,
                DownloadInterface di,
                PluginForHost plugin,
                long startByte,
                Browser b,
                URLConnectionAdapter connection) {
//            try {
//                while (/*downloadLink.getLivePlugin()*/plugin.waitForNextConnectionAllowed()) {
//                }
//                /*downloadLink.getLivePlugin()*/plugin.putLastConnectionTime(System.currentTimeMillis());
//            } catch (InterruptedException e) {
//                return null;
//            } catch (NullPointerException npe){
//                npe.printStackTrace(System.err);
//                //ignore
//            }
            
            long start = startByte;
            //String end = (endByte > 0 ? endByte + 1 : "") + "";

            /*if (start == 0) {
                di.logger.finer("Takeover 0 Connection");
                return connection;
            }*/
            if (connection.getRange() != null && connection.getRange()[0] == (start)) {
                di.logger.finer("Takeover connection at " + connection.getRange()[0]);
                return connection;
            }

            try {
                /* only forward referer if referer already has been sent! */
                boolean forwardReferer = /*plugin.getBrowser()*/b.getHeaders().contains("Referer");
                Browser br = /*plugin.getBrowser()*/b.cloneBrowser();
                br.setReadTimeout(di.getReadTimeout());
                br.setConnectTimeout(di.getRequestTimeout());
                /* set requested range */

                Map<String, String> request = connection.getRequestProperties();
                if (request != null) {
                    String value;
                    for (Entry<String, String> next : request.entrySet()) {
                        if (next.getValue() == null) continue;
                        value = next.getValue().toString();
                        br.getHeaders().put(next.getKey(), value);
                    }
                }
                if (!forwardReferer) {
                    /* only forward referer if referer already has been sent! */
                    br.setCurrentURL(null);
                }
                URLConnectionAdapter con = null;
                //clonedconnection = true;
                if (connection.getRequestMethod() == RequestMethod.POST) {
                    connection.getRequest().getHeaders().put("Range", "bytes=" + start + "-");
                    con = br.openRequestConnection(connection.getRequest());
                } else {
                    br.getHeaders().put("Range", "bytes=" + start + "-");
                    con = br.openGetConnection(connection.getURL() + "");
                }
                if (!con.isOK()) {
                    try {
                        /* always close connections that got opened */
                        con.disconnect();
                    } catch (Throwable e) {
                    }
                    if (con.getResponseCode() != 416) {
                        di.logger.severe(LinkStatus.ERROR_DOWNLOAD_FAILED+ "Server: " + con.getResponseMessage());
                    } else {
                        di.logger.warning("HTTP 416, maybe finished last chunk?");
                    }
                    return null;
                }
                if (con.getHeaderField("Location") != null) {
                    try {
                        /* always close connections that got opened */
                        con.disconnect();
                    } catch (Throwable e) {
                    }
                    di.logger.severe(LinkStatus.ERROR_DOWNLOAD_FAILED+ "Server: Redirect");
                    return null;
                }
                return con;
            } catch (Exception e) {
                di.logger.log(Level.SEVERE,"ERROR_RETRY", e);
                JDLogger.exception(e);
            }
            return null;
        }
}
