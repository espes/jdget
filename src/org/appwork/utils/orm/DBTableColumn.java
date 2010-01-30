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
public class DBTableColumn {

    private int i;
    private String columnName;
    private int columnType;

    /**
     * @return the i
     */
    public int getI() {
        return i;
    }

    /**
     * @param i
     *            the i to set
     */
    public void setI(int i) {
        this.i = i;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param columnName
     *            the columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * @return the columnType
     */
    public int getColumnType() {
        return columnType;
    }

    /**
     * @param columnType
     *            the columnType to set
     */
    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    /**
     * @return the columnTypeName
     */
    public String getColumnTypeName() {
        return columnTypeName;
    }

    /**
     * @param columnTypeName
     *            the columnTypeName to set
     */
    public void setColumnTypeName(String columnTypeName) {
        this.columnTypeName = columnTypeName;
    }

    /**
     * @return the columnClassName
     */
    public String getColumnClassName() {
        return columnClassName;
    }

    /**
     * @param columnClassName
     *            the columnClassName to set
     */
    public void setColumnClassName(String columnClassName) {
        this.columnClassName = columnClassName;
    }

    private String columnTypeName;
    private String columnClassName;

    /**
     * @param i
     * @param columnName
     * @param columnType
     * @param columnTypeName
     * @param columnClassName
     */
    public DBTableColumn(int i, String columnName, int columnType, String columnTypeName, String columnClassName) {
        this.i = i;
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnTypeName = columnTypeName;
        this.columnClassName = columnClassName;
    }

}
