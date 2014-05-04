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

import java.util.ArrayList;
import java.util.List;

import org.appwork.storage.simplejson.JSonFactory;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.ParserException;
import org.appwork.storage.simplejson.mapper.JSonMapper;
import org.appwork.storage.simplejson.mapper.MapperException;

/**
 * @author thomas
 * 
 */
public class SimpleMapper implements JSONMapper {
    protected final JSonMapper mapper;

    public SimpleMapper() {
        mapper = new JSonMapper() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.appwork.storage.simplejson.mapper.JSonMapper#create(java.
             * lang.Object)
             */
            @Override
            public JSonNode create(final Object obj) throws MapperException {
                for (final JsonSerializerEntry se : serializer) {
                    if (obj!=null&&se.clazz.isAssignableFrom(obj.getClass())) { return new JSonNode() {
                     
                        @Override
                        public String toString() {                     
                            return se.serializer.toJSonString(obj);
                        }
                    }; }
                }
                return super.create(obj);
            }
        };
    }

    public JSonMapper getMapper() {
        return mapper;
    }

    class JsonSerializerEntry {
        /**
         * @param <T>
         * @param clazz2
         * @param jsonSerializer
         */
        public <T> JsonSerializerEntry(final Class<T> clazz2, final JsonSerializer<T> jsonSerializer) {
            clazz = clazz2;
            serializer = jsonSerializer;
        }

        JsonSerializer serializer;
        Class<?>       clazz;
    }

    private List<JsonSerializerEntry> serializer = new ArrayList<JsonSerializerEntry>();

    /**
     * @param jsonSerializer
     */
    public <T> void addSerializer(final Class<T> clazz, final JsonSerializer<T> jsonSerializer) {

        final ArrayList<JsonSerializerEntry> newList = new ArrayList<JsonSerializerEntry>();
        synchronized (serializer) {
            newList.addAll(serializer);

        }
        newList.add(new JsonSerializerEntry(clazz, jsonSerializer));
        serializer = newList;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.JSONMapper#objectToString(java.lang.Object)
     */
    @Override
    public String objectToString(final Object o) throws JSonMapperException {
        try {
        
            return mapper.create(o).toString();
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
            return (T) mapper.jsonToObject(new JSonFactory(jsonString).parse(), clazz);
        } catch (final ParserException e) {
            throw new JSonMapperException(e);
        } catch (final MapperException e) {
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
    public <T> T stringToObject(final String jsonString, final TypeRef<T> type) throws JSonMapperException {
        try {

            return mapper.jsonToObject(new JSonFactory(jsonString).parse(), type);
        } catch (final ParserException e) {
            throw new JSonMapperException(e);

        } catch (final MapperException e) {
            throw new JSonMapperException(e);
        }
    }

}
