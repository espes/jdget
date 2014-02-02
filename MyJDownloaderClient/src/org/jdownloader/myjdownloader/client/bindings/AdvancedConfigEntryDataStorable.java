package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AdvancedConfigEntryDataStorable extends AbstractJsonData {
    private String   docs          = null;
    private String   storage       = null;
    private Object   value         = null;
    private Object   defaultValue  = null;
    private String   type          = null;
    private String   interfaceName = null;
    private String   enumLabel     = null;
    private String[] enumOptions   = null;
    private String[] enumLabels    = null;

    public String[] getEnumOptions() {
        return enumOptions;
    }

    public void setEnumOptions(String[] enumOptions) {
        this.enumOptions = enumOptions;
    }

    public String[] getEnumLabels() {
        return enumLabels;
    }

    public void setEnumLabels(String[] enumLabels) {
        this.enumLabels = enumLabels;
    }

    public String getEnumLabel() {
        return enumLabel;
    }

    public void setEnumLabel(String enumLabel) {
        this.enumLabel = enumLabel;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key = null;

    @SuppressWarnings("unused")
    protected AdvancedConfigEntryDataStorable(/* Storable */) {
    }

}
