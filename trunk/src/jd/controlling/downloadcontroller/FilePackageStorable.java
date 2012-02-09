package jd.controlling.downloadcontroller;

import java.util.ArrayList;
import java.util.HashMap;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.storage.Storable;

public class FilePackageStorable implements Storable {

    private FilePackage                     filePackage;
    private ArrayList<DownloadLinkStorable> links;

    @SuppressWarnings("unused")
    private FilePackageStorable(/* Storable */) {
        this.filePackage = FilePackage.getInstance();
    }

    public FilePackageStorable(FilePackage filePackage) {
        this.filePackage = filePackage;
        links = new ArrayList<DownloadLinkStorable>(filePackage.size());
        synchronized (filePackage) {
            for (DownloadLink link : filePackage.getChildren()) {
                links.add(new DownloadLinkStorable(link));
            }
        }
    }

    public String getName() {
        return filePackage.getName();
    }

    public void setName(String name) {
        filePackage.setName(name);
    }

    public HashMap<String, Object> getProperties() {
        return filePackage.getProperties();
    }

    public void setProperties(HashMap<String, Object> props) {
        filePackage.setProperties(props);
    }

    public long getCreated() {
        return filePackage.getCreated();
    }

    public void setCreated(long time) {
        filePackage.setCreated(time);
    }

    public long getFinished() {
        return filePackage.getFinishedDate();
    }

    public void setFinished(long time) {
        filePackage.setFinishedDate(time);
    }

    public String getDownloadFolder() {
        return filePackage.getDownloadDirectory();
    }

    public void setDownloadFolder(String dest) {
        filePackage.setDownloadDirectory(dest);
    }

    public ArrayList<DownloadLinkStorable> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<DownloadLinkStorable> links) {
        if (links != null) {
            this.links = links;
            synchronized (filePackage) {
                for (DownloadLinkStorable link : links) {
                    filePackage.add(link._getDownloadLink());
                }
            }
        }
    }

    public FilePackage _getFilePackage() {
        return filePackage;
    }
}
