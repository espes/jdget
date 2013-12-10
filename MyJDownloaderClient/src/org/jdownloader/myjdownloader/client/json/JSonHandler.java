package org.jdownloader.myjdownloader.client.json;

import java.lang.reflect.Type;

/**
 * @author Thomas
 * 
 */
public interface JSonHandler {

  
    public <T> T jsonToObject(String dec, Type clazz);

    String objectToJSon(Object payload);
}
