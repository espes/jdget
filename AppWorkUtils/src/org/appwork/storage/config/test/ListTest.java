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

import java.util.ArrayList;

import org.appwork.storage.config.JsonConfig;

/**
 * @author Thomas
 * 
 */
public class ListTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ListConfig cfg = JsonConfig.create(ListConfig.class);
    
    double[] da = cfg.getDoubleArray();
 Double[] dwa = cfg.getDoubleWrapperArray();
 ArrayList<Double> dwl = cfg.getDoubleWrapperList();
    cfg.setDoubleArray(new double[]{9.8,7.6});
    cfg.setDoubleWrapperArray(new Double[]{7.6,5.4});
    dwl.add(Math.random());
    cfg.setDoubleWrapperList(dwl);
    
    System.out.println(da);
    System.out.println(dwa);
    System.out.println("SUCCESSFUL");
    System.exit(1);
    }

}
