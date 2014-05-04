/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.test;

import java.io.IOException;
import java.net.URL;

import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.FileResponse;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author thomas
 * 
 */
public class ResourceHandler implements HttpRequestHandler {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.HttpRequestHandler#onGetRequest(org.
     * appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetRequest(final GetRequest request, final HttpResponse response) {
        // TODO Auto-generated method stub
        // TODO: SECURITY
        if (!request.getRequestedPath().startsWith("/resources/")) { return false; }
        final URL url = this.getClass().getResource(request.getRequestedPath().substring(1));
        if (url != null) {
            final FileResponse fr = new FileResponse(request, response, url);
            try {
                fr.sendFile();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.HttpRequestHandler#onPostRequest(org
     * .appwork.utils.net.httpserver.requests.PostRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onPostRequest(final PostRequest request, final HttpResponse response) {
        // TODO Auto-generated method stub
        return false;
    }

}
