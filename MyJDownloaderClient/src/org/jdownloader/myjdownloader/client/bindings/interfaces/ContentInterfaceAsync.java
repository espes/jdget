package org.jdownloader.myjdownloader.client.bindings.interfaces;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ContentInterfaceAsync {

	void getFavIcon(String hostername, AsyncCallback<byte[]> callback);

	void getFileIcon(String filename, AsyncCallback<byte[]> callback);

}
