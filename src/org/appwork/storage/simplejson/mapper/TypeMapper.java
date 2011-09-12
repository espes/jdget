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

import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.JSonValue;

/**
 * @author thomas
 *
 */
public abstract class TypeMapper<T> {
    /**
     * @param json
     * @return
     */
    protected String getString(JSonNode json) {
        try {
            return (String) ((JSonValue) json).getValue();
        } catch (Throwable e) {
            return null;
        }

    }
    protected Long getLong(JSonNode json) {
        try {
            return (Long) ((JSonValue) json).getValue();
        } catch (Throwable e) {
            return null;
        }

    }
    /**
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected  JSonNode map(Object obj){
        if(obj==null)return new JSonValue(null);
        return obj2Json((T)obj);
    }

    /**
     * @param obj
     * @return
     */
    public abstract  JSonNode obj2Json(T obj);

    /**
     * @param json
     * @return
     */
    public abstract T json2Obj(JSonNode json);
    /**
     * @param json
     * @return
     */
    public T reverseMap(JSonNode json) {
        if(json instanceof  JSonValue&&((JSonValue) json).getValue()==null)return null;
       
        return json2Obj(json);
    }

}
