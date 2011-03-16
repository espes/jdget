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

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author thomas
 * 
 */
public class JacksonMapper implements JSONMapper {
    private final ObjectMapper mapper;

    public JacksonMapper() {

        this.mapper = new ObjectMapper(new ExtJsonFactory());

        this.mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.JSONMapper#objectToString(java.lang.Object)
     */
    @Override
    public String objectToString(final Object o) throws JSonMapperException {

        try {
            return this.mapper.writeValueAsString(o);

        } catch (final JsonGenerationException e) {
            throw new JSonMapperException(e);
        } catch (final JsonMappingException e) {
            throw new JSonMapperException(e);
        } catch (final IOException e) {
            throw new JSonMapperException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.JSONMapper#stringToObject(java.lang.String,
     * java.lang.Class)
     */

    @Override
    public <T> T stringToObject(final String jsonString, final Class<T> clazz) throws JSonMapperException {

        try {
            return this.mapper.readValue(jsonString, clazz);
        } catch (final JsonParseException e) {
            throw new JSonMapperException(e);
        } catch (final JsonMappingException e) {
            throw new JSonMapperException(e);
        } catch (final IOException e) {
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
            return this.mapper.readValue(jsonString, new TypeReference<T>() {
            });
        } catch (final JsonParseException e) {
            throw new JSonMapperException(e);
        } catch (final JsonMappingException e) {
            throw new JSonMapperException(e);
        } catch (final IOException e) {
            throw new JSonMapperException(e);
        }
    }

}
