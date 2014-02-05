package org.jdownloader.myjdownloader.client;

import java.io.UnsupportedEncodingException;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.CryptedStorageItem;
import org.jdownloader.myjdownloader.client.json.JSonRequest;
import org.jdownloader.myjdownloader.client.json.StorageItem;
import org.jdownloader.myjdownloader.client.json.StoragePutResponse;

public class StorageMyJDClient {

    private final AbstractMyJDClient<?> api;

    public StorageMyJDClient(final AbstractMyJDClient<?> abstractMyJDClient) {
        this.api = abstractMyJDClient;
    }

    private CryptedStorageItem encrypt(final StorageItem storageItem, final SessionInfo sessionInfo) throws MyJDownloaderException {
        final CryptedStorageItem cryptedStorageItem = new CryptedStorageItem();
        cryptedStorageItem.setTimestamp(storageItem.getTimestamp());
        cryptedStorageItem.setSessionToken(sessionInfo.getSessionToken());
        try {
            if (storageItem.getName() != null) {
                final byte[] encryptKey = this.api.updateEncryptionToken(this.api.getSessionInfo().getDeviceEncryptionToken(), (storageItem.getTimestamp() + "").getBytes("UTF-8"));
                cryptedStorageItem.setCryptedName(AbstractMyJDClient.byteArrayToHex(this.api.encrypt(storageItem.getName().getBytes("UTF-8"), encryptKey)));
            }
            if (storageItem.getContent() != null) {
                cryptedStorageItem.setCryptedContent(AbstractMyJDClient.byteArrayToHex(this.api.encrypt(storageItem.getContent().getBytes("UTF-8"), sessionInfo.getDeviceEncryptionToken())));
            }
        } catch (final UnsupportedEncodingException e) {
            throw MyJDownloaderException.get(e);
        }
        return cryptedStorageItem;
    }

    public long put(final StorageItem storageItem) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final JSonRequest re = new JSonRequest();

        re.setRid(this.api.getUniqueRID());
        re.setParams(new Object[] { this.encrypt(storageItem, sessionInfo) });
        final String url = "/storage/put?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        re.setUrl(url);
        return this.api.callServer(url, re, sessionInfo, StoragePutResponse.class).getItemID();
    }
}
