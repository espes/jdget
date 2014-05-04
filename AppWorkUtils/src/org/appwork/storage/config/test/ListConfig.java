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

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.DefaultDoubleArrayValue;
import org.appwork.storage.config.annotations.DefaultJsonObject;

/**
 * @author Thomas
 * 
 */
public interface ListConfig extends ConfigInterface {
    @DefaultJsonObject("[ 1.2, 3.4 ]")
    public ArrayList<Double> getDoubleWrapperList();

    public void setDoubleWrapperList(ArrayList<Double> list);

    @DefaultDoubleArrayValue({ 1.2, 3.4 })
    public Double[] getDoubleWrapperArray();

    public void setDoubleWrapperArray(Double[] list);

    @DefaultDoubleArrayValue({ 1.2, 3.4 })
    public double[] getDoubleArray();

    public void setDoubleArray(double[] list);
}
