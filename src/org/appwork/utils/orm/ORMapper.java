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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;
import org.appwork.utils.storage.DBException;

/**
 * @author coalado TODO:
 * 
 *         wenn man eine instanc überschreibt, werden die crossreferenzen
 *         natürlich aktualisiert. Dabei können einträge ohne referenz übrig
 *         bleiben. bei einem update müsste man deshalb schauen ob man
 *         referenzen löst die damit verloren sind
 * 
 */
public class ORMapper {

    private HashMap<String, ClassConverter> converter;

    /**
     * Load database driver
     */
    static {

        try {
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    private Connection db;
    private HashMap<String, String> typeMap;

    public ORMapper() {
        typeMap = new HashMap<String, String>();
        converter = new HashMap<String, ClassConverter>();

        // typeMap.put(Boolean.class, "INT");

        typeMap.put("long", "BIGINT");
        typeMap.put("byte", "INT");
        typeMap.put("char", "INT");
        typeMap.put("int", "INT");
        typeMap.put("double", "FLOAT");
        typeMap.put("float", "FLOAT");
        initDB();

        converter.put(String.class.getName(), new StringConverter(db));
        converter.put(Long.class.getName(), new LongConverter(db));
        converter.put(Class.class.getName(), new ClassClassConverter(db));
    }

    /**
     * 
     */
    private void initDB() {

        try {
            db = DriverManager.getConnection("jdbc:hsqldb:file:" + Application.getRessource("/config/databases/orm") + ";shutdown=true", "sa", "");

            db.setAutoCommit(false);
            db.createStatement().executeUpdate("SET LOGSIZE 10");
            // add ShutdownHook so we have chance to save database properly
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {

                        ORMapper.this.close();

                    } catch (Throwable e) {
                        Log.exception(e);
                    }
                }
            });

        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * 
     */
    protected void close() {
        try {
            db.commit();
            db.close();
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ORMapper mapper = new ORMapper();

        TestClass item = new TestClass();
        TestClass item2 = new TestClass();
        item.stringD = "ZWQEITER";
        item.testClassA = item2;
        item2.ich = item;
        item.obj = new String("III");

        // mapper.store(item, null, null, null);

        Object restore = mapper.get(TestClass.class, 0, null);
        System.out.println(restore);

    }

    /**
     * @param class1
     * @param string
     * @return
     */

    private Object get(Class<?> clazz, String instanceID, HashMap<String, Object> idMap) {
        return getByWhere(clazz, " WHERE INSTANCEID = '" + instanceID + "'", idMap);
    }

    /**
     * @param type
     * @param id
     * @param idMap
     * @return
     */
    private Object get(Class<?> clazz, int id, HashMap<String, Object> idMap) {
        return getByWhere(clazz, " WHERE ID = '" + id + "'", idMap);
    }

    /**
     * @param clazz
     * @param string
     * @return
     */
    private Object getByWhere(Class<?> clazz, String where, HashMap<String, Object> idMap) {
        if (idMap == null) {
            idMap = new HashMap<String, Object>();
        }
        try {
            MappableClassID anno = clazz.getAnnotation(MappableClassID.class);
            String tableID = anno == null ? clazz.getName().replace(".", "_") : anno.value();
            // ArrayList<Field> fields =
            // this.getDeclaredFieldsToStore(clazz.getDeclaredFields());

            ClassConverter conv = converter.get(clazz.getName());
            if (conv != null) return conv.get(clazz, where);
            checkTableIntegraty(tableID, clazz);
            ResultSet rs;

            System.out.println("SELECT * FROM " + tableID + where);
            if (!(rs = db.prepareStatement("SELECT * FROM " + tableID + where).executeQuery()).next()) return null;
            String instanceID = rs.getString(2);
            Object instance = idMap.get(instanceID);
            if (instance != null) return instance;
            instance = clazz.newInstance();
            idMap.put(rs.getString(2), instance);
            Field instanceIDField = null;
            for (Field f : getFields(clazz)) {
                f.setAccessible(true);
                if (f.getAnnotation(InstanceID.class) != null) {
                    instanceIDField = f;
                    break;
                }
            }
            if (instanceIDField != null) instanceIDField.setAccessible(true);
            if (instanceIDField != null) instanceIDField.set(instance, rs.getString(2));

            Field f;
            for (int i = 4; i <= rs.getMetaData().getColumnCount(); i++) {
                f = getField(clazz, rs.getMetaData().getColumnName(i));
                f.setAccessible(true);
                if (f.getType().isArray()) {
                    f.set(instance, restoreArray(rs.getString(i), f.getType(), idMap));
                } else if (!f.getType().isPrimitive() && !Modifier.isTransient(f.getType().getModifiers())) {
                    try {
                        String[] reference = rs.getString(i).split(":");
                        ;
                        f.set(instance, getJavaValue(f, get(Class.forName(reference[0]), Integer.parseInt(reference[1]), idMap)));
                    } catch (NullPointerException e) {
                        f.set(instance, null);
                    }
                } else {
                    f.set(instance, getJavaValue(f, rs.getObject(i)));
                }

            }

            return instance;
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * @param idMap
     * @param string
     * @param class1
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     * @throws ArrayIndexOutOfBoundsException
     */
    private Object restoreArray(String table, Class<?> clazz, HashMap<String, Object> idMap) throws SQLException, ArrayIndexOutOfBoundsException, NumberFormatException, IllegalArgumentException, ClassNotFoundException {

        ResultSet rs = db.prepareStatement("SELECT * FROM " + table).executeQuery();

        ResultSet r = db.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
        r.next();
        int count = r.getInt(1);
        r.close();
        Object array = idMap.get(table);
        if (array != null) return array;
        array = Array.newInstance(clazz.getComponentType(), count);
        idMap.put(table, array);
        Class<?> ct = clazz.getComponentType();
        for (int i = 0; i < count; i++) {
            rs.next();
            if (clazz.getComponentType().isArray()) {

                Array.set(array, i, restoreArray(rs.getString(4), clazz.getComponentType(), idMap));
            } else if (!clazz.getComponentType().isPrimitive()) {
                try {
                    String[] reference = rs.getString(4).split(":");

                    Array.set(array, i, get(Class.forName(reference[0]), Integer.parseInt(reference[1]), idMap));
                } catch (NullPointerException e) {
                    Array.set(array, i, null);
                }

            } else {

                String type = rs.getString(3);
                Object obj = rs.getObject(4);
                Array.set(array, i, obj);

            }
        }
        return array;
    }

    /**
     * @param obj
     * @param type
     * @return
     */
    private Object getPrimitive(Object obj, String type) {
        if (Number.class.isAssignableFrom(obj.getClass())) { return ((Number) obj).longValue(); }

        return obj;
    }

    /**
     * @param f
     * @param object
     * @return
     */
    private Object getJavaValue(Field f, Object object) {
        // casting would fail here
        if (f.getType().isPrimitive()) return object;
        return f.getType().cast(object);
    }

    /**
     * @param item
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    private int store(Object item, HashMap<Object, Integer> saved, HashMap<Object, Object> rewrite, HashMap<Object, Integer> finalSaved) {

        try {

            String instanceID = null;
            // table is created, or verified as ok noew

            for (Field f : getFields(item.getClass())) {

                f.setAccessible(true);
                if (f.getAnnotation(InstanceID.class) != null) {
                    instanceID = (String) f.get(item);
                }
            }
            if (instanceID == null) {
                instanceID = "T" + item.hashCode() + "";
                // throw new DBException("COuld not find instance id");
            }

            String tableID = checkTable(item);
            boolean doRewrites = false;
            if (saved == null) {
                doRewrites = true;
                saved = new HashMap<Object, Integer>();
            }
            if (rewrite == null) {

                rewrite = new HashMap<Object, Object>();
            }
            if (finalSaved == null) {

                finalSaved = new HashMap<Object, Integer>();

            }
            int ret = write(tableID, item, instanceID, saved, rewrite, finalSaved);
            if (doRewrites) {
                System.out.println("LAST RUOND");
            }
            for (Iterator<Entry<Object, Object>> it = rewrite.entrySet().iterator(); it.hasNext();) {
                Entry<Object, Object> next = it.next();
                if (next.getValue() == item) {
                    System.out.println("Reqrite: " + next.getKey());

                    saved.remove(next.getKey());
                    if (next.getKey().getClass().isArray()) {
                        this.storeArray(null, next.getKey().getClass(), next.getKey(), saved, null, finalSaved);
                    } else {
                        store(next.getKey(), saved, null, finalSaved);
                    }
                }

            }
            return ret;

        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * @param class1
     * @return
     */
    private Field[] getFields(Class<? extends Object> clazz) {
        ArrayList<Field> ret = new ArrayList<Field>();

        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                ret.add(f);
            }
            clazz = clazz.getSuperclass();
        }
        return ret.toArray(new Field[] {});
    }

    /**
     * @param tableID
     * @param item
     * @param instanceID
     * @param saved
     * @param rewrite
     * @param finalSaved
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    private int write(String tableID, Object item, String instanceID, HashMap<Object, Integer> saved, HashMap<Object, Object> rewrite, HashMap<Object, Integer> finalSaved) throws SQLException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {

        if (db.prepareStatement("SELECT * FROM " + tableID + " WHERE INSTANCEID = '" + instanceID + "'").executeQuery().next()) {

            ClassConverter conv = converter.get(item.getClass().getName());
            if (conv != null) return conv.update(tableID, item, instanceID);
            // row id is already determined

            if (saved.containsKey(item)) {
                // loop reference

                return saved.get(item);
            }
            saved.put(item, -1);
            // instanceod is present. just update
            StringBuilder sb = new StringBuilder();

            ArrayList<DBTableColumn> columns = getDBTableColumns(tableID);
            sb.append("UPDATE ");
            sb.append(tableID);
            sb.append(" SET ");
            Field f;
            for (int i = 3; i < columns.size(); i++) {
                f = getField(item.getClass(), columns.get(i).getColumnName());
                if (f.getType().isArray()) {
                    f.setAccessible(true);
                    String table = storeArray(instanceID + "__" + f.getName(), f.getType(), f.get(item), saved, rewrite, finalSaved);

                    sb.append("\"");
                    sb.append(f.getName());
                    sb.append("\"");
                    sb.append("='");
                    sb.append(table);
                    sb.append("'");
                    if (i < columns.size() - 1) {
                        sb.append(",");
                    }
                } else if (!f.getType().isPrimitive() && !Modifier.isTransient(f.getType().getModifiers())) {
                    Object cross = (Object) getSQLValue(f, item);

                    if (cross != null) {

                        int rw;
                        // special for self references
                        if (cross == item) {
                            if (finalSaved.containsKey(cross)) {
                                cross = "'" + cross.getClass().getName() + ":" + (rw = finalSaved.get(cross)) + "'";
                            } else {
                                cross = "'" + cross.getClass().getName() + ":" + (rw = store(cross, saved, rewrite, finalSaved)) + "'";
                            }
                        } else {
                            cross = "'" + cross.getClass().getName() + ":" + (rw = store(cross, saved, rewrite, finalSaved)) + "'";
                        }

                        if (rw < 0) {
                            rewrite.put(item, (Object) getSQLValue(f, item));
                        }
                    }

                    sb.append("\"");
                    sb.append(f.getName());
                    sb.append("\"");
                    sb.append("=");
                    sb.append(cross);
                    if (i < columns.size() - 1) {
                        sb.append(",");
                    }
                } else {

                    sb.append("\"");
                    sb.append(f.getName());
                    sb.append("\"");
                    sb.append("='");
                    sb.append(getSQLValue(f, item));
                    sb.append("'");
                    if (i < columns.size() - 1) {
                        sb.append(",");
                    }
                }

            }
            // item may have be saved through a loopback reference
            if (saved.get(item) >= 0) {

                // loop reference

                return saved.get(item);
            }
            sb.append(" WHERE INSTANCEID = '");
            sb.append(instanceID);
            sb.append("'");
            // there must be a better way
            System.err.println(sb);
            db.prepareStatement(sb.toString()).execute();

            ResultSet rs;
            (rs = db.prepareStatement("SELECT id FROM " + tableID + " WHERE INSTANCEID ='" + instanceID + "'").executeQuery()).next();

            int ret;
            saved.put(item, ret = rs.getInt(1));
            // self references
            finalSaved.put(item, ret);
            return ret;
        } else {

            ClassConverter conv = converter.get(item.getClass().getName());
            if (conv != null) return conv.write(tableID, item, instanceID);

            // new entry
            if (saved.containsKey(item)) {

                // loop reference

                return saved.get(item);
            }
            saved.put(item, -1);
            StringBuilder sb = new StringBuilder();

            ArrayList<DBTableColumn> columns = getDBTableColumns(tableID);
            sb.append("INSERT INTO ");
            sb.append(tableID);
            sb.append(" VALUES (?,?,?");
            for (int i = 3; i < columns.size(); i++) {
                sb.append(",");
                sb.append("?");
            }
            sb.append(")");

            PreparedStatement insertStatement = db.prepareStatement(sb.toString());
            insertStatement.setObject(2, instanceID);
            Field f;

            for (int i = 3; i < columns.size(); i++) {
                f = getField(item.getClass(), columns.get(i).getColumnName());
                f.setAccessible(true);
                if (f.getType().isArray()) {

                    String table = storeArray(instanceID + "__" + f.getName(), f.getType(), f.get(item), saved, rewrite, finalSaved);
                    insertStatement.setObject(i + 1, table);

                } else if (!f.getType().isPrimitive() && !Modifier.isTransient(f.getType().getModifiers())) {
                    Object cross = (Object) getSQLValue(f, item);
                    if (cross != null) {

                        int rw;

                        if (cross == item) {
                            if (finalSaved.containsKey(cross)) {
                                cross = cross.getClass().getName() + ":" + +(rw = finalSaved.get(cross));
                            } else {
                                cross = cross.getClass().getName() + ":" + +(rw = store(cross, saved, rewrite, finalSaved));
                            }
                        } else {
                            cross = cross.getClass().getName() + ":" + +(rw = store(cross, saved, rewrite, finalSaved));
                        }

                        if (rw < 0) {

                            rewrite.put(item, (Object) getSQLValue(f, item));
                        }
                    }
                    insertStatement.setObject(i + 1, cross);
                } else {
                    insertStatement.setObject(i + 1, getSQLValue(f, item));
                }
            }
            // item may have be saved through a loopback reference
            if (saved.get(item) >= 0) {

                // loop reference

                return saved.get(item);
            }
            insertStatement.execute();

            System.err.println(insertStatement);
            // there must be a better way
            ResultSet rs;
            (rs = db.prepareStatement("SELECT id FROM " + tableID + " WHERE INSTANCEID ='" + instanceID + "'").executeQuery()).next();
            int ret;
            saved.put(item, ret = rs.getInt(1));
            // self references
            finalSaved.put(item, ret);
            return ret;
        }
    }

    /**
     * @param class1
     * @param columnName
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    private Field getField(Class<? extends Object> clazz, String name) throws SecurityException, NoSuchFieldException {
        Field ret = null;
        while (clazz != null && ret == null) {
            try {
                ret = clazz.getDeclaredField(name);
            } catch (java.lang.NoSuchFieldException e) {

            }
            clazz = clazz.getSuperclass();
        }
        return ret;
    }

    /**
     * @param table
     * @param cast
     * @param object
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    private String storeArray(String table, Class<?> clazz, Object object, HashMap<Object, Integer> saved, HashMap<Object, Object> rewrite, HashMap<Object, Integer> finalSaved) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, SQLException {
        if (object == null) return null;

        if (saved == null) saved = new HashMap<Object, Integer>();

        if (rewrite == null) {
            rewrite = new HashMap<Object, Object>();

        }

        if (saved.containsKey(object)) { return table; }
        saved.put(object, -1);
        table = table.toUpperCase();
        table = this.checkTable(clazz, table);
        clearTable(table);
        Class<?> type = clazz.getComponentType();

        StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ");
        sb.append(table);
        sb.append(" VALUES (?,?,?,?)");
        PreparedStatement insertStatement = db.prepareStatement(sb.toString());

        Object obj;
        for (int i = 0; i < Array.getLength(object); i++) {
            insertStatement.setInt(1, i);
            obj = Array.get(object, i);
            insertStatement.setString(3, type.getName());
            if (type.isArray()) {
                String sub = storeArray(table + "__" + i, type, obj, saved, rewrite, finalSaved);
                insertStatement.setObject(4, sub);
            } else if (!type.isPrimitive()) {
                Object cross = (Object) getSQLValue(type, obj);

                if (cross != null) {

                    int rw;
                    cross = "'" + cross.getClass().getName() + ":" + +(rw = store(cross, saved, rewrite, finalSaved)) + "'";
                    if (rw < 0) rewrite.put(object, (Object) getSQLValue(type, obj));
                }

                insertStatement.setObject(4, cross);
            } else {
                insertStatement.setObject(4, getSQLValue(type, obj));
            }
            insertStatement.execute();
            System.err.println(insertStatement);
        }
        finalSaved.put(object, -1);
        return table;
    }

    /**
     * @param table
     * @throws SQLException
     */
    private void clearTable(String table) throws SQLException {
        // TODO: better clear statement
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(table);
        sb.append(" WHERE ID >= '0'");
        System.err.println(sb);
        db.createStatement().executeQuery(sb.toString());

    }

    /**
     * @param tableID
     * @return
     * @throws SQLException
     */
    private ArrayList<DBTableColumn> getDBTableColumns(String tableID) throws SQLException {
        ResultSet rs = db.prepareStatement("SELECT * FROM " + tableID).executeQuery();
        ArrayList<DBTableColumn> ret = new ArrayList<DBTableColumn>();
        // create column name-id map
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            ret.add(new DBTableColumn(i, rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnType(i), rs.getMetaData().getColumnTypeName(i), rs.getMetaData().getColumnClassName(i)));
        }
        return ret;
    }

    /**
     * @param field
     * @param item
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private Object getSQLValue(Field field, Object item) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        Object obj = field.get(item);

        return getSQLValue(field.getType(), obj);
    }

    /**
     * @param type
     * @param obj
     * @return
     */
    private Object getSQLValue(Class<?> type, Object obj) {
        // add converter here
        return obj;
    }

    /**
     * @param item
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SQLException
     */
    private String checkTable(Object item) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException {
        return checkTable(item.getClass(), null);

    }

    private String checkTable(Class<?> clazz, String tableID) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException {
        if (tableID == null) {
            MappableClassID anno = clazz.getAnnotation(MappableClassID.class);
            tableID = anno == null ? clazz.getName().replace(".", "_") : anno.value();
        }

        if (!tableExists(tableID)) {
            return createTable(tableID, clazz);
        } else {
            // check inte.
            checkTableIntegraty(tableID, clazz);
        }

        return tableID;
    }

    /**
     * @param tableID
     * @param class1
     * @throws SQLException
     */
    private void checkTableIntegraty(String tableID, Class<?> clazz) throws SQLException {

        ClassConverter conv = converter.get(clazz.getName());
        if (conv != null) {
            conv.checkIntegrety(tableID);
            return;
        }

        ResultSet rs;
        rs = db.prepareStatement("SELECT * FROM " + tableID).executeQuery();

        HashMap<String, Field> fields = getDeclaredFieldsToStore(this.getFields(clazz));
        HashMap<String, Integer> columnmap = new HashMap<String, Integer>();
        // create column name-id map
        for (int i = 3; i <= rs.getMetaData().getColumnCount(); i++) {
            columnmap.put(rs.getMetaData().getColumnName(i), i);
        }
        Field next;
        Integer id;
        for (Iterator<Field> it = fields.values().iterator(); it.hasNext();) {
            next = it.next();
            id = columnmap.get(next.getName());
            if (id == null) { throw new ConflictTableException("Column " + next.getName() + " in " + tableID + " is not present ");
            // we could autoenhance the column here
            }
            if (!typeCheck(rs.getMetaData().getColumnType(id), next.getType())) {
                // this is a bit tricky. we could try to implement external
                // converter for such issues
                throw new ConflictTableException("Column " + next.getName() + " in " + tableID + " has an incorrect type");
            }

        }

    }

    /**
     * @param columnType
     * @param type
     * @return
     */
    private boolean typeCheck(int columnType, Class<?> type) {

        return true;
    }

    /**
     * @param tableID
     * @param clazz
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    private String createTable(String tableID, Class<?> clazz) throws SQLException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        ClassConverter conv = converter.get(clazz.getName());
        if (conv != null) return conv.createTable(tableID);
        StringBuilder sb = new StringBuilder();
        HashMap<String, Field> fields = this.getDeclaredFieldsToStore(getFields(clazz));
        sb.append("CREATE TABLE ");
        sb.append(tableID);
        sb.append(" ( ID INT IDENTITY ,INSTANCEID LONGVARCHAR,CLASSID LONGVARCHAR");

        if (clazz.isArray()) {

            sb.append(", ELEMENT ");

            if (clazz.getComponentType().isArray()) {
                sb.append("LONGVARCHAR");

            } else if (!clazz.getComponentType().isPrimitive()) {
                sb.append("LONGVARCHAR");

            } else {
                sb.append(javaToSQLType(clazz.getComponentType()));
            }
            sb.append(")");
        } else {
            Field next;
            for (Iterator<Field> it = fields.values().iterator(); it.hasNext();) {
                next = it.next();

                if (next.getType().isArray()) {
                    // für arrays wird eine eigene table angelegt
                    sb.append(" ,\"");
                    sb.append(next.getName());
                    sb.append("\" LONGVARCHAR");

                } else if (!next.getType().isPrimitive()) {
                    sb.append(" ,\"");
                    sb.append(next.getName());
                    sb.append("\" ");

                    sb.append("LONGVARCHAR");

                } else {
                    sb.append(" ,\"");
                    sb.append(next.getName());
                    sb.append("\" ");

                    sb.append(javaToSQLType(next.getType()));
                }

            }

            sb.append(")");
        }

        System.err.println(sb.toString());
        db.createStatement().executeUpdate(sb.toString());

        return tableID;
    }

    /**
     * @param type
     * @return
     */
    private String javaToSQLType(Class<?> type) {

        return typeMap.get(type.getName());
    }

    /**
     * @param fields
     * @return
     */
    private HashMap<String, Field> getDeclaredFieldsToStore(Field[] fields) {
        HashMap<String, Field> ret = new HashMap<String, Field>();

        for (Field f : fields) {
            // filter instanceid
            if (f.getAnnotation(InstanceID.class) != null) {
                continue;
            }
            // no transients
            if (Modifier.isTransient(f.getModifiers())) {
                continue;
            }
            // no incompatible class types
            if (!isClassCompatible(f.getType())) {
                continue;
            }
            // no statics
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            ret.put(f.getName(), f);

        }
        return ret;
    }

    /**
     * @param type
     * @return
     */
    private boolean isClassCompatible(Class<?> type) {

        if (type == String.class) return true;
        // if (Number.class.isAssignableFrom(type)) return true;
        if (type.isPrimitive()) return true;
        if (type.isArray()) return true;

        return true;
    }

    /**
     * CHecks if a table exists
     * 
     * @param tablename
     *            name of the table
     * @return
     * @throws SQLException
     */
    boolean tableExists(String tablename) {

        ResultSet rs;
        try {
            rs = db.getMetaData().getTables(null, null, tablename.toUpperCase(), null);
            return rs.next();
        } catch (SQLException e1) {

            throw new DBException(e1);
        }

    }

    /**
     * @param a
     */
    public void store(Object a) {
        store(a, null, null, null);
    }
}
