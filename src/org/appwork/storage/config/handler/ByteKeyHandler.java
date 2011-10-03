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

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author Thomas
 *
 */
public class ByteKeyHandler extends KeyHandler<Byte> {

    private SpinnerValidator validator;
    private byte min;
    private byte max;

    /**
     * @param storageHandler
     * @param key
     */
    public ByteKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Byte object) {
        this.storageHandler.putPrimitive(getKey(),  object);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {       
        return (Class<? extends Annotation>[]) new Class<?>[]{DefaultByteValue.class,SpinnerValidator.class};
    }
    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() {
        try{
            this.defaultValue = getAnnotation(DefaultByteValue.class).value();
           }catch(NullPointerException e){}
        
        validator = getAnnotation(SpinnerValidator.class);
        if (validator != null) {
            min = (byte) validator.min();
            max = (byte) validator.max();

        }
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Byte object) throws Throwable {
        if (validator != null) {
            byte v = object.byteValue();
            if (v < min || v > max) throw new ValidationException();
        }
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    protected Byte getValue() {
        return primitiveStorage.get(getKey(), defaultValue);
    }

}
