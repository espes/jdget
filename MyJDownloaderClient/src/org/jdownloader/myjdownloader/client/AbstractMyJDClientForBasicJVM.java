package org.jdownloader.myjdownloader.client;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.bindings.interfaces.Linkable;
import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public abstract class AbstractMyJDClientForBasicJVM extends AbstractMyJDClient<Type> {
    private static AtomicLong RID_COUNTER = new AtomicLong(System.currentTimeMillis());

    @Override
    protected long getUniqueRID() {
        return RID_COUNTER.incrementAndGet();
    }

    public <T> T callAction(final String deviceID, final String action, final Class<T> returnType, final Object... args) throws MyJDownloaderException, APIException {
        return (T) super.callAction(deviceID, action, returnType, args);
    }

    @Override
    protected byte[] updateEncryptionToken(final byte[] oldSecret, final byte[] update) throws MyJDownloaderException {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(oldSecret);
            md.update(update);
            return md.digest();
        } catch (final NoSuchAlgorithmException e) {
            throw MyJDownloaderException.get(e);

        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Link an API INterface  and call methods directly
     * @param class1
     * @param namespace
     * @return
     */
    public <T extends Linkable> T link(final Class<T> class1, final String namespace, final String deviceID) {

        return (T) Proxy.newProxyInstance(class1.getClassLoader(), new Class<?>[] { class1 }, new InvocationHandler() {

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                try {
                    final String action = "/" + namespace + "/" + method.getName();
                    final Type returnType = method.getGenericReturnType();
                    return callAction(deviceID, action, returnType, args);

                } catch (final Throwable e) {
                    final Class<?>[] exceptions = method.getExceptionTypes();
                    if (exceptions != null) {
                        for (final Class<?> c : exceptions) {
                            if (c.isAssignableFrom(e.getClass())) { throw e; }

                        }
                    }
                    throw new RuntimeException(e);
                }

            }

        });
    }

    public <T extends Linkable> T link(final Class<T> class1, final String deviceID) {
        final ClientApiNameSpace ann = class1.getAnnotation(ClientApiNameSpace.class);
        if (ann == null) { throw new NullPointerException("ApiNameSpace missing in " + class1.getName()); }

        return link(class1, ann.value(), deviceID);
    }

    public AbstractMyJDClientForBasicJVM(final String appKey) {
        super(appKey);

    }

    @Override
    protected byte[] decrypt(final byte[] crypted, final byte[] keyAndIV) throws MyJDownloaderException {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final byte[] iv = new byte[16];
            final byte[] key = new byte[16];
            System.arraycopy(keyAndIV, 0, iv, 0, 16);
            System.arraycopy(keyAndIV, 16, key, 0, 16);
            final IvParameterSpec ivSpec = new IvParameterSpec(iv);
            final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            return cipher.doFinal(crypted);
        } catch (final NoSuchAlgorithmException e) {
            throw MyJDownloaderException.get(e);

        } catch (final NoSuchPaddingException e) {
            throw MyJDownloaderException.get(e);

        } catch (final InvalidKeyException e) {
            throw MyJDownloaderException.get(e);

        } catch (final InvalidAlgorithmParameterException e) {
            throw MyJDownloaderException.get(e);

        } catch (final IllegalBlockSizeException e) {
            throw MyJDownloaderException.get(e);

        } catch (final BadPaddingException e) {
            throw MyJDownloaderException.get(e);

        }
    }

    @Override
    protected byte[] createSecret(final String username, final String password, final String domain) throws MyJDownloaderException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            return md.digest((username.toLowerCase(Locale.ENGLISH) + password + domain.toLowerCase(Locale.ENGLISH)).getBytes("UTF-8"));
        } catch (final NoSuchAlgorithmException e) {
            throw MyJDownloaderException.get(e);

        } catch (final UnsupportedEncodingException e) {
            throw MyJDownloaderException.get(e);

        }
    }

    @Override
    protected byte[] encrypt(final byte[] data, final byte[] keyAndIV) throws MyJDownloaderException {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyAndIV, 0, 16));
            final SecretKeySpec skeySpec = new SecretKeySpec(Arrays.copyOfRange(keyAndIV, 16, 32), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

            return cipher.doFinal(data);

        } catch (final NoSuchAlgorithmException e) {
            throw MyJDownloaderException.get(e);

        } catch (final NoSuchPaddingException e) {
            throw MyJDownloaderException.get(e);

        } catch (final InvalidKeyException e) {
            throw MyJDownloaderException.get(e);

        } catch (final InvalidAlgorithmParameterException e) {
            throw MyJDownloaderException.get(e);

        } catch (final IllegalBlockSizeException e) {
            throw MyJDownloaderException.get(e);

        } catch (final BadPaddingException e) {
            throw MyJDownloaderException.get(e);

        }
    }

    /**
     * Calculates a HmacSHA256 of content with the key
     * 
     * @param key
     * @param content
     * @return
     * @throws MyJDownloaderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    @Override
    protected byte[] hmac(final byte[] key, final byte[] content) throws MyJDownloaderException {
        try {

            final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return sha256_HMAC.doFinal(content);

        } catch (final NoSuchAlgorithmException e) {
            throw MyJDownloaderException.get(e);

        } catch (final InvalidKeyException e) {
            throw MyJDownloaderException.get(e);

        }
    }

}
