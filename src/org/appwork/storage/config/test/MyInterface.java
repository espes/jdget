/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import java.util.ArrayList;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultDoubleValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.DefaultObjectValue;
import org.appwork.storage.config.annotations.DefaultStringValue;

/**
 * @author thomas
 * 
 */
public interface MyInterface extends ConfigInterface {

    @DefaultBooleanValue(value = true)
    public boolean getB2();

    @DefaultByteValue(value = 0)
    public byte getByte();

    @DefaultDoubleValue(value = 0.1d)
    public double getD();

    @DefaultEnumValue(value = "org.appwork.storage.config.test.Type.A")
    public Type getEnum();

    @DefaultFloatValue(value = 0.5f)
    public float getFloatr();

    /**
     * @return
     */
    public ArrayList<TestObject> getGenericList();

    @DefaultIntValue(value = 0)
    public int getInt();

    public int[] getIntArray();

    @DefaultLongValue(value = 0l)
    public double getL();

    @DefaultObjectValue(value = "{\"a\":5}")
    public TestObject getObject();

    public ArrayList<TestObject[]> getStorableArrayList();

    // public Object[] getObjectArray();
    @DefaultStringValue(value = "test")
    public String getString();

    /**
     * @param list
     */
    public void setGenericList(ArrayList<TestObject> list);

    public int setInt(int i);

    /**
     * @param is
     */
    public void setIntArray(int[] is);

    /**
     * @param o
     */
    public void setObject(TestObject o);

}
