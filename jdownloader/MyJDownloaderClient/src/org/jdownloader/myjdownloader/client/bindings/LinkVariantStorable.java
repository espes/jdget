package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class LinkVariantStorable extends AbstractJsonData {

    public LinkVariantStorable(/* Storable */) {
    }

    private String id   = null;
    public String getId() {
        return id;
    }
    public void setId(final String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }

    private String name = null;

}