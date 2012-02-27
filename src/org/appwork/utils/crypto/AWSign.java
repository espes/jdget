package org.appwork.utils.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.appwork.utils.IO;
import org.appwork.utils.encoding.Base64;

public class AWSign {

    /**
     * @param f
     * @param key
     * @return
     * @throws SignatureViolation
     */
    public static byte[] createSign(File f, String key) throws SignatureViolation {

        try {
            return createSign(f, KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key))));
        } catch (Throwable e) {
            throw new SignatureViolation(e);
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        createKeyPair();
    }

    public static void createKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        System.out.println("PUBLIC  " + Base64.encodeToString(keyPair.getPublic().getEncoded(), false));
        System.out.println("PRIVATE " + Base64.encodeToString(keyPair.getPrivate().getEncoded(), false));
    }

    public static byte[] createSign(File f, PrivateKey publicKey) throws SignatureViolation {
        try {

            Signature sig = Signature.getInstance("Sha256WithRSA");
            sig.initSign(publicKey);

            InputStream input = null;
            try {
                byte[] buffer = new byte[1024];
                int len;
                input = new FileInputStream(f);
                while ((len = input.read(buffer)) != -1) {
                    if (len > 0) {
                        sig.update(buffer, 0, len);
                    }
                }
            } finally {

                try {
                    input.close();
                } catch (final Exception e) {
                }

            }

            return sig.sign();

        } catch (Throwable e) {
            throw new SignatureViolation(e);
        }
    }

    public static void verifyFile(File f, String pub) throws SignatureViolation {
        try {
            verifyFile(f, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(pub))));
        } catch (InvalidKeySpecException e) {
            throw new SignatureViolation(e);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureViolation(e);
        }

    }

    /**
     * @param f
     * @param pub
     * @throws SignatureViolation
     */
    public static void verifyFile(File f, PublicKey pub) throws SignatureViolation {
        try {
            verifySignature(f, pub, IO.readFile(new File(f.getAbsolutePath() + ".updateSignature")));
        } catch (IOException e) {
            throw new SignatureViolation(e);
        }
    }

    /**
     * @param f
     * @param pub
     * @param bs
     * @throws SignatureViolation
     */
    public static void verifySignature(File f, PublicKey pub, byte[] signature) throws SignatureViolation {
        try {

            Signature sig = Signature.getInstance("Sha256WithRSA");
            sig.initVerify(pub);

            InputStream input = null;
            try {
                byte[] buffer = new byte[1024];
                int len;
                input = f.toURI().toURL().openStream();
                while ((len = input.read(buffer)) != -1) {
                    if (len > 0) {
                        sig.update(buffer, 0, len);
                    }
                }
            } finally {

                try {
                    input.close();
                } catch (final Exception e) {
                }

            }
            if (!sig.verify(signature)) {
                new SignatureViolation("Signatur Check Failed: " + f);
            }
        } catch (Throwable e) {

            throw new SignatureViolation(e);
        }
    }

    /**
     * @param decode
     * @param decode2
     * @throws SignatureViolation
     */
    public static void verifyFile(byte[] dataToVerify, PublicKey pub, byte[] signature) throws SignatureViolation {
        try {

            Signature sig = Signature.getInstance("Sha256WithRSA");
            sig.initVerify(pub);
            sig.update(dataToVerify);
            if (!sig.verify(signature)) {
                new SignatureViolation("Signatur Check Failed");
            }
        } catch (Throwable e) {

            throw new SignatureViolation(e);
        }
    }

}
