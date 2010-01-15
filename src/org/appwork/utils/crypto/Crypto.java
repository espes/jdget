/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.appwork.utils.logging.Log;

/**
 * Crypto class provides a few easy to use functions to encrypt or decrypt data.
 * AES CBC Mode is used.
 * 
 * @author thomas
 * 
 */
public class Crypto {

    /**
     * Encrypts a String
     * 
     * @param string
     *            data to encrypt
     * @param key
     *            Key for encryption. Use 128 Bit (16 Byte) key
     * @return
     */
    public static byte[] encrypt(String string, byte[] key) {
        return encrypt(string, key, key);
    }

    /**
     * Encrypts a string
     * 
     * @param string
     *            String to encrypt
     * @param key
     *            to use (128Bit (16 Byte))
     * @param iv
     *            to use (128Bit (16 Byte))
     * @return
     */
    public static byte[] encrypt(String string, byte[] key, byte[] iv) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            return cipher.doFinal(string.getBytes());

        } catch (Exception e) {

            Log.exception(e);
        }
        return null;
    }

    /**
     * Decrypt data which has been encrypted width
     * {@link Crypto#encrypt(String, byte[], byte[])}
     * 
     * @param b
     *            data to decrypt
     * @param key
     *            to use (128Bit (16 Byte))
     * @param iv
     *            to use (128Bit (16 Byte))
     * @return
     */
    public static String decrypt(byte[] b, byte[] key, byte[] iv) {
        Cipher cipher;
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            return new String(cipher.doFinal(b));
        } catch (Exception e) {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            try {
                cipher = Cipher.getInstance("AES/CBC/nopadding");

                cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                return new String(cipher.doFinal(b));
            } catch (Exception e1) {
                Log.exception(e1);
            }

        }
        return null;
    }

    /**
     * Decrypts data which has been encrypted with
     * {@link Crypto#encrypt(String, byte[])}
     * 
     * @param b
     *            data to decrypt
     * @param key
     *            to use (128 Bit/16 Byte)
     * @return
     */
    public static String decrypt(byte[] b, byte[] key) {
        return decrypt(b, key, key);
    }

}
