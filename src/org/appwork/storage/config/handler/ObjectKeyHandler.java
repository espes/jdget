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
import java.lang.reflect.Type;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.InterfaceParseException;
import org.appwork.storage.config.MinTimeWeakReference;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultObjectValue;
import org.appwork.storage.config.annotations.DefaultValue;
import org.appwork.storage.config.defaults.DefaultFactory;
import org.appwork.utils.logging.Log;

/**
 * @author Thomas
 * 
 */
public class ObjectKeyHandler extends KeyHandler<Object> {
    static final int                     MIN_LIFETIME = 10000;
    private MinTimeWeakReference<Object> cache;
    private TypeRef<Object>              typeRef;
    private String defaultJson;
    private Class<? extends DefaultFactory> defaultFactoryClass;

    /**
     * @param storageHandler
     * @param key
     * @param type
     */
    public ObjectKeyHandler(StorageHandler<?> storageHandler, String key, Type type) {
        super(storageHandler, key);
        typeRef = new TypeRef<Object>(type) {
        };
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Object object) {
        this.write(object);
        this.cache = new MinTimeWeakReference<Object>(object, MIN_LIFETIME, "Storage " + this.getKey());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() throws Throwable {
        // if rawtypoe is an storable, we can have defaultvalues in
        // json7String from
        final DefaultObjectValue ann = this.getAnnotation(DefaultObjectValue.class);
        if (ann != null) {
            defaultJson = ann.value();
//            this.defaultValue = JSonStorage.restoreFromString(v, typeRef, null);
        }
        DefaultValue dv = getAnnotation(DefaultValue.class);
        if(dv!=null){
            if(ann!=null){
                throw new InterfaceParseException("Cannot use DefaultObjectValue AND DefaultValue annotation");
            }
            defaultFactoryClass=dv.value();
        }
        this.path = new File(this.storageHandler.getPath() + "." + getKey() + "." + (this.isCrypted() ? "ejs" : "json"));

    }

    @Override
    public Object getValue() {
        Object ret = this.cache != null ? this.cache.get() : null;
        if (ret == null) {
            try {
                ret = this.read();
            } catch (InstantiationException e) {
              Log.exception(e);
            } catch (IllegalAccessException e) {
               Log.exception(e);
            }

            this.cache = new MinTimeWeakReference<Object>(ret, MIN_LIFETIME, "Storage " + this.getKey());

        }
        return ret;
    }
    private File                           path;
    /**
     * @return
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    @SuppressWarnings("unchecked")
    protected Object read() throws InstantiationException, IllegalAccessException {
        try {
            Log.L.finer("Read Config: " + this.path.getAbsolutePath());
//            if (this.defaultFactory != null) {
//                final Object dummy = new Object();
//                Object ret = JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, new TypeRef(this.getter.getMethod().getGenericReturnType()) {
//                }, dummy);
//                if (ret == dummy) {
//                    try {
//                        ret = this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
//                    } catch (final Exception e) {
//                        Log.exception(e);
//                        ret = null;
//                    }
//                    this.defaultObject = ret;
//                }
//                return (RawClass) ret;
//            } else {
               
            final Object dummy = new Object();
                Object ret = JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, typeRef, dummy);
                
                if(ret==dummy){
                    if(defaultValue!=null)return defaultValue;
                    if(defaultJson!=null){
                        defaultValue=JSonStorage.restoreFromString(defaultJson, typeRef, null);
                        return   defaultValue;
                    }else if(defaultFactoryClass!=null){
                     
                        defaultValue= defaultFactoryClass.newInstance().getDefaultValue();
                        return   defaultValue;
                    }else{
                        return null;
                    }
                }
                return ret;
                
//            }

        } finally {
            if (!this.path.exists()) {
                this.write(defaultValue);
            }

        }
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {       
        return (Class<? extends Annotation>[]) new Class<?>[]{DefaultObjectValue.class};
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
