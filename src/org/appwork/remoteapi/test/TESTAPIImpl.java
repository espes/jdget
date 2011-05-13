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

/**
 * @author daniel
 * 
 */
public class TESTAPIImpl implements TESTAPI, TestApiInterface {

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.test.TestApiInterface#merge(java.lang.String,
     * java.lang.String, int, boolean)
     */
    @Override
    public String merge(final String a, final String b, final int a2, final boolean b2) {
        // TODO Auto-generated method stub
        return a + b + a2 + b2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.test.TestApiInterface#sum(int, int)
     */
    @Override
    public int sum(final int a, final int b) {
        // TODO Auto-generated method stub
        return a + b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.test.TESTAPI#test()
     */
    @Override
    public String test() {
        return "TestSucessfull";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.test.TestApiInterface#toggle(boolean)
     */
    @Override
    public boolean toggle(final boolean b) {
        // TODO Auto-generated method stub
        return false;
    }

}
