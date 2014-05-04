package org.appwork.utils;

import org.appwork.storage.JSonStorage;

public class AWUObject {
    public AWUObject() {

    }

    @Override
    public String toString() {
        return JSonStorage.toString(this);
    }

}
