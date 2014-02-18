package org.jdownloader.api.test;

import org.appwork.storage.Storage;
import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.api.test.TestClient.Test;
import org.jdownloader.myjdownloader.client.AbstractMyJDClientForDesktopJVM;
import org.jdownloader.myjdownloader.client.AccessToken;

public class RequestAccessTokenTest extends Test {
    
    @Override
    public void run(Storage config, AbstractMyJDClientForDesktopJVM api) throws Exception {
        final String service = Dialog.getInstance().showInputDialog(0, "Enter servicename", "Enter", null, null, null, null);
        AccessToken accessToken = api.requestAccessToken(service);
        Dialog.getInstance().showMessageDialog("Token: " + accessToken.getAccessToken());
        
    }
    
}
