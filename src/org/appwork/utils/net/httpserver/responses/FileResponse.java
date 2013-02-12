/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.responses
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.responses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.Files;
import org.appwork.utils.ReusableByteArrayOutputStream;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.net.ChunkedOutputStream;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.requests.HttpRequestInterface;

/**
 * @author daniel
 * 
 */
public class FileResponse {

    public static String getMimeType(final String name) {
        final String extension = Files.getExtension(name);
        String mime = FileResponse.MIMES.get(extension.toLowerCase(Locale.ENGLISH));
        if (mime == null) {
            mime = "application/octet-stream";
        }
        return mime;
    }

    private final HttpRequestInterface     request;
    private final HttpResponseInterface    response;
    private File                           inputFile;

    private URL                            inputURL;

    private static HashMap<String, String> MIMES = new HashMap<String, String>();

    static {
        FileResponse.MIMES.put("html", "text/html");
        FileResponse.MIMES.put("htm", "text/html");
        FileResponse.MIMES.put("txt", "text/plain");
        FileResponse.MIMES.put("gif", "image/gif");
        FileResponse.MIMES.put("css", "text/css");
        FileResponse.MIMES.put("js", "text/javascript");
        FileResponse.MIMES.put("png", "image/png");
        FileResponse.MIMES.put("jpeg", "image/jpeg");
        FileResponse.MIMES.put("jpg", "image/jpeg");
        FileResponse.MIMES.put("jpe", "image/jpeg");
        FileResponse.MIMES.put("ico", "image/x-icon");
    }

    public FileResponse(final HttpRequestInterface request, final HttpResponseInterface response, final File inputFile) {
        this.request = request;
        this.response = response;
        this.inputFile = inputFile;
    }

    public FileResponse(final HttpRequestInterface request, final HttpResponseInterface response, final URL inputURL) {
        this.request = request;
        this.response = response;
        this.inputURL = inputURL;
    }

    /* do we allow gzip-encoded? */
    protected boolean allowGZIP() {
        final HTTPHeader acceptEncoding = this.request.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_ACCEPT_ENCODING);
        if (acceptEncoding != null) {
            final String value = acceptEncoding.getValue();
            if (value != null && value.contains("gzip")) { return true; }
        }
        return false;
    }

    /* do we allow Ranges for the given Content? */
    protected boolean allowRanges() {
        if (this.inputURL != null) {
            /* we do not support ranges for URL resources at the moment */
            return false;
        }
        return false;
    }

    /* what is the size of the given Content ? */
    protected long getContentLength(final long knownLength) {
        /* TODO: check for unsatisfied range or not allowed range */
        final boolean allowRanges = this.allowRanges();
        if (this.inputURL != null) {
            /* we do not know size of URL resources in advance! */
            if (knownLength >= 0) { return knownLength; }
            return -1;
        } else {
            if (!allowRanges) {
                /* send complete file */
                return this.inputFile.length();
            } else {
                return -1;
            }
        }
    }

    /* return filename for given Content, eg used for Content-Disposition */
    protected String getFileName() {
        String name = null;
        if (this.inputFile != null) {
            name = this.inputFile.getName();
        } else {
            name = this.inputURL.getFile();
        }
        return name;
    }

    /* return mimetype for given Content */
    protected String getMimeType() {
        return FileResponse.getMimeType(this.getFileName());
    }

    public void sendFile() throws IOException {
        InputStream is = null;
        URLConnection con = null;
        GZIPOutputStream gos = null;
        OutputStream os = null;
        ReusableByteArrayOutputStream ros = null;
        boolean chunked = false;
        boolean gzip = false;
        long knownLength = -1;
        try {
            /* get inputstream */
            if (this.inputURL != null) {
                con = this.inputURL.openConnection();
                knownLength = con.getContentLengthLong();
                is = con.getInputStream();
            } else if (this.inputFile != null) {
                is = new FileInputStream(this.inputFile);
                knownLength = this.inputFile.length();
            }
            this.response.setResponseCode(ResponseCode.SUCCESS_OK);
            if (this.allowRanges()) {
                /* do we allow ranges? */
                this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "bytes"));
            }
            if (this.allowGZIP()) {
                /* do we use gzip for content encoding? */
                if (!this.useContentDisposition()) {
                    /* only allow gzip when not offering to save the file */
                    gzip = true;
                    this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "gzip"));
                }
            }
            final long length = this.getContentLength(knownLength);
            if (length >= 0 && !gzip) {
                /* we know content length, send it */
                this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
            } else {
                /*
                 * content length is unknown or we use gzipped coding, let us
                 * use chunked encoding
                 */
                chunked = true;
                this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, "chunked"));
            }
            /* set content-type */
            this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, this.getMimeType()));
            if (this.useContentDisposition()) {
                /* offer file to download */
                this.response.getResponseHeaders().add(new HTTPHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(this.getFileName(), "UTF-8")));
            }
            /* configure outputstream */
            if (gzip) {
                if (chunked) {
                    os = gos = new GZIPOutputStream(new ChunkedOutputStream(this.response.getOutputStream()));
                } else {
                    os = gos = new GZIPOutputStream(this.response.getOutputStream());
                }
            } else {
                if (chunked) {
                    os = new ChunkedOutputStream(this.response.getOutputStream());
                } else {
                    os = this.response.getOutputStream();
                }
            }
            /* forward the data from inputstream to outputstream */
            ros = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(1024);
            int read = 0;
            while ((read = is.read(ros.getInternalBuffer())) >= 0) {
                if (read > 0) {
                    os.write(ros.getInternalBuffer(), 0, read);
                } else {
                    synchronized (this) {
                        try {
                            this.wait(500);
                        } catch (final InterruptedException e) {
                            throw new IOException(e);
                        }
                    }
                }
            }
        } finally {
            try {
                /* gzip first */
                gos.finish();
            } catch (final Throwable e) {
            }
            try {
                /* gzip first */
                gos.close();
            } catch (final Throwable e) {
            }
            try {
                /* output next, can be chunked */
                os.close();
            } catch (final Throwable e) {
            }
            try {
                ReusableByteArrayOutputStreamPool.reuseReusableByteArrayOutputStream(ros);
            } catch (final Throwable e) {
            }
            try {
                is.close();
            } catch (final Throwable e) {
            }
        }
    }

    /* do we want the client to download this file or not? */
    protected boolean useContentDisposition() {
        if (this.inputURL != null) { return false; }
        return true;
    }

}
