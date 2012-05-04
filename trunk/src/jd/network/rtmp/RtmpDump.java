package jd.network.rtmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.network.rtmp.url.RtmpUrlConnection;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.hoster.RTMPDownload;

import org.appwork.utils.Application;
import org.appwork.utils.Regex;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.net.throttledconnection.ThrottledConnection;
import org.appwork.utils.net.throttledconnection.ThrottledConnectionHandler;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.speedmeter.SpeedMeterInterface;
import org.jdownloader.nativ.NativeProcess;
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
    private volatile long     BYTESLOADED = 0l;
    private long              SPEED       = 0l;
    private int               PID         = -1;
    private static String     RTMPDUMP    = null;
    private static String     RTMPVERSION = null;
    private NativeProcess     NP;
    private Process           P;

    private InputStreamReader R;

    public RtmpDump(final PluginForHost plugin, final DownloadLink downloadLink, final String rtmpURL) throws IOException, PluginException {
        super(plugin, downloadLink, rtmpURL);
    }

    /**
     * Attempt to locate a rtmpdump executable. The local tools folder is searched first, then *nix /usr bin folders. If found, the path will is saved to the
     * variable RTMPDUMP.
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
        if (RTMPDUMP != null && !new File(RTMPDUMP).exists()) RTMPDUMP = null;
        if (RTMPDUMP == null && (CrossSystem.isLinux() || CrossSystem.isMac())) {
            if (RTMPDUMP == null && (RTMPDUMP = "/usr/bin/rtmpdump") != null && !new File(RTMPDUMP).exists()) RTMPDUMP = null;
            if (RTMPDUMP == null && (RTMPDUMP = "/usr/local/bin/rtmpdump") != null && !new File(RTMPDUMP).exists()) RTMPDUMP = null;
        }
        if (RTMPDUMP == null) RTMPDUMP = "";
        return RTMPDUMP.length() > 0;
    }

    /**
     * Attempt to locate a rtmpdump executable and parse the version number from the 'rtmpdump -h' output.
     * 
     * @return The version number of the RTMPDump executable
     */
    public synchronized String getRtmpDumpVersion() throws Exception {
        if (RTMPVERSION != null) return RTMPVERSION;
        if (!findRtmpDump()) { throw new Exception("Error " + RTMPDUMP + " not found!"); }
        String arg = " -h";
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
            Pattern reg = Pattern.compile("RTMPDump v([0-9.]+)");
            String line = null;
            while ((line = br.readLine()) != null) {
                Matcher match = reg.matcher(line);
                if (match.find()) {
                    RTMPVERSION = match.group(1);
                    return RTMPVERSION;
                }
            }
            throw new Exception("Error " + RTMPDUMP + " version not found!");
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

    private void getProcessId() {
        try {
            final Field pidField = P.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            PID = pidField.getInt(P);
        } catch (final Exception e) {
            PID = -1;
        }
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

    public boolean start(final RtmpUrlConnection rtmpConnection) throws Exception {
        if (!findRtmpDump()) { throw new PluginException(LinkStatus.ERROR_FATAL, "Error " + RTMPDUMP + " not found!"); }
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
            CHUNK = new Chunk(0, 0, null, downloadLink.getDownloadInstance()) {

            };
            CHUNK.setInProgress(true);
            getChunks().add(CHUNK);
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
            String line = "", error = "";
            long iSize = 0;
            long before = 0;
            long lastTime = System.currentTimeMillis();

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
                if (CrossSystem.isWindows()) {
                    NP = new NativeProcess(RTMPDUMP, cmd);
                    R = new InputStreamReader(NP.getErrorStream());
                } else {
                    P = Runtime.getRuntime().exec(RTMPDUMP + cmd);
                    R = new InputStreamReader(P.getErrorStream());
                }
                final BufferedReader br = new BufferedReader(R);
                int sizeCalulateBuffer = 0;
                float progressFloat = 0;
                while ((line = br.readLine()) != null) {
                    if (!line.equals("")) {
                        error = line;
                    }
                    if (!new Regex(line, "^[0-9]").matches()) {
                        if (line.contains("length")) {
                            final String size = new Regex(line, ".*?(\\d.+)").getMatch(0);
                            iSize += SizeFormatter.getSize(size);
                        }
                    } else {
                        if (downloadLink.getDownloadSize() == 0) {
                            downloadLink.setDownloadSize(iSize);
                        }
                        final int pos1 = line.indexOf("(");
                        final int pos2 = line.indexOf(")");
                        if (pos1 != -1 && pos2 != -1 && line.toUpperCase().contains("KB")) {
                            progressFloat = Float.parseFloat(line.substring(pos1 + 1, pos2 - 1));
                            BYTESLOADED = SizeFormatter.getSize(line.substring(0, line.toLowerCase().indexOf("kb") + 2));
                            if (Thread.currentThread().isInterrupted()) {
                                if (CrossSystem.isWindows()) {
                                    NP.sendCtrlCSignal();
                                } else {
                                    sendSIGINT();
                                }
                                throw new InterruptedIOException();
                            }
                            if (sizeCalulateBuffer > 6) {
                                downloadLink.setDownloadSize((long) (BYTESLOADED * 100.0F / progressFloat));
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
                    if (progressFloat < 99.8) {
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
            if (downloadLink.getLinkStatus().getStatus() == LinkStatus.ERROR_DOWNLOAD_INCOMPLETE) {
                return false;
            } else if (line != null && line.toLowerCase().contains("download complete")) {
                downloadLink.setDownloadSize(BYTESLOADED);
                logger.finest("no errors : rename");
                if (!tmpFile.renameTo(new File(downloadLink.getFileOutput()))) {
                    logger.severe("Could not rename file " + tmpFile + " to " + downloadLink.getFileOutput());
                    error(LinkStatus.ERROR_LOCAL_IO, _JDT._.system_download_errors_couldnotrename());
                }
                downloadLink.getLinkStatus().addStatus(LinkStatus.FINISHED);
            } else {
                logger.severe("cmd: " + cmd);
                throw new PluginException(LinkStatus.ERROR_FATAL, error);
            }
            return true;
        } finally {
            downloadLink.setDownloadCurrent(BYTESLOADED);
            downloadLink.getLinkStatus().removeStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
            downloadLink.setDownloadInstance(null);
            downloadLink.getLinkStatus().setStatusText(null);
            CHUNK.setInProgress(false);
            getManagedConnetionHandler().removeThrottledConnection(tcon);
            try {
                downloadLink.getDownloadLinkController().getConnectionHandler().removeConnectionHandler(getManagedConnetionHandler());
            } catch (final Throwable e) {
            }
        }
    }

    @Override
    public long getTotalLinkBytesLoadedLive() {
        return BYTESLOADED;
    }

}
