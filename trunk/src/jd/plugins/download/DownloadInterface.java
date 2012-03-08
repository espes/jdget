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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;

import jd.controlling.GarbageController;
import jd.controlling.JDLogger;
import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.ManagedThrottledConnectionHandler;
import jd.http.Browser;
import jd.http.Request;
import jd.http.URLConnectionAdapter;
import jd.nutils.Formatter;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Exceptions;
import org.appwork.utils.Regex;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;
import org.appwork.utils.net.httpconnection.HTTPConnection.RequestMethod;
import org.appwork.utils.net.throttledconnection.MeteredThrottledInputStream;
import org.appwork.utils.speedmeter.AverageSpeedMeter;
import org.jdownloader.settings.GeneralSettings;
import org.jdownloader.settings.IfFileExistsAction;
import org.jdownloader.translate._JDT;

abstract public class DownloadInterface {

    /**
     * Chunk Klasse verwaltet eine einzelne Downloadverbindung.
     * 
     * @author coalado
     */
    public class Chunk extends Thread {

        /**
         * Wird durch die Speedbegrenzung ein chunk uter diesen Wert geregelt,
         * so wird er weggelassen. Sehr niedrig geregelte chunks haben einen
         * kleinen Buffer und eine sehr hohe Intervalzeit. Das fuehrt zu
         * verstaerkt intervalartigem laden und ist ungewuenscht
         */
        public static final long                MIN_CHUNKSIZE        = 1 * 1024 * 1024;

        protected ReusableByteArrayOutputStream buffer               = null;

        private long                            chunkBytesLoaded     = 0;

        private URLConnectionAdapter            connection;

        private long                            endByte;

        private int                             id                   = -1;

        private MeteredThrottledInputStream     inputStream;

        private long                            startByte;
        private long                            bytes2Do             = -1;

        private DownloadInterface               dl;

        private boolean                         connectionclosed     = false;

        private boolean                         addedtoStartedChunks = false;

        private boolean                         chunkinprogress      = false;

        private boolean                         clonedconnection     = false;

        /**
         * Die Connection wird entsprechend der start und endbytes neu
         * aufgebaut.
         * 
         * @param startByte
         * @param endByte
         * @param connection
         */
        public Chunk(long startByte, long endByte, URLConnectionAdapter connection, DownloadInterface dl) {
            super("Downloadchunk " + startByte + " - " + endByte);
            this.startByte = startByte;
            this.endByte = endByte;
            this.connection = connection;
            this.clonedconnection = false;
            this.dl = dl;
            setPriority(Thread.MIN_PRIORITY);
        }

        @Deprecated
        public int getMaximalSpeed() {
            return 0;
        }

        @Deprecated
        public void setMaximalSpeed(final int i) {
        }

        private void addChunkBytesLoaded(long limit) {
            chunkBytesLoaded += limit;
        }

        /**
         * is this Chunk still in progress?
         * 
         * @return
         */
        public boolean inProgress() {
            return chunkinprogress;
        }

        /**
         * is this Chunk using the root connection or a cloned one
         * 
         * @return
         */
        public boolean isClonedConnection() {
            return clonedconnection;
        }

        public void setInProgress(boolean b) {
            chunkinprogress = b;
        }

        private void setChunkStartet() {
            /* Chunk kann nur einmal gestartet werden */
            if (addedtoStartedChunks) return;
            addChunksStarted(+1);
            addedtoStartedChunks = true;
        }

        /**
         * Gibt Fortschritt in % an (10000 entspricht 100%))
         * 
         * @return
         */
        public int getPercent() {
            return (int) (10000 * chunkBytesLoaded / Math.max(1, Math.max(chunkBytesLoaded, (endByte - startByte))));
        }

        /**
         * Kopiert die Verbindung. Es wird bis auf die Range und timeouts exakt
         * die selbe Verbindung nochmals aufgebaut.
         * 
         * @param connection
         * @return
         */
        private URLConnectionAdapter copyConnection(URLConnectionAdapter connection) {
            try {
                while (downloadLink.getLivePlugin().waitForNextConnectionAllowed()) {
                }
            } catch (InterruptedException e) {
                return null;
            } catch (NullPointerException e) {
                if (downloadLink.getLivePlugin() == null) return null;
                throw e;
            }
            downloadLink.getLivePlugin().putLastConnectionTime(System.currentTimeMillis());
            long start = startByte;
            String end = (endByte > 0 ? endByte + 1 : "") + "";

            if (start == 0) {
                logger.finer("Takeover 0 Connection");
                return connection;
            }
            if (connection.getRange() != null && connection.getRange()[0] == (start)) {
                logger.finer("Takeover connection at " + connection.getRange()[0]);
                return connection;
            }

            try {
                /* only forward referer if referer already has been sent! */
                boolean forwardReferer = plugin.getBrowser().getHeaders().contains("Referer");
                Browser br = plugin.getBrowser().cloneBrowser();
                br.setReadTimeout(getReadTimeout());
                br.setConnectTimeout(getRequestTimeout());
                /* set requested range */

                Map<String, String> request = connection.getRequestProperties();
                if (request != null) {
                    String value;
                    for (Entry<String, String> next : request.entrySet()) {
                        if (next.getValue() == null) continue;
                        value = next.getValue().toString();
                        br.getHeaders().put(next.getKey(), value);
                    }
                }
                if (!forwardReferer) {
                    /* only forward referer if referer already has been sent! */
                    br.setCurrentURL(null);
                }
                URLConnectionAdapter con = null;
                clonedconnection = true;
                if (connection.getRequestMethod() == RequestMethod.POST) {
                    connection.getRequest().getHeaders().put("Range", "bytes=" + start + "-" + end);
                    con = br.openRequestConnection(connection.getRequest());
                } else {
                    br.getHeaders().put("Range", "bytes=" + start + "-" + end);
                    con = br.openGetConnection(connection.getURL() + "");
                }
                if (!con.isOK()) {
                    try {
                        /* always close connections that got opened */
                        con.disconnect();
                    } catch (Throwable e) {
                    }
                    if (con.getResponseCode() != 416) {
                        error(LinkStatus.ERROR_DOWNLOAD_FAILED, "Server: " + con.getResponseMessage());
                    } else {
                        logger.warning("HTTP 416, maybe finished last chunk?");
                    }
                    return null;
                }
                if (con.getHeaderField("Location") != null) {
                    try {
                        /* always close connections that got opened */
                        con.disconnect();
                    } catch (Throwable e) {
                    }
                    error(LinkStatus.ERROR_DOWNLOAD_FAILED, "Server: Redirect");
                    return null;
                }
                return con;
            } catch (Exception e) {
                addException(e);
                error(LinkStatus.ERROR_RETRY, Exceptions.getStackTrace(e));
                JDLogger.exception(e);
            }
            return null;
        }

        /** Die eigentliche Downloadfunktion */
        private void download() {
            int flushLevel = 0;
            int flushTimeout = JsonConfig.create(GeneralSettings.class).getFlushBufferTimeout();
            try {
                int maxbuffersize = JsonConfig.create(GeneralSettings.class).getMaxBufferSize() * 1024;
                buffer = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(Math.max(maxbuffersize, 10240), false);
                /*
                 * now we calculate the max fill level when to force buffer
                 * flushing
                 */
                flushLevel = Math.max((maxbuffersize / 100 * JsonConfig.create(GeneralSettings.class).getFlushBufferLevel()), 1);
            } catch (Throwable e) {
                error(LinkStatus.ERROR_FATAL, _JDT._.download_error_message_outofmemory());
                return;
            }
            /* +1 because of startByte also gets loaded (startbyte till endbyte) */
            if (endByte > 0) bytes2Do = (endByte - startByte) + 1;
            try {
                chunkinprogress = true;
                connection.setReadTimeout(getReadTimeout());
                connection.setConnectTimeout(getRequestTimeout());
                inputStream = new MeteredThrottledInputStream(connection.getInputStream(), new AverageSpeedMeter(10));
                connectionHandler.addThrottledConnection(inputStream);
                int towrite = 0;
                int read = 0;
                boolean reachedEOF = false;
                long lastFlush = 0;
                while (!isExternalyAborted()) {
                    try {
                        buffer.reset();
                        if (reachedEOF == true) {
                            /* we already reached EOF, so nothing more to read */
                            towrite = -1;
                        } else {
                            /* lets try to read some data */
                            towrite = 0;
                        }
                        lastFlush = System.currentTimeMillis();
                        while (!reachedEOF && buffer.free() > 0 && buffer.size() <= flushLevel) {
                            if (endByte > 0) {
                                /* read only as much as needed */
                                read = inputStream.read(buffer.getInternalBuffer(), buffer.size(), (int) Math.min(bytes2Do, (buffer.getInternalBuffer().length - buffer.size())));
                                if (read > 0) {
                                    bytes2Do -= read;
                                    if (bytes2Do == 0) {
                                        /* we reached our artificial EOF */
                                        reachedEOF = true;
                                    }
                                }
                            } else {
                                /* read as much as possible */
                                read = inputStream.read(buffer.getInternalBuffer(), buffer.size(), (buffer.getInternalBuffer().length - buffer.size()));
                            }
                            if (read > 0) {
                                /* we read some data */
                                towrite += read;
                                buffer.setUsed(towrite);
                            } else if (read == -1) {
                                /* we reached EOF */
                                reachedEOF = true;
                            } else {
                                /*
                                 * wait a moment, give system chance to fill up
                                 * its buffers
                                 */
                                synchronized (this) {
                                    this.wait(500);
                                }
                            }
                            if (System.currentTimeMillis() - lastFlush > flushTimeout) {
                                /* we reached our flush timeout */
                                break;
                            }
                        }
                    } catch (NullPointerException e) {
                        if (inputStream == null) {
                            /* connection is closed and steam is null */
                            if (!isExternalyAborted() && !connectionclosed) throw e;
                            towrite = -1;
                            break;
                        }
                        throw e;
                    } catch (SocketException e2) {
                        if (!isExternalyAborted()) throw e2;
                        towrite = -1;
                        break;
                    } catch (ClosedByInterruptException e) {
                        if (!isExternalyAborted()) {
                            logger.severe("Timeout detected");
                            error(LinkStatus.ERROR_TIMEOUT_REACHED, null);
                        }
                        towrite = -1;
                        break;
                    } catch (AsynchronousCloseException e3) {
                        if (!isExternalyAborted() && !connectionclosed) throw e3;
                        towrite = -1;
                        break;
                    } catch (IOException e4) {
                        if (!isExternalyAborted() && !connectionclosed) throw e4;
                        towrite = -1;
                        break;
                    }
                    if (towrite == -1 || isExternalyAborted() || connectionclosed) {
                        break;
                    }
                    if (towrite > 0) {
                        addToTotalLinkBytesLoaded(towrite);
                        addChunkBytesLoaded(towrite);
                        writeBytes(this);
                    }
                    /* enough bytes loaded */
                    if (bytes2Do == 0 && endByte > 0) break;
                    if (getCurrentBytesPosition() > endByte && endByte > 0) {
                        break;
                    }
                }
                if (getCurrentBytesPosition() < endByte && endByte > 0 || getCurrentBytesPosition() <= 0) {
                    logger.warning("Download not finished. Loaded until now: " + getCurrentBytesPosition() + "/" + endByte);
                    error(LinkStatus.ERROR_DOWNLOAD_FAILED, _JDT._.download_error_message_incomplete());
                }
            } catch (FileNotFoundException e) {
                logger.severe("file not found. " + e.getLocalizedMessage());
                error(LinkStatus.ERROR_FILE_NOT_FOUND, null);
            } catch (SecurityException e) {
                logger.severe("not enough rights to write the file. " + e.getLocalizedMessage());
                error(LinkStatus.ERROR_LOCAL_IO, _JDT._.download_error_message_iopermissions());
            } catch (UnknownHostException e) {
                linkStatus.setValue(10 * 60000l);
                error(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, _JDT._.download_error_message_unavailable());
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("reset")) {
                    JDLogger.getLogger().info("Connection reset: network problems!");
                    linkStatus.setValue(1000l * 60 * 5);
                    error(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, _JDT._.download_error_message_networkreset());
                } else if (e.getMessage() != null && e.getMessage().indexOf("timed out") >= 0) {
                    JDLogger.getLogger().info("Read timeout: network problems! (too many connections?, firewall/antivirus?)");
                    error(LinkStatus.ERROR_TIMEOUT_REACHED, _JDT._.download_error_message_networkreset());
                    JDLogger.exception(e);
                } else {
                    JDLogger.exception(e);
                    if (e.getMessage() != null && e.getMessage().contains("503")) {
                        linkStatus.setValue(10 * 60000l);
                        error(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, _JDT._.download_error_message_unavailable());
                    } else {
                        logger.severe("error occurred while writing to file. " + e.getMessage());
                        error(LinkStatus.ERROR_LOCAL_IO, _JDT._.download_error_message_iopermissions());
                    }
                }
            } catch (Exception e) {
                JDLogger.exception(e);
                error(LinkStatus.ERROR_RETRY, Exceptions.getStackTrace(e));
                addException(e);
            } finally {
                ReusableByteArrayOutputStreamPool.reuseReusableByteArrayOutputStream(buffer);
                buffer = null;
                chunkinprogress = false;
                try {
                    inputStream.close();
                } catch (Throwable e) {
                } finally {
                    inputStream = null;
                }
                try {
                    if (this.clonedconnection) {
                        /* cloned connection, we can disconnect now */
                        this.connection.disconnect();
                        this.connection = null;
                    }
                } catch (Throwable e) {
                }
            }
        }

        /**
         * Gibt die Geladenen ChunkBytes zurueck
         * 
         * @return
         */
        public long getBytesLoaded() {
            return getCurrentBytesPosition() - startByte;
        }

        public long getChunkSize() {
            return endByte - startByte + 1;
        }

        /**
         * Gibt die Aktuelle Endposition in der gesamtfile zurueck. Diese
         * Methode gibt die Endposition unahaengig davon an Ob der aktuelle
         * BUffer schon geschrieben wurde oder nicht.
         * 
         * @return
         */
        public long getCurrentBytesPosition() {
            return startByte + chunkBytesLoaded;
        }

        public long getEndByte() {
            return endByte;
        }

        public int getID() {
            if (id < 0) {
                synchronized (chunks) {
                    id = chunks.indexOf(this);
                }
            }
            return id;
        }

        public long getStartByte() {
            return startByte;
        }

        /**
         * Gibt die Schreibposition des Chunks in der gesamtfile zurueck
         * 
         * @throws Exception
         */
        public long getWritePosition() throws Exception {
            long c = getCurrentBytesPosition();
            long l = buffer.size();
            return c - l;
        }

        /**
         * Gibt zurueck ob der chunk von einem externen eregniss unterbrochen
         * wurde
         * 
         * @return
         */
        private boolean isExternalyAborted() {
            DownloadInterface dli = downloadLink.getDownloadInstance();
            return isInterrupted() || (dli != null && dli.externalDownloadStop());
        }

        /**
         * Thread runner
         */
        @Override
        public void run() {
            try {
                run0();
                while (true) {
                    /* wait for all chunks being started */
                    if (getChunksStarted() == chunkNum) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    /* external abort, proceed to close connection */
                    if (this.isExternalyAborted()) break;
                }
            } finally {
                try {
                    addToChunksInProgress(-1);
                } catch (Throwable e) {
                    JDLogger.exception(e);
                }
                onChunkFinished();
            }
        }

        /**
         * returns Long[] with Start/Stop/Size if available
         * 
         * @param connection
         * @return
         */
        private Long[] parseContentRange(URLConnectionAdapter connection, long startByte, long endByte) {
            String contentRange = connection.getHeaderField("Content-Range");
            String[] range = null;
            if (contentRange != null) {
                if ((range = new Regex(contentRange, ".*?(\\d+).*?-.*?(\\d+).*?/.*?(\\d+)").getRow(0)) != null) {
                    /* RFC-2616 */
                    /* START-STOP/SIZE */
                    /* Content-Range=[133333332-199999999/200000000] */
                    long gotSB = Formatter.filterLong(range[0]);
                    long gotEB = Formatter.filterLong(range[1]);
                    long gotS = Formatter.filterLong(range[2]);
                    if (gotSB != startByte) {
                        logger.severe("Range Conflict " + range[0] + " - " + range[1] + " wished start: " + 0);
                    }
                    if (endByte <= 0) {
                        endByte = gotS - 1;
                    }
                    if (gotEB == endByte) {
                        logger.finer("ServerType: RETURN Rangeend-1");
                    } else if (gotEB == endByte + 1) {
                        logger.finer("ServerType: RETURN exact rangeend");
                    } else if (gotEB < endByte) {
                        logger.severe("Range Conflict " + range[0] + " - " + range[1] + " wishedend: " + endByte);
                    } else if (gotEB > endByte + 1) {
                        logger.warning("Possible RangeConflict or Servermisconfiguration. wished endByte: " + endByte + " got: " + gotEB);
                    }
                    endByte = Math.min(endByte, gotEB);
                    return new Long[] { gotSB, endByte, gotS };
                } else if ((range = new Regex(contentRange, ".*?(\\d+).*?-/.*?(\\d+)").getRow(0)) != null) {
                    /* NON RFC-2616! STOP is missing */
                    /*
                     * this happend for some stupid servers, seems to happen
                     * when request is bytes=9500- (x till end)
                     */
                    /* START-/SIZE */
                    /* content-range: bytes 1020054729-/1073741824 */
                    long gotSB = Formatter.filterLong(range[0]);
                    long gotS = Formatter.filterLong(range[1]);
                    if (gotSB != startByte) {
                        logger.severe("Range Conflict " + range[0] + "->wished start: " + 0);
                    }
                    if (endByte <= 0) {
                        endByte = gotS - 1;
                    }
                    long guessEB = gotSB + connection.getLongContentLength();
                    if (guessEB == endByte) {
                        logger.finer("ServerType: RETURN Rangeend-1");
                    } else if (guessEB == endByte + 1) {
                        logger.finer("ServerType: RETURN exact rangeend");
                    } else if (guessEB < endByte) {
                        logger.severe("Range Conflict " + range[0] + " - " + range[1] + " wishedend: " + endByte);
                    } else if (guessEB > endByte + 1) {
                        logger.warning("Possible RangeConflict or Servermisconfiguration. wished endByte: " + endByte + " got: " + guessEB);
                    }
                    endByte = Math.min(endByte, guessEB);
                    return new Long[] { gotSB, endByte, gotS };
                }
            }
            return null;
        }

        public void run0() {
            try {
                logger.finer("Start Chunk " + getID() + " : " + startByte + " - " + endByte);
                if (startByte >= endByte && endByte > 0 || startByte >= getFileSize() && endByte > 0) return;
                if (chunkNum > 1) {
                    /* we requested multiple chunks */
                    connection = copyConnection(connection);
                    if (connection == null) {
                        /* copy failed!, lets check if this is the last chunk */
                        if (startByte >= fileSize && fileSize > 0) {
                            downloadLink.getLinkStatus().removeStatus(LinkStatus.ERROR_DOWNLOAD_FAILED);
                            logger.finer("Is no error. Last chunk is just already finished");
                            return;
                        }
                        error(LinkStatus.ERROR_DOWNLOAD_FAILED, _JDT._.download_error_message_connectioncopyerror());
                        if (!this.isExternalyAborted()) logger.severe("ERROR Chunk (connection copy failed) " + getID());
                        return;
                    }
                } else if (startByte > 0) {
                    connection = copyConnection(connection);
                    // workaround fuer fertigen endchunk
                    if (startByte >= fileSize && fileSize > 0) {
                        downloadLink.getLinkStatus().removeStatus(LinkStatus.ERROR_DOWNLOAD_FAILED);
                        logger.finer("Is no error. Last chunk is just already finished");
                        return;
                    }
                    if (connection == null) {
                        error(LinkStatus.ERROR_DOWNLOAD_FAILED, _JDT._.download_error_message_connectioncopyerror());
                        if (!this.isExternalyAborted()) logger.severe("ERROR Chunk (connection copy failed) " + getID());
                        return;
                    }

                    if (startByte > 0 && (connection.getHeaderField("Content-Range") == null || connection.getHeaderField("Content-Range").length() == 0)) {
                        error(LinkStatus.ERROR_DOWNLOAD_FAILED, _JDT._.download_error_message_rangeheaders());
                        logger.severe("ERROR Chunk (no range header response)" + getID() + connection.toString());
                        // logger.finest(connection.toString());
                        return;
                    }
                }
                Long[] ContentRange = parseContentRange(connection, startByte, endByte);
                if (startByte > 0) {
                    /* startByte >0, we should have a Content-Range in response! */
                    if (ContentRange != null && ContentRange.length == 3) {
                        endByte = ContentRange[1];
                    } else if (chunkNum > 1) {
                        /* WTF? no Content-Range response available! */
                        if (connection.getLongContentLength() == startByte) {
                            /*
                             * Content-Length equals startByte -> Chunk is
                             * Complete!
                             */
                            return;
                        }
                        logger.severe("ERROR Chunk (range header parse error)" + getID() + connection.toString());
                        error(LinkStatus.ERROR_DOWNLOAD_FAILED, _JDT._.download_error_message_rangeheaderparseerror() + connection.getHeaderField("Content-Range"));
                        return;
                    } else {
                        /* only one chunk requested, set correct endByte */
                        endByte = connection.getLongContentLength() - 1;
                    }
                } else if (ContentRange != null) {
                    /*
                     * we did not request a range but got a content-range
                     * response,WTF?!
                     */
                    logger.severe("No Range Request->Content-Range Response?!");
                    endByte = ContentRange[1];
                }
                if (endByte <= 0) {
                    /* endByte not yet set!, use Content-Length */
                    endByte = connection.getLongContentLength() - 1;
                }
                addChunksDownloading(+1);
                setChunkStartet();
                download();
                addChunksDownloading(-1);
                logger.finer("Chunk finished " + getID() + " " + getBytesLoaded() + " bytes");
            } finally {
                setChunkStartet();
                try {
                    /* we can close cloned connections here */
                    if (this.clonedconnection) {
                        connection.disconnect();
                        connection = null;
                    }
                } catch (Throwable e) {
                }
            }
        }

        /**
         * Setzt die anzahl der schon geladenen partbytes. Ist fuer resume
         * wichtig.
         * 
         * @param loaded
         */
        public void setLoaded(long loaded) {
            loaded = Math.max(0, loaded);
            addToTotalLinkBytesLoaded(loaded);
        }

        public void startChunk() {
            start();
        }

        public void closeConnections() {
            connectionclosed = true;
            try {
                inputStream.close();
            } catch (Throwable e) {
            } finally {
                inputStream = null;
            }
            try {
                connection.disconnect();
            } catch (Throwable e) {
            } finally {
                connection = null;
            }
        }

        public MeteredThrottledInputStream getInputStream() {
            return inputStream;
        }

    }

    public static final int                   ERROR_REDIRECTED                 = -1;

    public Logger                             logger                           = null;

    protected int                             chunkNum                         = 1;

    private Vector<Chunk>                     chunks                           = new Vector<Chunk>();
    private ManagedThrottledConnectionHandler connectionHandler                = null;

    private int                               chunksDownloading                = 0;

    private int                               chunksInProgress                 = 0;

    protected URLConnectionAdapter            connection;

    protected DownloadLink                    downloadLink;

    private Vector<Integer>                   errors                           = new Vector<Integer>();

    private Vector<Exception>                 exceptions                       = null;

    protected long                            fileSize                         = -1;

    protected LinkStatus                      linkStatus;

    protected PluginForHost                   plugin;

    private int                               readTimeout                      = 100000;
    private int                               requestTimeout                   = 100000;

    private boolean                           resume                           = false;

    private boolean                           fixWrongContentDispositionHeader = false;

    private boolean                           allowFilenameFromURL             = false;

    protected long                            totaleLinkBytesLoaded            = 0;

    public long getTotaleLinkBytesLoaded() {
        return totaleLinkBytesLoaded;
    }

    private boolean          waitFlag          = true;

    private boolean          fatalErrorOccured = false;

    private boolean          doFileSizeCheck   = true;

    private Request          request           = null;

    private boolean          fileSizeVerified  = false;

    private boolean          connected;

    private boolean          firstChunkRangeless;

    private int              chunksStarted     = 0;

    private Browser          browser;

    /** normal stop of download (eg manually or reconnect request) */
    private volatile boolean externalStop      = false;

    private boolean          resumable         = false;

    public void setFilenameFix(boolean b) {
        this.fixWrongContentDispositionHeader = b;
    }

    public synchronized void addChunksStarted(int i) {
        chunksStarted += i;
    }

    public synchronized int getChunksStarted() {
        return chunksStarted;
    }

    public void setAllowFilenameFromURL(boolean b) {
        this.allowFilenameFromURL = b;
    }

    private DownloadInterface(PluginForHost plugin, DownloadLink downloadLink) {
        this.downloadLink = downloadLink;
        this.plugin = plugin;
        logger = plugin.getLogger();
        linkStatus = downloadLink.getLinkStatus();
        linkStatus.setStatusText(_JDT._.download_connection_normal());
        browser = plugin.getBrowser().cloneBrowser();
        downloadLink.setDownloadInstance(this);
        requestTimeout = JsonConfig.create(GeneralSettings.class).getHttpConnectTimeout();
        readTimeout = JsonConfig.create(GeneralSettings.class).getHttpReadTimeout();
        connectionHandler = new ManagedThrottledConnectionHandler(downloadLink);
    }

    public ManagedThrottledConnectionHandler getManagedConnetionHandler() {
        return connectionHandler;
    }

    public int getSpeed() {
        return connectionHandler.getSpeed();
    }

    public DownloadInterface(PluginForHost plugin, DownloadLink downloadLink, Request request) throws IOException, PluginException {
        this(plugin, downloadLink);
        this.request = request;
    }

    /**
     * Gibt zurueck ob die Dateigroesse 100% richtig ermittelt werden konnte
     */
    public boolean isFileSizeVerified() {
        return fileSizeVerified;
    }

    /**
     * darf NUR dann auf true gesetzt werden, wenn die dateigroesse 100% richtig
     * ist!
     * 
     * @param fileSizeVerified
     * @throws PluginException
     */
    public void setFileSizeVerified(boolean fileSizeVerified) throws PluginException {
        this.fileSizeVerified = fileSizeVerified;
        if (fileSize <= 0 && fileSizeVerified) {
            logger.severe("Downloadsize==0");
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 20 * 60 * 1000l);
        }
    }

    /**
     * Validiert das Chunk Progress array
     */
    protected boolean checkResumabled() {
        if (downloadLink.getChunksProgress() == null || downloadLink.getChunksProgress().length == 0) return false;

        long fileSize = getFileSize();
        int chunks = downloadLink.getChunksProgress().length;
        long part = fileSize / chunks;
        long dif;
        long last = -1;
        for (int i = 0; i < chunks; i++) {
            dif = downloadLink.getChunksProgress()[i] - i * part;
            if (dif < 0) return false;
            if (downloadLink.getChunksProgress()[i] <= last) return false;
            last = downloadLink.getChunksProgress()[i];
        }
        if (chunks > 0) {
            if (chunks <= this.getChunkNum()) {
                /* downloadchunks are less or equal to allowed chunks */
                setChunkNum(chunks);
            } else {
                /*
                 * downloadchunks are more than allowed chunks, need to
                 * repartition the download
                 */
                logger.info("Download has " + chunks + " Chunks but only " + getChunkNum() + " allowed! Change to 1!");
                setChunkNum(1);
                downloadLink.setChunksProgress(new long[] { downloadLink.getChunksProgress()[0] });
            }
            return true;
        }
        return false;
    }

    public URLConnectionAdapter connect(Browser br) throws Exception {
        /* reset timeouts here, because it can be they got not set yet */
        setReadTimeout(br.getReadTimeout());
        setRequestTimeout(br.getConnectTimeout());
        request.setConnectTimeout(getRequestTimeout());
        request.setReadTimeout(getReadTimeout());
        br.setRequest(request);
        URLConnectionAdapter ret = connect();
        /* we have to update cookie for used browser instance here */
        br.updateCookies(request);
        return ret;
    }

    public URLConnectionAdapter connect() throws Exception {
        logger.finer("Connect...");
        if (request == null) throw new IllegalStateException("Wrong Mode. Instance is in direct Connection mode");
        this.connected = true;
        if (this.isResume() && this.checkResumabled()) {
            connectResumable();
        } else {
            if (this.isFileSizeVerified()) {
                int tmp = Math.min(Math.max(1, (int) (downloadLink.getDownloadSize() / Chunk.MIN_CHUNKSIZE)), getChunkNum());

                if (tmp != getChunkNum()) {
                    logger.finer("Corrected Chunknum: " + getChunkNum() + " -->" + tmp);
                    setChunkNum(tmp);
                }
            }
            if (this.isFileSizeVerified() && downloadLink.getDownloadSize() > 0 && this.getChunkNum() > 1 && !this.isFirstChunkRangeless()) {
                connectFirstRange();
            } else {
                request.getHeaders().remove("Range");
                browser.connect(request);
            }
        }
        if (this.plugin.getBrowser().isDebug()) logger.finest(request.printHeaders());
        connection = request.getHttpConnection();
        if (request.getLocation() != null) throw new PluginException(LinkStatus.ERROR_DOWNLOAD_FAILED, DownloadInterface.ERROR_REDIRECTED);
        if (connection.getRange() != null) {
            // Dateigroesse wird aus dem Range-Response gelesen
            if (connection.getRange()[2] > 0) {
                this.setFilesizeCheck(true);
                this.downloadLink.setDownloadSize(connection.getRange()[2]);
            }
        } else {
            if (connection.getLongContentLength() > 0) {
                this.setFilesizeCheck(true);
                this.downloadLink.setDownloadSize(connection.getLongContentLength());
            }

        }
        fileSize = downloadLink.getDownloadSize();

        return connection;
    }

    private void connectFirstRange() throws IOException {
        long part = downloadLink.getDownloadSize() / this.getChunkNum();
        request.getHeaders().put("Range", "bytes=" + (0) + "-" + (part - 1));
        browser.connect(request);
        if (request.getHttpConnection().getResponseCode() == 416) {
            logger.warning("HTTP/1.1 416 Requested Range Not Satisfiable");
            if (this.plugin.getBrowser().isDebug()) logger.finest(request.printHeaders());
            throw new IllegalStateException("HTTP/1.1 416 Requested Range Not Satisfiable");

        } else if (request.getHttpConnection().getRange() == null) {
            logger.warning("No Chunkload");
            setChunkNum(1);
        } else {
            if (request.getHttpConnection().getRange()[0] != 0) throw new IllegalStateException("Range Error. Requested " + request.getHeaders().get("Range") + ". Got range: " + request.getHttpConnection().getHeaderField("Content-Range"));
            if (request.getHttpConnection().getRange()[1] < (part - 2)) throw new IllegalStateException("Range Error. Requested " + request.getHeaders().get("Range") + " Got range: " + request.getHttpConnection().getHeaderField("Content-Range"));
            if (request.getHttpConnection().getRange()[1] == request.getHttpConnection().getRange()[2] - 1 && getChunkNum() > 1) {
                logger.warning(" Chunkload Protection.. Requested " + request.getHeaders().get("Range") + " Got range: " + request.getHttpConnection().getHeaderField("Content-Range"));
            } else if (request.getHttpConnection().getRange()[1] > (part - 1)) throw new IllegalStateException("Range Error. Requested " + request.getHeaders().get("Range") + " Got range: " + request.getHttpConnection().getHeaderField("Content-Range"));
        }
    }

    private void connectResumable() throws IOException {
        // TODO: endrange pruefen

        long[] chunkProgress = downloadLink.getChunksProgress();
        String start, end;
        start = end = "";

        if (this.isFileSizeVerified()) {
            start = chunkProgress[0] == 0 ? "0" : (chunkProgress[0] + 1) + "";
            end = (fileSize / chunkProgress.length) + "";
        } else {
            start = chunkProgress[0] == 0 ? "0" : (chunkProgress[0] + 1) + "";
            end = chunkProgress.length > 1 ? (chunkProgress[1] + 1) + "" : "";
        }
        if (this.isFirstChunkRangeless() && start.equals("0")) {
            request.getHeaders().remove("Range");
        } else {
            request.getHeaders().put("Range", "bytes=" + start + "-" + end);
        }
        browser.connect(request);
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    /**
     * Fuegt einen Chunk hinzu und startet diesen
     * 
     * @param chunk
     */
    protected void addChunk(Chunk chunk) {
        synchronized (chunks) {
            chunks.add(chunk);
        }
        chunk.startChunk();
    }

    /**
     * Public for dummy mode
     * 
     * @param i
     */
    public synchronized void addChunksDownloading(long i) {
        chunksDownloading += i;
    }

    protected void addException(Exception e) {
        if (exceptions == null) {
            exceptions = new Vector<Exception>();
        }
        exceptions.add(e);
    }

    public synchronized void addToChunksInProgress(long i) {
        chunksInProgress += i;
    }

    protected synchronized void addToTotalLinkBytesLoaded(long block) {
        totaleLinkBytesLoaded += block;
    }

    /**
     * ueber error() kann ein fehler gemeldet werden. DIe Methode entscheided
     * dann ob dieser fehler zu einem Abbruch fuehren muss
     */
    protected synchronized void error(int id, String string) {
        /* if we recieved external stop, then we dont have to handle errors */
        if (externalDownloadStop()) return;

        logger.severe("Error occured (" + id + "): " + LinkStatus.toString(id));

        if (errors.indexOf(id) < 0) errors.add(id);
        if (fatalErrorOccured) return;

        linkStatus.addStatus(id);

        linkStatus.setErrorMessage(string);
        switch (id) {
        case LinkStatus.ERROR_RETRY:
        case LinkStatus.ERROR_FATAL:
        case LinkStatus.ERROR_TIMEOUT_REACHED:
        case LinkStatus.ERROR_FILE_NOT_FOUND:
        case LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE:
        case LinkStatus.ERROR_LOCAL_IO:
        case LinkStatus.ERROR_NO_CONNECTION:
        case LinkStatus.ERROR_ALREADYEXISTS:
        case LinkStatus.ERROR_LINK_IN_PROGRESS:
        case LinkStatus.ERROR_DOWNLOAD_FAILED:
            fatalErrorOccured = true;
            terminate();
        }
    }

    /**
     * Gibt die Anzahl der verwendeten Chunks zurueck
     */
    public int getChunkNum() {
        return chunkNum;
    }

    public Vector<Chunk> getChunks() {
        return chunks;
    }

    /**
     * Gibt zurueck wieviele Chunks tatsaechlich in der Downloadphase sind
     */
    public int getChunksDownloading() {
        return chunksDownloading;
    }

    /**
     * Gibt die aufgetretenen Fehler zurueck
     */
    public Vector<Integer> getErrors() {
        return errors;
    }

    public Vector<Exception> getExceptions() {
        return exceptions;
    }

    public File getFile() {
        return new File(downloadLink.getFileOutput());

    }

    /**
     * Gibt eine bestmoegliche abschaetzung der Dateigroesse zurueck
     */
    protected long getFileSize() {
        if (fileSize > 0) return fileSize;
        if (connection != null && connection.getLongContentLength() > 0) return connection.getLongContentLength();
        if (downloadLink.getDownloadSize() > 0) return downloadLink.getDownloadSize();
        return -1;
    }

    /**
     * Gibt den aktuellen readtimeout zurueck
     */
    public int getReadTimeout() {
        return Math.max(10000, readTimeout);
    }

    /**
     * Gibt den requesttimeout zurueck
     */
    public int getRequestTimeout() {
        return Math.max(10000, requestTimeout);
    }

    /**
     * Gibt zurueck wieviele Chunks gerade am arbeiten sind
     */
    public int getRunningChunks() {
        return chunksInProgress;
    }

    /**
     * Setzt im Downloadlink und PLugin die entsprechende Fehlerids
     */
    public boolean handleErrors() {
        if (externalDownloadStop()) return false;
        if (this.doFileSizeCheck && (totaleLinkBytesLoaded <= 0 || totaleLinkBytesLoaded != fileSize && fileSize > 0)) {
            if (totaleLinkBytesLoaded > fileSize) {
                /*
                 * workaround for old bug deep in this downloadsystem. more data
                 * got loaded (maybe just counting bug) than filesize. but in
                 * most cases the file is okay! WONTFIX because new
                 * downloadsystem is on its way
                 */
                logger.severe("Filesize: " + fileSize + " Loaded: " + totaleLinkBytesLoaded);
                if (!linkStatus.isFailed()) {
                    linkStatus.setStatus(LinkStatus.FINISHED);
                }
                return true;
            }
            logger.severe("Filesize: " + fileSize + " Loaded: " + totaleLinkBytesLoaded);
            logger.severe("DOWNLOAD INCOMPLETE DUE TO FILESIZECHECK");
            error(LinkStatus.ERROR_DOWNLOAD_INCOMPLETE, _JDT._.download_error_message_incomplete());
            return false;
        }

        if (getExceptions() != null && getExceptions().size() > 0) {
            error(LinkStatus.ERROR_RETRY, _JDT._.download_error_message_incomplete());
            return false;
        }
        if (!linkStatus.isFailed()) {
            linkStatus.setStatus(LinkStatus.FINISHED);
        }
        return true;
    }

    /**
     * Ist resume aktiv?
     */
    public boolean isResume() {
        return resume;
    }

    /**
     * Wartet bis alle Chunks fertig sind, aktuelisiert den downloadlink
     * regelmaesig und fordert beim Controller eine aktualisierung des links an
     */
    private void onChunkFinished() {
        synchronized (this) {
            if (waitFlag) {
                waitFlag = false;
                notify();
            }
        }
        GarbageController.requestGC();
    }

    /**
     * Wird aufgerufen sobald alle Chunks fertig geladen sind
     */
    abstract protected void onChunksReady();

    /**
     * Gibt die Anzahl der Chunks an die dieser Download verwenden soll. Chu8nks
     * koennen nur vor dem Downloadstart gesetzt werden!
     */
    public void setChunkNum(int num) {
        if (num <= 0) {
            logger.severe("Chunks value must be >=1");
            return;
        }
        chunkNum = num;
    }

    /**
     * Setzt die filesize.
     */
    public void setFilesize(long length) {
        fileSize = length;
    }

    /**
     * Setzt den aktuellen readtimeout(nur vor dem dl start)
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Setzt vor ! dem download dden requesttimeout. Sollte nicht zu niedrig
     * sein weil sonst das automatische kopieren der Connections fehl schlaegt.,
     */
    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    /**
     * File soll resumed werden
     */
    public void setResume(boolean value) {
        this.resumable = value;
        if (checkResumabled()) {
            resume = value;
        } else {
            logger.warning("Resumepoint not valid");
        }
    }

    public boolean isResumable() {
        return this.resumable;
    }

    /**
     * Wird aufgerufen um die Chunks zu initialisieren
     * 
     * @throws Exception
     */
    abstract protected void setupChunks() throws Exception;

    public static boolean preDownloadCheckFailed(DownloadLink link) {
        if (!link.isAvailabilityStatusChecked()) {
            /*
             * dont proceed if no linkcheck has done yet, maybe we dont know
             * filename yet
             */
            return false;
        }
        DownloadLink downloadLink = link;
        DownloadLink block = DownloadController.getInstance().getFirstLinkThatBlocks(downloadLink);
        LinkStatus linkstatus = link.getLinkStatus();
        if (block != null) {
            linkstatus.addStatus(LinkStatus.ERROR_ALREADYEXISTS);
            if (block.getDefaultPlugin() != null) linkstatus.setStatusText(_JDT._.system_download_errors_linkisBlocked(block.getHost()));
            return true;
        }
        File fileOutput = new File(downloadLink.getFileOutput());
        if (fileOutput.getParentFile() == null) {
            linkstatus.addStatus(LinkStatus.ERROR_FATAL);
            linkstatus.setErrorMessage(_JDT._.system_download_errors_invalidoutputfile());
            return true;
        }
        if (fileOutput.isDirectory()) return false;
        if (!fileOutput.getParentFile().exists()) {
            if (!fileOutput.getParentFile().mkdirs()) {
                linkstatus.addStatus(LinkStatus.ERROR_FATAL);
                linkstatus.setErrorMessage(_JDT._.system_download_errors_invalidoutputfile());
                return true;
            }
        }
        if (fileOutput.exists()) {
            // TODO: handle all options!?
            if (JsonConfig.create(GeneralSettings.class).getIfFileExistsAction() == IfFileExistsAction.OVERWRITE_FILE) {
                if (!new File(downloadLink.getFileOutput()).delete()) {
                    linkstatus.addStatus(LinkStatus.ERROR_FATAL);
                    linkstatus.setErrorMessage(_JDT._.system_download_errors_couldnotoverwrite());
                    return true;
                }
            } else {
                linkstatus.addStatus(LinkStatus.ERROR_ALREADYEXISTS);
                linkstatus.setErrorMessage(_JDT._.downloadlink_status_error_file_exists());
                return true;
            }
        }
        return false;
    }

    /**
     * Startet den Download. Nach dem Aufruf dieser Funktion koennen keine
     * Downlaodparameter mehr gesetzt werden bzw bleiben wirkungslos.
     * 
     * @return
     * @throws Exception
     */
    public boolean startDownload() throws Exception {
        try {
            linkStatus.addStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
            logger.finer("Start Download");
            if (!connected) connect();
            if (connection != null && connection.getHeaderField("Content-Encoding") != null && connection.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                /* GZIP Encoding kann weder chunk noch resume */
                setResume(false);
                setChunkNum(1);
            }
            // Erst hier Dateinamen holen, somit umgeht man das Problem das bei
            // mehrfachAufruf von connect entstehen kann
            if (this.downloadLink.getFinalFileName() == null && ((connection != null && connection.isContentDisposition()) || this.allowFilenameFromURL)) {
                String name = Plugin.getFileNameFromHeader(connection);
                if (this.fixWrongContentDispositionHeader) {
                    this.downloadLink.setFinalFileName(Encoding.htmlDecode(name));
                } else {
                    this.downloadLink.setFinalFileName(name);
                }
            }
            downloadLink.getLinkStatus().setStatusText(null);
            if (connection == null || !connection.isOK()) {
                if (connection != null) logger.finest(connection.toString());
                try {
                    connection.disconnect();
                } catch (final Throwable e) {
                }
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 10 * 60 * 1000l);
            }
            if (connection.getHeaderField("Location") != null) {
                error(LinkStatus.ERROR_PLUGIN_DEFECT, "Sent a redirect to Downloadinterface");
                return false;
            }
            if (preDownloadCheckFailed(downloadLink)) return false;
            try {
                downloadLink.getDownloadLinkController().getConnectionHandler().addConnectionHandler(connectionHandler);
            } catch (final Throwable e) {
            }
            setupChunks();
            waitForChunks();
            onChunksReady();
            return handleErrors();
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                this.error(LinkStatus.ERROR_LOCAL_IO, _JDT._.download_error_message_localio(e.getMessage()));
            } else {
                JDLogger.exception(e);
            }
            handleErrors();
            return false;
        } finally {
            try {
                downloadLink.getDownloadLinkController().getConnectionHandler().removeConnectionHandler(connectionHandler);
            } catch (final Throwable e) {
            }
            linkStatus.removeStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
            try {
                this.connection.disconnect();
            } catch (Throwable e) {
            }
            /* make sure file is really closed */
            cleanupDownladInterface();
        }
    }

    public abstract void cleanupDownladInterface();

    /**
     * Bricht den Download komplett ab.
     */
    private void terminate() {
        if (!externalDownloadStop()) logger.severe("A critical Downloaderror occured. Terminate...");
        int oldSize = -1;
        ArrayList<Chunk> stopChunks = new ArrayList<Chunk>();
        while (true) {
            oldSize = stopChunks.size();
            synchronized (chunks) {
                stopChunks = new ArrayList<Chunk>(chunks);
            }
            boolean allClosed = true;
            if (stopChunks.size() != oldSize) {
                /* new Chunks found in this loop */
                allClosed = false;
            }
            for (Chunk ch : stopChunks) {
                try {
                    if (ch.getInputStream() != null) allClosed = false;
                    ch.closeConnections();
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
                ch.interrupt();
            }
            if (allClosed) break;
            try {
                Thread.sleep(200);
            } catch (final InterruptedException e) {
                break;
            }
        }
    }

    private void waitForChunks() {
        try {
            logger.finer("Wait for chunks");
            while (chunksInProgress > 0) {
                synchronized (this) {
                    if (waitFlag) {
                        try {
                            this.wait();
                        } catch (final InterruptedException e) {
                        } catch (Exception e) {
                            // logger.log(Level.SEVERE,"Exception occurred",e);
                            terminate();
                            return;
                        }
                    }
                }
                waitFlag = true;
            }
            downloadLink.setDownloadCurrent(totaleLinkBytesLoaded);
        } catch (Throwable e) {
            JDLogger.exception(e);
        }
    }

    protected synchronized boolean writeBytes(Chunk chunk) {
        return writeChunkBytes(chunk);
    }

    /**
     * Schreibt den puffer eines chunks in die zugehoerige Datei
     * 
     * @param buffer
     * @param currentBytePosition
     */
    abstract protected boolean writeChunkBytes(Chunk chunk);

    public void setFilesizeCheck(boolean b) {
        this.doFileSizeCheck = b;
    }

    public URLConnectionAdapter getConnection() {
        return this.connection;
    }

    public Request getRequest() {
        return this.request;
    }

    /**
     * Setzt man diesen Wert auf true, so wird der erste Chunk nicht per ranges
     * geladen. d.h. es gibt keinen 0-...range
     * 
     * @param b
     */
    public void setFirstChunkRangeless(boolean b) {
        firstChunkRangeless = b;

    }

    public boolean isFirstChunkRangeless() {
        return firstChunkRangeless;
    }

    /** signal that we stopped download external */
    public synchronized void stopDownload() {
        if (externalStop) return;
        logger.severe("externalStop recieved");
        externalStop = true;
        terminate();
    }

    public synchronized boolean externalDownloadStop() {
        return externalStop;
    }

}