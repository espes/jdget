/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper.test;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.appwork.storage.Storable;

/**
 * @author thomas
 * 
 */
public class TestClass implements Storable {

    private enum AnyEnum {
        TEST,
        BLUMM

    }

    public static java.util.List<TestClass> createList() {
        final java.util.List<TestClass> ret = new ArrayList<TestClass>();
        ret.add(TestClass.createObject());
        ret.add(TestClass.createObject());
        ret.add(TestClass.createObject());
        ret.add(TestClass.createObject());
        return ret;

    }

    public static TestClass createObject() {
        final TestClass ret1 = new TestClass("1");

        final TestClass ret2 = new TestClass("2");
        final TestClass ret3 = new TestClass("3");
        final TestClass ret4 = new TestClass("4");
        final TestClass ret5 = new TestClass("5");
        ret1.getList().add(1);
        ret1.getList().add(2);
        ret1.getList().add(3);
        ret1.getMap().put("2", ret2);
        ret1.getMap().put("3", ret3);
        ret1.getMap().put("4", ret4);
        ret1.getMap().put("5", ret5);
        return ret1;

    }

    private double                     pDouble  = 0.5d;

    private float                      pFloat   = 0.4f;

    private long                       pLong    = 43543l;
    private int                        pInt     = 43253;
    private byte                       pByte    = 0x24;
    private char                       pChar    = 0x12;
    private boolean                    pBoolean = true;
    private String                     string   = "affe";
    private Double                     oDouble  = 0.5d;
    private Float                      oFloat   = 0.4f;
    private Long                       oLong    = 43543l;
    private Integer                    oInt     = 43253;
    private Byte                       oByte    = 0x24;
    private Character                  oChar    = 0x12;
    private AnyEnum                    num      = AnyEnum.TEST;
    private Boolean                    oBoolean = true;
    private int[]                      intArray = new int[] { 1, 2 };
    private TestClass[]                objArray = null;
    private HashMap<String, TestClass> map      = new HashMap<String, TestClass>();
    private java.util.List<Integer>         list     = new ArrayList<Integer>();

    private TestClass                  obj;

    public TestClass() {

    }

    /**
     * @param string2
     */
    public TestClass(final String string2) {
        this.string = string2;
        this.objArray = new TestClass[] { new TestClass(), new TestClass(), new TestClass() };
        this.pDouble = 0.3d;
        this.intArray = new int[] { 3, 2, 1 };
        this.num = AnyEnum.BLUMM;
        this.pFloat = 0.423f;
        this.pLong = 4355543543l;
        this.pInt = 2435253;
        this.pByte = 0x14;
        this.pChar = 0x13;
        this.pBoolean = false;
        this.string = "affe232";
        this.oDouble = 0.52d;
        this.oFloat = 0.4123f;
        this.oLong = 5435443543l;
        this.oInt = 45343253;
        this.oByte = 0x44;
        this.oChar = 0x10;
        this.oBoolean = false;

    }

    @Override
    public boolean equals(final Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public int[] getIntArray() {
        return this.intArray;
    }

    public java.util.List<Integer> getList() {
        return this.list;
    }

    public HashMap<String, TestClass> getMap() {
        return this.map;
    }

    public AnyEnum getNum() {
        return this.num;
    }

    public TestClass getObj() {
        return this.obj;
    }

    public TestClass[] getObjArray() {
        return this.objArray;
    }

    public Boolean getoBoolean() {
        return this.oBoolean;
    }

    public Byte getoByte() {
        return this.oByte;
    }

    public Character getoChar() {
        return this.oChar;
    }

    public Double getoDouble() {
        return this.oDouble;
    }

    public Float getoFloat() {
        return this.oFloat;
    }

    public Integer getoInt() {
        return this.oInt;
    }

    public Long getoLong() {
        return this.oLong;
    }

    public byte getpByte() {
        return this.pByte;
    }

    public char getpChar() {
        return this.pChar;
    }

    public double getpDouble() {
        return this.pDouble;
    }

    public float getpFloat() {
        return this.pFloat;
    }

    public int getpInt() {
        return this.pInt;
    }

    public long getpLong() {
        return this.pLong;
    }

    public String getString() {
        return this.string;
    }

    public boolean ispBoolean() {
        return this.pBoolean;
    }

    public void setIntArray(final int[] intArray) {
        this.intArray = intArray;
    }

    public void setList(final java.util.List<Integer> list) {
        this.list = list;
    }

    public void setMap(final HashMap<String, TestClass> map) {
        this.map = map;
    }

    public void setNum(final AnyEnum num) {
        this.num = num;
    }

    public void setObj(final TestClass obj) {
        this.obj = obj;
    }

    public void setObjArray(final TestClass[] objArray) {
        this.objArray = objArray;
    }

    public void setoBoolean(final Boolean oBoolean) {
        this.oBoolean = oBoolean;
    }

    public void setoByte(final Byte oByte) {
        this.oByte = oByte;
    }

    public void setoChar(final Character oChar) {
        this.oChar = oChar;
    }

    public void setoDouble(final Double oDouble) {
        this.oDouble = oDouble;
    }

    public void setoFloat(final Float oFloat) {
        this.oFloat = oFloat;
    }

    public void setoInt(final Integer oInt) {
        this.oInt = oInt;
    }

    public void setoLong(final Long oLong) {
        this.oLong = oLong;
    }

    public void setpBoolean(final boolean pBoolean) {
        this.pBoolean = pBoolean;
    }

    public void setpByte(final byte pByte) {
        this.pByte = pByte;
    }

    public void setpChar(final char pChar) {
        this.pChar = pChar;
    }

    public void setpDouble(final double pDouble) {
        this.pDouble = pDouble;
    }

    public void setpFloat(final float pFloat) {
        this.pFloat = pFloat;
    }

    public void setpInt(final int pInt) {
        this.pInt = pInt;
    }

    public void setpLong(final long pLong) {
        this.pLong = pLong;
    }

    public void setString(final String string) {
        this.string = string;
    }
}
