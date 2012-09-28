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
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.appwork.utils.IO;
import org.appwork.utils.encoding.Base64;

public class AWSign {

    public static void createKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final KeyPair keyPair = keyPairGenerator.genKeyPair();

        System.out.println("PUBLIC  " + Base64.encodeToString(keyPair.getPublic().getEncoded(), false));
        System.out.println("PRIVATE " + Base64.encodeToString(keyPair.getPrivate().getEncoded(), false));
    }

    public static byte[] createSign(final File f, final PrivateKey publicKey) throws SignatureViolation {
        return createSign(f, publicKey, false);
    }

    /**
     * @param bytes
     * @param pk
     * @param salt
     * @return
     * @throws SignatureViolation
     */
    public static byte[] createSign(byte[] bytes, PrivateKey pk, boolean salt) throws SignatureViolation {
        try {

            Signature sig = Signature.getInstance("Sha256WithRSA");
            if (salt) {

                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                sig.initSign(pk, sr);
            } else {
                sig.initSign(pk);
            }

            sig.update(bytes, 0, bytes.length);

            return sig.sign();

        } catch (Throwable e) {
            throw new SignatureViolation(e);
        }
    }

    public static byte[] createSign(File f, PrivateKey publicKey, boolean salt) throws SignatureViolation {
        try {

            final Signature sig = Signature.getInstance("Sha256WithRSA");
            if (salt) {

                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                sig.initSign(publicKey, sr);
            } else {
                sig.initSign(publicKey);
            }

            InputStream input = null;
            try {
                final byte[] buffer = new byte[1024];
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

        } catch (final Throwable e) {
            throw new SignatureViolation(e);
        }
    }

    /**
     * @param f
     * @param key
     * @return
     * @throws SignatureViolation
     */
    public static byte[] createSign(final File f, final String key) throws SignatureViolation {

        try {
            return AWSign.createSign(f, KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key))));
        } catch (final Throwable e) {
            throw new SignatureViolation(e);
        }
    }

    public static void main(final String[] args) throws NoSuchAlgorithmException {
        AWSign.createKeyPair();
    }

    /**
     * @param decode
     * @param decode2
     * @throws SignatureViolation
     */
    public static void verifyFile(final byte[] dataToVerify, final PublicKey pub, final byte[] signature) throws SignatureViolation {
        try {

            final Signature sig = Signature.getInstance("Sha256WithRSA");
            sig.initVerify(pub);
            sig.update(dataToVerify);
            if (!sig.verify(signature)) {
                new SignatureViolation("Signatur Check Failed");
            }
        } catch (final Throwable e) {

            throw new SignatureViolation(e);
        }
    }

    /**
     * @param f
     * @param pub
     * @throws SignatureViolation
     */
    public static void verifyFile(final File f, final PublicKey pub) throws SignatureViolation {
        try {
            AWSign.verifySignature(f, pub, IO.readFile(new File(f.getAbsolutePath() + ".updateSignature")));
        } catch (final IOException e) {
            throw new SignatureViolation(e);
        }
    }

    public static void verifyFile(final File f, final String pub) throws SignatureViolation {
        try {
            AWSign.verifyFile(f, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(pub))));
        } catch (final InvalidKeySpecException e) {
            throw new SignatureViolation(e);
        } catch (final NoSuchAlgorithmException e) {
            throw new SignatureViolation(e);
        }

    }

    /**
     * @param f
     * @param pub
     * @param bs
     * @throws SignatureViolation
     */
    public static void verifySignature(final File f, final PublicKey pub, final byte[] signature) throws SignatureViolation {
        try {

            final Signature sig = Signature.getInstance("Sha256WithRSA");
            sig.initVerify(pub);
            InputStream input = null;
            try {
                final byte[] buffer = new byte[16384];
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
        } catch (final Throwable e) {

            throw new SignatureViolation(e);
        }
    }

}
