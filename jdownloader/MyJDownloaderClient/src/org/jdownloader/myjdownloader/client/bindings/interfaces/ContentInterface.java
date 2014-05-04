package org.jdownloader.myjdownloader.client.bindings.interfaces;

import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.exceptions.device.ApiFileNotFoundException;
import org.jdownloader.myjdownloader.client.exceptions.device.InternalServerErrorException;

@ClientApiNameSpace("contentV2")
public interface ContentInterface extends Linkable {

    public byte[] getFavIcon(String hostername) throws ApiFileNotFoundException, InternalServerErrorException;

    public byte[] getFileIcon(String filename) throws InternalServerErrorException;
}
