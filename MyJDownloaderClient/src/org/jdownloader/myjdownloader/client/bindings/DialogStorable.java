package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashMap;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class DialogStorable extends AbstractJsonData {

    public DialogStorable(/* Storable */) {

    }

    HashMap<String, String> properties = new HashMap<String, String>();
    private String          type;

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(final HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void put(final String key, final Object value) {
        if (value == null) {
            return;
        }
        properties.put(key, value.toString());
    }

    public void setType(final String name) {
        type = name;
    }

    public String getType() {
        return type;
    }
}
