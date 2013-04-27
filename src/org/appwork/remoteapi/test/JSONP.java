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
import java.io.UnsupportedEncodingException;

import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.annotations.ApiNamespace;

/**
 * @author daniel
 * 
 */
@ApiNamespace("demo")
public interface JSONP extends RemoteAPIInterface {

    public boolean color(int r, int g, int b);

    public void test(String callback, long id, long timestamp, final RemoteAPIRequest request, final RemoteAPIResponse response) throws UnsupportedEncodingException, IOException;
}
