/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage;

import org.appwork.storage.simplejson.JSonFactory;
import org.appwork.storage.simplejson.ParserException;
import org.appwork.storage.simplejson.mapper.JSonMapper;
import org.appwork.storage.simplejson.mapper.MapperException;

/**
 * @author thomas
 * 
 */
public class SimpleMapper implements JSONMapper {
    private final JSonMapper mapper;

    public SimpleMapper() {
        this.mapper = new JSonMapper();
    }

    public JSonMapper getMapper() {
        return this.mapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.JSONMapper#objectToString(java.lang.Object)
     */
    @Override
    public String objectToString(final Object o) throws JSonMapperException {

        try {
            return this.mapper.create(o).toString();
        } catch (final MapperException e) {
            throw new JSonMapperException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.JSONMapper#stringToObject(java.lang.String,
     * java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T stringToObject(final String jsonString, final Class<T> clazz) throws JSonMapperException {

        try {
            return (T) this.mapper.jsonToObject(new JSonFactory(jsonString).parse(), clazz);
        } catch (final ParserException e) {
            throw new JSonMapperException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.JSONMapper#stringToObject(java.lang.String,
     * org.appwork.storage.TypeRef)
     */
    @Override
    public <T> T stringToObject( String jsonString, final TypeRef<T> type) throws JSonMapperException {
        try {
           
            return this.mapper.jsonToObject(new JSonFactory(jsonString).parse(), type);
        } catch (final ParserException e) {
            throw new JSonMapperException(e);

        }
    }

}
