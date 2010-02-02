/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.orm
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.orm;

/**
 * @author coalado
 * 
 */
// @MappableClassID("Tester")
public class TestClass {
    /**
     * 
     */
    private static final long serialVersionUID = -8502363687618425998L;
    public TestClass testClassA;
    public TestClass testClassB;
    public Long longclass = new Long(34);
    private int intc;
    transient private int transientintb;
    String stringD;
    public int[] intArrayE;

    @InstanceID
    // be carefull. do NEVER change this if yuor are not absolutly sure what you
    // are doing
    transient public String instanceID = System.currentTimeMillis() + "_" + Math.random();
    public int[][] doubleInt;
    public Object obj;
    public TestClass ich;

    public TestClass() {
        intc = 1;
        transientintb = 5;
        testClassA = null;
        obj = null;
        testClassB = null;

        ich = this;
    }

    /**
     * @param i
     * @param string
     */
    public TestClass(int i, String string) {
        intc = i;
        stringD = string;

    }

}
