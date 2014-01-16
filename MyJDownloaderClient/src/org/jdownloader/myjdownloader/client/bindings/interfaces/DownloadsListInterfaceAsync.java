package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.List;

import org.jdownloader.myjdownloader.client.bindings.DownloadLinkStorable;
import org.jdownloader.myjdownloader.client.bindings.FilePackageStorable;
import org.jdownloader.myjdownloader.client.bindings.LinkQuery;
import org.jdownloader.myjdownloader.client.bindings.PackageQuery;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DownloadsListInterfaceAsync {

	void getStructureChangeCounter(long oldCounterValue,
			AsyncCallback<Long> callback);

	void moveLinks(long[] linkIds, long afterLinkID, long destPackageID,
			AsyncCallback<Void> callback);

	void movePackages(long[] packageIds, long afterDestPackageId,
			AsyncCallback<Void> callback);

	void packageCount(AsyncCallback<Integer> callback);

	void queryLinks(LinkQuery queryParams,
			AsyncCallback<List<DownloadLinkStorable>> callback);

	void queryPackages(PackageQuery queryParams,
			AsyncCallback<List<FilePackageStorable>> callback);

	void removeLinks(long[] linkIds, long[] packageIds,
			AsyncCallback<Void> callback);

	void renamePackage(Long packageId, String newName,
			AsyncCallback<Void> callback);

	void resetLinks(long[] linkIds, long[] packageIds,
			AsyncCallback<Void> callback);

	void setEnabled(boolean enabled, long[] linkIds, long[] packageIds,
			AsyncCallback<Void> callback);

}
