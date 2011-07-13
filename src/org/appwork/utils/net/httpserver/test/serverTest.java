/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.test;

import java.io.IOException;

import org.appwork.remoteapi.ParseException;
import org.appwork.remoteapi.SessionRemoteAPI;
import org.appwork.remoteapi.test.TESTAPIImpl;
import org.appwork.utils.net.httpserver.HttpServer;

/**
 * @author daniel
 * 
 */
public class serverTest {

    /**
     * @param args
     * @throws IOException
     */
    public static SessionRemoteAPI<TestSession> rapi = new SessionRemoteAPI<TestSession>();

    public static void main(final String[] args) throws IOException {
        final HttpServer server = new HttpServer(3128);
        HttpSessionControllerImp ll;
        server.registerRequestHandler(ll = new HttpSessionControllerImp());
        ll.registerSessionRequestHandler(serverTest.rapi);
        try {
            serverTest.rapi.register(ll);
            serverTest.rapi.register(new TESTAPIImpl());
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        server.start();
    }

}
