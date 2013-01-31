package org.jdownloader.api.downloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.packagecontroller.AbstractPackageChildrenNodeFilter;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.remoteapi.APIQuery;
import org.appwork.remoteapi.QueryResponseMap;

public class DownloadsAPIImpl implements DownloadsAPI {

    public boolean start() {
        DownloadWatchDog.getInstance().startDownloads();
        return true;
    }

    public boolean stop() {
        DownloadWatchDog.getInstance().stopDownloads();
        return true;
    }

    public boolean pause(Boolean value) {
        DownloadWatchDog.getInstance().pauseDownloadWatchDog(value);
        return true;
    }

    @Override
    public List<FilePackageAPIStorable> queryPackages(APIQuery queryParams) {
        DownloadController dlc = DownloadController.getInstance();
        // DownloadWatchDog dwd = DownloadWatchDog.getInstance();

        boolean b = dlc.readLock();
        try {
            List<FilePackageAPIStorable> ret = new ArrayList<FilePackageAPIStorable>(dlc.size());

            int startWith = queryParams.getStartAt();
            int maxResults = queryParams.getMaxResults();

            if (startWith > dlc.size() - 1) return ret;
            if (startWith < 0) startWith = 0;
            if (maxResults < 0) maxResults = dlc.size();

            for (int i = startWith; i < Math.min(startWith + maxResults, dlc.size()); i++) {
                FilePackage fp = dlc.getPackages().get(i);
                FilePackageAPIStorable fps = new FilePackageAPIStorable(fp);

                QueryResponseMap infomap = new QueryResponseMap();

                if (queryParams._getQueryParam("saveTo", Boolean.class, false)) {
                    infomap.put("saveTo", fp.getDownloadDirectory());
                }
                if (queryParams._getQueryParam("size", Boolean.class, false)) {
                    long size = 0;
                    for (DownloadLink dl : fp.getChildren()) {
                        size = size + dl.getDownloadSize();
                    }
                    infomap.put("size", size);
                }
                if (queryParams._getQueryParam("childCount", Boolean.class, false)) {
                    infomap.put("childCount", fp.getChildren().size());
                }
                if (queryParams._getQueryParam("hosts", Boolean.class, false)) {
                    Set<String> hosts = new HashSet<String>();
                    for (DownloadLink dl : fp.getChildren()) {
                        hosts.add(dl.getHost());
                    }
                    infomap.put("hosts", hosts);
                }
                if (queryParams._getQueryParam("activeTask", Boolean.class, false)) {
                    infomap.put("activeTask", "N/A");
                }
                if (queryParams._getQueryParam("speed", Boolean.class, false)) {
                    infomap.put("speed", -1);
                }
                if (queryParams._getQueryParam("eta", Boolean.class, false)) {
                    infomap.put("eta", -1);
                }
                if (queryParams._getQueryParam("done", Boolean.class, false)) {
                    Long done = 0l;
                    for (DownloadLink dl : fp.getChildren()) {
                        done = done + dl.getDownloadCurrent();
                    }
                    infomap.put("done", done);
                }
                if (queryParams._getQueryParam("progress", Boolean.class, false)) {
                    infomap.put("progress", -1);
                }
                if (queryParams._getQueryParam("comment", Boolean.class, false)) {
                    infomap.put("comment", fp.getComment());
                }
                if (queryParams.fieldRequested("enabled")) {
                    boolean enabled = false;
                    for (DownloadLink dl : fp.getChildren()) {
                        if (dl.isEnabled()) {
                            enabled = true;
                            break;
                        }
                    }
                    infomap.put("enabled", enabled);
                }

                fps.setInfoMap(infomap);
                ret.add(fps);
            }
            return ret;
        } finally {
            dlc.readUnlock(b);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<DownloadLinkAPIStorable> queryLinks(APIQuery queryParams) {
        List<DownloadLinkAPIStorable> result = new ArrayList<DownloadLinkAPIStorable>();

        DownloadController dlc = DownloadController.getInstance();
        // DownloadWatchDog dwd = DownloadWatchDog.getInstance();

        // retrieve packageUUIDs from queryParams
        List<Long> packageUUIDs = new ArrayList<Long>();
        if (!queryParams._getQueryParam("packageUUIDs", List.class, new ArrayList()).isEmpty()) {
            List uuidsFromQuery = queryParams._getQueryParam("packageUUIDs", List.class, new ArrayList());
            for (Object o : uuidsFromQuery) {
                try {
                    packageUUIDs.add((Long) o);
                } catch (ClassCastException e) {
                    continue;
                }
            }
        }

        List<FilePackage> matched = new ArrayList<FilePackage>();

        boolean b = dlc.readLock();
        try {
            // if no specific uuids are specified collect all packages
            if (packageUUIDs.isEmpty()) {
                matched = dlc.getPackages();
            } else {
                for (FilePackage pkg : dlc.getPackages()) {
                    if (packageUUIDs.contains(pkg.getUniqueID().getID())) {
                        matched.add(pkg);
                    }
                }
            }
        } finally {
            dlc.readUnlock(b);
        }

        // collect children of the selected packages and convert to storables for response
        List<DownloadLink> links = new ArrayList<DownloadLink>();
        for (FilePackage pkg : matched) {
            links.addAll(pkg.getChildren());
        }

        if (links.isEmpty()) return result;

        int startWith = queryParams.getStartAt();
        int maxResults = queryParams.getMaxResults();

        if (startWith > links.size() - 1) return result;
        if (startWith < 0) startWith = 0;
        if (maxResults < 0) maxResults = links.size();

        for (int i = startWith; i < Math.min(startWith + maxResults, links.size()); i++) {

            DownloadLink dl = links.get(i);
            DownloadLinkAPIStorable dls = new DownloadLinkAPIStorable(dl);

            QueryResponseMap infomap = new QueryResponseMap();

            if (queryParams._getQueryParam("host", Boolean.class, false)) {
                infomap.put("host", dl.getHost());
            }
            if (queryParams._getQueryParam("size", Boolean.class, false)) {
                infomap.put("size", dl.getDownloadSize());
            }
            if (queryParams._getQueryParam("done", Boolean.class, false)) {
                infomap.put("done", dl.getDownloadCurrent());
            }
            if (queryParams.fieldRequested("enabled")) infomap.put("enabled", dl.isEnabled());

            infomap.put("packageUUID", dl.getParentNode().getUniqueID().getID());

            dls.setInfoMap(infomap);
            result.add(dls);
        }

        return result;
    }

    @Override
    public int speed() {
        DownloadWatchDog dwd = DownloadWatchDog.getInstance();
        return dwd.getDownloadSpeedManager().getSpeed();
    }

    @Override
    public boolean removeLinks(final List<Long> linkIds) {
        if (linkIds == null) return true;

        DownloadController dlc = DownloadController.getInstance();

        List<DownloadLink> rmv;

        boolean b = dlc.readLock();
        try {
            rmv = dlc.getChildrenByFilter(new AbstractPackageChildrenNodeFilter<DownloadLink>() {
                @Override
                public int returnMaxResults() {
                    return 0;
                }

                @Override
                public boolean isChildrenNodeFiltered(DownloadLink node) {
                    if (linkIds.contains(node.getUniqueID().getID())) return true;
                    return false;
                }
            });
        } finally {
            dlc.readUnlock(b);
        }

        dlc.writeLock();
        dlc.removeChildren(rmv);
        dlc.writeUnlock();

        return true;
    }

    @Override
    public boolean forceDownload(final List<Long> linkIds) {
        if (linkIds == null) return true;

        DownloadController dlc = DownloadController.getInstance();

        List<DownloadLink> sdl;

        boolean b = dlc.readLock();
        try {
            sdl = dlc.getChildrenByFilter(new AbstractPackageChildrenNodeFilter<DownloadLink>() {
                @Override
                public int returnMaxResults() {
                    return 0;
                }

                @Override
                public boolean isChildrenNodeFiltered(DownloadLink node) {
                    if (linkIds.contains(node.getUniqueID().getID())) return true;
                    return false;
                }
            });
        } finally {
            dlc.readUnlock(b);
        }

        DownloadWatchDog dwd = DownloadWatchDog.getInstance();
        dwd.forceDownload(sdl);

        return true;
    }

    @Override
    public boolean enableLinks(final List<Long> linkIds) {
        if (linkIds == null) return true;

        DownloadController dlc = DownloadController.getInstance();

        List<DownloadLink> sdl;

        boolean b = dlc.readLock();
        try {
            sdl = dlc.getChildrenByFilter(new AbstractPackageChildrenNodeFilter<DownloadLink>() {
                @Override
                public int returnMaxResults() {
                    return 0;
                }

                @Override
                public boolean isChildrenNodeFiltered(DownloadLink node) {
                    if (linkIds.contains(node.getUniqueID().getID())) return true;
                    return false;
                }
            });
        } finally {
            dlc.readUnlock(b);
        }

        for (DownloadLink dl : sdl) {
            dl.setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean disableLinks(final List<Long> linkIds) {
        if (linkIds == null) return true;

        DownloadController dlc = DownloadController.getInstance();

        List<DownloadLink> sdl;

        boolean b = dlc.readLock();
        try {
            sdl = dlc.getChildrenByFilter(new AbstractPackageChildrenNodeFilter<DownloadLink>() {
                @Override
                public int returnMaxResults() {
                    return 0;
                }

                @Override
                public boolean isChildrenNodeFiltered(DownloadLink node) {
                    if (linkIds.contains(node.getUniqueID().getID())) return true;
                    return false;
                }
            });
        } finally {
            dlc.readUnlock(b);
        }

        for (DownloadLink dl : sdl) {
            dl.setEnabled(false);
        }

        return true;
    }
}
