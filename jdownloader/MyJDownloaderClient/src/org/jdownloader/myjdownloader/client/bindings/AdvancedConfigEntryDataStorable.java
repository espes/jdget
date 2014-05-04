package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AdvancedConfigEntryDataStorable extends AbstractJsonData {
    private String     docs          = null;
    private String     storage       = null;
    private Object     value         = null;
    private Object     defaultValue  = null;
    @Deprecated
    /**
     * @deprecated use #abstractType instead
     */
    private String     type          = null;
    private String     interfaceName = null;
    private String     enumLabel     = null;
    private String[][] enumOptions   = null;

    public String[][] getEnumOptions() {
        return enumOptions;
    }

    public void setEnumOptions(final String[][] enumOptions) {
        this.enumOptions = enumOptions;
    }
/**
 * @see org.appwork.storage.config.handler.KeyHandler.AbstractType<RawClass>
 * @author $Author: unknown$
 *
 */
    public static enum AbstractType {
        BOOLEAN,
        INT,
        LONG,
        STRING,
        OBJECT,
        OBJECT_LIST,
        STRING_LIST,
        ENUM,
        BYTE,
        CHAR,
        DOUBLE,
        FLOAT,
        SHORT,
        BOOLEAN_LIST,
        BYTE_LIST,
        SHORT_LIST,
        LONG_LIST,
        INT_LIST,
        FLOAT_LIST,
        ENUM_LIST,
        DOUBLE_LIST,
        CHAR_LIST,
        UNKNOWN,
        HEX_COLOR,
        HEX_COLOR_LIST,
        ACTION;
    }

    private AbstractType abstractType = null;

    public AbstractType getAbstractType() {
        return abstractType;
    }

    public void setAbstractType(final AbstractType abstractType) {
        this.abstractType = abstractType;
    }

    public String getEnumLabel() {
        return enumLabel;
    }

    public void setEnumLabel(final String enumLabel) {
        this.enumLabel = enumLabel;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(final String docs) {
        this.docs = docs;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(final String storage) {
        this.storage = storage;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Deprecated
    /**
     * @Deprecated use #getAbstractType instead
     * @return
     */
    public String getType() {
        return type;
    }

    @Deprecated
    public void setType(final String type) {
        this.type = type;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(final String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    private String key = null;

    @SuppressWarnings("unused")
    protected AdvancedConfigEntryDataStorable(/* Storable */) {
    }

}
