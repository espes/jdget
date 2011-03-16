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

/**
 * @author thomas
 * 
 */
public interface JSONMapper {

    public String objectToString(Object o) throws JSonMapperException;

    public <T> T stringToObject(String jsonString, Class<T> clazz) throws JSonMapperException;

    public <T> T stringToObject(String jsonString, TypeRef<T> type) throws JSonMapperException;
}
