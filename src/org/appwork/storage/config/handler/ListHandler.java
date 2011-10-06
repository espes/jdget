/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.handler;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.MinTimeWeakReference;
import org.appwork.storage.config.annotations.DefaultJsonObject;
import org.appwork.utils.logging.Log;

/**
 * @author Thomas
 * 
 */
public abstract class ListHandler<T> extends KeyHandler<T> {
    public static final int         MIN_LIFETIME = 10000;
    private MinTimeWeakReference<T> cache;
    private TypeRef<Object>         typeRef;
  

    /**
     * @param storageHandler
     * @param key
     */
    public ListHandler(StorageHandler<?> storageHandler, String key, Type type) {
        super(storageHandler, key);
        typeRef = new TypeRef<Object>(type) {
        };
       

    }
    
    @Override
    protected void initHandler() throws Throwable {
        this.path = new File(this.storageHandler.getPath() + "." + getKey() + "." + (this.isCrypted() ? "ejs" : "json"));
    }


  
    @Override
    protected void putValue(T object) {
        this.write(object);
        this.cache = new MinTimeWeakReference<T>(object, MIN_LIFETIME, "Storage " + this.getKey());
    }

  

    @Override
    public T getValue() {
        T ret = this.cache != null ? this.cache.get() : null;
        if (ret == null) {
            try {
                ret = (T) this.read();
            } catch (InstantiationException e) {
                Log.exception(e);
            } catch (IllegalAccessException e) {
                Log.exception(e);
            }

            this.cache = new MinTimeWeakReference<T>(ret, MIN_LIFETIME, "Storage " + this.getKey());

        }
        return ret;
    }

    private File path;
    protected boolean initDefaults() throws Throwable {
      //objects init thier defaults only if required

        return false;
    }
    /**
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    protected Object read() throws InstantiationException, IllegalAccessException {
        try {
            Log.L.finer("Read Config: " + this.path.getAbsolutePath());

            final Object dummy = new Object();
            Object ret = JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, typeRef, dummy);

            if (ret == dummy) {
                if (defaultValue != null) return defaultValue;
                Annotation ann;
                if (defaultJson != null) {
                    defaultValue = (T) JSonStorage.restoreFromString(defaultJson, typeRef, null);
                    defaultJson=null;
                    return defaultValue;
                } else if (defaultFactoryClass != null) {

                    defaultValue = (T) defaultFactoryClass.newInstance().getDefaultValue();
                    defaultFactoryClass=null;
                    return defaultValue;
                } else if(  (ann = getAnnotation(getDefaultAnnotation()))!=null){
                    try {
                        defaultValue = (T) ann.annotationType().getMethod("value", new Class[] {}).invoke(ann, new Object[] {});
                    
                    } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return defaultValue;
                } else {
                    return null;
                }
            }
            return ret;

            // }

        } finally {
            if (!this.path.exists()) {
                this.write(defaultValue);
            }

        }
    }

  
    /**
     * @param object
     */
    protected void write(final Object object) {

        JSonStorage.saveTo(this.path, !this.crypted, this.cryptKey, JSonStorage.serializeToJson(object));

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Object object) throws Throwable {
        // TODO Auto-generated method stub

    }

}
