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
import java.util.HashSet;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.AbstractValidator;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultBooleanArrayValue;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultByteArrayValue;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultDoubleArrayValue;
import org.appwork.storage.config.annotations.DefaultEnumArrayValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;
import org.appwork.storage.config.annotations.DefaultFactory;
import org.appwork.storage.config.annotations.DefaultFloatArrayValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultIntArrayValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultLongArrayValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.DefaultStringArrayValue;
import org.appwork.storage.config.annotations.DefaultStringValue;
import org.appwork.storage.config.annotations.PlainStorage;
import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.storage.config.annotations.ValidatorFactory;
import org.appwork.storage.config.defaults.AbstractDefaultFactory;
import org.appwork.storage.config.handler.IntegerKeyHandler;
import org.appwork.storage.config.handler.StorageHandler;

/**
 * @author thomas
 * 
 */
@PlainStorage
public interface MyInterface extends ConfigInterface {
    public static final MyInterface                 CFG = JsonConfig.create(MyInterface.class);
    @SuppressWarnings("unchecked")
    public static final StorageHandler<MyInterface> SH  = (StorageHandler<MyInterface>) CFG._getStorageHandler();

    @DefaultBooleanValue(value = true)
    public boolean getB2();

    @DefaultBooleanArrayValue(value = { true, false, true })
    public boolean[] getBooleanArray();

    @DefaultByteValue(value = 0)
    public byte getByte();

    @DefaultByteArrayValue(value = { 1, 2, 3 })
    public byte[] getByteArray();

    @DefaultFactory(DefaultD.class)
    public double getD();

    @DefaultDoubleArrayValue(value = { 1.0d, 2.0d, 3.0d })
    public double[] getDoubleArray();

    public void setDoubleArray(double[] arr);

    @DefaultEnumValue(value = "A")
    public Type getEnum();

    @DefaultFloatValue(value = 0.5f)
    public float getFloat();

    @DefaultFloatArrayValue(value = { 1.0f, 2.0f, 3.0f })
    public Float[] getFloatArray();

    /**
     * @return
     */

    public ArrayList<TestObject> getGenericList();

    public static final IntegerKeyHandler INT = CFG._getStorageHandler().getKeyHandler("Int", IntegerKeyHandler.class);

    @DefaultIntValue(value = 2)
    @SpinnerValidator(min = 1, max = 10, step = 1)
    public int getInt();

    @DefaultIntArrayValue(value = { 1, 2, 3 })
    public int[] getIntArray();

    @DefaultLongValue(value = 0l)
    public long getL();

    @DefaultLongArrayValue(value = { 1, 2, 3 })
    public long[] getLongArray();

    // @DefaultJsonObject(value = "{\"a\":5}")
    public TestObject getObject();

    public HashSet<String> getSet();

    public void setSet(HashSet<String> set);

    public ArrayList<TestObject[]> getStorableArrayList();

    // public Object[] getObjectArray();
    @DefaultStringValue(value = "test")
    public String getString();

    @DefaultStringArrayValue(value = { "test", "testb" })
    public String[] getStringArray();

    @DefaultEnumArrayValue(value = { "org.appwork.storage.config.test.Type.A", "org.appwork.storage.config.test.Type.B" })
    public Type[] getTypeArray();

    /**
     * @param list
     */
    @CryptedStorage(key = { 0x01, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 })
    public void setGenericList(ArrayList<TestObject> list);

    public void setInt(int i);

    /**
     * @param is
     */
    public void setIntArray(int[] is);

    class DefFac extends AbstractDefaultFactory<TestObject> {

        @Override
        public TestObject getDefaultValue() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    class DefValid extends AbstractValidator<TestObject> {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.appwork.storage.config.annotations.AbstractValidator#validate
         * (java.lang.Object)
         */
        @Override
        public void validate(TestObject object) throws ValidationException {
            System.out.println("CHECK " + object);
        }

    }

    /**
     * @param o
     */
    @DefaultFactory(org.appwork.storage.config.test.MyInterface.DefFac.class)
    @ValidatorFactory(org.appwork.storage.config.test.MyInterface.DefValid.class)
    public void setObject(TestObject o);

    @DefaultFactory(MyDefaultCreator.class)
    public ArrayList<Integer> getDefault();

}
