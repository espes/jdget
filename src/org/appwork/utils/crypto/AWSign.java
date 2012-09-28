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
        return createSign(f, publicKey, false, null);
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
                byte[] seedbytes = new byte[1024];
                sr.nextBytes(seedbytes);
                System.out.println(new String(seedbytes));
                // sr.setSeed(System.currentTimeMillis());
                // sr.setSeed(sr.generateSeed(1024));
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

    public static byte[] createSign(File f, PrivateKey publicKey, boolean salt, byte[] addInfo) throws SignatureViolation {
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
                if (addInfo != null) {
                    sig.update(addInfo, 0, addInfo.length);
                }
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
        // AWSign.createKeyPair();

        // PUBLIC
        try {
            PublicKey pub = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg+EQ1wHD62QGjzJalAkl1WjExeS345ZkCMtuyvqP3NLVpUbfZjc/IeHVi9qKBUPtV8ca8QfOZo8ACNIBvUxEiVy4YFE7vqZfBNV0uEz/kHSXxDlFeiv0+BFMgcXow0NYBjGDT02/1ddmjEMtnAXnjqUwlVPorzOmJoeuNSLCyCcOe0pKuF1yDha9TkEsaUcJ8kho+09kQvhMl5mKnuTUc81nIHHVb4GClRmFp1kfB9BbqPc9sL5jg1BrmjHMCD84HZk4OehxJ8AeA+veVRH2Gn6gcslPcrgNw1zK6VcXzCqsuZCAejAyDHnX+jay1SaxmHDgk5jc+agee+M2+QPiwQIDAQAB")));
            PrivateKey pk = KeyFactory
                    .getInstance("RSA")
                    .generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    Base64.decode("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCD4RDXAcPrZAaPMlqUCSXVaMTF5LfjlmQIy27K+o/c0tWlRt9mNz8h4dWL2ooFQ+1XxxrxB85mjwAI0gG9TESJXLhgUTu+pl8E1XS4TP+QdJfEOUV6K/T4EUyBxejDQ1gGMYNPTb/V12aMQy2cBeeOpTCVU+ivM6Ymh641IsLIJw57Skq4XXIOFr1OQSxpRwnySGj7T2RC+EyXmYqe5NRzzWcgcdVvgYKVGYWnWR8H0Fuo9z2wvmODUGuaMcwIPzgdmTg56HEnwB4D695VEfYafqByyU9yuA3DXMrpVxfMKqy5kIB6MDIMedf6NrLVJrGYcOCTmNz5qB574zb5A+LBAgMBAAECggEAWLi/lYZQgjoG16tumI0W8N3NE71toST6I5iI7vFme48zwD9P5/pe9LJz8eSSWjx6nkUK8QDpcMHfqg9usCVxLmA8gj/kS7ytzBi2r47NmCd4OsC05x5PbdxldiDpGQRjYbdJub56wqhpCw/ezUqDn8muR6ftsIC01NMO9hxuoiv1tE1GXZwBo36YSPb5NsB0Og5p0w8gwogXo0/TLIOJVy5ysZGACrXMaSN7DX/XP5hp4rXEfbY9vQdegVShejIKOIc9r5+0btRPjGP7YkMRWvTQQt43jWgI8cBIFUcZ4fYmwegzGnl1OONVzXjum10B2E3R2vmDEZqrLVB22I5Y4QKBgQDq0W0/g9H7hyd0NroZP8yP0/bOZO6ZYzUNMRQgZ/BO76yMUId6+wi0RREzqya+r/ur+kDXCs+liTlbJ+KyjRv29ls40eDW5OpCG9ccFguzg1CUpyIRu2obKC5i59x3I4KGiUplumKcSE8QILD09DslvoSf2pHBIQKZNEdVMROObQKBgQCPxoGORYshbsptqZ5batMTWAeb5xeBn6rxBNDWAzeD+qXazOsTWYgU4310nq/Vqyc7UU18VPoRTTflUyhJFoFxJRjTEHxa/hKjIOGPYayCK2EHrMXHoSxZsUvSbSH1Y84zFAbDcRPylXg1pGnn5CyDB5jijS6mQxnT94TRgX1hJQKBgFwzcUcgNmIiFn7OQlJJt8O9wcoW3Y0C5EDSxYlX5obIGyNZN2k1ipxmBjQYfvUe2p4TfEQzrYbdE9VUGvJq79EPuI/d8P/QEJ92mQchLOUGqaxE197IjQguxc/2JJ3vJoA3Bixde/zLc6fsfi8getz+Ksstok+H66JGYb/0ri4dAoGAFnZeAVtOHGAR0kZAzmmHJquHLM1S99Z5P4SQGA+SmdUMGn4PcAt53kGYdSLht9EwpOzT3UvtccyNog926MxSVtoD4d3ef9zYDpJxixQofoHGfAt7LvA4XJ79iJeySYNZUNOdJuXAxxKhIEhan3cfmS0Trrl+A03SeDJgltbTPt0CgYEA1uPP5gpL029gtx3shiQFblpVl3AhUE1dmDITJYrGqD+06Z+nPHu73kOnVdPKgy9wYIIxcyx/DrQfcT5e1+IZy9bZ5OOIUVi9qNsQ1RhvFzEwo8tiE/1LX7XUIC2gIjyY0Q+VXLk03UgjV7qAgOg4X/foetGZn2NHmc4NUUaCoNE=")));
            byte[] sign = createSign("Apfelbaum".getBytes(), pk, true);

            System.out.println(Base64.encodeToString(sign, false));
            verifyFile(sign, pub, sign);
            System.out.println("OK");
        } catch (SignatureViolation e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
     * @param additionalBytes
     *            TODO
     * @throws SignatureViolation
     */
    public static void verifyFile(final File f, final PublicKey pub, byte[] additionalBytes) throws SignatureViolation {
        try {
            AWSign.verifySignature(f, pub, IO.readFile(new File(f.getAbsolutePath() + ".updateSignature")), additionalBytes);
        } catch (final IOException e) {
            throw new SignatureViolation(e);
        }
    }

    public static void verifyFile(final File f, final String pub) throws SignatureViolation {
        try {
            AWSign.verifyFile(f, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(pub))), null);
        } catch (final InvalidKeySpecException e) {
            throw new SignatureViolation(e);
        } catch (final NoSuchAlgorithmException e) {
            throw new SignatureViolation(e);
        }

    }

    /**
     * @param f
     * @param pub
     * @param additionalBytes
     *            TODO
     * @param bs
     * @throws SignatureViolation
     */
    public static void verifySignature(final File f, final PublicKey pub, final byte[] signature, byte[] additionalBytes) throws SignatureViolation {
        try {

            final Signature sig = Signature.getInstance("Sha256WithRSA");
            sig.initVerify(pub);
            InputStream input = null;
            try {
                final byte[] buffer = new byte[16384];
                int len;
                input = f.toURI().toURL().openStream();
                if (additionalBytes != null) {
                    sig.update(additionalBytes, 0, additionalBytes.length);
                }
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
