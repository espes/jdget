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

import java.util.List;

import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.exceptions.BasicRemoteAPIException;

/**
 * @author daniel
 * 
 */
public interface RemoteUploadAPIInterface extends RemoteAPIInterface {
    public List<UploadUnit> list();

    public boolean remove(String eTag);

    public void uploadFile(final RemoteAPIRequest request, final RemoteAPIResponse response) throws BasicRemoteAPIException;

}
