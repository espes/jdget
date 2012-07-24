package org.jdownloader.extensions.vlcstreaming;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import jd.controlling.AccountController;
import jd.http.Browser;
import jd.http.Request;
import jd.http.URLConnectionAdapter;
import jd.plugins.Account;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForHost;
import jd.plugins.download.DownloadInterface;
import jd.plugins.download.DownloadInterfaceFactory;

import org.appwork.utils.Application;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;
import org.appwork.utils.io.streamingio.Streaming;
import org.appwork.utils.io.streamingio.StreamingInputStream;
import org.appwork.utils.io.streamingio.StreamingOutputStream;
import org.appwork.utils.io.streamingio.StreamingOverlapWrite;

public class VLCStreamingProvider {

    private VLCStreamingExtension extension;

    public VLCStreamingProvider(VLCStreamingExtension extension) {
        this.extension = extension;
    }

    protected static HashMap<DownloadLink, Streaming> streaming = new HashMap<DownloadLink, Streaming>();

    public Streaming getStreamingProvider(final DownloadLink remoteLink) throws IOException {
        synchronized (streaming) {
            Streaming stream = streaming.get(remoteLink);
            if (stream == null) {
                File tmp = Application.getResource("/tmp/streaming/" + remoteLink.getUniqueID());
                tmp.getParentFile().mkdirs();
                stream = new Streaming(tmp.getAbsolutePath()) {
                    private long fileSize = -1;

                    @Override
                    public boolean connectStreamingOutputStream(final StreamingOutputStream streamingOutputStream, long startPosition, long endPosition) throws IOException {
                        /* this method should be called within our VLCStreamingThread */
                        ClassLoader oldClassLoader = null;
                        try {
                            LinkedList<Account> accounts = AccountController.getInstance().getValidAccounts(remoteLink.getHost());
                            oldClassLoader = Thread.currentThread().getContextClassLoader();
                            final DownloadLink mirror = new DownloadLink(remoteLink.getDefaultPlugin(), remoteLink.getName(), remoteLink.getHost(), remoteLink.getDownloadURL(), true);
                            remoteLink.copyTo(mirror);
                            final PluginForHost plugin = remoteLink.getDefaultPlugin().getLazyP().newInstance();
                            plugin.setBrowser(new Browser());
                            plugin.setCustomizedDownloadFactory(new DownloadInterfaceFactory() {

                                @Override
                                public DownloadInterface getDownloadInterface(DownloadLink downloadLink, Request request) throws Exception {
                                    return new VLCStreamingDownloadInterface(downloadLink.getLivePlugin(), downloadLink, request);
                                }

                                @Override
                                public DownloadInterface getDownloadInterface(DownloadLink downloadLink, Request request, boolean resume, int chunks) throws Exception {
                                    return new VLCStreamingDownloadInterface(downloadLink.getLivePlugin(), downloadLink, request);
                                }
                            });
                            mirror.setLivePlugin(plugin);
                            Thread.currentThread().setContextClassLoader(plugin.getLazyP().getClassLoader());
                            plugin.init();
                            /* forward requested range for DownloadInterface */
                            if (endPosition > 0) {
                                mirror.setProperty("streamingRange", "bytes=" + startPosition + "-" + endPosition);
                            } else {
                                mirror.setProperty("streamingRange", "bytes=" + startPosition + "-");
                            }
                            if (accounts != null && accounts.size() > 0) {
                                plugin.handlePremium(mirror, accounts.get(0));
                            } else {
                                plugin.handleFree(mirror);
                            }
                            final URLConnectionAdapter con = mirror.getDownloadInstance().getConnection();
                            if (con.getResponseCode() == 200 || con.getResponseCode() == 206) {
                                if (fileSize == -1) fileSize = con.getCompleteContentLength();
                                new Thread() {
                                    public void run() {
                                        ReusableByteArrayOutputStream buffer = null;
                                        try {
                                            buffer = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(10240, false);
                                            InputStream is = con.getInputStream();
                                            while (true) {
                                                int ret = is.read(buffer.getInternalBuffer());
                                                if (ret == -1) break;
                                                if (ret == 0) continue;
                                                streamingOutputStream.write(buffer.getInternalBuffer(), 0, ret);
                                            }
                                        } catch (StreamingOverlapWrite e) {
                                            System.out.println("Chunk overlapping");
                                        } catch (final Throwable e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                con.disconnect();
                                            } catch (final Throwable e) {
                                            }
                                            try {
                                                plugin.clean();
                                            } catch (final Throwable e) {
                                                e.printStackTrace();
                                            }
                                            ReusableByteArrayOutputStreamPool.reuseReusableByteArrayOutputStream(buffer);
                                            streamingOutputStream.close();
                                        }
                                    }
                                }.start();
                                return true;
                            } else {
                                plugin.clean();
                                return false;
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                            throw new IOException(e);
                        } finally {
                            Thread.currentThread().setContextClassLoader(oldClassLoader);
                        }
                    }

                    @Override
                    protected synchronized void closeInputStream(StreamingInputStream streamingInputStream) {
                        StreamingOutputStream outputStream = findLastStreamingOutputStreamFor(streamingInputStream);
                        super.closeInputStream(streamingInputStream);
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }

                    @Override
                    protected synchronized void closeOutputStream(StreamingOutputStream streamingOutputStream) {
                        System.out.println("outputStream closed");
                        super.closeOutputStream(streamingOutputStream);
                    }

                    @Override
                    public long getFinalFileSize() {
                        long ret = remoteLink.getVerifiedFileSize();
                        if (ret != -1) return ret;
                        return fileSize;
                    }

                };
                streaming.put(remoteLink, stream);
            }
            return stream;
        }
    }
}
