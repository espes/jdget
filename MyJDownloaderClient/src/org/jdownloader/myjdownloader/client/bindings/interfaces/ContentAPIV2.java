package org.jdownloader.myjdownloader.client.bindings.interfaces;

import org.jdownloader.myjdownloader.client.bindings.ApiNamespace;
import org.jdownloader.myjdownloader.client.exceptions.device.ApiFileNotFoundException;
import org.jdownloader.myjdownloader.client.exceptions.device.InternalServerErrorException;

@ApiNamespace("contentV2")
public interface ContentAPIV2 {

    public byte[] getFavIcon(String hostername) throws ApiFileNotFoundException, InternalServerErrorException;

    public byte[] getFileIcon(String filename) throws InternalServerErrorException;
}
