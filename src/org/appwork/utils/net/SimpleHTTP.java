package org.appwork.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.locale.Loc;
import org.appwork.utils.logging.Log;

public class SimpleHTTP {

    private static final Object           CALL_LOCK       = new Object();

    private final HashMap<String, String> requestHeader;
    private HttpURLConnection             connection;

    private int                           connectTimeout  = 15000;

    private int                           readTimeout     = 30000;

    private boolean                       followRedirects = true;
    private boolean                       headerDebug     = false;

    public SimpleHTTP() {
        this.requestHeader = new HashMap<String, String>();

    }

    public void clearRequestHeader() {
        this.requestHeader.clear();
    }

    /**
     * @param url
     * @param progress
     * @param file
     * @throws InterruptedException
     * @throws IOException
     */
    public void download(final URL url, final DownloadProgress progress, final File file) throws IOException, InterruptedException {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            try {
                this.download(url, progress, 0, out);
            } catch (final IOException e) {
                try {
                    out.close();
                } catch (final Throwable t) {
                }

                if (file.length() > 0) {
                    final IOException ex = new HTTPException(this.connection, IO.readFileToString(file), e);
                    file.delete();
                    throw ex;
                }
            }
        } finally {
            try {
                out.close();
            } catch (final Throwable t) {
            }
        }
    }

    public byte[] download(final URL url, final DownloadProgress progress, final long maxSize) throws IOException, InterruptedException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            this.download(url, progress, maxSize, baos);
        } catch (final IOException e) {
            if (baos.size() > 0) { throw new HTTPException(this.connection, new String(baos.toByteArray(), "UTF-8"), e); }
        }
        try {
            baos.close();
        } catch (final Throwable t) {
        }
        return baos.toByteArray();
    }

    /**
     * 
     * Please do not forget to close the output stream.
     * 
     * @param url
     * @param progress
     * @param maxSize
     * @param baos
     * @throws IOException
     * @throws InterruptedException
     */
    public void download(final URL url, final DownloadProgress progress, final long maxSize, final OutputStream baos) throws IOException, InterruptedException {
        BufferedInputStream input = null;
        GZIPInputStream gzi = null;
        try {

            this.connection = (HttpURLConnection) url.openConnection();
            this.connection.setInstanceFollowRedirects(this.followRedirects);
            this.connection.setConnectTimeout(this.connectTimeout);
            this.connection.setReadTimeout(this.readTimeout);
            try {
                final String loc = Loc.getLocale().split("_")[0];
                this.connection.setRequestProperty("Accept-Language", loc);
            } catch (final Throwable e) {
                // Log.exception(Level.WARNING, e);
            }
            this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
            this.connection.setRequestProperty("Connection", "Close");
            for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                this.connection.setRequestProperty(next.getKey(), next.getValue());
            }
            this.connection.connect();
            IOException exception = null;
            try {
                if (this.connection.getHeaderField("Content-Encoding") != null && this.connection.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                    input = new BufferedInputStream(gzi = new GZIPInputStream(this.connection.getInputStream()));
                } else {
                    input = new BufferedInputStream(this.connection.getInputStream());
                }
            } catch (final IOException e) {
                exception = e;
                if (this.connection.getHeaderField("Content-Encoding") != null && this.connection.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                    input = new BufferedInputStream(gzi = new GZIPInputStream(this.connection.getErrorStream()));
                } else {
                    input = new BufferedInputStream(this.connection.getErrorStream());
                }
            }

            if (maxSize > 0 && this.connection.getContentLength() > maxSize) { throw new IOException("Max size exeeded!"); }
            if (progress != null) {
                progress.setTotal(this.connection.getContentLength());
            }
            final byte[] b = new byte[32767];
            int len;
            long loaded = 0;
            while ((len = input.read(b)) != -1) {
                if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
                if (len > 0) {
                    baos.write(b, 0, len);
                    loaded += len;
                    if (maxSize > 0 && loaded > maxSize) { throw new IOException("Max size exeeded!"); }
                }
                if (progress != null) {
                    progress.increaseLoaded(len);
                }
            }
            if (exception != null) {

            throw exception;

            }
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
                this.connection.disconnect();
            } catch (final Throwable e) {
            }

        }
    }

    public HttpURLConnection getConnection() {
        return this.connection;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public String getPage(final URL url) throws IOException, InterruptedException {
        synchronized (SimpleHTTP.CALL_LOCK) {
            BufferedReader in = null;
            InputStreamReader isr = null;
            final StringBuilder sb = new StringBuilder();
            try {
                this.connection = (HttpURLConnection) url.openConnection();
                this.connection.setInstanceFollowRedirects(this.followRedirects);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(this.readTimeout);
                try {
                    final String loc = Loc.getLocale().split("_")[0];
                    this.connection.setRequestProperty("Accept-Language", loc);

                } catch (final Throwable e) {

                }
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty("Connection", "Close");
                this.connection.setRequestProperty("Accept-Charset", "UTF-8");
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }
                if (this.headerDebug) {
                    final Iterator<Entry<String, List<String>>> it = this.connection.getRequestProperties().entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<String, List<String>> next = it.next();
                        for (final String value : next.getValue()) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append("REQUEST: " + next.getKey() + " = " + value);
                        }
                    }
                }
                int lookupTry = 0;
                while (true) {
                    try {
                        this.connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }

                in = new BufferedReader(isr = new InputStreamReader(this.connection.getInputStream(), "UTF-8"));

                String str;
                final StringBuilder sb2 = new StringBuilder();
                while ((str = in.readLine()) != null) {
                    if (sb2.length() > 0) {
                        sb2.append("\r\n");
                    }
                    sb2.append(str);

                }
                if (this.headerDebug) {
                    final Iterator<Entry<String, List<String>>> it = this.connection.getHeaderFields().entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<String, List<String>> next = it.next();
                        for (final String value : next.getValue()) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append("RESPONSE: " + next.getKey() + " = " + value);
                        }
                    }
                    Log.L.info(sb.toString());
                }
                return sb2.toString();

            } finally {
                try {
                    in.close();
                } catch (final Throwable e) {
                }
                try {
                    isr.close();
                } catch (final Throwable e) {
                }
                try {
                    this.connection.disconnect();
                } catch (final Throwable e) {
                }

            }
        }
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    /**
     * @return
     */
    public HashMap<String, String> getRequestHeader() {
        return this.requestHeader;
    }

    public String getRequestHeader(final String key) {
        return this.requestHeader.get(key);
    }

    public String getResponseHeader(final String string) {
        synchronized (SimpleHTTP.CALL_LOCK) {
            if (this.connection == null) { return null; }
            return this.connection.getHeaderField(string);

        }
    }

    public boolean isFollowRedirects() {
        return this.followRedirects;
    }

    /**
     * @return the headerDebug
     */
    public boolean isHeaderDebug() {
        return this.headerDebug;
    }

    public HttpURLConnection openGetConnection(final URL url) throws IOException, InterruptedException {
        return this.openGetConnection(url, this.readTimeout);
    }

    public HttpURLConnection openGetConnection(final URL url, final int readTimeout) throws IOException, InterruptedException {
        boolean close = true;
        synchronized (SimpleHTTP.CALL_LOCK) {
            final StringBuilder sb = new StringBuilder();
            try {
                this.connection = (HttpURLConnection) url.openConnection();
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setInstanceFollowRedirects(this.followRedirects);
                this.connection.setReadTimeout(readTimeout < 0 ? readTimeout : readTimeout);
                try {
                    final String loc = Loc.getLocale().split("_")[0];
                    this.connection.setRequestProperty("Accept-Language", loc);
                } catch (final Throwable e) {
                    // Log.exception(Level.WARNING, e);
                }
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty("Connection", "Close");
                this.connection.setRequestProperty("Accept-Charset", "UTF-8");
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }
                if (this.headerDebug) {
                    final Iterator<Entry<String, List<String>>> it = this.connection.getRequestProperties().entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<String, List<String>> next = it.next();
                        for (final String value : next.getValue()) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append("REQUEST: " + next.getKey() + " = " + value);
                        }
                    }
                }
                int lookupTry = 0;
                while (true) {
                    try {
                        this.connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }
                close = false;
                if (this.headerDebug) {
                    final Iterator<Entry<String, List<String>>> it = this.connection.getHeaderFields().entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<String, List<String>> next = it.next();
                        for (final String value : next.getValue()) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append("RESPONSE: " + next.getKey() + " = " + value);
                        }
                    }
                    Log.L.info(sb.toString());
                }
                return this.connection;
            } finally {
                try {
                    if (close) {
                        this.connection.disconnect();
                    }
                } catch (final Throwable e2) {
                }

            }
        }
    }

    public HttpURLConnection openPostConnection(final URL url, final String postData, final HashMap<String, String> header) throws IOException, InterruptedException {
        boolean close = true;
        synchronized (SimpleHTTP.CALL_LOCK) {
            OutputStreamWriter writer = null;
            OutputStream outputStream = null;
            final StringBuilder sb = new StringBuilder();
            try {
                this.connection = (HttpURLConnection) url.openConnection();
                this.connection.setInstanceFollowRedirects(this.followRedirects);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(this.readTimeout);
                this.connection.setRequestMethod("POST");
                this.connection.setDoInput(true);
                this.connection.setUseCaches(false);
                this.connection.setDoOutput(true);
                if (Loc.getLocale() != null) {
                    try {
                        final String loc = Loc.getLocale().split("_")[0];
                        this.connection.setRequestProperty("Accept-Language", loc);
                    } catch (final Throwable e) {
                        Log.exception(Level.WARNING, e);
                    }
                }
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty("Connection", "Close");
                /* connection specific headers */
                if (header != null) {
                    for (final Entry<String, String> next : header.entrySet()) {
                        this.connection.setRequestProperty(next.getKey(), next.getValue());
                    }
                }
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }
                if (this.headerDebug) {
                    final Iterator<Entry<String, List<String>>> it = this.connection.getRequestProperties().entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<String, List<String>> next = it.next();
                        for (final String value : next.getValue()) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append("REQUEST: " + next.getKey() + " = " + value);
                        }
                    }
                }
                int lookupTry = 0;
                while (true) {
                    try {
                        this.connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }
                outputStream = this.connection.getOutputStream();
                writer = new OutputStreamWriter(outputStream);
                writer.write(postData);
                writer.flush();
                close = false;
                if (this.headerDebug) {
                    final Iterator<Entry<String, List<String>>> it = this.connection.getHeaderFields().entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry<String, List<String>> next = it.next();
                        for (final String value : next.getValue()) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append("RESPONSE: " + next.getKey() + " = " + value);
                        }
                    }
                    Log.L.info(sb.toString());
                }
                return this.connection;

            } finally {
                try {
                    if (close) {
                        this.connection.disconnect();
                    }
                } catch (final Throwable e2) {
                }
                try {
                    writer.close();
                } catch (final Throwable e) {
                }
                try {
                    outputStream.close();
                } catch (final Throwable e) {
                }
            }
        }
    }

    public String postPage(final URL url, final String data) throws IOException, InterruptedException {
        synchronized (SimpleHTTP.CALL_LOCK) {
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            OutputStream outputStream = null;
            InputStreamReader isr = null;
            try {
                this.connection = (HttpURLConnection) url.openConnection();
                this.connection.setInstanceFollowRedirects(this.followRedirects);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(this.readTimeout);
                this.connection.setRequestMethod("POST");
                this.connection.setDoInput(true);
                this.connection.setUseCaches(false);
                this.connection.setDoOutput(true);
                try {
                    final String loc = Loc.getLocale().split("_")[0];
                    this.connection.setRequestProperty("Accept-Language", loc);
                } catch (final Throwable e) {
                    //
                }
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty("Connection", "Close");
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }

                int lookupTry = 0;
                while (true) {
                    try {
                        this.connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }
                outputStream = this.connection.getOutputStream();
                writer = new OutputStreamWriter(outputStream);
                writer.write(data);
                writer.flush();
                reader = new BufferedReader(isr = new InputStreamReader(this.connection.getInputStream(), "UTF-8"));
                final StringBuilder sb = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                    }
                    sb.append(str);

                }

                return sb.toString();

            } finally {
                try {
                    reader.close();
                } catch (final Throwable e) {
                }
                try {
                    isr.close();
                } catch (final Throwable e) {
                }
                try {
                    writer.close();
                } catch (final Throwable e) {
                }
                try {
                    outputStream.close();
                } catch (final Throwable e) {
                }
                try {
                    this.connection.disconnect();
                } catch (final Throwable e) {
                }

            }
        }
    }

    public void putRequestHeader(final String key, final String value) {
        this.requestHeader.put(key, value);
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setFollowRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * @param headerDebug
     *            the headerDebug to set
     */
    public void setHeaderDebug(final boolean headerDebug) {
        this.headerDebug = headerDebug;
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

}
