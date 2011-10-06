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

import org.appwork.storage.config.JsonConfig;

/**
 * @author Thomas
 * 
 */
public class EncryptionTest {

    private static CryptedConfig enc;

    /**
     * @param args
     */
    public static void main(String[] args) {
       enc=JsonConfig.create(CryptedConfig.class);
       System.out.println(enc.getPlain());
       System.out.println(enc.getString());
       enc.setString(System.currentTimeMillis()+"");
       enc.setPlain(new TestObject());
       PlainConfig plain = JsonConfig.create(PlainConfig.class);
       
      System.out.println(plain.getCrypted());
      System.out.println(plain.getString());
      plain.setString(System.currentTimeMillis()+"");
      plain.setCrypted(new TestObject());

    }
}
