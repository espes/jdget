package org.jdownloader.myjdownloader.client.bindings.interfaces;

import org.jdownloader.myjdownloader.client.bindings.AddLinksQuery;
import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.bindings.CrawledLinkQuery;
import org.jdownloader.myjdownloader.client.bindings.CrawledLinkStorable;
import org.jdownloader.myjdownloader.client.bindings.CrawledPackageQuery;
import org.jdownloader.myjdownloader.client.bindings.CrawledPackageStorable;
import org.jdownloader.myjdownloader.client.bindings.LinkVariantStorable;

@ClientApiNameSpace("linkgrabberv2")
public interface LinkgrabberInterface extends Linkable {

    CrawledPackageStorable[] queryPackages(CrawledPackageQuery queryParams);

    CrawledLinkStorable[] queryLinks(CrawledLinkQuery queryParams);

    void moveToDownloadlist(long[] linkIds, long[] packageIds);

    void removeLinks(long[] packageIds, long[] linkIds);

    void setEnabled(boolean enabled, long[] linkIds, long[] packageIds);

    void renameLink(long linkId, String newName);

    void renamePackage(long packageId, String newName);

    long getChildrenChanged(long structureWatermark);

    String[] getDownloadFolderHistorySelectionBase();

    int getPackageCount();

    void movePackages(long[] packageIds, long afterDestPackageId);

    void moveLinks(long[] linkIds, long afterLinkID, long destPackageID);

    void addLinks(AddLinksQuery query);

    LinkVariantStorable[] getVariants(long linkid);

    void setVariant(long linkid, String variantID);
}
