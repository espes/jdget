package org.jdownloader.myjdownloader.client.bindings.interfaces;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DownloadControllerInterfaceAsync {

	void forceDownload(long[] linkIds, long[] packageIds,
			AsyncCallback<Void> callback);

	void getCurrentState(AsyncCallback<String> callback);

	void pause(boolean value, AsyncCallback<Boolean> callback);

	/*
	 * Info
	 */
	void getSpeedInBps(AsyncCallback<Integer> callback);

	/*
	 * Controls
	 */
	void start(AsyncCallback<Boolean> callback);

	void stop(AsyncCallback<Boolean> callback);
}
