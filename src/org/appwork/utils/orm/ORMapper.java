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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;
import org.appwork.utils.orm.adapter.ArrayListAdapter;
import org.appwork.utils.orm.adapter.ClassAdapter;
import org.appwork.utils.orm.converter.ClassClassConverter;
import org.appwork.utils.orm.converter.ClassConverter;
import org.appwork.utils.orm.converter.DateClassConverter;
import org.appwork.utils.orm.converter.LongConverter;
import org.appwork.utils.orm.converter.StringConverter;
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
    /**
     * Database connection. hsqldb in this case
     */
    private Connection db;
    /**
     * maps java primitive types to SQL types
     */
    private HashMap<String, String> typeMap;
    private HashMap<Class<?>, ClassAdapter> adapter;
    private String name;

    public ORMapper() {
        this("orm");

    }

    /**
     * @param string
     */
    public ORMapper(String dbname) {
        typeMap = new HashMap<String, String>();
        converter = new HashMap<String, ClassConverter>();
        adapter = new HashMap<Class<?>, ClassAdapter>();

        // typeMap.put(Boolean.class, "INT");

        typeMap.put("long", "BIGINT");
        typeMap.put("byte", "TINYINT");
        typeMap.put("char", "INT");
        typeMap.put("int", "INT");
        typeMap.put("double", "FLOAT");
        typeMap.put("float", "FLOAT");
        initDB(dbname);
        initDefaultConverter();
        initAdapter();
    }

    /**
     * 
     */
    private void initAdapter() {
        addAdapter(ArrayList.class, new ArrayListAdapter());
    }

    /**
     * @param class1
     * @param arrayListAdapter
     */
    private void addAdapter(Class<?> class1, ClassAdapter ca) {
        adapter.put(class1, ca);
        ca.setOwner(this);
    }

    private void initDefaultConverter() {

        addConverter(String.class, new StringConverter());
        addConverter(Long.class, new LongConverter());
        addConverter(Class.class, new ClassClassConverter());
        addConverter(Date.class, new DateClassConverter());

    }

    /**
     * /** The mapper is able to use so called converter. these converter define
     * how to
     * 
     * @param class1
     * @param stringConverter
     */
    public void addConverter(Class<?> class1, ClassConverter classConverter) {
        converter.put(class1.getName(), classConverter);
        classConverter.setOwner(this);
        classConverter.setDb(db);

    }

    /**
     * @param dbname
     * 
     */
    private void initDB(String dbname) {

        try {
            this.name = dbname;
            db = DriverManager.getConnection("jdbc:hsqldb:file:" + Application.getRessource("/config/databases/" + dbname) + ";shutdown=true", "sa", "");

            db.setAutoCommit(false);
            db.createStatement().executeUpdate("SET LOGSIZE 10");
            // add ShutdownHook so we have chance to save database properly
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Log.L.finer("Saved mapper " + name);
                        ORMapper.this.close();
                    } catch (Throwable e) {

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
    public void close() {
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
        mapper.clear();
        // create a test object structure
        // TestClass item = new TestClass();
        // item.stringD = "affenhaut";
        // item.intArrayE = new int[] { 1, 2, 3, 4, 5 };
        // item.doubleInt = new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 } };
        // item.instanceID = "Mainitem";
        // TestClass item2 = new TestClass();
        // item2.instanceID = "Childitem";
        // item2.stringD = "babla";
        // item2.intArrayE = new int[] { 6, 4, 7 };
        // item2.doubleInt = new int[][] { { 10, 20 }, { 20, 30 }, { 40, 50 } };
        // item.stringD = "ZWQEITER";
        // item.testClassA = item2;
        // item2.ich = item;
        // ArrayList<String> list = new ArrayList<String>();
        // list.add("aaa");
        // list.add("bbb");
        // list.add("ccc");
        // item.obj = list;
        //
        // // store it
        // mapper.store(item);
        // // reswtore it from db
        // TestClass restore = (TestClass) mapper.get(TestClass.class, 0, null);
        // // these testcases have to print TRUE
        // // compare
        // System.out.println("Restore Strings: " +
        // restore.stringD.equals(item.stringD));
        // System.out.println("Restore Reference Loops: " +
        // (restore.testClassA.ich == restore));
        // System.out.println("Restore SelfReference Loops: " + (restore.ich ==
        // restore));
        // // System.out.println("Restore Childclass types: " +
        // // (restore.obj.class.equals(item.obj.class)));
        // System.out.println("Restore Deep arrays: " +
        // Arrays.deepToString(restore.doubleInt).equals(Arrays.deepToString(item.doubleInt)));
        // System.out.println("Restore instanceids: " +
        // restore.instanceID.equals(item.instanceID) + " & Deep :" +
        // restore.testClassA.instanceID.equals(item.testClassA.instanceID));

    }

    /**
     * Restore a instance of clazz with the instanceID
     * 
     * @param clazz
     * @param instanceID
     * @return
     */
    public Object get(Class<?> clazz, String instanceID) {
        return getByWhere(clazz, " WHERE INSTANCEID = '" + instanceID + "'", null);
    }

    /**
     * @param type
     * @param id
     * @param idMap
     *            stores all new instances to restore create referenceloops;
     *            Null for the first call
     * @return
     */
    private Object get(Class<?> clazz, int id, HashMap<String, Object> idMap) {
        return getByWhere(clazz, " WHERE ID = '" + id + "'", idMap);
    }

    /**
     * Returns a new instance of type clazz with there given whereclause.
     * 
     * @param clazz
     * @param where
     * @param idMap
     *            stores all new instances to restore create referenceloops;
     *            Null for the first call
     * @return
     */
    private Object getByWhere(Class<?> clazz, String where, HashMap<String, Object> idMap) {
        if (idMap == null) {
            idMap = new HashMap<String, Object>();
        }
        try {
            // get tableid either by MappableClassID or the classname
            MappableClassID anno = clazz.getAnnotation(MappableClassID.class);
            String tableID = anno == null ? clazz.getName().replace(".", "_") : anno.value();
            // use converter for the given class if there is one
            ClassConverter conv = converter.get(clazz.getName());
            if (conv != null) return conv.get(clazz, where);
            // checktable if table has errors
            checkTableIntegrity(tableID, clazz);
            // get result row from the classtable
            ResultSet rs;
            if (!(rs = db.prepareStatement("SELECT * FROM " + tableID + where).executeQuery()).next()) return null;
            // instanceid is unique across sessions
            String instanceID = rs.getString(2);
            // check if we have already restored this instanceid
            Object instance = idMap.get(instanceID);
            if (instance != null) return instance;
            // create new instance
            instance = clazz.newInstance();
            idMap.put(rs.getString(2), instance);
            // get instanceid field by InstanceID annotation
            Field instanceIDField = null;
            for (Field f : getFields(clazz)) {
                f.setAccessible(true);
                if (f.getAnnotation(InstanceID.class) != null) {
                    instanceIDField = f;
                    break;
                }
            }
            // if found, store instanceid to new instance
            if (instanceIDField != null) instanceIDField.setAccessible(true);
            if (instanceIDField != null) instanceIDField.set(instance, rs.getString(2));

            // set all fields defined by the table
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
            e.printStackTrace();
            if (e.getMessage().startsWith("Table not found in statement")) { return null; }
            throw new DBException(e);
        }
    }

    /**
     * Restores an Array
     * 
     * @param idMap
     *            stores all new instances to restore create referenceloops;
     *            Null for the first call
     * @param table
     *            Tablename for the array
     * @param class1
     *            ArrayClass
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     * @throws ArrayIndexOutOfBoundsException
     */
    private Object restoreArray(String table, Class<?> clazz, HashMap<String, Object> idMap) throws SQLException, ArrayIndexOutOfBoundsException, NumberFormatException, IllegalArgumentException, ClassNotFoundException {
        // get whole table
        ResultSet rs = db.prepareStatement("SELECT * FROM " + table).executeQuery();
        // get row count
        ResultSet r = db.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
        r.next();
        int count = r.getInt(1);
        r.close();
        // create idMap entry or return fi the array has already be restored
        Object array = idMap.get(table);
        if (array != null) return array;
        // create new Array
        array = Array.newInstance(clazz.getComponentType(), count);
        idMap.put(table, array);
        // restore all Arrayentries
        for (int i = 0; i < count; i++) {
            rs.next();
            if (clazz.getComponentType().isArray()) {
                // array is multiDimensional. retsore a new array for each entry
                Array.set(array, i, restoreArray(rs.getString(4), clazz.getComponentType(), idMap));
            } else if (!clazz.getComponentType().isPrimitive()) {
                // is a object. use object getter
                try {
                    String[] reference = rs.getString(4).split(":");
                    Array.set(array, i, get(Class.forName(reference[0]), Integer.parseInt(reference[1]), idMap));
                } catch (NullPointerException e) {
                    Array.set(array, i, null);
                }

            } else {
                // primitive type

                Array.set(array, i, rs.getObject(4));

            }
        }
        return array;
    }

    /**
     * Casts the object to the right class. Has no direct use. Just checks for
     * integrity
     * 
     * @param f
     * @param object
     * @return
     */
    private Object getJavaValue(Field f, Object object) {
        // casting would fail here

        if (f.getType().isPrimitive()) {
            if (object instanceof Integer) return (int) ((Integer) object).intValue();
            if (object instanceof Long) return (long) ((Long) object).longValue();
            if (object instanceof Float) return (float) ((Float) object).floatValue();
            if (object instanceof Double) return (double) ((Double) object).doubleValue();
            if (object instanceof Byte) return (byte) ((Byte) object).byteValue();
            if (object instanceof Character) return (char) ((Character) object).charValue();
            return object;
        }
        return f.getType().cast(object);
    }

    /**
     * Store item to the table
     * 
     * @param item
     * @param saved
     *            internal use. stores all items that have started to store
     * @param rewrite
     *            internal use. stores all items which have a missing reference.
     *            Have to be rewritten
     * @param finalSaved
     *            internal use. stores all items least at least one full write
     *            cycle
     * @param instanceID
     * @return
     */
    private int store(Object item, HashMap<Object, Integer> saved, HashMap<Object, Object> rewrite, HashMap<Object, Integer> finalSaved, String instanceID) {

        try {
            System.out.println(item);

            // get Instanceid
            // instance id can be givven by a String with InstanceID Annotation,
            // or it is outbuild by hashcode
            if (instanceID == null) {
                for (Field f : getFields(item.getClass())) {

                    f.setAccessible(true);
                    if (f.getAnnotation(InstanceID.class) != null) {
                        instanceID = (String) f.get(item);
                    }
                }
                if (instanceID == null) {
                    instanceID = "T" + Math.abs(item.hashCode()) + "";
                    // throw new DBException("COuld not find instance id");
                }
            }
            // check table integrity or create anew table.
            String tableID = checkTable(item);

            // init loop controller maps
            if (saved == null) {
                saved = new HashMap<Object, Integer>();
            }
            if (rewrite == null) {
                rewrite = new HashMap<Object, Object>();
            }
            if (finalSaved == null) {
                finalSaved = new HashMap<Object, Integer>();

            }
            // write item
            int ret = write(tableID, item, instanceID, saved, rewrite, finalSaved);
            // check if there are rewrite requests
            // rewrites may be required if there are missing references after
            // the first write cycle
            for (Iterator<Entry<Object, Object>> it = rewrite.entrySet().iterator(); it.hasNext();) {
                Entry<Object, Object> next = it.next();
                if (next.getValue() == item) {
                    Integer rowID = -2;
                    Integer rowID2 = -2;
                    if ((saved.containsKey(next.getValue()) && (rowID = saved.get(next.getValue())) >= 0) || ((finalSaved.containsKey(next.getValue()) && (rowID2 = finalSaved.get(next.getValue())) >= 0))) {
                        saved.remove(next.getKey());
                        if (next.getKey().getClass().isArray()) {
                            storeArray(null, next.getKey().getClass(), next.getKey(), saved, null, finalSaved);
                        } else {
                            store(next.getKey(), saved, null, finalSaved, null);
                        }
                    }
                }

            }
            return ret;

        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * Runs down the Class hirarchy and returns All fields of the given clazz
     * 
     * @param class1
     * @return
     */
    public Field[] getFields(Class<? extends Object> clazz) {
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
     * Write method. writes item to tableID with instanceID
     * 
     * @param tableID
     * @param item
     * @param instanceID
     * @param saved
     * @see #store(Object, HashMap, HashMap, HashMap)
     * @param rewrite
     * @see #store(Object, HashMap, HashMap, HashMap)
     * @param finalSaved
     * @see #store(Object, HashMap, HashMap, HashMap)
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private int write(String tableID, Object item, String instanceID, HashMap<Object, Integer> saved, HashMap<Object, Object> rewrite, HashMap<Object, Integer> finalSaved) throws SQLException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {

        // checks if there is a tableentry for instanceif
        if (db.prepareStatement("SELECT * FROM " + tableID + " WHERE INSTANCEID = '" + instanceID + "'").executeQuery().next()) {
            // if yes, update id
            // check if there are classconverter and use them
            ClassConverter conv = converter.get(item.getClass().getName());
            if (conv != null) return conv.update(tableID, item, instanceID);

            if (saved.containsKey(item)) {
                // saving for item is in progress. Probably we have reference
                // conflict which can only be solved by rewriting.
                // saved may also contain the final rowid.
                // however, item is already saved, so we return the saved rowID

                return saved.get(item);
            }
            // putting -1 as rowid. this indicates, that saving is in progress,
            // but not finished yet.
            // value is >=0 if saving is finished
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
                    // update array. this means rebuilding the wholoe array
                    // table
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
                    // object. we run recursive through the object tree
                    Object cross = (Object) getSQLValue(f, item);
                    if (cross != null) {
                        int rw;
                        // special for self references
                        if (cross == item) {
                            if (finalSaved.containsKey(cross)) {
                                cross = "'" + cross.getClass().getName() + ":" + (rw = finalSaved.get(cross)) + "'";
                            } else {
                                cross = "'" + cross.getClass().getName() + ":" + (rw = store(cross, saved, rewrite, finalSaved, null)) + "'";
                            }
                        } else {
                            cross = "'" + cross.getClass().getName() + ":" + (rw = store(cross, saved, rewrite, finalSaved, null)) + "'";
                        }
                        // could not get a final rowid for cross. this means
                        // that we have to rewrite item
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
                } else if (f.getType().isPrimitive()) {
                    // things are simple for primitive types
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
            // do another check before performing the statement
            if (saved.get(item) >= 0) {
                // loop reference
                return saved.get(item);
            }
            sb.append(" WHERE INSTANCEID = '");
            sb.append(instanceID);
            sb.append("'");
            // get autocreated rowID (autoincrement)
            // there must be a better way

            Log.L.finer(sb.toString());
            db.prepareStatement(sb.toString()).execute();

            ResultSet rs;
            (rs = db.prepareStatement("SELECT id FROM " + tableID + " WHERE INSTANCEID ='" + instanceID + "'").executeQuery()).next();

            int ret;
            // put final rowid to saved map
            saved.put(item, ret = rs.getInt(1));
            // first writecycle finished. put id to finalSaved
            finalSaved.put(item, ret);
            return ret;
        } else {
            // no entry. create a new one
            // first check for converters
            ClassConverter conv = converter.get(item.getClass().getName());
            if (conv != null) return conv.write(tableID, item, instanceID);

            // return cached rowid if storage of item si in progress
            if (saved.containsKey(item)) {
                // loop reference
                return saved.get(item);
            }
            // put -1 as ropwid to saved. indicating that storage is in progress
            saved.put(item, -1);
            // build statement
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
                    // is an array.
                    // create array table and store string reference (tablename)
                    // to current table
                    insertStatement.setObject(i + 1, storeArray(instanceID + "__" + f.getName(), f.getType(), f.get(item), saved, rewrite, finalSaved));

                } else if (!f.getType().isPrimitive() && !Modifier.isTransient(f.getType().getModifiers())) {
                    // is a non transient object. run recursive throu object
                    // tree and return resulting row id
                    Object cross = (Object) getSQLValue(f, item);
                    if (cross != null) {

                        int rw;
                        // special for self references
                        if (cross == item) {
                            if (finalSaved.containsKey(cross)) {
                                cross = cross.getClass().getName() + ":" + +(rw = finalSaved.get(cross));
                            } else {
                                cross = cross.getClass().getName() + ":" + +(rw = store(cross, saved, rewrite, finalSaved, null));
                            }
                        } else {
                            cross = cross.getClass().getName() + ":" + +(rw = store(cross, saved, rewrite, finalSaved, null));
                        }

                        if (rw < 0) {
                            // resulting rowid could not be determined.
                            // References could not be resolved. put item to
                            // rewritemap. this forces the mapper to update the
                            // item table afterreferences have been written
                            rewrite.put(item, (Object) getSQLValue(f, item));
                        }
                    }
                    insertStatement.setObject(i + 1, cross);
                } else {
                    insertStatement.setObject(i + 1, getSQLValue(f, item));
                }
            }
            // item may have be saved through a loopback reference
            // do another check. item may have been written deeper in object
            // tree already
            if (saved.get(item) >= 0) {
                // loop reference

                return saved.get(item);
            }
            // execute query
            Log.L.finer(insertStatement.toString());
            insertStatement.execute();

            // get final row id
            // there must be a better way
            ResultSet rs;
            (rs = db.prepareStatement("SELECT id FROM " + tableID + " WHERE INSTANCEID ='" + instanceID + "'").executeQuery()).next();
            int ret;
            // update rowid in saved map
            saved.put(item, ret = rs.getInt(1));
            // self references
            // first cycle is through, and we have a final rowid
            finalSaved.put(item, ret);
            return ret;
        }
    }

    /**
     * Get the filed with the given name. loops through classhirarchy to find
     * the field
     * 
     * @param clazz
     * @param name
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
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
     * Stores array object to table. object is of class clazz
     * 
     * @param table
     * @param clazz
     * @param object
     * @param saved
     * @see {@link #store(Object, HashMap, HashMap, HashMap)}
     * @param rewrite
     * @see {@link #store(Object, HashMap, HashMap, HashMap)}
     * @param finalSaved
     * @see {@link #store(Object, HashMap, HashMap, HashMap)}
     * @return
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private String storeArray(String table, Class<?> clazz, Object object, HashMap<Object, Integer> saved, HashMap<Object, Object> rewrite, HashMap<Object, Integer> finalSaved) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, SQLException {
        // Array is null. return null
        if (object == null) return null;

        // create references controller maps
        if (saved == null) {
            saved = new HashMap<Object, Integer>();
        }
        if (finalSaved == null) {
            finalSaved = new HashMap<Object, Integer>();

        }
        if (rewrite == null) {
            rewrite = new HashMap<Object, Object>();

        }
        // return tablename if the array has already been stored
        // arrays ay also contain invalid references. this is a TODO
        if (saved.containsKey(object)) { return table; }
        // but array to saved map, indictaing that saving is in progress
        saved.put(object, -1);
        table = table.toUpperCase();
        // table integrity check
        table = this.checkTable(clazz, table);
        // clear table. we have to rebuild it from scratch
        clearTable(table);
        // get class of array entries
        Class<?> type = clazz.getComponentType();
        // build table
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
                // MultiDim array. create subarray Table
                String sub = storeArray(table + "__" + i, type, obj, saved, rewrite, finalSaved);
                insertStatement.setObject(4, sub);
            } else if (!type.isPrimitive()) {
                Object cross = (Object) obj;
                if (cross != null) {

                    // we have no selfreferencs check in arrays yet.
                    // this may produce endless loops.
                    // TODO
                    int rw;
                    cross = cross.getClass().getName() + ":" + +(rw = store(cross, saved, rewrite, finalSaved, null));
                    if (rw < 0) {
                        rewrite.put(object, obj);
                    }
                }

                insertStatement.setObject(4, cross);
            } else {
                insertStatement.setObject(4, obj);
            }
            Log.L.finer(insertStatement.toString());
            insertStatement.execute();

        }
        finalSaved.put(object, -1);
        return table;
    }

    /**
     * Clear array TODO: better sql
     * 
     * @param table
     * @throws SQLException
     */
    private void clearTable(String table) throws SQLException {
        // TODO: better clear statement
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(table);
        sb.append(" WHERE ID >= '0'");
        db.createStatement().executeQuery(sb.toString());

    }

    /**
     * returns all columns for table with name tableID
     * 
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
     * sets the field readable, and returns the object from item
     * 
     * @param field
     * @param item
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private Object getSQLValue(Field field, Object item) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        Object obj = field.get(item);
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

    /**
     * checks table integrity and creates a table if required. This would be the
     * right plae to autofix broken table-Class relations
     * 
     * @param clazz
     * @param tableID
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private String checkTable(Class<?> clazz, String tableID) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException {
        if (tableID == null) {
            MappableClassID anno = clazz.getAnnotation(MappableClassID.class);
            tableID = anno == null ? clazz.getName().replace(".", "_") : anno.value();
        }

        if (!tableExists(tableID)) {
            return createTable(tableID, clazz);
        } else {
            // check inte.
            checkTableIntegrity(tableID, clazz);
        }

        return tableID;
    }

    /**
     * checks if the table for clazz has errors, missing fields etc. This would
     * be the right place to autofix tables
     * 
     * @param tableID
     * @param class1
     * @throws SQLException
     */
    private void checkTableIntegrity(String tableID, Class<?> clazz) throws SQLException {

        ClassConverter conv = converter.get(clazz.getName());
        if (conv != null) {
            conv.checkIntegrety(tableID);
            return;
        }

        ResultSet rs;
        rs = db.prepareStatement("SELECT * FROM " + tableID).executeQuery();

        HashMap<String, Field> fields = getDeclaredFieldsToStore(clazz);
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

        }

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
        HashMap<String, Field> fields = this.getDeclaredFieldsToStore(clazz);
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
    private HashMap<String, Field> getDeclaredFieldsToStore(Class<?> clazz) {
        if (adapter.containsKey(clazz)) { return adapter.get(clazz).getDeclaredFields(clazz); }
        HashMap<String, Field> ret = new HashMap<String, Field>();
        Field[] fields = this.getFields(clazz);
        for (Field f : fields) {
            // filter instanceid
            if (f.getAnnotation(InstanceID.class) != null) {
                continue;
            }
            // no transients
            if (Modifier.isTransient(f.getModifiers())) {
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
     * @see #store(Object, HashMap, HashMap, HashMap)
     * @param a
     */
    public void store(Object a) {
        store(a, null, null, null, null);
    }

    /**
     * @param uploads
     * @param string
     */
    public void store(Object uploads, String instanceID) {
        this.store(uploads, null, null, null, instanceID);

    }

    /**
     * drops all tables
     */
    public void clear() {
        try {
            ResultSet rs = db.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next()) {

                db.prepareStatement("DROP TABLE " + rs.getString(3)).execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
