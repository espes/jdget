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
import org.appwork.storage.config.annotations.DefaultJsonObject;
import org.appwork.storage.config.annotations.DefaultFactory;
import org.appwork.storage.config.defaults.AbstractDefaultFactory;
import org.appwork.utils.logging.Log;

/**
 * @author Thomas
 * 
 */
public class ObjectKeyHandler extends ListHandler<Object> {
  

    /**
     * @param storageHandler
     * @param key
     * @param type
     */
    public ObjectKeyHandler(StorageHandler<?> storageHandler, String key, Type type) {
        super(storageHandler, key,type);
   
        // TODO Auto-generated constructor stub
    }



 

}
