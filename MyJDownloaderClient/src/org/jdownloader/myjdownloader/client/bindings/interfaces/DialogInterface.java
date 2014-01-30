package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.HashMap;

import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.bindings.DialogStorable;
import org.jdownloader.myjdownloader.client.bindings.DialogTypeStorable;

@ClientApiNameSpace("dialogs")
public interface DialogInterface extends Linkable {

    public DialogStorable get(long id, boolean icon, boolean properties);

    public void answer(long id, HashMap<String, Object> data);

    public long[] list();

    public DialogTypeStorable getTypeInfo(String dialogType);
}
