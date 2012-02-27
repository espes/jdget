package org.appwork.utils.net.BasicHTTP;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.txtresource.TranslationFactory;
import org.appwork.utils.Application;
import org.appwork.utils.net.DownloadProgress;
import org.appwork.utils.net.httpconnection.HTTPConnection;
import org.appwork.utils.net.httpconnection.HTTPConnection.RequestMethod;
import org.appwork.utils.net.httpconnection.HTTPConnectionFactory;
import org.appwork.utils.net.httpconnection.HTTPProxy;

public class BasicHTTP {

    private static final Object CALL_LOCK = new Object();

    public static void main(final String[] args) throws MalformedURLException, IOException, InterruptedException {

        final BasicHTTP client = new BasicHTTP();
        System.out.println(client.getPage(new URL("http://ipcheck0.jdownloader.org")));
        // client.download(new URL("http://update3.jdownloader.org/speed.avi"),
        // null, new File("/home/daniel/speed.avi"));

        // System.out.println(new BasicHTTP().postPage(new
        // URL("http://ipcheck0.jdownloader.org"), "BKA"));
    }

    private final HashMap<String, String> requestHeader;

    private HTTPConnection                connection;

    private int                           connectTimeout = 15000;

    private int                           readTimeout    = 30000;

    private HTTPProxy                     proxy;

    public BasicHTTP() {
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
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            try {
                this.download(url, progress, 0, fos, file.length());
            } catch (final IOException e) {
                final IOException ex = new BasicHTTPException(this.connection, e);
                throw ex;
            }
        } finally {
            try {
                fos.close();
            } catch (final Throwable t) {
            }
        }
    }

    public byte[] download(final URL url, final DownloadProgress progress, final long maxSize) throws IOException, InterruptedException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            this.download(url, progress, maxSize, baos, -1);
        } catch (final IOException e) {
            if (baos.size() > 0) {
                throw new BasicHTTPException(this.connection, e);
            } else {
                throw e;
            }
        } finally {
            try {
                baos.close();
            } catch (final Throwable t) {
            }
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
    public void download(final URL url, final DownloadProgress progress, final long maxSize, final OutputStream baos, final long resumePosition) throws IOException, InterruptedException {
        InputStream input = null;
        try {

            this.connection = HTTPConnectionFactory.createHTTPConnection(url, this.proxy);
            this.connection.setConnectTimeout(this.connectTimeout);
            this.connection.setReadTimeout(this.readTimeout);
            this.connection.setRequestProperty("Accept-Language", TranslationFactory.getDesiredLanguage());
            this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
            for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                this.connection.setRequestProperty(next.getKey(), next.getValue());
            }
            if (resumePosition > 0) {
                this.connection.setRequestProperty("Range", "bytes=" + resumePosition + "-");
            }
            this.connection.setRequestProperty("Connection", "Close");
            this.connection.connect();

            input = this.connection.getInputStream();

            if (maxSize > 0 && this.connection.getCompleteContentLength() > maxSize) { throw new IOException("Max size exeeded!"); }
            if (progress != null) {
                progress.setTotal(this.connection.getCompleteContentLength());
            }
            final byte[] b = new byte[32767];
            int len;
            long loaded = Math.max(0, resumePosition);
            if (progress != null) {
                progress.setLoaded(loaded);
            }
            while ((len = input.read(b)) != -1) {
                if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
                if (len > 0) {
                    baos.write(b, 0, len);
                    loaded += len;
                    if (maxSize > 0 && loaded > maxSize) { throw new IOException("Max size exeeded!"); }
                    if (progress != null) {
                        progress.increaseLoaded(len);
                    }
                }
            }
            if (loaded != this.connection.getCompleteContentLength()) { throw new IOException("Incomplete download!"); }
        } catch (IOException e) {
            throw new BasicHTTPException(connection, e);
        } finally {
            try {
                input.close();
            } catch (final Exception e) {
            }
            try {
                this.connection.disconnect();
            } catch (final Throwable e) {
            }

        }
    }

    public HTTPConnection getConnection() {
        return this.connection;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public String getPage(final URL url) throws IOException, InterruptedException {
        synchronized (BasicHTTP.CALL_LOCK) {
            BufferedReader in = null;
            InputStreamReader isr = null;
            try {

                this.connection = HTTPConnectionFactory.createHTTPConnection(url, this.proxy);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(this.readTimeout);
                this.connection.setRequestProperty("Accept-Language", TranslationFactory.getDesiredLanguage());
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty("Accept-Charset", "UTF-8");
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }
                this.connection.setRequestProperty("Connection", "Close");
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
                final StringBuilder sb = new StringBuilder();
                while ((str = in.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                    }
                    sb.append(str);

                }

                return sb.toString();
            } catch (IOException e) {
                throw new BasicHTTPException(connection, e);
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

    public HTTPProxy getProxy() {
        return this.proxy;
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
        synchronized (BasicHTTP.CALL_LOCK) {
            if (this.connection == null) { return null; }
            return this.connection.getHeaderField(string);

        }
    }

    public HTTPConnection openGetConnection(final URL url) throws IOException, InterruptedException {
        return this.openGetConnection(url, this.readTimeout);
    }

    public HTTPConnection openGetConnection(final URL url, final int readTimeout) throws IOException, InterruptedException {
        boolean close = true;
        synchronized (BasicHTTP.CALL_LOCK) {
            try {
                this.connection = HTTPConnectionFactory.createHTTPConnection(url, this.proxy);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(readTimeout < 0 ? this.readTimeout : readTimeout);
                this.connection.setRequestProperty("Accept-Language", TranslationFactory.getDesiredLanguage());
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty("Accept-Charset", "UTF-8");
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }
                this.connection.setRequestProperty("Connection", "Close");
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

    public HTTPConnection openPostConnection(final URL url, final String postData, final HashMap<String, String> header) throws IOException, InterruptedException {
        boolean close = true;
        synchronized (BasicHTTP.CALL_LOCK) {
            OutputStreamWriter writer = null;
            OutputStream outputStream = null;
            try {

                this.connection = HTTPConnectionFactory.createHTTPConnection(url, this.proxy);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(this.readTimeout);
                this.connection.setRequestMethod(RequestMethod.POST);

                this.connection.setRequestProperty("Accept-Language", TranslationFactory.getDesiredLanguage());
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, postData.getBytes().length + "");
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
                this.connection.finalizeConnect();
                close = false;
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
        synchronized (BasicHTTP.CALL_LOCK) {
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            OutputStream outputStream = null;
            InputStreamReader isr = null;
            try {
                this.connection = HTTPConnectionFactory.createHTTPConnection(url, this.proxy);
                this.connection.setConnectTimeout(this.connectTimeout);
                this.connection.setReadTimeout(this.readTimeout);
                this.connection.setRequestMethod(RequestMethod.POST);
                this.connection.setRequestProperty("Accept-Language", TranslationFactory.getDesiredLanguage());
                this.connection.setRequestProperty("User-Agent", "AppWork " + Application.getApplication());
                this.connection.setRequestProperty(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, data.getBytes().length + "");
                for (final Entry<String, String> next : this.requestHeader.entrySet()) {
                    this.connection.setRequestProperty(next.getKey(), next.getValue());
                }
                this.connection.setRequestProperty("Connection", "Close");
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
                this.connection.finalizeConnect();
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
            } catch (IOException e) {
                throw new BasicHTTPException(connection, e);
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
        this.connectTimeout = Math.max(1000, connectTimeout);
    }

    public void setProxy(final HTTPProxy proxy) {
        this.proxy = proxy;
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = Math.max(1000, readTimeout);
    }

}
