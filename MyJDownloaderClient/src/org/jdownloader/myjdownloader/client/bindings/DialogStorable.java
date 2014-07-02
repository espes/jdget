package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashMap;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class DialogStorable extends AbstractJsonData {

    public DialogStorable(/* Storable */) {

    }

    protected HashMap<String, String> properties = new HashMap<String, String>();
    private String                    type;

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(final HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void setType(final String name) {
        type = name;
    }

    public String getType() {
        return type;
    }
}
