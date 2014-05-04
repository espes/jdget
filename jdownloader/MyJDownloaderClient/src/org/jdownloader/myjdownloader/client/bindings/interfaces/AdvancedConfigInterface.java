package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;

import org.jdownloader.myjdownloader.client.bindings.AdvancedConfigEntryDataStorable;
import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.bindings.EnumOptionStorable;

@ClientApiNameSpace(AdvancedConfigInterface.NAMESPACE)
public interface AdvancedConfigInterface extends Linkable {

    public static final String NAMESPACE = "config";

    public ArrayList<EnumOptionStorable> listEnum(String type);

    public ArrayList<AdvancedConfigEntryDataStorable> list();

    @Deprecated
    public ArrayList<AdvancedConfigEntryDataStorable> list(String pattern, boolean returnDescription, boolean returnValues, boolean returnDefaultValues);

    public ArrayList<AdvancedConfigEntryDataStorable> list(String pattern, boolean returnDescription, boolean returnValues, boolean returnDefaultValues, boolean returnEnumInfo);

    public Object get(String interfaceName, String storage, String key);

    public boolean set(String interfaceName, String storage, String key, Object value);

    public boolean reset(String interfaceName, String storage, String key);

    public Object getDefault(String interfaceName, String storage, String key);

}
