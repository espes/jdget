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
    private final HashMap<String, RemoteAPIInterface> interfaces = new HashMap<String, RemoteAPIInterface>();
    private final Object                              LOCK       = new Object();

    public RemoteAPI() {
    }

    private void _handleRemoteAPICall(final RemoteAPIRequest request, final RemoteAPIResponse response) throws UnsupportedEncodingException, IOException {
        /*
         * hier die antwort als string setzen, können wahlweise auch bytearray
         * nehmen
         */
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

    public boolean canHandle(final HttpRequest request) {
        final String[] intf = new Regex(request.getRequestedPath(), "/(.+)/(.+)$").getRow(0);
        RemoteAPIInterface x = null;
        synchronized (this.LOCK) {
            if (intf.length == 2) {
                x = this.interfaces.get(intf[0]);
            }
        }
        if (x != null) {
            /*
             * TODO: hier bereits aus dem Cache oder Live schaun, obs den
             * methoden namen überhaupt gibt
             */
            final RemoteAPIRequest call = new RemoteAPIRequest(x, intf[1]);
            request.setHandlerExtension(call);
            return true;
        } else {
            return false;
        }
    }

    public void onGetRequest(final GetRequest request, final HttpResponse response) {
        final RemoteAPIRequest r = (RemoteAPIRequest) request.getHandlerExtension();
        /* TODO: build paramterArray */
        try {
            this._handleRemoteAPICall(r, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);

        }

    }

    public void onPostRequest(final PostRequest request, final HttpResponse response) {
        final RemoteAPIRequest r = (RemoteAPIRequest) request.getHandlerExtension();
        /* TODO: parse postData into parameter+build paramterArray */
        try {
            this._handleRemoteAPICall(r, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void register(final RemoteAPIInterface x) {
        synchronized (this.LOCK) {
            this.interfaces.put(x.getClass().getName(), x);
            /*
             * TODO: caches von funktionsnamen und paramter und co hier
             * erstellen, das nicht immer nachschaun müssen
             */
        }
    }

    public void unregister(final RemoteAPIInterface x) {
        synchronized (this.LOCK) {
            this.interfaces.remove(x.getClass().getName());
        }
    }

}
