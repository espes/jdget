package org.jdownloader.myjdownloader.client;

import java.util.Map;

import org.jdownloader.myjdownloader.client.exceptions.ExceptionResponse;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.exceptions.UnexpectedIOException;
import org.jdownloader.myjdownloader.client.exceptions.storage.StorageAlreadyExistsException;
import org.jdownloader.myjdownloader.client.exceptions.storage.StorageInvalidIDException;
import org.jdownloader.myjdownloader.client.exceptions.storage.StorageInvalidKeyException;
import org.jdownloader.myjdownloader.client.exceptions.storage.StorageKeyNotFoundException;
import org.jdownloader.myjdownloader.client.exceptions.storage.StorageLimitReachedException;
import org.jdownloader.myjdownloader.client.exceptions.storage.StorageNotFoundException;
import org.jdownloader.myjdownloader.client.json.ErrorResponse;
import org.jdownloader.myjdownloader.client.json.JSonRequest;
import org.jdownloader.myjdownloader.client.json.RequestIDOnly;
import org.jdownloader.myjdownloader.client.json.ServerErrorType;
import org.jdownloader.myjdownloader.client.json.storage.StorageGetValueResponse;
import org.jdownloader.myjdownloader.client.json.storage.StorageListResponse;

public class StorageMyJDClient<GenericType> {

    private final AbstractMyJDClient<GenericType> api;

    public StorageMyJDClient(final AbstractMyJDClient<GenericType> abstractMyJDClient) {
        this.api = abstractMyJDClient;
    }

    protected <T> T callServer(final String query, final JSonRequest jsonRequest, final SessionInfo session, final Class<T> class1) throws MyJDownloaderException {
        try {
            return this.api.callServer(query, jsonRequest, session, class1);
        } catch (final UnexpectedIOException e) {
            if (e.getCause() instanceof ExceptionResponse) {
                ErrorResponse error = null;
                try {
                    final ExceptionResponse cause = (ExceptionResponse) e.getCause();
                    error = this.api.jsonToObject(cause.getContent(), (GenericType) ErrorResponse.class);
                } catch (final Throwable e2) {
                }
                if (error != null) {
                    switch (error.getSrc()) {
                    case MYJD:
                        final ServerErrorType type = ServerErrorType.valueOf(error.getType());
                        switch (type) {
                        case STORAGE_ALREADY_EXISTS:
                            throw new StorageAlreadyExistsException();
                        case STORAGE_INVALID_KEY:
                            throw new StorageInvalidKeyException();
                        case STORAGE_INVALID_STORAGEID:
                            throw new StorageInvalidIDException();
                        case STORAGE_NOT_FOUND:
                            throw new StorageNotFoundException();
                        case STORAGE_KEY_NOT_FOUND:
                            throw new StorageKeyNotFoundException();
                        case STORAGE_LIMIT_REACHED:
                            throw new StorageLimitReachedException();
                        }
                    }
                }
            }
            throw e;
        }
    }

    public void create(final String storageID) throws MyJDownloaderException, StorageAlreadyExistsException, StorageInvalidIDException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/storage/createstorage?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setParams(new Object[] { storageID });
        re.setUrl(url);
        this.callServer(url, re, sessionInfo, RequestIDOnly.class);
    }

    public void drop(final String storageID) throws MyJDownloaderException, StorageNotFoundException, StorageInvalidIDException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/storage/dropstorage?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest request = new JSonRequest();
        request.setParams(new Object[] { storageID });
        request.setUrl(url);
        this.callServer(url, request, sessionInfo, RequestIDOnly.class);
    }

    public String getValue(final String storageID, final String key) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/storage/getvalue?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest request = new JSonRequest();
        request.setParams(new Object[] { storageID, key });
        request.setUrl(url);
        return this.callServer(url, request, sessionInfo, StorageGetValueResponse.class).getValue();
    }

    public Map<String, Long> list() throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/storage/liststorages?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        return this.callServer(url, null, sessionInfo, StorageListResponse.class).getList();
    }

    public Map<String, Long> listKeys(final String storageID) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/storage/listkeys?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest request = new JSonRequest();
        request.setParams(new Object[] { storageID });
        request.setUrl(url);
        return this.callServer(url, request, sessionInfo, StorageListResponse.class).getList();
    }

    public void putValue(final String storageID, final String key, final String value) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/storage/putvalue?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest request = new JSonRequest();
        request.setParams(new Object[] { storageID, key, value });
        request.setUrl(url);
        this.callServer(url, request, sessionInfo, RequestIDOnly.class);

    }

}
