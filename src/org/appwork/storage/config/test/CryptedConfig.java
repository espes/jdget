/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.PlainStorage;

/**
 * @author Thomas
 *
 */

@CryptedStorage(key= {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16})
public interface CryptedConfig extends ConfigInterface {

    
    public String getString();
    public void setString(String str);
    
    
@PlainStorage
    public TestObject getPlain();
    public void setPlain(TestObject str);
    
    
    
    
}
