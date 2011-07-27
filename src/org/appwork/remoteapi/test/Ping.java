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

import org.appwork.remoteapi.ApiMethodName;
import org.appwork.remoteapi.ApiNamespace;
import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;

/**
 * @author daniel
 * 
 */
@ApiNamespace("")
public interface Ping extends RemoteAPIInterface {

    @ApiMethodName("jdcheck.js")
    public String ping();

    public String ping(String in);

    public CounterProcess startCounter(RemoteAPIRequest request);

}
