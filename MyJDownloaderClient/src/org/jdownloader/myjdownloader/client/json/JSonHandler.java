package org.jdownloader.myjdownloader.client.json;


/**
 * @author Thomas
 * 
 */
public interface JSonHandler<ClassType> {

  
    public <T> T jsonToObject(String dec, ClassType clazz);

    String objectToJSon(Object payload);
}
