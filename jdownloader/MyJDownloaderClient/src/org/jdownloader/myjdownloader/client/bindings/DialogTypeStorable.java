package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashMap;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class DialogTypeStorable extends AbstractJsonData {

    public DialogTypeStorable(/* Storable */) {

        in = new HashMap<String, String>();
        out = new HashMap<String, String>();
    }

    private HashMap<String, String> in;

    public HashMap<String, String> getIn() {
        return in;
    }

    public void setIn(final HashMap<String, String> in) {
        this.in = in;
    }

    public HashMap<String, String> getOut() {
        return out;
    }

    public void setOut(final HashMap<String, String> out) {
        this.out = out;
    }

    private HashMap<String, String> out;

    public void addIn(final String key, final String type) {
        in.put(key, type);
    }

    public void addOut(final String key, final String type) {
        out.put(key, type);
    }
}
