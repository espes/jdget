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

import org.appwork.remoteapi.RemoteAPI;
import org.appwork.utils.net.httpserver.HttpServer;

/**
 * @author daniel
 * 
 */
public class Test {

    public static RemoteAPI rapi = new RemoteAPI();

    public static void main(final String[] args) throws IOException {
        final HttpServer server = new HttpServer(3128);
        server.registerRequestHandler(Test.rapi);
        server.start();

        Test.rapi.register(new TESTAPIImpl());
    }
}
