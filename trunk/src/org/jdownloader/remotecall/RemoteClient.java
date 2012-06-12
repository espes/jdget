package org.jdownloader.remotecall;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;

import jd.http.Browser;
import jd.http.URLConnectionAdapter;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.remotecall.RemoteCallInterface;
import org.appwork.remotecall.client.RemoteCallClient;
import org.appwork.remotecall.client.RemoteCallCommunicationException;
import org.appwork.remotecall.server.ParsingException;
import org.appwork.remotecall.server.Requestor;
import org.appwork.remotecall.server.ServerInvokationException;
import org.appwork.utils.logging.Log;

public class RemoteClient extends RemoteCallClient {

    private String    host;
    protected Browser br;

    public RemoteClient(String host) {
        this.host = host;
        br = new Browser();
        br.setConnectTimeout(125000);
        br.setReadTimeout(125000);
        br.setAllowedResponseCodes(new int[] { 500 });
    }

    public String getHost() {
        return host;
    }

    @Override
    protected Object send(String serviceName, Method routine, String serialise) throws ServerInvokationException {
        try {
            String url = "http://" + this.host + "/" + serviceName + "/" + URLEncoder.encode(routine.getName(), "UTF-8");
            Log.L.finer(url + "?" + serialise);

            String red = br.postPageRaw(url, serialise);

            URLConnectionAdapter con = br.getHttpConnection();
            if (con.getResponseCode() == HTTPConstants.ResponseCode.SUCCESS_OK.getCode()) {
                return red;
            } else if (con.getResponseCode() == HTTPConstants.ResponseCode.SERVERERROR_INTERNAL.getCode()) {
                // Exception
                throw new ServerInvokationException(red, new Requestor(serviceName, routine.getName(), serialise));
            } else {
                throw new RemoteCallCommunicationException("Wrong ResponseCode " + con.getResponseCode());
            }
        } catch (final ServerInvokationException e) {
            throw e;
        } catch (final IOException e) {

            throw new RemoteCallCommunicationException(e);
        } catch (final Exception e) {
            if (e instanceof RuntimeException) { throw (RuntimeException) e; }
            throw new RuntimeException(e);
        }

    }

    public <T extends RemoteCallInterface> T create(Class<T> class1) {
        try {
            return getFactory().newInstance(class1);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        }
    }
}
