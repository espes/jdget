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

import org.appwork.remoteapi.ParseException;
import org.appwork.remoteapi.RemoteAPI;
import org.appwork.utils.net.httpserver.HttpServerController;

/**
 * @author daniel
 * 
 */
public class Test {
    static {
        // USe Jacksonmapper in this project
        // JSonStorage.setMapper(new JacksonMapper());

    }
    public static RemoteAPI rapi = new RemoteAPI();

    public static void main(final String[] args) throws IOException {
        final HttpServerController server = new HttpServerController();

        server.registerRequestHandler(3128, false, Test.rapi);
        server.registerRequestHandler(3129, false, new ResourceHandler());
        server.registerRequestHandler(3129, false, new FileServer());

        try {
            Test.rapi.register(new TESTAPIImpl());
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
