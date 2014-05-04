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
import java.util.Date;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultBooleanArrayValue;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultByteArrayValue;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultDoubleArrayValue;
import org.appwork.storage.config.annotations.DefaultDoubleValue;
import org.appwork.storage.config.annotations.DefaultEnumArrayValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;
import org.appwork.storage.config.annotations.DefaultFloatArrayValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultIntArrayValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultJsonObject;
import org.appwork.storage.config.annotations.DefaultLongArrayValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.DefaultStringArrayValue;
import org.appwork.storage.config.annotations.DefaultStringValue;
import org.appwork.storage.config.annotations.PlainStorage;

/**
 * @author thomas
 * 
 */
@PlainStorage
public interface BadInterface extends ConfigInterface {

    @DefaultBooleanValue(value = true)
    public boolean getB2();

    @DefaultBooleanArrayValue(value = { true, false, true })
    public boolean[] getBooleanArray();

    @DefaultByteValue(value = 0)
    public byte getByte();

    @DefaultByteArrayValue(value = { 1, 2, 3 })
    public byte[] getByteArray();

    @DefaultDoubleValue(value = 0.1d)
    public double getD();

    @DefaultDoubleArrayValue(value = { 1.0d, 2.0d, 3.0d })
    public double[] getDoubleArray();

    @DefaultEnumValue(value = "org.appwork.storage.config.test.Type.A")
    public Type getEnum();

    @DefaultFloatValue(value = 0.5f)
    public float getFloat();

    @DefaultFloatArrayValue(value = { 1.0f, 2.0f, 3.0f })
    public Float[] getFloatArray();

    /**
     * @return
     */
    /*
     * BAD: BadTestObject contains a bad datatype
     */
    @CryptedStorage(key = { 0x00, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 })
    public ArrayList<BadTestObject> getGenericList();

    @DefaultIntValue(value = 0)
    public int getInt();

    @DefaultIntArrayValue(value = { 1, 2, 3 })
    public int[] getIntArray();

    @DefaultLongValue(value = 0l)
    public long getL();

    @DefaultLongArrayValue(value = { 1, 2, 3 })
    public long[] getLongArray();

    @DefaultJsonObject(value = "{\"a\":5}")
    public BadTestObject getObject();

    public ArrayList<BadTestObject[]> getStorableArrayList();

    // public Object[] getObjectArray();
    @DefaultStringValue(value = "test")
    public String getString();

    /*
     * BAD:Annotation<-->Return Type mismatch
     */
    @DefaultStringArrayValue(value = { "test", "testb" })
    public String getStringArray();

    @DefaultEnumArrayValue(value = { "org.appwork.storage.config.test.Type.A", "org.appwork.storage.config.test.Type.B" })
    public Type[] getTypeArray();

    /*
     * BAD:Invalid Type Date
     */
    public void setDate(Date d);

    /**
     * @param list
     */
    /*
     * BAD:Cryptkey mismatch
     */
    @CryptedStorage(key = { 0x01, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 })
    public void setGenericList(ArrayList<BadTestObject> list);

    public int setInt(int i);

    /**
     * @param is
     */
    public void setIntArray(int[] is);

    /**
     * @param o
     */
    public void setObject(BadTestObject o);

}
