/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

/**
 * @author thomas
 * 
 */
public interface ConfigInterface {

    public void addListener(final ConfigEventListener listener);

    public StorageHandler<?> getStorageHandler();

    public void removeListener(final ConfigEventListener listener);

    String toString();

}
