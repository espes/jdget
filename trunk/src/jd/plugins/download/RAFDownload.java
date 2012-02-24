//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;

import jd.controlling.JDLogger;
import jd.http.Request;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.Exceptions;
import org.appwork.utils.Hash;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.logging.Log;
import org.jdownloader.settings.GeneralSettings;
import org.jdownloader.translate._JDT;

public class RAFDownload extends DownloadInterface {

    public static final Object HASHCHECKLOCK = new Object();
    private RandomAccessFile   outputFile;

    public RAFDownload(PluginForHost plugin, DownloadLink downloadLink, Request request) throws IOException, PluginException {
        super(plugin, downloadLink, request);
    }

    @Override
    protected void onChunksReady() {
        logger.info("Close connections if they are not closed yet");
        try {
            for (Chunk c : this.getChunks()) {
                c.closeConnections();
            }
        } finally {
            logger.info("Close File. Let AV programs run");
            try {
                outputFile.close();
            } catch (Throwable e) {
            }
        }
        downloadLink.getLinkStatus().setStatusText(null);
        if (!handleErrors()) return;
        try {
            File part = new File(downloadLink.getFileOutput() + ".part");
            /* lets check the hash/crc/sfv */
            if (JsonConfig.create(GeneralSettings.class).isHashCheckEnabled()) {
                synchronized (HASHCHECKLOCK) {
                    /*
                     * we only want one hashcheck running at the same time. many
                     * finished downloads can cause heavy diskusage here
                     */
                    String hash = null;
                    String type = null;
                    Boolean success = null;
                    if ((hash = downloadLink.getMD5Hash()) != null) {
                        /* MD5 Check */
                        type = "MD5";
                        downloadLink.getLinkStatus().setStatusText(_JDT._.system_download_doCRC2("MD5"));
                        String hashFile = Hash.getMD5(part);
                        success = hash.equalsIgnoreCase(hashFile);
                    } else if ((hash = downloadLink.getSha1Hash()) != null) {
                        /* SHA1 Check */
                        type = "MD5";
                        downloadLink.getLinkStatus().setStatusText(_JDT._.system_download_doCRC2("SHA1"));
                        String hashFile = Hash.getSHA1(part);
                        success = hash.equalsIgnoreCase(hashFile);
                    } else {
                        DownloadLink sfv = null;
                        synchronized (downloadLink.getFilePackage()) {
                            for (DownloadLink dl : downloadLink.getFilePackage().getChildren()) {
                                if (dl.getFileOutput().toLowerCase().endsWith(".sfv")) {
                                    sfv = dl;
                                    break;
                                }
                            }
                        }
                        /* SFV File Available, lets use it */
                        if (sfv != null && sfv.getLinkStatus().hasStatus(LinkStatus.FINISHED)) {
                            String sfvText = IO.readFileToString(new File(sfv.getFileOutput()));
                            if (sfvText != null) {
                                /* Delete comments */
                                sfvText = sfvText.replaceAll(";(.*?)[\r\n]{1,2}", "");
                                if (sfvText != null && sfvText.contains(downloadLink.getName())) {
                                    downloadLink.getLinkStatus().setStatusText(_JDT._.system_download_doCRC2("CRC32"));
                                    type = "CRC32";
                                    String crc = Long.toHexString(Hash.getCRC32(part));
                                    success = new Regex(sfvText, downloadLink.getName() + "\\s*" + crc).matches();
                                }
                            }
                        }
                    }
                    if (success != null) {
                        hashCheckFinished(type, success);
                    }
                }
            }
            File complete = new File(downloadLink.getFileOutput());
            boolean renameOkay = false;
            int retry = 5;
            /* rename part file to final filename */
            while (retry > 0) {
                /* first we try normal rename method */
                if ((renameOkay = part.renameTo(complete)) == true) {
                    break;
                }
                /* this may fail because something might lock the file */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                retry--;
            }
            /* Fallback */
            if (renameOkay == false) {
                /* rename failed, lets try fallback */
                logger.severe("Could not rename file " + part + " to " + complete);
                logger.severe("Try copy workaround!");
                try {
                    if (Application.getJavaVersion() >= Application.JAVA16) {
                        long free = 0;
                        if ((free = part.getParentFile().getFreeSpace()) > 0 && free < part.length()) { throw new Throwable("not enough diskspace free to copy part to complete file"); }
                    }
                    IO.copyFile(part, complete);
                    renameOkay = true;
                    part.deleteOnExit();
                    part.delete();
                } catch (Throwable e) {
                    logger.log(Level.SEVERE, "Exception", e);
                    /* error happened, lets delete complete file */
                    complete.delete();
                    complete.deleteOnExit();
                }
                if (!renameOkay) {
                    logger.severe("Copy workaround: :(");
                    error(LinkStatus.ERROR_LOCAL_IO, _JDT._.system_download_errors_couldnotrename());
                } else {
                    logger.severe("Copy workaround: :)");
                }
            }
            if (renameOkay) {
                Date last = TimeFormatter.parseDateString(connection.getHeaderField("Last-Modified"));
                if (last != null && JsonConfig.create(GeneralSettings.class).isUseOriginalLastModified()) {
                    /* set original lastModified timestamp */
                    complete.setLastModified(last.getTime());
                } else {
                    /* set current timestamp as lastModified timestamp */
                    complete.setLastModified(System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            Log.exception(e);
            addException(e);
        }
    }

    private void hashCheckFinished(String hashType, boolean success) {
        logger.info(hashType + "-Check: " + (success ? "ok" : "failed"));
        if (success) {
            downloadLink.getLinkStatus().setStatusText(_JDT._.system_download_doCRC2_success(hashType));
        } else {
            String error = _JDT._.system_download_doCRC2_failed(hashType);
            downloadLink.getLinkStatus().removeStatus(LinkStatus.FINISHED);
            downloadLink.getLinkStatus().setStatusText(error);
            downloadLink.getLinkStatus().setValue(LinkStatus.VALUE_FAILED_HASH);
            error(LinkStatus.ERROR_DOWNLOAD_FAILED, error);
        }
    }

    @Override
    protected void setupChunks() throws Exception {
        try {
            if (isResume() && checkResumabled()) {
                logger.finer("Setup resume");
                this.setupResume();
            } else {
                logger.finer("Setup virgin download");
                this.setupVirginStart();
            }
        } catch (Exception e) {
            try {
                logger.info("CLOSE HD FILE");
                outputFile.close();
            } catch (Throwable e2) {
            }
            addException(e);
            throw e;
        }
    }

    private void setupVirginStart() throws FileNotFoundException {
        Chunk chunk;
        totaleLinkBytesLoaded = 0;
        downloadLink.setDownloadCurrent(0);
        long partSize = fileSize / getChunkNum();
        if (connection.getRange() != null) {
            if ((connection.getRange()[1] == connection.getRange()[2] - 1) || (connection.getRange()[1] == connection.getRange()[2])) {
                logger.warning("Chunkload protection. this may cause traffic errors");
                partSize = fileSize / getChunkNum();
            } else {
                // Falls schon der 1. range angefordert wurde.... werden die
                // restlichen chunks angepasst
                partSize = (fileSize - connection.getLongContentLength()) / (getChunkNum() - 1);
            }
        }
        if (partSize <= 0) {
            logger.warning("Could not get Filesize.... reset chunks to 1");
            setChunkNum(1);
        }
        logger.finer("Start Download in " + getChunkNum() + " chunks. Chunksize: " + partSize);

        createOutputChannel();
        downloadLink.setChunksProgress(new long[chunkNum]);

        addToChunksInProgress(getChunkNum());
        int start = 0;

        long rangePosition = 0;
        if (connection.getRange() != null && connection.getRange()[1] != connection.getRange()[2] - 1) {
            // Erster range schon angefordert

            chunk = new Chunk(0, rangePosition = connection.getRange()[1], connection, this);
            rangePosition++;
            logger.finer("Setup chunk 0: " + chunk);
            addChunk(chunk);
            start++;
        }

        for (int i = start; i < getChunkNum(); i++) {
            if (i == getChunkNum() - 1) {
                chunk = new Chunk(rangePosition, -1, connection, this);
            } else {
                chunk = new Chunk(rangePosition, rangePosition + partSize - 1, connection, this);
                rangePosition = rangePosition + partSize;
            }
            logger.finer("Setup chunk " + i + ": " + chunk);
            addChunk(chunk);
        }

    }

    private void createOutputChannel() throws FileNotFoundException {
        if (!new File(downloadLink.getFileOutput()).getParentFile().exists()) {
            new File(downloadLink.getFileOutput()).getParentFile().mkdirs();
        }
        outputFile = new RandomAccessFile(downloadLink.getFileOutput() + ".part", "rw");
    }

    private void setupResume() throws FileNotFoundException {

        long parts = fileSize / getChunkNum();
        logger.info("Resume: " + fileSize + " partsize: " + parts);
        Chunk chunk;
        this.createOutputChannel();
        addToChunksInProgress(getChunkNum());

        for (int i = 0; i < getChunkNum(); i++) {
            if (i == getChunkNum() - 1) {
                chunk = new Chunk(downloadLink.getChunksProgress()[i] == 0 ? 0 : downloadLink.getChunksProgress()[i] + 1, -1, connection, this);
                chunk.setLoaded((downloadLink.getChunksProgress()[i] - i * parts + 1));
            } else {
                chunk = new Chunk(downloadLink.getChunksProgress()[i] == 0 ? 0 : downloadLink.getChunksProgress()[i] + 1, (i + 1) * parts - 1, connection, this);
                chunk.setLoaded((downloadLink.getChunksProgress()[i] - i * parts + 1));
            }
            logger.finer("Setup chunk " + i + ": " + chunk);
            addChunk(chunk);
        }

    }

    @Override
    protected boolean writeChunkBytes(Chunk chunk) {
        try {
            synchronized (outputFile) {
                outputFile.seek(chunk.getWritePosition());
                outputFile.write(chunk.buffer.getInternalBuffer(), 0, chunk.buffer.size());
                if (chunk.getID() >= 0) {
                    downloadLink.getChunksProgress()[chunk.getID()] = chunk.getCurrentBytesPosition() - 1;
                }
                return true;
            }
        } catch (Exception e) {
            JDLogger.exception(e);
            error(LinkStatus.ERROR_LOCAL_IO, Exceptions.getStackTrace(e));
            addException(e);
            return false;
        }
    }

    /**
     * 
     * @param downloadLink
     *            downloadlink der geladne werden soll (wird zur darstellung
     *            verwendet)
     * @param request
     *            Verbindung die geladen werden soll
     * @param b
     *            Resumefaehige verbindung
     * @param i
     *            max chunks. fuer negative werte wirden die chunks aus der
     *            config verwendet. Bsp: -3 : Min(3,Configwert);
     * @return
     * @throws IOException
     * @throws PluginException
     */
    public static DownloadInterface download(DownloadLink downloadLink, Request request, boolean b, int i) throws IOException, PluginException {
        /* disable gzip, because current downloadsystem cannot handle it correct */
        request.getHeaders().put("Accept-Encoding", null);
        DownloadInterface dl = new RAFDownload(downloadLink.getLivePlugin(), downloadLink, request);
        PluginForHost plugin = downloadLink.getLivePlugin();
        if (plugin != null) plugin.setDownloadInterface(dl);
        if (i == 0) {
            dl.setChunkNum(JsonConfig.create(GeneralSettings.class).getMaxChunksPerFile());
        } else {
            dl.setChunkNum(i < 0 ? Math.min(i * -1, JsonConfig.create(GeneralSettings.class).getMaxChunksPerFile()) : i);
        }
        dl.setResume(b);
        return dl;

    }

    public static DownloadInterface download(DownloadLink downloadLink, Request request) throws Exception {
        return download(downloadLink, request, false, 1);
    }

    @Override
    public void cleanupDownladInterface() {
        try {
            this.outputFile.close();
        } catch (Throwable e) {
        }
    }

}