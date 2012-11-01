package jd.network.rtmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.network.rtmp.url.RtmpUrlConnection;
import jd.nutils.JDHash;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.hoster.RTMPDownload;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.Regex;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.net.throttledconnection.ThrottledConnection;
import org.appwork.utils.net.throttledconnection.ThrottledConnectionHandler;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.speedmeter.SpeedMeterInterface;
import org.jdownloader.nativ.NativeProcess;
import org.jdownloader.settings.RtmpdumpSettings;
import org.jdownloader.translate._JDT;

public class RtmpDump extends RTMPDownload {

    private static class RTMPCon implements SpeedMeterInterface, ThrottledConnection {

        private ThrottledConnectionHandler handler;

        public ThrottledConnectionHandler getHandler() {
            return handler;
        }

        public int getLimit() {
            return 0;
        }

        public long getSpeedMeter() {
            return 0;
        }

        public void putSpeedMeter(final long bytes, final long time) {
        }

        public void resetSpeedMeter() {
        }

        public void setHandler(final ThrottledConnectionHandler manager) {
            handler = manager;
        }

        public void setLimit(final int kpsLimit) {
        }

        public long transfered() {
            return 0;
        }

    }

    private Chunk             CHUNK;

    private volatile long     BYTESLOADED  = 0l;

    private long              SPEED        = 0l;

    private int               PID          = -1;

    private static String     RTMPDUMP     = null;

    private static String     RTMPVERSION  = null;

    private NativeProcess     NP;

    private Process           P;

    private InputStreamReader R;

    private RtmpdumpSettings  config;

    private final Object      watchDogLOCK = new Object();

    private Thread            watchDog     = null;

    public RtmpDump(final PluginForHost plugin, final DownloadLink downloadLink, final String rtmpURL) throws IOException, PluginException {
        super(plugin, downloadLink, rtmpURL);
        config = JsonConfig.create(RtmpdumpSettings.class);
    }

    /**
     * Attempt to locate a rtmpdump executable. The local tools folder is searched first, then *nix /usr bin folders. If found, the path
     * will is saved to the variable RTMPDUMP.
     * 
     * @return Whether or not rtmpdump executable was found
     */
    private synchronized boolean findRtmpDump() {
        if (RTMPDUMP != null) { return RTMPDUMP.length() > 0; }
        if (CrossSystem.isWindows()) {
            RTMPDUMP = Application.getResource("tools/Windows/rtmpdump/rtmpdump.exe").getAbsolutePath();
        } else if (CrossSystem.isLinux()) {
            RTMPDUMP = Application.getResource("tools/linux/rtmpdump/rtmpdump").getAbsolutePath();
        } else if (CrossSystem.isMac()) {
            RTMPDUMP = Application.getResource("tools/mac/rtmpdump/rtmpdump").getAbsolutePath();
        }
        if (RTMPDUMP != null && !new File(RTMPDUMP).exists()) {
            RTMPDUMP = null;
        }
        if (RTMPDUMP == null && (CrossSystem.isLinux() || CrossSystem.isMac())) {
            if (RTMPDUMP == null && (RTMPDUMP = "/usr/bin/rtmpdump") != null && !new File(RTMPDUMP).exists()) {
                RTMPDUMP = null;
            }
            if (RTMPDUMP == null && (RTMPDUMP = "/usr/local/bin/rtmpdump") != null && !new File(RTMPDUMP).exists()) {
                RTMPDUMP = null;
            }
        }
        if (RTMPDUMP == null) {
            RTMPDUMP = "";
        }
        return RTMPDUMP.length() > 0;
    }

    private void getProcessId() {
        try {
            final Field pidField = P.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            PID = pidField.getInt(P);
        } catch (final Exception e) {
            PID = -1;
        }
    }

    public String getRtmpDumpChecksum() throws Exception {
        if (!findRtmpDump()) return null;
        return JDHash.getMD5(new File(RTMPDUMP));
    }

    /**
     * Attempt to locate a rtmpdump executable and parse the version number from the 'rtmpdump -h' output.
     * 
     * @return The version number of the RTMPDump executable
     */
    public synchronized String getRtmpDumpVersion() throws Exception {
        if (RTMPVERSION != null) { return RTMPVERSION; }
        if (!findRtmpDump()) throw new PluginException(LinkStatus.ERROR_FATAL, "RTMPDump not found!");
        final String arg = " -h";
        NativeProcess verNP = null;
        Process verP = null;
        InputStreamReader verR = null;
        try {
            if (CrossSystem.isWindows()) {
                verNP = new NativeProcess(RTMPDUMP, arg);
                verR = new InputStreamReader(verNP.getErrorStream());
            } else {
                verP = Runtime.getRuntime().exec(RTMPDUMP + arg);
                verR = new InputStreamReader(verP.getErrorStream());
            }
            final BufferedReader br = new BufferedReader(verR);
            final Pattern reg = Pattern.compile("RTMPDump v([0-9.]+)");
            String line = null;
            while ((line = br.readLine()) != null) {
                final Matcher match = reg.matcher(line);
                if (match.find()) {
                    RTMPVERSION = match.group(1);
                    return RTMPVERSION;
                }
            }
            throw new PluginException(LinkStatus.ERROR_FATAL, "Error " + RTMPDUMP + " version not found!");
        } finally {
            try {
                /* make sure we destroyed the process */
                verP.destroy();
            } catch (final Throwable e) {
            }
            try {
                /* close InputStreamReader */
                verR.close();
            } catch (final Throwable e) {
            }
            if (verNP != null) {
                /* close Streams from native */
                verNP.closeStreams();
            }
        }
    }

    @Override
    public long getTotalLinkBytesLoadedLive() {
        return BYTESLOADED;
    }

    private void sendSIGINT() {
        getProcessId();
        if (PID >= 0) {
            try {
                Runtime.getRuntime().exec("kill -SIGINT " + String.valueOf(PID));
            } catch (final Throwable e1) {
            }
        }
    }

    // private void startRtmpDumpWatchDog() {
    // synchronized (this.watchDogLOCK) {
    // if (this.watchDog != null && this.watchDog.isAlive()) { return; }
    // /* we have to start a new watchDog */
    // this.watchDog = new Thread() {
    // @Override
    // public void run() {
    // while (true) {
    // final long sleepTime = Math.max(1000, 2000);
    // try {
    // Thread.sleep(sleepTime);
    // } catch (final InterruptedException e) {
    // Log.exception(e);
    // } catch (Exception e1) {
    // e1.printStackTrace();
    // }
    // /* this watchDog is finished, do some cleanup stuff */
    // synchronized (watchDogLOCK) {
    // watchDog = null;
    // }
    // }
    // }
    // };
    // this.watchDog.setDaemon(true);
    // this.watchDog.start();
    // }
    // }

    public boolean start(final RtmpUrlConnection rtmpConnection) throws Exception {
        if (!findRtmpDump()) { throw new PluginException(LinkStatus.ERROR_FATAL, "Error " + RTMPDUMP + " not found!"); }

        boolean debug = config.isDebugModeEnabled();

        final ThrottledConnection tcon = new RTMPCon() {
            @Override
            public long getSpeedMeter() {
                return SPEED;
            }

            @Override
            public long transfered() {
                return BYTESLOADED;
            }
        };
        try {
            getManagedConnetionHandler().addThrottledConnection(tcon);
            addChunksDownloading(1);
            downloadLink.getLinkStatus().addStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
            try {
                downloadLink.getDownloadLinkController().getConnectionHandler().addConnectionHandler(getManagedConnetionHandler());
            } catch (final Throwable e) {
            }
            rtmpConnection.connect();

            File tmpFile = new File(downloadLink.getFileOutput() + ".part");
            if (!CrossSystem.isWindows()) {
                tmpFile = new File(downloadLink.getFileOutput().replaceAll("\\s", "\\\\s") + ".part");
            }
            String line = "";
            String error = "";
            long iSize = 0;
            long before = 0;
            long lastTime = System.currentTimeMillis();

            /* hanging threads handling */
            Future<String> future;
            String crashMessage = "rtmpdump timed out while waiting for a reply after";
            long readerTimeOut = 5 * 1000l; // wait readerTimeOut then kill process

            String cmd = rtmpConnection.getCommandLineParameter();
            if (CrossSystem.isWindows()) {
                // MAX_PATH Fix --> \\?\ + Path
                if (String.valueOf(tmpFile).length() >= 260) {
                    cmd += " -o \"\\\\?\\" + String.valueOf(tmpFile) + "\"";
                } else {
                    cmd += " -o \"" + String.valueOf(tmpFile) + "\"";
                }
            } else {
                cmd = cmd.replaceAll("\"", "") + " -o " + String.valueOf(tmpFile);
            }

            if (cmd.contains(" -e ")) {
                setResume(true);
            }

            try {
                ExecutorService executor = Executors.newFixedThreadPool(2);
                if (CrossSystem.isWindows()) {
                    NP = new NativeProcess(RTMPDUMP, cmd);
                    R = new InputStreamReader(NP.getErrorStream());
                } else {
                    P = Runtime.getRuntime().exec(RTMPDUMP + cmd);
                    R = new InputStreamReader(P.getErrorStream());
                }
                final BufferedReader br = new BufferedReader(R);

                /* hanging threads handling */
                Callable<String> readTask = new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return br.readLine();
                    }
                };

                int sizeCalulateBuffer = 0;
                float progressFloat = 0;
                boolean runTimeSize = false;
                long tmplen = 0, fixedlen = 0, calc = 0;
                while ((line = br.readLine()) != null) {

                    /* hanging threads handling */
                    future = executor.submit(readTask);
                    try {
                        error = line;
                        line = future.get(readerTimeOut, TimeUnit.MILLISECONDS);
                        if (line == null) line = error;
                    } catch (Throwable e) {
                        if (CrossSystem.isWindows()) {
                            NP.sendCtrlCSignal();
                        } else {
                            sendSIGINT();
                        }
                        error = crashMessage + " " + (readerTimeOut / 1000) + " sec";
                        break;
                    }

                    if (debug) logger.finest(line);
                    if (!line.isEmpty() && line.startsWith("ERROR:")) {
                        error = line;
                    }
                    if (!new Regex(line, "^[0-9]").matches()) {
                        if (line.contains("length") || line.contains("lastkeyframelocation")) {
                            if (line.contains("length")) runTimeSize = true;
                            String size = new Regex(line, ".*?(\\d.+)").getMatch(0);
                            iSize = SizeFormatter.getSize(size);
                        }
                    } else {
                        if (downloadLink.getDownloadSize() == 0) {
                            downloadLink.setDownloadSize(iSize);
                        }
                        // is resumed
                        if (iSize == 0) iSize = downloadLink.getDownloadSize();

                        int pos1 = line.indexOf("(");
                        int pos2 = line.indexOf(")");
                        if (line.toUpperCase().matches("\\d+\\.\\d+\\sKB\\s/\\s\\d+\\.\\d+\\sSEC(\\s\\(\\d+\\.\\d%\\))?")) {
                            progressFloat = (float) (Math.round(BYTESLOADED * 100.0F / (float) iSize * 10) / 10.0F);
                            if (runTimeSize && pos1 != -1 && pos2 != -1) {
                                progressFloat = Float.parseFloat(line.substring(pos1 + 1, pos2 - 1));
                            }
                            BYTESLOADED = SizeFormatter.getSize(line.substring(0, line.toUpperCase().indexOf("KB") + 2));

                            if (Thread.currentThread().isInterrupted()) {
                                if (CrossSystem.isWindows()) {
                                    NP.sendCtrlCSignal();
                                } else {
                                    sendSIGINT();
                                }
                                return true;
                            }

                            if (sizeCalulateBuffer > 6 && runTimeSize) {
                                tmplen = (long) (BYTESLOADED * 100.0F / progressFloat);
                                fixedlen = downloadLink.getDownloadSize();
                                calc = Math.abs(((fixedlen / 1024) - (tmplen / 1024)) % 1024);
                                if (calc > 768 && calc < 960) {
                                    downloadLink.setDownloadSize(tmplen);
                                }
                            } else {
                                sizeCalulateBuffer++;
                            }

                            if (System.currentTimeMillis() - lastTime > 1000) {
                                SPEED = (BYTESLOADED - before) / (System.currentTimeMillis() - lastTime) * 1000l;
                                lastTime = System.currentTimeMillis();
                                before = BYTESLOADED;
                                // downloadLink.requestGuiUpdate();
                                downloadLink.setChunksProgress(new long[] { BYTESLOADED });
                            }
                        }
                    }
                    if (!line.toLowerCase().contains("download complete")) {
                        continue;
                    }
                    // autoresuming when FMS sends NetStatus.Play.Stop and
                    // progress less than 100%
                    if (progressFloat < 99.8 && !line.toLowerCase().contains("download complete")) {
                        System.out.println("Versuch Nr.: " + downloadLink.getLinkStatus().getRetryCount() + " ::: " + plugin.getMaxRetries(downloadLink, null));
                        if (downloadLink.getLinkStatus().getRetryCount() >= plugin.getMaxRetries(downloadLink, null)) {
                            downloadLink.getLinkStatus().setRetryCount(0);
                        }
                        downloadLink.getLinkStatus().setStatus(LinkStatus.ERROR_DOWNLOAD_INCOMPLETE);
                    }
                    Thread.sleep(500);
                    break;
                }
            } finally {
                rtmpConnection.disconnect();
                try {
                    /* make sure we destroyed the process */
                    P.destroy();
                } catch (final Throwable e) {
                }
                try {
                    /* close InputStreamReader */
                    R.close();
                } catch (final Throwable e) {
                }
                if (NP != null) {
                    /* close Streams from native */
                    NP.closeStreams();
                }
            }

            if (downloadLink.getLinkStatus().getStatus() == LinkStatus.ERROR_DOWNLOAD_INCOMPLETE) return false;
            if (line != null) {
                if (line.toLowerCase().contains("download complete")) {
                    downloadLink.setDownloadSize(BYTESLOADED);
                    logger.finest("no errors : rename");
                    if (!tmpFile.renameTo(new File(downloadLink.getFileOutput()))) {
                        logger.severe("Could not rename file " + tmpFile + " to " + downloadLink.getFileOutput());
                        error(LinkStatus.ERROR_LOCAL_IO, _JDT._.system_download_errors_couldnotrename());
                    }
                    downloadLink.getLinkStatus().addStatus(LinkStatus.FINISHED);
                    /* Stream is succesfully downloaded */
                    return true;
                }
            }
            if (error != null) {
                if (error.toLowerCase().contains("last tag size must be greater/equal zero")) {
                    FlvFixer flvfix = new FlvFixer();
                    flvfix.setInputFile(tmpFile);
                    if (debug) flvfix.setDebug(true);

                    LinkStatus lnkStatus = downloadLink.getLinkStatus();
                    linkStatus.reset();
                    if (!flvfix.scan(downloadLink)) return false;
                    linkStatus.setStatus(lnkStatus.getStatus());

                    File fixedFile = flvfix.getoutputFile();
                    if (!fixedFile.exists()) {
                        logger.severe("File " + fixedFile.getAbsolutePath() + " not found!");
                        error(LinkStatus.ERROR_LOCAL_IO, _JDT._.downloadlink_status_error_file_not_found());
                    }
                    if (!tmpFile.delete()) {
                        logger.severe("Could not delete part file " + tmpFile);
                        error(LinkStatus.ERROR_LOCAL_IO, _JDT._.system_download_errors_couldnotdelete());
                    }
                    if (!fixedFile.renameTo(tmpFile)) {
                        logger.severe("Could not rename file " + tmpFile + " to " + downloadLink.getFileOutput());
                        error(LinkStatus.ERROR_LOCAL_IO, _JDT._.system_download_errors_couldnotrename());
                    }
                    downloadLink.getLinkStatus().addStatus(LinkStatus.ERROR_DOWNLOAD_INCOMPLETE);
                } else if (error.toLowerCase().contains("rtmp_readpacket, failed to read rtmp packet header")) {
                    downloadLink.getLinkStatus().addStatus(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE);
                } else if (error.toLowerCase().contains("netstream.play.streamnotfound")) {
                    downloadLink.deleteFile(true, false);
                    downloadLink.getLinkStatus().addStatus(LinkStatus.ERROR_FILE_NOT_FOUND);
                } else if (error.startsWith(crashMessage)) {
                    downloadLink.getLinkStatus().addStatus(LinkStatus.ERROR_RETRY);
                } else {
                    logger.severe("cmd: " + cmd);
                    logger.severe(error);
                    throw new PluginException(LinkStatus.ERROR_FATAL, error);
                }
            }
            if (error.isEmpty() && line == null) {
                logger.severe("RtmpDump: An unknown error has occured!");
                downloadLink.getLinkStatus().addStatus(LinkStatus.ERROR_RETRY);
                downloadLink.reset();
            }
            return false;
        } finally {
            if (BYTESLOADED > 0) {
                downloadLink.setDownloadCurrent(BYTESLOADED);
                downloadLink.getLinkStatus().setStatusText(null);
            }
            downloadLink.getLinkStatus().removeStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
            downloadLink.setDownloadInstance(null);
            getManagedConnetionHandler().removeThrottledConnection(tcon);
            try {
                downloadLink.getDownloadLinkController().getConnectionHandler().removeConnectionHandler(getManagedConnetionHandler());
            } catch (final Throwable e) {
            }
        }
    }

}
