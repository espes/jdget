/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

/**
 * @author daniel
 * 
 */
public interface UploadProgress {

    public long getTotal();

    public long getUploaded();

    public void increaseUploaded(long increase);

    public void onBytesUploaded(byte[] b, int len);

    public void setTotal(long total);

    public void setUploaded(long loaded);
}
