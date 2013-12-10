package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.exceptions.device.ApiFileNotFoundException;
import org.jdownloader.myjdownloader.client.exceptions.device.InternalServerErrorException;

@ApiNamespace("content")
public interface ContentAPI {

    public byte[] favicon(String hostername) throws ApiFileNotFoundException, InternalServerErrorException;

    public byte[] fileIcon(String filename) throws InternalServerErrorException;
}
