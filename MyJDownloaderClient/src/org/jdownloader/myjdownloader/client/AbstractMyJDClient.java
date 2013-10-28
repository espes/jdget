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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.AuthException;
import org.jdownloader.myjdownloader.client.exceptions.ChallengeFailedException;
import org.jdownloader.myjdownloader.client.exceptions.DeviceIsOfflineException;
import org.jdownloader.myjdownloader.client.exceptions.EmailInvalidException;
import org.jdownloader.myjdownloader.client.exceptions.EmailNotAllowedException;
import org.jdownloader.myjdownloader.client.exceptions.EmailNotValidatedException;
import org.jdownloader.myjdownloader.client.exceptions.ExceptionResponse;
import org.jdownloader.myjdownloader.client.exceptions.MaintenanceException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.exceptions.OutdatedException;
import org.jdownloader.myjdownloader.client.exceptions.OverloadException;
import org.jdownloader.myjdownloader.client.exceptions.TokenException;
import org.jdownloader.myjdownloader.client.exceptions.TooManyRequestsException;
import org.jdownloader.myjdownloader.client.exceptions.UnconnectedException;
import org.jdownloader.myjdownloader.client.exceptions.UnexpectedIOException;
import org.jdownloader.myjdownloader.client.json.CaptchaChallenge;
import org.jdownloader.myjdownloader.client.json.ConnectResponse;
import org.jdownloader.myjdownloader.client.json.DeviceConnectResponse;
import org.jdownloader.myjdownloader.client.json.DeviceData;
import org.jdownloader.myjdownloader.client.json.DeviceList;
import org.jdownloader.myjdownloader.client.json.ErrorResponse;
import org.jdownloader.myjdownloader.client.json.FeedbackResponse;
import org.jdownloader.myjdownloader.client.json.JSonRequest;
import org.jdownloader.myjdownloader.client.json.NotificationRequestMessage;
import org.jdownloader.myjdownloader.client.json.NotificationRequestTypesResponse;
import org.jdownloader.myjdownloader.client.json.ObjectData;
import org.jdownloader.myjdownloader.client.json.RequestIDOnly;
import org.jdownloader.myjdownloader.client.json.RequestIDValidator;
import org.jdownloader.myjdownloader.client.json.ServerErrorType;
import org.jdownloader.myjdownloader.client.json.SuccessfulResponse;

public abstract class AbstractMyJDClient {
    /**
     * Transforms a byte array into a hex encoded String representation
     * 
     * @param digest
     * @return
     */
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

    /**
     * Converts a Hex String into a byte array
     * 
     * @param s
     * @return
     */
    public static byte[] hexToByteArray(final String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Calculates a HmacSHA256 of content with the key
     * 
     * @param key
     * @param content
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static byte[] hmac(final byte[] key, final byte[] content) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return sha256_HMAC.doFinal(content);
    }

    private String       serverRoot         = "http://api.jdownloader.org";

    private String       email;
    private long         counter;

    private SessionInfo  currentSessionInfo = null;
    private final String appKey;

    /**
     * Create a New API.
     * 
     * @param appKey
     *            - write to e-mail@appwork.org to get an own appKey.
     */
    public AbstractMyJDClient(final String appKey) {
        this.appKey = appKey;
        this.counter = System.currentTimeMillis();
    }

    /**
     * convert a base64 string in a byte array
     * 
     * @param base64encodedString
     * @return
     */
    protected abstract byte[] base64decode(String base64encodedString);

    /**
     * Convert a byte array in a base64 String
     * 
     * @param encryptedBytes
     * @return
     */
    protected abstract String base64Encode(byte[] encryptedBytes);

    public DeviceData bindDevice(final DeviceData device) throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/my/binddevice?sessiontoken=" + this.urlencode(session.getSessionToken()) + "&deviceID=" + this.urlencode(device.getId()) + "&type=" + this.urlencode(device.getType()) + "&name=" + this.urlencode(device.getName());
        final DeviceConnectResponse ret = this.callServer(query, null, session.getServerEncryptionToken(), DeviceConnectResponse.class);
        device.setId(ret.getDeviceid());
        return device;
    }

    @SuppressWarnings("unchecked")
    /**
     * Calls a API function
     * @param action
     * @param returnType
     * @param args
     * @return
     * @throws MyJDownloaderException
     * @throws APIException
     */
    public synchronized <T> T callAction(final String deviceID, final String action, final Class<T> returnType, final Object... args) throws MyJDownloaderException, APIException {
        return (T) this.callActionInternal(deviceID, action, returnType, args);

    }

    protected synchronized Object callActionInternal(final String deviceID, final String action, final Type returnType, final Object... args) throws MyJDownloaderException, APIException {
        try {
            final SessionInfo session = this.getSessionInfo();
            final String query = "/t_" + session.getSessionToken() + "_" + this.urlencode(deviceID) + action;
            final String[] params = new String[args != null ? args.length : 0];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    params[i] = this.objectToJSon(args[i]);
                }
            }
            final JSonRequest payload = new JSonRequest();
            payload.setUrl(action);
            payload.setRid(this.inc());
            payload.setParams(params);
            final String json = this.objectToJSon(payload);
            final String dec = this.cryptedPost(query, this.base64Encode(this.encrypt(json.getBytes("UTF-8"), session.getDeviceEncryptionToken())), session.getDeviceEncryptionToken());
            final ObjectData data = this.jsonToObject(dec, ObjectData.class);
            if (data == null) {
                // invalid response
                throw new MyJDownloaderException("Invalid Response: " + dec);
            }
            // ugly!!! but this will be changed when we have a proper remoteAPI response format

            return this.jsonToObject(this.objectToJSon(data.getData()) + "", returnType);

        } catch (final ExceptionResponse e) {
            this.handleInvalidResponseCodes(e);
            throw e;
        } catch (final MyJDownloaderException e) {
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            throw APIException.get(e);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * to a call to the MyJdownloader Server.
     * @param query The query String
     * @param postData Post Data - can be null
     * @param key The encryptionkey. This is either #serverEncryptionToken or loginSecret
     * @param class1 The return Type
     * @return
     * @throws MyJDownloaderException
     */
    protected synchronized <T> T callServer(String query, final String postData, final byte[] key, final Class<T> class1) throws MyJDownloaderException {
        try {
            query += query.contains("?") ? "&" : "?";
            final long i = this.inc();
            query += "rid=" + i;
            final String retString = this.cryptedPost(query + "&signature=" + this.sign(key, query), postData, key);
            final Object ret = this.jsonToObject(retString, class1);
            System.out.println(this.objectToJSon(ret));
            if (ret instanceof RequestIDValidator) {
                if (((RequestIDValidator) ret).getRid() != i) { throw new BadResponseException("RID Mismatch"); }
            }
            return (T) ret;
        } catch (final ExceptionResponse e) {
            try {
                this.handleInvalidResponseCodes(e);
            } catch (final APIException e1) {
                // actually not possible.
                throw new RuntimeException(e);

            }
            throw e;
        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);
        }
    }

    public synchronized void cancelRegistrationEmail(final String email, final String key) throws MyJDownloaderException {
        try {
            this.uncryptedPost("/my/cancelregistrationemail?email=" + this.urlencode(email) + "&key=" + this.urlencode(key));
        } catch (final APIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a new Session. Do never store email and password in your application. throw away the password after connect and work with #getSessionInfo
     * #setSessionInfo and #reconnect to restore a session
     * 
     * @param email
     * @param password
     * @throws MyJDownloaderException
     */
    public synchronized void connect(final String email, final String password) throws MyJDownloaderException {
        try {
            this.email = email;

            // localSecret = createSecret(username, password, "jd");
            final byte[] loginSecret = this.createSecret(email, password, "server");
            final byte[] deviceSecret = this.createSecret(email, password, "device");
            final long rid = this.inc();
            final StringBuilder query = new StringBuilder().append("/my/connect?email=").append(this.urlencode(email)).append("&appkey=").append(this.urlencode(this.appKey)).append("&rid=").append(rid);

            final String signature = this.sign(loginSecret, query.toString());
            query.append("&signature=").append(this.urlencode(signature));

            final String retString = this.cryptedPost(query.toString(), "", loginSecret);
            final ConnectResponse ret = this.jsonToObject(retString, ConnectResponse.class);
            if (ret.getRid() != rid) { throw new BadResponseException("RID Mismatch"); }

            final byte[] serverEncryptionToken = this.updateEncryptionToken(loginSecret, AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final byte[] deviceEncryptionToken = this.updateEncryptionToken(deviceSecret, AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final String sessionToken = ret.getSessiontoken();
            final String regainToken = ret.getRegaintoken();
            final SessionInfo newSessionInfo = new SessionInfo(deviceSecret, serverEncryptionToken, deviceEncryptionToken, sessionToken, regainToken);
            this.currentSessionInfo = newSessionInfo;
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);

        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);

        } catch (final InvalidKeyException e) {
            throw new RuntimeException(e);

        } catch (final APIException e) {
            throw new RuntimeException(e);

        }

    }

    private byte[] createSecret(final String username, final String password, final String domain) throws BadResponseException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            return md.digest((username.toLowerCase(Locale.ENGLISH) + password + domain.toLowerCase(Locale.ENGLISH)).getBytes("UTF-8"));
        } catch (final NoSuchAlgorithmException e) {
            throw new BadResponseException("Secret Creation Failed", e);

        } catch (final UnsupportedEncodingException e) {
            throw new BadResponseException("Secret Creation Failed", e);

        }
    }

    private synchronized String cryptedPost(final String url, final String objectToJSon, final byte[] keyAndIV) throws MyJDownloaderException, APIException {
        try {
            return this.post(url, objectToJSon, keyAndIV);
        } catch (final ExceptionResponse e) {
            this.handleInvalidResponseCodes(e);
            throw e;
        }

    }

    protected byte[] decrypt(final byte[] crypted, final byte[] keyAndIV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyAndIV, 0, 16));
        final SecretKeySpec skeySpec = new SecretKeySpec(Arrays.copyOfRange(keyAndIV, 16, 32), "AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        return cipher.doFinal(crypted);

    }

    // @SuppressWarnings("unchecked")
    // private <T> T jsonToObjectGeneric(String dec, Class<T> clazz) {
    // return (T) jsonToObject(dec, clazz);
    // }
    /**
     * Disconnect. This will invalidate your session. You have to call #connect to get a new session afterwards
     * 
     * @throws MyJDownloaderException
     */
    public void disconnect() throws MyJDownloaderException {
        try {
            final SessionInfo session = this.getSessionInfo();
            final String query = "/my/disconnect?sessiontoken=" + this.urlencode(session.getSessionToken());
            this.callServer(query, null, session.getServerEncryptionToken(), RequestIDOnly.class);
        } finally {
            this.currentSessionInfo = null;
        }
    }

    protected byte[] encrypt(final byte[] data, final byte[] keyAndIV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyAndIV, 0, 16));
        final SecretKeySpec skeySpec = new SecretKeySpec(Arrays.copyOfRange(keyAndIV, 16, 32), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

        return cipher.doFinal(data);
    }

    public synchronized String feedback(final String message) throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final JSonRequest re = new JSonRequest();
        re.setRid(this.inc());
        re.setParams(new Object[] { message });
        final String url = "/my/feedback?sessiontoken=" + this.urlencode(session.getSessionToken());
        re.setUrl(url);
        final FeedbackResponse ret = this.callServer(url, this.objectToJSon(re), session.getServerEncryptionToken(), FeedbackResponse.class);
        return ret.getFeedbackID();
    }

    /**
     * Change your password.
     * 
     * @param newPassword
     * @param oldPassword
     * @param key
     * @param string
     * @throws MyJDownloaderException
     */
    public synchronized void finishPasswordReset(final String email, final String key, final String newPassword) throws MyJDownloaderException {

        try {
            final byte[] k = AbstractMyJDClient.hexToByteArray(key);
            if (k.length != 32) { throw new IllegalArgumentException("Bad Key. Expected: 64 hexchars"); }
            final byte[] newLoginSecret = this.createSecret(email, newPassword, "server");
            final String encryptedNewSecret = AbstractMyJDClient.byteArrayToHex(this.encrypt(newLoginSecret, k));
            this.callServer("/my/finishpasswordreset?email=" + this.urlencode(email) + "&encryptedLoginSecret=" + encryptedNewSecret, null, k, RequestIDOnly.class);
            this.connect(email, newPassword);
        } catch (final InvalidKeyException e) {
            throw new RuntimeException(e);

        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);

        } catch (final NoSuchPaddingException e) {
            throw new RuntimeException(e);

        } catch (final InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);

        } catch (final IllegalBlockSizeException e) {
            throw new RuntimeException(e);

        } catch (final BadPaddingException e) {
            throw new RuntimeException(e);

        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Confirm your email by sending the Confirm Key.
     * 
     * @param key
     * @param email
     * @param password
     * @throws MyJDownloaderException
     */
    public synchronized void finishRegistration(final String key, final String email, final String password) throws MyJDownloaderException {

        try {
            final byte[] k = AbstractMyJDClient.hexToByteArray(key);
            if (k.length != 32) { throw new IllegalArgumentException("Bad Key. Expected: 64 hexchars"); }
            final byte[] loginSecret = this.createSecret(email, password, "server");
            final String pw = AbstractMyJDClient.byteArrayToHex(this.encrypt(loginSecret, k));
            this.callServer("/my/finishregistration?email=" + this.urlencode(email) + "&loginsecret=" + this.urlencode(pw), null, k, RequestIDOnly.class);

        } catch (final InvalidKeyException e) {
            throw new RuntimeException(e);

        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);

        } catch (final NoSuchPaddingException e) {
            throw new RuntimeException(e);

        } catch (final InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);

        } catch (final IllegalBlockSizeException e) {
            throw new RuntimeException(e);

        } catch (final BadPaddingException e) {
            throw new RuntimeException(e);

        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * Downloads a CaptchaChallenge from the server
     * 
     * @return
     * @throws MyJDownloaderException
     */
    public synchronized CaptchaChallenge getChallenge() throws MyJDownloaderException {
        try {
            return this.jsonToObject(this.uncryptedPost("/captcha/getCaptcha", (Object[]) null), CaptchaChallenge.class);
        } catch (final APIException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Can be used to calculate a foreign device encryption token based on the remote session token
     * 
     * @param sessionToken
     * @return
     * @throws NoSuchAlgorithmException
     * @throws MyJDownloaderException
     */
    public byte[] getDeviceEncryptionTokenBySession(final String sessionToken) throws NoSuchAlgorithmException, MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        return this.updateEncryptionToken(session.getDeviceSecret(), AbstractMyJDClient.hexToByteArray(sessionToken));

    }

    public String getServerRoot() {
        return this.serverRoot;
    }

    /**
     * Get The current Session Info Object. You can store it to reconnect to the same session later
     * 
     * @return
     */
    public SessionInfo getSessionInfo() throws MyJDownloaderException {
        final SessionInfo ret = this.currentSessionInfo;
        if (ret == null) { throw new UnconnectedException(); }
        return ret;
    }

    protected void handleInvalidResponseCodes(final ExceptionResponse e) throws MyJDownloaderException, APIException {
        if (e != null && e.getContent() != null && e.getContent().trim().length() != 0) {
            ErrorResponse error = null;
            try {
                error = this.jsonToObject(e.getContent(), ErrorResponse.class);
                switch (error.getSrc()) {
                case DEVICE:
                    throw new APIException(error.getType(), error.getData());
                case MYJD:
                    final ServerErrorType type = ServerErrorType.valueOf(error.getType());
                    switch (type) {
                    case AUTH_FAILED:
                        throw new AuthException();
                    case ERROR_EMAIL_NOT_CONFIRMED:
                        throw new EmailNotValidatedException();
                    case OUTDATED:
                        throw new OutdatedException();
                    case OFFLINE:
                        throw new DeviceIsOfflineException();
                    case TOKEN_INVALID:
                        throw new TokenException();
                    case UNKNOWN:
                        throw new RuntimeException("Not Implemented: unkown");
                    case CHALLENGE_FAILED:
                        throw new ChallengeFailedException();
                    case EMAIL_FORBIDDEN:
                        throw new EmailNotAllowedException();
                    case EMAIL_INVALID:
                        throw new EmailInvalidException();
                    case OVERLOAD:
                        throw new OverloadException();
                    case TOO_MANY_REQUESTS:
                        throw new TooManyRequestsException();
                    case MAINTENANCE:
                        throw new MaintenanceException();
                    }
                    break;
                }
            } catch (final MyJDownloaderException e1) {
                e1.setSource(error.getSrc());
                throw e1;
            } catch (final Exception e2) {
                throw new UnexpectedIOException(e2);
            }
        }
        switch (e.getResponseCode()) {
        case 403:
            throw new AuthException();
        case 503:
            throw new OverloadException();
        case 401:
            throw new EmailNotValidatedException();
        case 407:
            throw new TokenException();
        default:
            throw new UnexpectedIOException(e);
        }
    }

    protected long inc() {
        return this.counter++;
    }

    protected abstract <T> T jsonToObject(String dec, Type clazz);

    public void keepalive() throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/my/keepalive?sessiontoken=" + this.urlencode(session.getSessionToken());
        this.callServer(query, null, session.getServerEncryptionToken(), RequestIDOnly.class);
    }

    @SuppressWarnings("unchecked")
    /**
     * Link an API INterface  and call methods directly
     * @param class1
     * @param namespace
     * @return
     */
    public <T> T link(final Class<T> class1, final String namespace, final String deviceID) {

        return (T) Proxy.newProxyInstance(class1.getClassLoader(), new Class<?>[] { class1 }, new InvocationHandler() {

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                try {
                    final String action = "/" + namespace + "/" + method.getName();
                    final Type returnType = method.getGenericReturnType();
                    return AbstractMyJDClient.this.callActionInternal(deviceID, action, returnType, args);

                } catch (final Throwable e) {
                    throw new RuntimeException(e);
                }

            }

        });
    }

    public DeviceList listDevices() throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/my/listdevices?sessiontoken=" + this.urlencode(session.getSessionToken());
        final DeviceList ret = this.callServer(query, null, session.getServerEncryptionToken(), DeviceList.class);
        return ret;
    }

    public NotificationRequestMessage.TYPE[] listrequesteddevicesnotifications() throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/notify/list?sessiontoken=" + this.urlencode(session.getSessionToken());
        final NotificationRequestTypesResponse ret = this.callServer(query, null, session.getServerEncryptionToken(), NotificationRequestTypesResponse.class);
        return ret.getTypes();
    }

    protected abstract String objectToJSon(Object payload);

    abstract protected String post(String query, String object, byte[] keyAndIV) throws ExceptionResponse;

    public boolean pushNotification(final NotificationRequestMessage message) throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/notify/push?sessiontoken=" + this.urlencode(session.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setRid(this.inc());
        re.setParams(new Object[] { message });
        re.setUrl(query);
        return this.callServer(query, this.objectToJSon(re), session.getServerEncryptionToken(), SuccessfulResponse.class).isSuccessful();
    }

    /**
     * If the Session becomes invalid(for example due to an ip change), you need to reconnect. The user does NOT have to reenter his logins. We use a regain
     * token to get a new session. Short: If you get a #TokenException, call reconnect to refresh your session.
     * 
     * @throws MyJDownloaderException
     */
    public synchronized void reconnect() throws MyJDownloaderException {
        try {
            final SessionInfo session = this.getSessionInfo();
            final String query = "/my/reconnect?sessiontoken=" + this.urlencode(session.getSessionToken()) + "&regaintoken=" + this.urlencode(session.getRegainToken());
            final ConnectResponse ret = this.callServer(query, null, session.getServerEncryptionToken(), ConnectResponse.class);

            final byte[] serverEncryptionToken = this.updateEncryptionToken(session.getServerEncryptionToken(), AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final byte[] deviceEncryptionToken = this.updateEncryptionToken(session.getDeviceSecret(), AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final String sessionToken = ret.getSessiontoken();
            final String regainToken = ret.getRegaintoken();
            final SessionInfo newSessionInfo = new SessionInfo(session.getDeviceSecret(), serverEncryptionToken, deviceEncryptionToken, sessionToken, regainToken);
            this.currentSessionInfo = newSessionInfo;
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);

        }
    }

    public void registerNotification(final String receiverID, final DeviceData device, final NotificationRequestMessage.TYPE... types) throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/notify/register?sessiontoken=" + this.urlencode(session.getSessionToken()) + "&receiverid=" + this.urlencode(receiverID) + "&deviceid=" + this.urlencode(device.getId());
        final JSonRequest re = new JSonRequest();
        re.setRid(this.inc());
        if (types == null || types.length == 0) {
            re.setParams(new Object[] { new NotificationRequestMessage.TYPE[] {} });
        } else {
            re.setParams(new Object[] { types });
        }
        re.setUrl(query);
        final String object = this.objectToJSon(re);
        this.callServer(query, object, session.getServerEncryptionToken(), RequestIDOnly.class);
    }

    /**
     * Call this method to request a password change. You will get an email containing a key. Use this key together with
     * {@link #changePassword(String, String, String)}
     * 
     * @throws MyJDownloaderException
     */
    public synchronized void requestPasswordResetEmail(final CaptchaChallenge challenge, final String email) throws MyJDownloaderException {
        try {
            this.uncryptedPost("/my/requestpasswordresetemail?email=" + this.urlencode(email) + "&captchaResponse=" + this.urlencode(challenge.getCaptchaResponse()) + "&captchaChallenge=" + this.urlencode(challenge.getCaptchaChallenge()));
        } catch (final APIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register for a new MyJDownloader Account. If there is a registration problem, this method throws an MyJDownloaderException
     * 
     * @see #getChallenge()
     * @see #requestConfirmationEmail(CaptchaChallenge);
     * @param email
     * @param pass
     * @param challenge
     * @throws APIException
     * @throws MyJDownloaderException
     */
    public synchronized void requestRegistrationEmail(final CaptchaChallenge challenge, final String email, final String referer) throws MyJDownloaderException {
        try {
            this.uncryptedPost("/my/requestregistrationemail?email=" + this.urlencode(email) + "&captchaResponse=" + this.urlencode(challenge.getCaptchaResponse()) + "&captchaChallenge=" + this.urlencode(challenge.getCaptchaChallenge()) + "&referer=" + this.urlencode(referer == null ? this.appKey : referer));
        } catch (final APIException e) {
            throw new RuntimeException(e);
        }
    }

    public void setServerRoot(final String serverRoot) {
        this.serverRoot = serverRoot;
    }

    /**
     * set old sessioninfo.
     * 
     * @param info
     */
    public void setSessionInfo(final SessionInfo info) {
        this.currentSessionInfo = info;
    }

    private String sign(final byte[] key, final String data) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        return AbstractMyJDClient.byteArrayToHex(AbstractMyJDClient.hmac(key, data.getBytes("UTF-8")));
    }

    private synchronized String uncryptedPost(final String path, final Object... params) throws MyJDownloaderException, APIException {
        final JSonRequest re = new JSonRequest();
        re.setRid(this.inc());
        re.setParams(params);
        re.setUrl(path);
        try {
            return this.post(path, this.objectToJSon(re), null);
        } catch (final ExceptionResponse e) {
            this.handleInvalidResponseCodes(e);
            throw e;
        }
    }

    public void unregisterNotification(final String receiverID, final DeviceData device) throws MyJDownloaderException {
        final SessionInfo session = this.getSessionInfo();
        final String query = "/notify/unregister?sessiontoken=" + this.urlencode(session.getSessionToken()) + "&receiverid=" + this.urlencode(receiverID) + "&deviceid=" + this.urlencode(device.getId());
        this.callServer(query, null, session.getServerEncryptionToken(), RequestIDOnly.class);
    }

    public byte[] updateEncryptionToken(final byte[] oldSecret, final byte[] update) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(oldSecret);
        md.update(update);
        return md.digest();
    }

    /**
     * Urlencode a String
     * 
     * @param text
     * @return
     */
    abstract public String urlencode(String text);

}
