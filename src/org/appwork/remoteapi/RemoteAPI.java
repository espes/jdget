/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.Regex;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public class RemoteAPI implements HttpRequestHandler {

    /* hashmap that holds all registered interfaces and their pathes */
    private final HashMap<String, InterfaceHandler<?>> interfaces = new HashMap<String, InterfaceHandler<?>>();
    private final Object                               LOCK       = new Object();

    public RemoteAPI() {
    }

    private void _handleRemoteAPICall(final RemoteAPIRequest request, final RemoteAPIResponse response) throws UnsupportedEncodingException, IOException {

        final InterfaceHandler<?> handler = request.getIface();
        response.setResponseString("YEAH");
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        String text = response.getResponseString();
        if (text == null) {
            text = "";
        }
        final int length = text.getBytes().length;
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text"));
        response.getOutputStream().write(text.getBytes("UTF-8"));

    }

    public RemoteAPIRequest getInterfaceHandler(final HttpRequest request) {
        final String[] intf = new Regex(request.getRequestedPath(), "/(.+)/(.+)$").getRow(0);
        if (intf == null) { return null; }

        synchronized (this.LOCK) {
            if (intf.length == 2) {
                final InterfaceHandler<?> interfaceHandler = this.interfaces.get(intf[0]);
                if (interfaceHandler != null) { return new RemoteAPIRequest(interfaceHandler, intf[1], new String[] { "1", "2" }); }
            }
        }
        return null;
    }

    /**
     * @param interfaceHandler
     * @param string
     * @return
     */

    public boolean onGetRequest(final GetRequest request, final HttpResponse response) {
        final RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return false; }

        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);

        }
        return true;

    }

    public boolean onPostRequest(final PostRequest request, final HttpResponse response) {
        final RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return false; }

        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);

        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public void register(final RemoteAPIInterface x) throws ParseException {
        synchronized (this.LOCK) {
            for (final Class<?> c : x.getClass().getInterfaces()) {
                if (RemoteAPIInterface.class.isAssignableFrom(c)) {
                    if (this.interfaces.containsKey(c.getName())) { throw new IllegalStateException("Interface " + c.getName() + " already has been registered by " + this.interfaces.get(c.getName())); }
                    this.interfaces.put(c.getName(), InterfaceHandler.create((Class<RemoteAPIInterface>) c, x));
                }
            }

        }
    }

    public void unregister(final RemoteAPIInterface x) {
        synchronized (this.LOCK) {

            for (final Class<?> c : x.getClass().getInterfaces()) {
                if (RemoteAPIInterface.class.isAssignableFrom(c)) {
                    this.interfaces.remove(c.getName());
                }
            }
        }
    }

}
