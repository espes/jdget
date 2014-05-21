package org.jdownloader.extensions.extraction.bindings.file;

import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.utils.Hash;
import org.appwork.utils.StringUtils;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.extensions.extraction.Archive;
import org.jdownloader.extensions.extraction.ArchiveFactory;
import org.jdownloader.extensions.extraction.ArchiveFile;
import org.jdownloader.extensions.extraction.BooleanStatus;
import org.jdownloader.extensions.extraction.ExtractionController;
import org.jdownloader.extensions.extraction.ExtractionStatus;
import org.jdownloader.extensions.extraction.bindings.downloadlink.DownloadLinkArchiveFile;
import org.jdownloader.settings.GeneralSettings;

public class FileArchiveFactory extends FileArchiveFile implements ArchiveFactory {
    
    private final Archive origin;
    
    public FileArchiveFactory(File archiveStartFile) {
        this(archiveStartFile, null);
    }
    
    public FileArchiveFactory(File archiveStartFile, Archive origin) {
        super(archiveStartFile);
        this.origin = origin;
    }
    
    public java.util.List<ArchiveFile> createPartFileList(String file, String pattern) {
        final Pattern pat = Pattern.compile(pattern, CrossSystem.isWindows() ? Pattern.CASE_INSENSITIVE : 0);
        List<ArchiveFile> ret = new ArrayList<ArchiveFile>();
        if (getFile().getParentFile() != null && getFile().getParentFile().exists()) {
            File[] list = getFile().getParentFile().listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isDirectory()) continue;
                    String nodeFile = f.getAbsolutePath();
                    if (nodeFile.equals(file) || pat.matcher(nodeFile).matches()) {
                        if (origin == null) {
                            ret.add(new FileArchiveFile(f));
                        } else {
                            ret.add(new FileArchiveFile(f) {
                                
                                @Override
                                public void setStatus(ExtractionController controller, ExtractionStatus error) {
                                    if (isFirstArchiveFile()) {
                                        origin.getFirstArchiveFile().setStatus(controller, error);
                                    } else {
                                        for (ArchiveFile archiveFile : origin.getArchiveFiles()) {
                                            archiveFile.setStatus(controller, error);
                                        }
                                    }
                                    super.setStatus(controller, error);
                                }
                                
                                @Override
                                public void setMessage(ExtractionController controller, String plugins_optional_extraction_status_notenoughspace) {
                                    if (isFirstArchiveFile()) {
                                        origin.getFirstArchiveFile().setMessage(controller, plugins_optional_extraction_status_notenoughspace);
                                    } else {
                                        for (ArchiveFile archiveFile : origin.getArchiveFiles()) {
                                            archiveFile.setMessage(controller, plugins_optional_extraction_status_notenoughspace);
                                        }
                                    }
                                    super.setMessage(controller, plugins_optional_extraction_status_notenoughspace);
                                }
                                
                                @Override
                                public void setProgress(ExtractionController controller, long value, long max, Color color) {
                                    if (isFirstArchiveFile()) {
                                        origin.getFirstArchiveFile().setProgress(controller, value, max, color);
                                    }
                                    super.setProgress(controller, value, max, color);
                                }
                                
                            });
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    public Archive createArchive() {
        if (origin == null) return new Archive(this);
        return new Archive(this) {
            
            @Override
            public Archive getPreviousArchive() {
                return origin;
            }
            
            @Override
            public void setFirstArchiveFile(ArchiveFile firstArchiveFile) {
                if (firstArchiveFile instanceof FileArchiveFile) {
                    ((FileArchiveFile) firstArchiveFile).setFirstArchiveFile(true);
                }
                super.setFirstArchiveFile(firstArchiveFile);
            }
        };
    }
    
    public Collection<? extends String> getGuessedPasswordList(Archive archive) {
        HashSet<String> ret = new HashSet<String>();
        ret.add(new File(archive.getFirstArchiveFile().getFilePath()).getName());
        return ret;
    }
    
    public void fireArchiveAddedToQueue(Archive archive) {
    }
    
    public String createDefaultExtractToPath(Archive archive) {
        return getFile().getParent();
    }
    
    public String createExtractSubPath(String path, Archive archiv) {
        try {
            if (path.contains(PACKAGENAME)) {
                String packageName = null;
                for (ArchiveFile file : archiv.getArchiveFiles()) {
                    if (packageName != null) break;
                    if (file instanceof DownloadLinkArchiveFile) {
                        DownloadLinkArchiveFile daf = (DownloadLinkArchiveFile) file;
                        if (daf.getDownloadLinks() != null) {
                            for (DownloadLink link : daf.getDownloadLinks()) {
                                if (!FilePackage.isDefaultFilePackage(link.getFilePackage())) {
                                    packageName = CrossSystem.alleviatePathParts(link.getFilePackage().getName());
                                    break;
                                } else if ((packageName = link.getStringProperty(DownloadLink.PROPERTY_LASTFPNAME, null)) != null) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!StringUtils.isEmpty(packageName)) {
                    path = path.replace(PACKAGENAME, packageName);
                } else {
                    path = path.replace(PACKAGENAME, "");
                    Log.L.severe("Could not set packagename for " + archiv.getFirstArchiveFile().getFilePath());
                }
            }
            if (path.contains(ARCHIVENAME)) {
                if (!StringUtils.isEmpty(archiv.getName())) {
                    path = path.replace(ARCHIVENAME, archiv.getName());
                } else {
                    path = path.replace(ARCHIVENAME, "");
                    Log.L.severe("Could not set archivename for " + archiv.getFirstArchiveFile().getFilePath());
                }
            }
            if (path.contains(HOSTER)) {
                path = path.replace(HOSTER, "");
                Log.L.severe("Could not set hoster for " + archiv.getFirstArchiveFile().getFilePath());
            }
            if (path.contains("$DATE:")) {
                int start = path.indexOf("$DATE:");
                int end = start + 6;
                
                while (end < path.length() && path.charAt(end) != '$') {
                    end++;
                }
                
                try {
                    SimpleDateFormat format = new SimpleDateFormat(path.substring(start + 6, end));
                    path = path.replace(path.substring(start, end + 1), format.format(new Date()));
                } catch (Exception e) {
                    path = path.replace(path.substring(start, end + 1), "");
                    Log.L.severe("Could not set extraction date. Maybe pattern is wrong. For " + archiv.getFirstArchiveFile().getFilePath());
                }
            }
            String dif = new File(org.appwork.storage.config.JsonConfig.create(GeneralSettings.class).getDefaultDownloadFolder()).getAbsolutePath().replace(getFile().getParent(), "");
            if (new File(dif).isAbsolute()) {
                dif = "";
            }
            path = path.replace(SUBFOLDER, dif);
            return CrossSystem.fixPathSeperators(path);
        } catch (Exception e) {
            Log.exception(e);
        }
        return null;
    }
    
    public File toFile(String path) {
        return new File(path);
    }
    
    @Override
    public File getFolder() {
        return getFile().getParentFile();
    }
    
    @Override
    public String getID() {
        return Hash.getMD5(getFolder() + "/" + getName());
    }
    
    @Override
    public void onArchiveFinished(Archive archive) {
    }
    
    @Override
    public BooleanStatus getDefaultAutoExtract() {
        return BooleanStatus.UNSET;
    }
    
}
