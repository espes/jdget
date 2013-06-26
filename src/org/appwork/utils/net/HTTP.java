/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.appwork.resources.AWUTheme;
import org.appwork.uio.UIOManager;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.ProgressDialog;
import org.appwork.utils.swing.dialog.ProgressDialog.ProgressGetter;

/**
 * @author thomas
 * 
 */
public class HTTP {

    public static void download(final URL url, final File file, final DownloadProgress progress) throws IOException {
        HTTP.download(url, file, progress, false);
    }

    /**
     * Downloads a file and stores data to a file
     * 
     * @param url
     * @param cache
     * @throws IOException
     */
    public static void download(final URL url, final File file, final DownloadProgress progress, final boolean keepAlive) throws IOException {
        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        FileOutputStream fos = null;
        BufferedOutputStream output = null;
        BufferedInputStream input = null;
        GZIPInputStream gzi = null;
        boolean deleteInterrupted = false;
        HttpURLConnection con = null;
        try {
            output = new BufferedOutputStream(fos = new FileOutputStream(file, false));
            con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setConnectTimeout(15000);
            con.setReadTimeout(30000);
            con.setUseCaches(false);
            if (keepAlive) {
                con.setRequestProperty("Connection", "keep-alive");
            } else {
                con.setRequestProperty("Connection", "close");
            }
            if (url.openConnection().getHeaderField("Content-Encoding") != null && con.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                input = new BufferedInputStream(gzi = new GZIPInputStream(con.getInputStream()));
            } else {
                input = new BufferedInputStream(con.getInputStream());
            }
            if (progress != null) {
                progress.setTotal(con.getContentLength());
            }
            final byte[] b = new byte[32767];
            int len;
            while ((len = input.read(b)) != -1) {
                if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
                output.write(b, 0, len);
                if (progress != null) {
                    progress.increaseLoaded(len);
                }
            }
        } catch (final InterruptedException e) {
            deleteInterrupted = true;
        } finally {
            try {
                input.close();
            } catch (final Exception e) {
            }
            try {
                gzi.close();
            } catch (final Exception e) {
            }
            try {
                output.flush();
            } catch (final Exception e) {
            }
            try {
                output.close();
            } catch (final Exception e) {
            }
            try {
                fos.flush();
            } catch (final Exception e) {
            }
            try {
                fos.close();
            } catch (final Exception e) {
            }
            try {
                if (!keepAlive) {
                    con.disconnect();
                }
            } catch (final Throwable e) {
            }
            if (deleteInterrupted) {
                file.delete();
            }
        }
    }

    /**
     * @param file
     * @param url
     * @param hash
     * @throws Exception
     */
    public static void downloadInDialog(final File file, final String url, final String hash) throws Exception {
        final Exception ret = new EDTHelper<Exception>() {

            @Override
            public Exception edtRun() {
                try {

                    final DownloadProgress progress = new DownloadProgress();
                    final ProgressGetter pg = new ProgressGetter() {

                        private long loaded = 0;
                        private long total  = 0;

                        @Override
                        public int getProgress() {
                            this.total = progress.getTotal();
                            this.loaded = progress.getLoaded();
                            if (this.total == 0) { return 0; }
                            return (int) (this.loaded * 100 / this.total);
                        }

                        @Override
                        public String getString() {
                            this.total = progress.getTotal();
                            this.loaded = progress.getLoaded();
                            if (this.total <= 0) { return _AWU.T.connecting(); }
                            return _AWU.T.progress(SizeFormatter.formatBytes(this.loaded), SizeFormatter.formatBytes(this.total), this.loaded * 10000f / this.total / 100.0);
                        }

                        @Override
                        public void run() throws Exception {
                            HTTP.download(new URL(url), file, progress);
                        }

                        @Override
                        public String getLabelString() {
                            // TODO Auto-generated method stub
                            return null;
                        }

                    };
                    final ProgressDialog dialog = new ProgressDialog(pg, UIOManager.BUTTONS_HIDE_CANCEL | UIOManager.BUTTONS_HIDE_OK, _AWU.T.download_title(), _AWU.T.download_msg(), AWUTheme.I().getIcon("download", 32)) {
                        /**
                         * 
                         */
                        private static final long serialVersionUID = 5303387916537596967L;

                        @Override
                        public boolean closeAllowed() {

                            Dialog.getInstance().showMessageDialog(_AWU.T.please_wait());

                            return false;
                        }
                    };
                    Dialog.getInstance().showDialog(dialog);
                } catch (final Exception e) {
                    return e;
                }
                return null;
            }

        }.getReturnValue();
        if (ret != null) { throw ret; }
    }
}
