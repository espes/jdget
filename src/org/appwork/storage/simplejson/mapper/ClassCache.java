/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import org.appwork.storage.simplejson.Ignore;
import org.appwork.storage.simplejson.Ignores;
import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class ClassCache {
    private static final HashMap<Class<?>, ClassCache> CACHE        = new HashMap<Class<?>, ClassCache>();
    private static final Object[]                      EMPTY_OBJECT = new Object[] {};
    private static final Class<?>[]                    EMPTY_TYPES  = new Class[] {};

    /**
     * @param clazz
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected static ClassCache create(final Class<? extends Object> clazz) throws SecurityException, NoSuchMethodException {
        final ClassCache cc = new ClassCache(clazz);
        Getter g;
        Setter s;

        Class<? extends Object> cls = clazz;

        HashSet<String> ignores = new HashSet<String>();
        do {
            Ignores ig = cls.getAnnotation(Ignores.class);
            if (ig != null) {
                for (String i : ig.value()) {
                    ignores.add(i);
                }
            }
            for (final Method m : cls.getDeclaredMethods()) {

                if (m.getAnnotation(Ignore.class) != null || ignores.contains(m.toString())) {
                    continue;
                }

                if (m.getName().startsWith("get") && m.getParameterTypes().length == 0 && m.getReturnType() != void.class) {
                    cc.getter.add(g = new Getter(createKey(m.getName().substring(3)), m));
                    cc.getterMap.put(g.getKey(), g);
                    Log.L.finer(m.toString());

                } else if (m.getName().startsWith("is") && m.getParameterTypes().length == 0 && m.getReturnType() != void.class) {
                    cc.getter.add(g = new Getter(createKey(m.getName().substring(2)), m));
                    cc.getterMap.put(g.getKey(), g);
                    Log.L.finer(m.toString());
                } else if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                    cc.setter.add(s = new Setter(createKey(m.getName().substring(3)), m));
                    cc.setterMap.put(s.getKey(), s);
                    Log.L.finer(m.toString());
                }

            }
        } while ((cls = cls.getSuperclass()) != null && cls != Object.class);
        // we do not want to serialize object's getter
        for (final Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (c.getParameterTypes().length == 0) {

                try {
                    c.setAccessible(true);
                    cc.constructor = c;
                } catch (java.lang.SecurityException e) {
                    Log.exception(Level.WARNING, e);
                }
                break;
            }
        }
        if (cc.constructor == null) {
            //
            String pkg = clazz.getPackage().getName();
            if (pkg.startsWith("java") || pkg.startsWith("sun.")) {

                Log.L.warning("No Null Constructor in " + clazz + " found. De-Json-serial will fail");
            } else {
                throw new NoSuchMethodException(" Class " + clazz + " requires a null constructor. please add private " + clazz.getSimpleName() + "(){}");
            }
        }
        return cc;
    }

    /**
     * 
     * Jackson maps methodnames to keys like this. setID becomes key "id" ,
     * setMethodName becomes "methodName". To keep compatibility between jackson
     * and simplemapper, we should do it the same way
     * 
     * @param substring
     * @return
     */
    private static String createKey(String key) {
        StringBuilder sb = new StringBuilder();
        char[] ca = key.toCharArray();
        boolean starter = true;
        for (int i = 0; i < ca.length; i++) {
            if (starter && Character.isUpperCase(ca[i])) {
                sb.append(Character.toLowerCase(ca[i]));
            } else {
                starter = false;
                sb.append(ca[i]);
            }
        }
        return sb.toString();
    }

    /**
     * @param clazz
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static ClassCache getClassCache(final Class<? extends Object> clazz) throws SecurityException, NoSuchMethodException {
        ClassCache cc = ClassCache.CACHE.get(clazz);
        if (cc == null) {
            cc = ClassCache.create(clazz);
            ClassCache.CACHE.put(clazz, cc);
        }
        return cc;
    }

    protected Constructor<? extends Object> constructor;

    protected final Class<? extends Object> clazz;
    protected final ArrayList<Getter>       getter;
    protected final ArrayList<Setter>       setter;
    protected final HashMap<String, Getter> getterMap;
    protected final HashMap<String, Setter> setterMap;

    /**
     * @param clazz
     */
    protected ClassCache(final Class<? extends Object> clazz) {
        this.clazz = clazz;
        this.getter = new ArrayList<Getter>();
        this.setter = new ArrayList<Setter>();
        this.getterMap = new HashMap<String, Getter>();
        this.setterMap = new HashMap<String, Setter>();
    }

    public ArrayList<Getter> getGetter() {
        return this.getter;
    }

    public Getter getGetter(final String key) {
        return this.getterMap.get(key);
    }

    /**
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    public Object getInstance() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        return this.constructor.newInstance(ClassCache.EMPTY_OBJECT);
    }

    public ArrayList<Setter> getSetter() {
        return this.setter;
    }

    public Setter getSetter(final String key) {
        return this.setterMap.get(key);
    }

    /**
     * @param class1
     * @param stackTraceElementClassCache
     */
    public static void put(Class<?> class1, ClassCache stackTraceElementClassCache) {
        CACHE.put(class1, stackTraceElementClassCache);

    }

}
