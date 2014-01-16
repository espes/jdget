package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;

import org.jdownloader.myjdownloader.client.bindings.AdvancedConfigEntryDataStorable;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AdvancedConfigInterfaceAsync {

	void get(String interfaceName, String storage, String key,
			AsyncCallback<Object> callback);

	void getDefault(String interfaceName, String storage, String key,
			AsyncCallback<Object> callback);

	void list(AsyncCallback<ArrayList<AdvancedConfigEntryDataStorable>> callback);

	void list(String pattern, boolean returnDescription, boolean returnValues,
			boolean returnDefaultValues,
			AsyncCallback<ArrayList<AdvancedConfigEntryDataStorable>> callback);

	void reset(String interfaceName, String storage, String key,
			AsyncCallback<Boolean> callback);

	void set(String interfaceName, String storage, String key, Object value,
			AsyncCallback<Boolean> callback);

}
