package org.appwork.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.appwork.utils.Application;

public class SimpleHTTP {

    private static final Object           CALL_LOCK = new Object();

    private final HashMap<String, String> requestHeader;
    private HttpURLConnection             connection;

    public SimpleHTTP() {
        requestHeader = new HashMap<String, String>();

    }

    public void clearRequestHeader() {
        requestHeader.clear();
    }

    public byte[] download(final URL url, final DownloadProgress progress, final long maxSize) throws IOException, InterruptedException {
        BufferedInputStream input = null;
        GZIPInputStream gzi = null;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
            connection.setRequestProperty("Connection", "Close");
            for (final Entry<String, String> next : requestHeader.entrySet()) {
                connection.setRequestProperty(next.getKey(), next.getValue());
            }
            connection.connect();
            if (url.openConnection().getHeaderField("Content-Encoding") != null && connection.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                input = new BufferedInputStream(gzi = new GZIPInputStream(connection.getInputStream()));
            } else {
                input = new BufferedInputStream(connection.getInputStream());
            }

            if (maxSize > 0 && connection.getContentLength() > maxSize) { throw new IOException("Max size exeeded!"); }
            if (progress != null) {
                progress.setTotal(connection.getContentLength());
            }
            final byte[] b = new byte[32767];
            int len;
            while ((len = input.read(b)) != -1) {
                if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
                if (len > 0) {
                    baos.write(b, 0, len);
                    if (maxSize > 0 && baos.size() > maxSize) { throw new IOException("Max size exeeded!"); }
                }
                if (progress != null) {
                    progress.increaseLoaded(len);
                }
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
                connection.disconnect();
            } catch (final Throwable e) {
            }

        }
        return baos.toByteArray();
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public String getPage(final URL url) throws IOException, InterruptedException {
        synchronized (SimpleHTTP.CALL_LOCK) {
            BufferedReader in = null;
            InputStreamReader isr = null;
            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                connection.setRequestProperty("Connection", "Close");
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                for (final Entry<String, String> next : requestHeader.entrySet()) {
                    connection.setRequestProperty(next.getKey(), next.getValue());
                }
                int lookupTry = 0;
                while (true) {
                    try {
                        connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }

                in = new BufferedReader(isr = new InputStreamReader(connection.getInputStream(), "UTF-8"));

                String str;
                final StringBuilder sb = new StringBuilder();
                while ((str = in.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                    }
                    sb.append(str);

                }

                return sb.toString();

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
                    connection.disconnect();
                } catch (final Throwable e) {
                }

            }
        }
    }

    public String getRequestHeader(final String key) {
        return requestHeader.get(key);
    }

    public String getResponseHeader(final String string) {
        synchronized (SimpleHTTP.CALL_LOCK) {
            if (connection == null) { return null; }
            return connection.getHeaderField(string);

        }
    }

    public HttpURLConnection openGetConnection(final URL url, final int readTimeout) throws IOException, InterruptedException {
        synchronized (SimpleHTTP.CALL_LOCK) {
            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);

                connection.setReadTimeout(readTimeout < 0 ? 20000 : readTimeout);
                connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                connection.setRequestProperty("Connection", "Close");
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                for (final Entry<String, String> next : requestHeader.entrySet()) {
                    connection.setRequestProperty(next.getKey(), next.getValue());
                }
                int lookupTry = 0;
                while (true) {
                    try {
                        connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }
                return connection;
            } finally {
                try {
                    connection.disconnect();
                } catch (final Throwable e2) {
                }

            }
        }
    }

    public HttpURLConnection openPostConnection(final URL url, final String postData, final HashMap<String, String> header) throws IOException, InterruptedException {
        synchronized (SimpleHTTP.CALL_LOCK) {
            OutputStreamWriter writer = null;
            OutputStream outputStream = null;
            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                connection.setRequestProperty("Connection", "Close");
                /* connection specific headers */
                if (header != null) {
                    for (final Entry<String, String> next : header.entrySet()) {
                        connection.setRequestProperty(next.getKey(), next.getValue());
                    }
                }
                for (final Entry<String, String> next : requestHeader.entrySet()) {
                    connection.setRequestProperty(next.getKey(), next.getValue());
                }

                int lookupTry = 0;
                while (true) {
                    try {
                        connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }
                outputStream = connection.getOutputStream();
                writer = new OutputStreamWriter(outputStream);
                writer.write(postData);
                writer.flush();

                return connection;

            } finally {
                try {
                    connection.disconnect();
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
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                connection.setRequestProperty("Connection", "Close");
                for (final Entry<String, String> next : requestHeader.entrySet()) {
                    connection.setRequestProperty(next.getKey(), next.getValue());
                }

                int lookupTry = 0;
                while (true) {
                    try {
                        connection.connect();
                        break;
                    } catch (final UnknownHostException e) {
                        if (++lookupTry > 3) { throw e; }
                        /* dns lookup failed, short wait and try again */
                        Thread.sleep(200);
                    }
                }
                outputStream = connection.getOutputStream();
                writer = new OutputStreamWriter(outputStream);
                writer.write(data);
                writer.flush();
                reader = new BufferedReader(isr = new InputStreamReader(connection.getInputStream(), "UTF-8"));
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
                    connection.disconnect();
                } catch (final Throwable e) {
                }

            }
        }
    }

    public void putRequestHeader(final String key, final String value) {
        requestHeader.put(key, value);
    }

}
