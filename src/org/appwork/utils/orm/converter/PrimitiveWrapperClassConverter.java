/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.orm
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.orm.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.appwork.utils.orm.MappableClassID;

/**
 * @author coalado
 * 
 */
public abstract class PrimitiveWrapperClassConverter extends ClassConverter {

    protected String type;

    /**
     * @param db
     */
    public PrimitiveWrapperClassConverter(String type) {

        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.orm.ClassConverter#checkIntegrety(java.lang.String)
     */
    @Override
    public void checkIntegrety(String tableID) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.orm.ClassConverter#createTable(java.lang.String)
     */
    @Override
    public String createTable(String tableID) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ");
        sb.append(tableID);
        sb.append(" ( ID INT IDENTITY ,INSTANCEID LONGVARCHAR,VALUE " + type + ") ");

        db.createStatement().executeUpdate(sb.toString());

        return tableID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.orm.ClassConverter#get(java.lang.Class, int)
     */
    public Object get(Class<?> clazz, String where) throws SQLException {
        MappableClassID anno = clazz.getAnnotation(MappableClassID.class);
        String tableID = anno == null ? clazz.getName().replace(".", "_") : anno.value();
        ResultSet rs;
        rs = db.prepareStatement("SELECT * FROM " + tableID + where).executeQuery();
        boolean b = rs.next();

        return rs.getObject(3);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.orm.ClassConverter#update(java.lang.String,
     * java.lang.Object, java.lang.String)
     */

    /**
     * @param tableID
     * @param item
     * @param instanceID
     * @return
     * @throws SQLException
     */
    public int update(String tableID, Object item, String instanceID) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ");
        sb.append(tableID);
        sb.append(" SET ");

        sb.append("VALUE");
        sb.append("");
        sb.append("='");
        sb.append(getValue(item));
        sb.append("'");

        sb.append(" WHERE INSTANCEID = '");
        sb.append(instanceID);
        sb.append("'");
        // there must be a better way

        db.prepareStatement(sb.toString()).execute();

        ResultSet rs;
        (rs = db.prepareStatement("SELECT id FROM " + tableID + " WHERE INSTANCEID ='" + instanceID + "'").executeQuery()).next();

        return rs.getInt(1);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.orm.ClassConverter#write(java.lang.String,
     * java.lang.Object, java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.orm.ClassConverter#write(java.lang.String,
     * java.lang.Object, java.lang.String)
     */
    @Override
    public int write(String tableID, Object item, String instanceID) throws SQLException {
        // new entry
        StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ");
        sb.append(tableID);
        sb.append(" VALUES (?,?,?)");

        PreparedStatement insertStatement = db.prepareStatement(sb.toString());
        insertStatement.setObject(2, instanceID);
        insertStatement.setObject(3, getValue(item));
        insertStatement.execute();
        // there must be a better way
        ResultSet rs;
        (rs = db.prepareStatement("SELECT id FROM " + tableID + " WHERE INSTANCEID ='" + instanceID + "'").executeQuery()).next();

        return rs.getInt(1);
    }

    /**
     * @param item
     * @return
     */
    abstract protected Object getValue(Object item);

}
