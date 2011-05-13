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

import org.appwork.remoteapi.RemoteAPIInterface;

/**
 * @author thomas
 * 
 */
public interface TestApiInterface extends RemoteAPIInterface {
    public int sum(int a, int b);
}
