/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.upload;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.remoteapi.RemoteAPIException;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.utils.Regex;
import org.appwork.utils.formatter.HexFormatter;
import org.appwork.utils.net.HTTPHeader;

/**
 * @author daniel
 * 
 */
public class RemoteAPIUpload implements RemoteUploadAPIInterface {

    private static enum STEP {
        CREATE,
        QUERY,
        RESUME
    }

    private final File                          uploadFolder;

    protected final HashMap<String, UploadUnit> uploadUnits = new HashMap<String, UploadUnit>();

    public RemoteAPIUpload(final File uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    public File get(final String eTag) {
        synchronized (this.uploadUnits) {
            final UploadUnit ret = this.uploadUnits.get("\"" + eTag + "\"");
            if (ret != null && ret.isComplete() && ret.isUploading() == false) {
                ret.setLastAccess(System.currentTimeMillis());
                return ret._getFile();
            }
        }
        return null;
    }

    public File getUploadFolder() {
        return this.uploadFolder;
    }

    @Override
    public List<UploadUnit> list() {
        synchronized (this.uploadUnits) {
            return new ArrayList<UploadUnit>(this.uploadUnits.values());
        }
    }

    /**
     * @param uploadUnit
     */
    protected void onComplete(final UploadUnit uploadUnit) {
    }

    protected void onCreate(final UploadUnit uploadUnit) {
    }

    /**
     * @param uploadUnit
     */
    protected void onResume(final UploadUnit uploadUnit) {
    }

    @Override
    public boolean remove(final String eTag) {
        synchronized (this.uploadUnits) {
            return this.uploadUnits.remove("\"" + eTag + "\"") != null;
        }
    }

    @Override
    public void uploadFile(final RemoteAPIRequest request, final RemoteAPIResponse response) {
        UploadUnit uploadUnit = null;
        STEP step = null;
        boolean processUpload = false;
        RandomAccessFile fos = null;
        HTTPHeader contentRange = null;
        try {
            synchronized (this.uploadUnits) {
                final HTTPHeader ifMatch = request.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_IF_MATCH);
                final HTTPHeader contentLength = request.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH);
                contentRange = request.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_CONTENT_RANGE);
                long contentLengthLong = -1;
                long contentSize = -1;
                if (contentLength != null) {
                    contentLengthLong = Long.parseLong(contentLength.getValue());
                } else {
                    contentLengthLong = 0;
                }
                if (contentRange != null) {
                    final String contentSizeString = new Regex(contentRange.getValue(), ".*?/\\s*?(\\d+)").getMatch(0);
                    if (contentSizeString != null) {
                        contentSize = Long.parseLong(contentSizeString);
                    }
                }
                if (ifMatch != null) {
                    /* check for existing UploadUnit */
                    uploadUnit = this.uploadUnits.get(ifMatch.getValue());
                }
                if (uploadUnit == null) {
                    step = STEP.CREATE;
                } else {
                    /* upload Unit does still exist */
                    if (contentLengthLong == 0) {
                        step = STEP.QUERY;
                    } else {
                        step = STEP.RESUME;
                    }
                }
                switch (step) {
                case QUERY:
                    if (uploadUnit.getExpectedFinalSize() != contentSize) {
                        /* size missmatch, so not found */
                        throw new RemoteAPIException(ResponseCode.ERROR_NOT_FOUND, "Size missmatch");
                    }
                    /* add ETag Header */
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_ETAG, uploadUnit._getQuotedETag()));
                    uploadUnit.setLastAccess(System.currentTimeMillis());
                    if (uploadUnit.isComplete()) {
                        /* upload is complete */
                        response.setResponseCode(ResponseCode.SUCCESS_OK);
                    } else {
                        /*
                         * add Range Header to signal current received
                         * contentSize
                         */
                        if (uploadUnit.getSize() != 0) {
                            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_RANGE, "0-" + (uploadUnit.getSize() - 1)));
                        }
                        /* upload is still incomplete */
                        response.setResponseCode(ResponseCode.RESUME_INCOMPLETE);
                    }
                    return;
                case CREATE:
                    if (ifMatch != null) {
                        /* given ETag no longer available */
                        throw new RemoteAPIException(ResponseCode.ERROR_NOT_FOUND);
                    }
                    /* upload is still incomplete */
                    if (contentSize <= 0) {
                        /* no or invalid contentSize given */
                        throw new RemoteAPIException(ResponseCode.ERROR_BAD_REQUEST);
                    }
                    uploadUnit = new UploadUnit(contentSize);
                    /* add ETag Header */
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_ETAG, uploadUnit._getQuotedETag()));
                    response.setResponseCode(ResponseCode.RESUME_INCOMPLETE);
                    uploadUnit.setLastAccess(System.currentTimeMillis());
                    final File uploadFile = new File(this.uploadFolder, uploadUnit.getETag());
                    uploadUnit._setFile(uploadFile);
                    this.uploadUnits.put(uploadUnit._getQuotedETag(), uploadUnit);
                    this.onCreate(uploadUnit);
                    return;
                case RESUME:
                    if (uploadUnit.getExpectedFinalSize() != contentSize) {
                        /* size missmatch, so not found */
                        throw new RemoteAPIException(ResponseCode.ERROR_NOT_FOUND, "Size missmatch");
                    }
                    if (uploadUnit.isUploading()) {
                        /* file is already in process */
                        throw new RemoteAPIException(ResponseCode.ERROR_FORBIDDEN, "Upload in process");
                    }
                    /* add ETag Header */
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_ETAG, uploadUnit._getQuotedETag()));
                    uploadUnit.setLastAccess(System.currentTimeMillis());
                    processUpload = true;
                    uploadUnit.setIsUploading(true);
                    this.onResume(uploadUnit);
                }
            }
            /* now we handle the upload */
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            fos = new RandomAccessFile(uploadUnit._getFile(), "rw");
            if (contentRange != null) {
                final String startRange = new Regex(contentRange.getValue(), "^\\s*?bytes\\s*?(\\d+)").getMatch(0);
                if (startRange != null) {
                    final long start = Long.parseLong(startRange);
                    if (start > uploadUnit.getExpectedFinalSize()) { throw new RemoteAPIException(ResponseCode.ERROR_RANGE_NOT_SUPPORTED); }
                    fos.seek(start);
                }
            } else {
                fos.seek(0);
            }
            final byte[] buffer = new byte[32767];
            int read = 0;
            final InputStream is = request.getInputStream();
            while ((read = is.read(buffer)) != -1) {
                if (read > 0) {
                    fos.write(buffer, 0, read);
                    md.update(buffer, 0, read);
                }
            }
            fos.close();
            uploadUnit.setLastAccess(System.currentTimeMillis());
            final String chunkHash = HexFormatter.byteArrayToHex(md.digest());
            if (uploadUnit.isComplete()) {
                /* upload is complete */
                this.onComplete(uploadUnit);
                response.setResponseCode(ResponseCode.SUCCESS_OK);
            } else {
                /* add Range Header to signal current received contentSize */
                if (uploadUnit.getSize() != 0) {
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_RANGE, "0-" + (uploadUnit.getSize() - 1)));
                }
                /* upload is still incomplete */
                response.setResponseCode(ResponseCode.RESUME_INCOMPLETE);
            }
            final OutputStream os = response.getOutputStream(true);
            os.write(chunkHash.getBytes("UTF-8"));
        } catch (final Throwable e) {
            if (e instanceof RemoteAPIException) { throw (RemoteAPIException) e; }
            throw new RemoteAPIException(ResponseCode.SERVERERROR_INTERNAL);
        } finally {
            try {
                response.getOutputStream(true).close();
            } catch (final Throwable e) {
            }
            try {
                fos.close();
            } catch (final Throwable e) {
            }
            if (processUpload && uploadUnit != null) {
                uploadUnit.setIsUploading(false);
            }
        }
    }
}
