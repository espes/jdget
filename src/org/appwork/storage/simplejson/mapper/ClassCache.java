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
import java.util.logging.Level;

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
    private static ClassCache create(final Class<? extends Object> clazz) throws SecurityException, NoSuchMethodException {
        final ClassCache cc = new ClassCache(clazz);
        Getter g;
        Setter s;
        
        Class<? extends Object> cls = clazz;
        do{
        for (final Method m : cls.getDeclaredMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0 && m.getReturnType() != void.class) {
                cc.getter.add(g = new Getter(m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4), m));
                cc.getterMap.put(g.getKey(), g);
            } else if (m.getName().startsWith("is") && m.getParameterTypes().length == 0 && m.getReturnType() != void.class) {
                cc.getter.add(g = new Getter(m.getName().substring(2, 3).toLowerCase() + m.getName().substring(3), m));
                cc.getterMap.put(g.getKey(), g);
            } else if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                cc.setter.add(s = new Setter(m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4), m));
                cc.setterMap.put(s.getKey(), s);
            }
        }
        }while((cls=cls.getSuperclass())!=null);
        for (final Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (c.getParameterTypes().length == 0) {
      
                try{
                c.setAccessible(true);
                cc.constructor = c;
                }catch(java.lang.SecurityException e){
                    Log.exception(Level.WARNING, e);
                }
                break;
            }
        }
        if (cc.constructor == null) { 
            //
            String pkg = clazz.getPackage().getName();
            if(pkg.startsWith("java")||pkg.startsWith("sun.")){
                
                Log.L.warning("No Null Constructor in "+clazz+" found. De-Json-serial will fail");
            }else{
            throw new NoSuchMethodException(" Class " + clazz + " requires a null constructor. please add private " + clazz.getSimpleName() + "(){}");
            }
            }
        return cc;
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

    private Constructor<? extends Object> constructor;

    private final Class<? extends Object> clazz;
    private final ArrayList<Getter>       getter;
    private final ArrayList<Setter>       setter;
    private final HashMap<String, Getter> getterMap;
    private final HashMap<String, Setter> setterMap;

    /**
     * @param clazz
     */
    private ClassCache(final Class<? extends Object> clazz) {
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

}
