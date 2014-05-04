package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.List;

import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.bindings.downloadlist.DownloadLinkStorable;
import org.jdownloader.myjdownloader.client.bindings.downloadlist.DownloadPackageStorable;
import org.jdownloader.myjdownloader.client.bindings.downloadlist.DownloadLinkQuery;
import org.jdownloader.myjdownloader.client.bindings.downloadlist.DownloadPackageQuery;

@ClientApiNameSpace("downloadsV2")
public interface DownloadsListInterface extends Linkable{

    void setEnabled(boolean enabled, long[] linkIds, long[] packageIds);

    int packageCount();

    List<DownloadPackageStorable> queryPackages(DownloadPackageQuery queryParams);

    void removeLinks(final long[] linkIds, final long[] packageIds);

    void renamePackage(Long packageId, String newName);

    void resetLinks(long[] linkIds, long[] packageIds);

    /**
     * Returns the new Counter if the counter does not equal oldCounterValue If the value changed, we should update the structure. Use this
     * method to check whether a structure update is required or not
     * 
     * @param oldCounterValue
     * @return
     */
    long getStructureChangeCounter(long oldCounterValue);

    void movePackages(long[] packageIds, long afterDestPackageId);

    void moveLinks(long[] linkIds, long afterLinkID, long destPackageID);

    /**
     * Query Packages links in downloads
     * 
     * Example:
     * http://localhost:3128/downloads/queryLinks?{"packageUUIDs":[1358496436106,1358496436107],"enabled":true,"size":true,"host":true
     * ,"startAt":0,"maxResults":10}
     * 
     * Default fields returned: name, uuid, packageUUID
     * 
     * @param queryParams
     *            Hashmap with the following allowed values:
     * 
     *            Optional selectors: packageUUIDs, long[], links contained in the packages with given uuids are returned, if empty all
     *            links are returned startAt, Integer, index of first element to be returned maxResults, Integer, total number of elements
     *            to be returned
     * 
     *            Optional fields (Boolean): host size done enabled
     * 
     * @return
     */
    List<DownloadLinkStorable> queryLinks(DownloadLinkQuery queryParams);

}
