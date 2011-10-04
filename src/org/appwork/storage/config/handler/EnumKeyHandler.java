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

import java.lang.annotation.Annotation;

import org.appwork.storage.config.InterfaceParseException;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;

/**
 * @author Thomas
 *
 */
public class EnumKeyHandler extends KeyHandler<Enum> {

    /**
     * @param storageHandler
     * @param key
     */
    public EnumKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() throws Throwable {
        // chek if this is really the best way to convert string to
        // enum
    
      
   
        final DefaultEnumValue ann = this.getAnnotation(DefaultEnumValue.class);
        if (ann != null) {        
           

         
                defaultValue = Enum.valueOf(  getRawClass(), ann.value());
         
        }else{
            defaultValue=  getRawClass().getEnumConstants()[0];
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {       
        return (Class<? extends Annotation>[]) new Class<?>[]{DefaultEnumValue.class};
    }
    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Enum object) throws Throwable {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Enum object) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public Enum getValue() {
        return primitiveStorage.get(getKey(), defaultValue);
    }

}
