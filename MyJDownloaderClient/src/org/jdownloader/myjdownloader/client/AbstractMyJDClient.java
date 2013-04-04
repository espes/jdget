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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.InvalidResponseCodeException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderAuthException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderInvalidTokenException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderOverloadException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderUnconfirmedAccountException;
import org.jdownloader.myjdownloader.client.json.CaptchaChallenge;
import org.jdownloader.myjdownloader.client.json.ConnectResponse;
import org.jdownloader.myjdownloader.client.json.ErrorResponse;
import org.jdownloader.myjdownloader.client.json.ObjectData;
import org.jdownloader.myjdownloader.client.json.RegisterPayload;
import org.jdownloader.myjdownloader.client.json.RegisterResponse;

public abstract class AbstractMyJDClient {

    public static String byteArrayToHex(final byte[] digest) {
        final StringBuilder ret = new StringBuilder();
        String tmp;
        for (final byte d : digest) {
            tmp = Integer.toHexString(d & 0xFF);
            if (tmp.length() < 2) {
                ret.append('0');
            }
            ret.append(tmp);
        }
        return ret.toString();
    }

    public static byte[] hexToByteArray(final String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] hmac(final byte[] key, final byte[] content) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return sha256_HMAC.doFinal(content);
    }

    private byte[]          localSecret;
    private byte[]          serverSecret;
    private String          serverRoot = "http://api.jdownloader.org";
    private String          username;

    private long            counter;

    private ConnectResponse connectInfo;

    private byte[]          transferCryptoToken;

    public AbstractMyJDClient() {

        counter = System.currentTimeMillis();

    }

    protected abstract byte[] base64decode(String base64encodedString);

    protected abstract String base64Encode(byte[] encryptedBytes);

    private byte[] calcTransferCryptoToken() throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(localSecret);
        md.update(AbstractMyJDClient.hexToByteArray(connectInfo.getToken()));
        return md.digest();
    }

    @SuppressWarnings("unchecked")
    public <T> T callAction(final String action, final Class<T> returnType, final Object... args) throws MyJDownloaderException, APIException {
        return (T) callActionInternal(action, returnType, args);

    }

    protected Object callActionInternal(final String action, final Type returnType, final Object... args) throws MyJDownloaderException, APIException {
        try {
            final String query = "/t_" + connectInfo.getToken() + action;
            final String[] params = new String[args != null ? args.length : 0];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    params[i] = objectToJSon(args[i]);
                }
            }
            final Payload payload = new Payload(action, inc(), params);
            final String json = objectToJSon(payload);
            final String ret = post(query, encrypt(json, transferCryptoToken));
            final String dec = decrypt(ret, transferCryptoToken);

            final ObjectData data = this.jsonToObject(dec, ObjectData.class);

            // ugly!!! but this will be changed when we have a proper remoteAPI response format

            return this.jsonToObject(objectToJSon(data.getData()) + "", returnType);

        } catch (final InvalidResponseCodeException e) {
            handleInvalidResponseCodes(e);
            throw e;
        } catch (final MyJDownloaderException e) {
            throw e;
        } catch (final Exception e) {
            throw APIException.get(e);
        }
    }

    protected <T> T callServer(String query, final String postData, final Class<T> class1) throws MyJDownloaderException {
        try {
            query += query.contains("?") ? "&" : "?";
            query += "timestamp=" + inc();
            final String encrypted = post(query + "&signature=" + sign(serverSecret, query), postData);
            return this.jsonToObject(decrypt(encrypted, serverSecret), class1);
        } catch (final InvalidResponseCodeException e) {
            handleInvalidResponseCodes(e);
            throw e;
        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);
        }
    }

    public void connect(final String email, final String pass) throws MyJDownloaderException, APIException {
        try {
            init(email, pass);
            connectInfo = this.callServer("/my/clientconnect?email=" + username, null, ConnectResponse.class);
            transferCryptoToken = calcTransferCryptoToken();
        } catch (final MyJDownloaderException e) {
            throw e;
        } catch (final Exception e) {
            throw APIException.get(e);
        }
    }

    private byte[] createSecret(final String username, final String password, final String domain) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest((username + password + domain).getBytes("UTF-8"));
    }

    private String decrypt(final String encrypted, final byte[] keyAndIV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyAndIV, 0, 16));
        final SecretKeySpec skeySpec = new SecretKeySpec(Arrays.copyOfRange(keyAndIV, 16, 32), "AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        final byte[] crypted = base64decode(encrypted);
        final byte[] decryptedBytes = cipher.doFinal(crypted);
        return new String(decryptedBytes, "UTF-8");
    }

    // @SuppressWarnings("unchecked")
    // private <T> T jsonToObjectGeneric(String dec, Class<T> clazz) {
    // return (T) jsonToObject(dec, clazz);
    // }

    public void disconnect() throws MyJDownloaderException {

        final String query = "/my/disconnect?clienttoken=" + connectInfo.getToken();

        this.callServer(query, null, ConnectResponse.class);

    }

    protected String encrypt(final String createPayloadString, final byte[] keyAndIV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyAndIV, 0, 16));
        final SecretKeySpec skeySpec = new SecretKeySpec(Arrays.copyOfRange(keyAndIV, 16, 32), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

        final byte[] encryptedBytes = cipher.doFinal(createPayloadString.getBytes("UTF-8"));
        return base64Encode(encryptedBytes);
    }

    /**
     * Downloads a CaptchaChallenge from the server
     * 
     * @return
     * @throws MyJDownloaderException
     */
    public CaptchaChallenge getChallenge() throws MyJDownloaderException {
        return this.jsonToObject(post("/captcha/getCaptcha", ""), CaptchaChallenge.class);
    }

    public String getServerRoot() {
        return serverRoot;
    }

    protected void handleInvalidResponseCodes(final InvalidResponseCodeException e) throws MyJDownloaderAuthException, MyJDownloaderOverloadException, MyJDownloaderUnconfirmedAccountException, MyJDownloaderInvalidTokenException {
        if (e != null && e.getContent() != null && e.getContent().trim().length() != 0) {
            final ErrorResponse error = this.jsonToObject(e.getContent(), ErrorResponse.class);
            switch (error.getSrc()) {

            case DEVICE:

                break;

            case MYJD:
                switch (error.getType()) {
                case AUTH_FAILED:
                    throw new MyJDownloaderAuthException();
                case ERROR_EMAIL_NOT_CONFIRMED:
                    throw new MyJDownloaderUnconfirmedAccountException();
                case OFFLINE:
                    throw new RuntimeException("Not Implemented: offline");
                case TOKEN_INVALID:
                    throw new MyJDownloaderInvalidTokenException();
                case UNKNOWN:
                    throw new RuntimeException("Not Implemented: unkown");

                }
                break;

            }

        }

        switch (e.getResponseCode()) {
        case 403:
            throw new MyJDownloaderAuthException();
        case 503:
            throw new MyJDownloaderOverloadException();
        case 401:
            throw new MyJDownloaderUnconfirmedAccountException();
        case 407:
            throw new MyJDownloaderInvalidTokenException();

        }
    }

    private long inc() {
        return counter++;
    }

    private void init(final String username, final String password) {
        this.username = username;
        try {
            localSecret = createSecret(username, password, "jd");

            serverSecret = createSecret(username, password, "server");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract <T> T jsonToObject(String dec, Type clazz);

    @SuppressWarnings("unchecked")
    public <T> T link(final Class<T> class1, final String namespace) {

        return (T) Proxy.newProxyInstance(class1.getClassLoader(), new Class<?>[] { class1 }, new InvocationHandler() {

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                try {
                    final String action = "/" + namespace + "/" + method.getName();
                    final Type returnType = method.getGenericReturnType();
                    return AbstractMyJDClient.this.callActionInternal(action, returnType, args);

                } catch (final Throwable e) {
                    throw new RuntimeException(e);
                }

            }

        });
    }

    protected abstract String objectToJSon(Object payload);

    abstract protected String post(String query, String object) throws MyJDownloaderException;

    /**
     * register for a new MyJDownloader Account. If there is a registration problem, this method throws an MyJDownloaderException
     * 
     * @see #getChallenge()
     * @see #requestConfirmationEmail(CaptchaChallenge);
     * @param email
     * @param pass
     * @param challenge
     * @throws APIException
     * @throws MyJDownloaderException
     */
    public void register(final String email, final String pass, final CaptchaChallenge challenge) throws MyJDownloaderException {
        init(email, pass);

        final String encrypted = post("/my/register", objectToJSon(new RegisterPayload(email, AbstractMyJDClient.byteArrayToHex(serverSecret), challenge.getCaptchaChallenge(), challenge.getCaptchaResponse())));
        final RegisterResponse ret = this.jsonToObject(encrypted, RegisterResponse.class);
  System.out.println(ret);

    }

    public void requestConfirmationEmail(final CaptchaChallenge challenge) {
        System.out.println(1);
    }

    public void setServerRoot(final String serverRoot) {
        this.serverRoot = serverRoot;
    }

    private String sign(final byte[] key, final String data) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        return AbstractMyJDClient.byteArrayToHex(AbstractMyJDClient.hmac(key, data.getBytes("UTF-8")));
    }

}
