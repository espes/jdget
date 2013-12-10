package org.jdownloader.myjdownloader.client.bindings;

import java.util.ArrayList;

@ApiNamespace("config")
public interface AdvancedConfigManagerAPI {

    public ArrayList<AdvancedConfigEntryDataStorable> list();

    public ArrayList<AdvancedConfigEntryDataStorable> list(String pattern, boolean returnDescription, boolean returnValues, boolean returnDefaultValues);

    public Object get(String interfaceName, String storage, String key);

    public boolean set(String interfaceName, String storage, String key, Object value);

    public boolean reset(String interfaceName, String storage, String key);

    public Object getDefault(String interfaceName, String storage, String key);

}
