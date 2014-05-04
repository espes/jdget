/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import org.appwork.storage.Storable;

/**
 * @author thomas
 * 
 */
public class TestObject implements Storable {
    private int a;
    private int b;

    public TestObject() {
        this.a = 1;
        this.b = 2;
    }

    public int getA() {
        return this.a;
    }

    public int getB() {
        return this.b;
    }

    public void setA(final int a) {
        this.a = a;
    }

    public void setB(final int b) {
        this.b = b;
    }

}
