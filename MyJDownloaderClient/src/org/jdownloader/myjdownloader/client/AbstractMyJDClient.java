package org.jdownloader.myjdownloader.client;



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
import org.jdownloader.myjdownloader.client.exceptions.device.ApiFileNotFoundException;
import org.jdownloader.myjdownloader.client.exceptions.device.InternalServerErrorException;
import org.jdownloader.myjdownloader.client.exceptions.device.SessionException;
import org.jdownloader.myjdownloader.client.exceptions.device.UnknownCommandException;
import org.jdownloader.myjdownloader.client.exceptions.device.UnknownInterfaceException;
import org.jdownloader.myjdownloader.client.exceptions.device.WrongParametersException;
import org.jdownloader.myjdownloader.client.json.CaptchaChallenge;
import org.jdownloader.myjdownloader.client.json.ConnectResponse;
import org.jdownloader.myjdownloader.client.json.DeviceConnectResponse;
import org.jdownloader.myjdownloader.client.json.DeviceData;
import org.jdownloader.myjdownloader.client.json.DeviceErrorType;
import org.jdownloader.myjdownloader.client.json.DeviceList;
import org.jdownloader.myjdownloader.client.json.ErrorResponse;
import org.jdownloader.myjdownloader.client.json.FeedbackResponse;
import org.jdownloader.myjdownloader.client.json.JSonRequest;
import org.jdownloader.myjdownloader.client.json.MyJDJsonMapper;
import org.jdownloader.myjdownloader.client.json.NotificationRequestMessage;
import org.jdownloader.myjdownloader.client.json.NotificationRequestTypesResponse;
import org.jdownloader.myjdownloader.client.json.ObjectData;
import org.jdownloader.myjdownloader.client.json.RequestIDOnly;
import org.jdownloader.myjdownloader.client.json.RequestIDValidator;
import org.jdownloader.myjdownloader.client.json.ServerErrorType;
import org.jdownloader.myjdownloader.client.json.SuccessfulResponse;

public abstract class AbstractMyJDClient<Type> {
    private static final int API_VERSION = 1;

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

    private String       serverRoot         = "http://api.jdownloader.org";

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
        final SessionInfo session = getSessionInfo();
        final String query = "/my/binddevice?sessiontoken=" + urlencode(session.getSessionToken()) + "&deviceID=" + urlencode(device.getId()) + "&type=" + urlencode(device.getType()) + "&name=" + urlencode(device.getName());
        final DeviceConnectResponse ret = this.callServer(query, null, session, DeviceConnectResponse.class);
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
    protected Object callAction(final String deviceID, final String action, final Type returnType, final Object... args) throws MyJDownloaderException, APIException {
        SessionInfo session = null;
        try {
            session = getSessionInfo();
            final String query = "/t_" + session.getSessionToken() + "_" + urlencode(deviceID) + action;
            final String[] params = new String[args != null ? args.length : 0];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    params[i] = objectToJSon(args[i]);
                }
            }
            final JSonRequest payload = new JSonRequest();
            payload.setUrl(action);
            payload.setApiVer(API_VERSION);
            long i;
            payload.setRid(i = getUniqueRID());
            payload.setParams(params);
            final String json = objectToJSon(payload);
            final byte[] data = cryptedPost(query, base64Encode(encrypt(json.getBytes("UTF-8"), session.getDeviceEncryptionToken())), session.getDeviceEncryptionToken());

            Object ret = convertData(data, returnType);
            if (ret != null) {
                if (ret instanceof RequestIDValidator) {
                    if (((RequestIDValidator) ret).getRid() != i) { throw new BadResponseException("RID Mismatch"); }
                }
                return ret;
            }

            final String dec = toString(data);
            // this is a workaround.. do not consider this as final solution!
            if (dec.indexOf("\"data\" :") > 0) {
                final ObjectData dataObject = this.jsonToObject(dec, (Type) ObjectData.class);
                if (data == null) {
                    // invalid response
                    throw new MyJDownloaderException("Invalid Response: " + dec);
                }

                if (dataObject.getRid() != i) { throw new BadResponseException("RID Mismatch"); }

                // ugly!!! but this will be changed when we have a proper remoteAPI response format

                ret = jsonToObject(objectToJSon(dataObject.getData()) + "", returnType);

                return ret;
            } else {
                ret = jsonToObject(dec, returnType);
                if (ret instanceof RequestIDValidator) {
                    if (((RequestIDValidator) ret).getRid() != i) { throw new BadResponseException("RID Mismatch"); }
                }
                return ret;
            }

        } catch (final ExceptionResponse e) {
            handleInvalidResponseCodes(e, session);
            throw e;
        } catch (final MyJDownloaderException e) {
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            throw APIException.get(e);
        }
    }

    protected <T> T convertData(final byte[] data, final Type returnType) throws MyJDownloaderException {
        if (returnType == byte[].class) { return (T) data; }
        return null;
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
    protected <T> T callServer(String query, final String postData, final SessionInfo session, final Class<T> class1) throws MyJDownloaderException {
        try {
            byte[] key = null;
            if (session != null) {
                key = session.getServerEncryptionToken();
            }
            query += query.contains("?") ? "&" : "?";
            final long i = getUniqueRID();
            query += "rid=" + i;
            final byte[] data = cryptedPost(query + "&signature=" + sign(key, query), postData, key);
            Object ret = convertData(data, (Type) class1);
            if (ret != null) {
                if (ret instanceof RequestIDValidator) {
                    if (((RequestIDValidator) ret).getRid() != i) { throw new BadResponseException("RID Mismatch"); }
                }
                return (T) ret;
            }

            ret = this.jsonToObject(toString(data), (Type) class1);
            // System.out.println(this.objectToJSon(ret));
            if (ret instanceof RequestIDValidator) {
                if (((RequestIDValidator) ret).getRid() != i) { throw new BadResponseException("RID Mismatch"); }
            }
            return (T) ret;
        } catch (final ExceptionResponse e) {
            try {
                handleInvalidResponseCodes(e, session);
            } catch (final APIException e1) {
                // actually not possible.
                throw new RuntimeException(e);

            }
            throw e;
        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);
        }
    }

    public void cancelRegistrationEmail(final String email, final String key) throws MyJDownloaderException {
        try {
            uncryptedPost("/my/cancelregistrationemail?email=" + urlencode(email) + "&key=" + urlencode(key));
        } catch (final APIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a new Session. Do never store email and password in your application. throw away the password after connect and work with
     * #getSessionInfo #setSessionInfo and #reconnect to restore a session
     * 
     * @param email
     * @param password
     * @throws MyJDownloaderException
     */
    public synchronized void connect(final String email, final String password) throws MyJDownloaderException {
        try {

            // localSecret = createSecret(username, password, "jd");
            final byte[] loginSecret = createSecret(email, password, "server");
            final byte[] deviceSecret = createSecret(email, password, "device");
            final long rid = getUniqueRID();
            final StringBuilder query = new StringBuilder().append("/my/connect?email=").append(urlencode(email)).append("&appkey=").append(urlencode(appKey)).append("&rid=").append(rid);

            final String signature = sign(loginSecret, query.toString());
            query.append("&signature=").append(urlencode(signature));

            final String retString = toString(cryptedPost(query.toString(), "", loginSecret));
            final ConnectResponse ret = this.jsonToObject(retString, (Type) ConnectResponse.class);
            if (ret.getRid() != rid) { throw new BadResponseException("RID Mismatch"); }

            final byte[] serverEncryptionToken = updateEncryptionToken(loginSecret, AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final byte[] deviceEncryptionToken = updateEncryptionToken(deviceSecret, AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final String sessionToken = ret.getSessiontoken();
            final String regainToken = ret.getRegaintoken();
            final SessionInfo newSessionInfo = new SessionInfo(deviceSecret, serverEncryptionToken, deviceEncryptionToken, sessionToken, regainToken);
            currentSessionInfo = newSessionInfo;

        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);

        }

    }

    protected abstract byte[] createSecret(final String username, final String password, final String domain) throws MyJDownloaderException;

    private byte[] cryptedPost(final String url, final String objectToJSon, final byte[] keyAndIV) throws MyJDownloaderException, APIException {
        return post(url, objectToJSon, keyAndIV);

    }

    protected abstract byte[] decrypt(final byte[] crypted, final byte[] keyAndIV) throws MyJDownloaderException;

    // @SuppressWarnings("unchecked")
    // private <T> T jsonToObjectGeneric(String dec, Class<T> clazz) {
    // return (T) jsonToObject(dec, clazz);
    // }
    /**
     * Disconnect. This will invalidate your session. You have to call #connect to get a new session afterwards
     * 
     * @throws MyJDownloaderException
     */
    public synchronized void disconnect() throws MyJDownloaderException {
        try {
            final SessionInfo session = getSessionInfo();
            final String query = "/my/disconnect?sessiontoken=" + urlencode(session.getSessionToken());
            this.callServer(query, null, session, RequestIDOnly.class);
        } finally {
            currentSessionInfo = null;
        }
    }

    protected abstract byte[] encrypt(final byte[] data, final byte[] keyAndIV) throws MyJDownloaderException;

    public String feedback(final String message) throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final JSonRequest re = new JSonRequest();
        re.setApiVer(API_VERSION);
        re.setRid(getUniqueRID());
        re.setParams(new Object[] { message });
        final String url = "/my/feedback?sessiontoken=" + urlencode(session.getSessionToken());
        re.setUrl(url);
        final FeedbackResponse ret = this.callServer(url, objectToJSon(re), session, FeedbackResponse.class);
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
    public void finishPasswordReset(final String email, final String key, final String newPassword) throws MyJDownloaderException {

        final byte[] k = AbstractMyJDClient.hexToByteArray(key);
        if (k.length != 32) { throw new IllegalArgumentException("Bad Key. Expected: 64 hexchars"); }
        final byte[] newLoginSecret = createSecret(email, newPassword, "server");
        final String encryptedNewSecret = AbstractMyJDClient.byteArrayToHex(encrypt(newLoginSecret, k));
        final SessionInfo session = new SessionInfo();
        session.setServerEncryptionToken(k);
        this.callServer("/my/finishpasswordreset?email=" + urlencode(email) + "&encryptedLoginSecret=" + encryptedNewSecret, null, session, RequestIDOnly.class);
        connect(email, newPassword);

    }

    /**
     * Confirm your email by sending the Confirm Key.
     * 
     * @param key
     * @param email
     * @param password
     * @throws MyJDownloaderException
     */
    public void finishRegistration(final String key, final String email, final String password) throws MyJDownloaderException {

        final byte[] k = AbstractMyJDClient.hexToByteArray(key);
        if (k.length != 32) { throw new IllegalArgumentException("Bad Key. Expected: 64 hexchars"); }
        final byte[] loginSecret = createSecret(email, password, "server");
        final String pw = AbstractMyJDClient.byteArrayToHex(encrypt(loginSecret, k));
        final SessionInfo session = new SessionInfo();
        session.setServerEncryptionToken(k);
        this.callServer("/my/finishregistration?email=" + urlencode(email) + "&loginsecret=" + urlencode(pw), null, session, RequestIDOnly.class);

    }

    /**
     * Downloads a CaptchaChallenge from the server
     * 
     * @return
     * @throws MyJDownloaderException
     */
    public CaptchaChallenge getChallenge() throws MyJDownloaderException {
        try {
            return jsonToObject(toString(uncryptedPost("/captcha/getCaptcha", (Object[]) null)), (Type) CaptchaChallenge.class);
        } catch (final APIException e) {
            throw new RuntimeException(e);

        }
    }

    protected String toString(final byte[] data) throws MyJDownloaderException {
        try {
            return new String(data, "UTF-8");
        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);

        }
    }

    /**
     * Can be used to calculate a foreign device encryption token based on the remote session token
     * 
     * @param sessionToken
     * @return
     * @throws MyJDownloaderException
     */
    public byte[] getDeviceEncryptionTokenBySession(final String sessionToken) throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        return updateEncryptionToken(session.getDeviceSecret(), AbstractMyJDClient.hexToByteArray(sessionToken));

    }

    public String getServerRoot() {
        return serverRoot;
    }

    /**
     * Get The current Session Info Object. You can store it to reconnect to the same session later
     * 
     * @return
     */
    public SessionInfo getSessionInfo() throws MyJDownloaderException {
        final SessionInfo ret = currentSessionInfo;
        if (ret == null) { throw new UnconnectedException(); }
        return ret;
    }

    protected void handleInvalidResponseCodes(final ExceptionResponse e, final SessionInfo session) throws MyJDownloaderException, APIException {
        if (e != null && e.getContent() != null && e.getContent().trim().length() != 0) {
            ErrorResponse error = null;
            try {
                error = this.jsonToObject(e.getContent(), (Type) ErrorResponse.class);
                switch (error.getSrc()) {
                case DEVICE:
                    if (error.getType() != null) {
                        final DeviceErrorType type = DeviceErrorType.valueOf(error.getType());
                        // SES
                        switch (type) {
                        case INTERNAL_SERVER_ERROR:
                            throw new InternalServerErrorException(error.getData());
                        case API_COMMAND_NOT_FOUND:
                            throw new UnknownCommandException(error.getData());
                        case API_INTERFACE_NOT_FOUND:
                            throw new UnknownInterfaceException(error.getData());
                        case AUTH_FAILED:
                            throw new AuthException();
                        case BAD_PARAMETERS:
                            throw new WrongParametersException(error.getData());
                        case FILE_NOT_FOUND:
                            throw new ApiFileNotFoundException(error.getData());
                        case SESSION:
                            throw new SessionException(error.getData());

                        default:
                            throw new APIException(error.getType(), error.getData());
                        }
                    }

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
                        throw new TokenException(session);
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
            throw new TokenException(session);
        default:
            throw new UnexpectedIOException(e);
        }
    }

    protected abstract long getUniqueRID();

    protected <T> T jsonToObject(final String dec, final Type clazz) {
        return (T) MyJDJsonMapper.HANDLER.jsonToObject(dec, clazz);
    }

    public void keepalive() throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final String query = "/my/keepalive?sessiontoken=" + urlencode(session.getSessionToken());
        this.callServer(query, null, session, RequestIDOnly.class);
    }

    public DeviceList listDevices() throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final String query = "/my/listdevices?sessiontoken=" + urlencode(session.getSessionToken());
        final DeviceList ret = this.callServer(query, null, session, DeviceList.class);
        return ret;
    }

    public NotificationRequestMessage.TYPE[] listrequesteddevicesnotifications() throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final String query = "/notify/list?sessiontoken=" + urlencode(session.getSessionToken());
        final NotificationRequestTypesResponse ret = this.callServer(query, null, session, NotificationRequestTypesResponse.class);
        return ret.getTypes();
    }

    protected String objectToJSon(final Object payload) {
        return MyJDJsonMapper.HANDLER.objectToJSon(payload);
    }

    abstract protected byte[] post(String query, String object, byte[] keyAndIV) throws ExceptionResponse;

    public boolean pushNotification(final NotificationRequestMessage message) throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final String query = "/notify/push?sessiontoken=" + urlencode(session.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setApiVer(API_VERSION);
        re.setRid(getUniqueRID());
        re.setParams(new Object[] { message });
        re.setUrl(query);
        return this.callServer(query, objectToJSon(re), session, SuccessfulResponse.class).isSuccessful();
    }

    /**
     * If the Session becomes invalid(for example due to an ip change), you need to reconnect. The user does NOT have to reenter his logins.
     * We use a regain token to get a new session. Short: If you get a #TokenException, call reconnect to refresh your session.
     * 
     * @throws MyJDownloaderException
     */
    public synchronized void reconnect() throws MyJDownloaderException {
        try {
            final SessionInfo session = getSessionInfo();
            final String query = "/my/reconnect?sessiontoken=" + urlencode(session.getSessionToken()) + "&regaintoken=" + urlencode(session.getRegainToken());
            final ConnectResponse ret = this.callServer(query, null, session, ConnectResponse.class);

            final byte[] serverEncryptionToken = updateEncryptionToken(session.getServerEncryptionToken(), AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final byte[] deviceEncryptionToken = updateEncryptionToken(session.getDeviceSecret(), AbstractMyJDClient.hexToByteArray(ret.getSessiontoken()));
            final String sessionToken = ret.getSessiontoken();
            final String regainToken = ret.getRegaintoken();
            final SessionInfo newSessionInfo = new SessionInfo(session.getDeviceSecret(), serverEncryptionToken, deviceEncryptionToken, sessionToken, regainToken);
            currentSessionInfo = newSessionInfo;
        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);

        }
    }

    public void registerNotification(final String receiverID, final DeviceData device, final NotificationRequestMessage.TYPE... types) throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final String query = "/notify/register?sessiontoken=" + urlencode(session.getSessionToken()) + "&receiverid=" + urlencode(receiverID) + "&deviceid=" + urlencode(device.getId());
        final JSonRequest re = new JSonRequest();
        re.setApiVer(API_VERSION);
        re.setRid(getUniqueRID());
        if (types == null || types.length == 0) {
            re.setParams(new Object[] { new NotificationRequestMessage.TYPE[] {} });
        } else {
            re.setParams(new Object[] { types });
        }
        re.setUrl(query);
        final String object = objectToJSon(re);
        this.callServer(query, object, session, RequestIDOnly.class);
    }

    /**
     * Call this method to request a password change. You will get an email containing a key. Use this key together with
     * {@link #changePassword(String, String, String)}
     * 
     * @throws MyJDownloaderException
     */
    public void requestPasswordResetEmail(final CaptchaChallenge challenge, final String email) throws MyJDownloaderException {
        try {
            uncryptedPost("/my/requestpasswordresetemail?email=" + urlencode(email) + "&captchaResponse=" + urlencode(challenge.getCaptchaResponse()) + "&captchaChallenge=" + urlencode(challenge.getCaptchaChallenge()));
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
    public void requestRegistrationEmail(final CaptchaChallenge challenge, final String email, final String referer) throws MyJDownloaderException {
        try {
            uncryptedPost("/my/requestregistrationemail?email=" + urlencode(email) + "&captchaResponse=" + urlencode(challenge.getCaptchaResponse()) + "&captchaChallenge=" + urlencode(challenge.getCaptchaChallenge()) + "&referer=" + urlencode(referer == null ? appKey : referer));
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
        currentSessionInfo = info;
    }

    private String sign(final byte[] key, final String data) throws MyJDownloaderException {
        try {
            return AbstractMyJDClient.byteArrayToHex(hmac(key, data.getBytes("UTF-8")));
        } catch (final Exception e) {
            throw MyJDownloaderException.get(e);

        }
    }

    protected abstract byte[] hmac(byte[] key, byte[] bytes) throws MyJDownloaderException;

    private byte[] uncryptedPost(final String path, final Object... params) throws MyJDownloaderException, APIException {
        final JSonRequest re = new JSonRequest();
        re.setApiVer(API_VERSION);
        re.setRid(getUniqueRID());
        re.setParams(params);
        re.setUrl(path);
        return post(path, objectToJSon(re), null);
    }

    public void unregisterNotification(final String receiverID, final DeviceData device) throws MyJDownloaderException {
        final SessionInfo session = getSessionInfo();
        final String query = "/notify/unregister?sessiontoken=" + urlencode(session.getSessionToken()) + "&receiverid=" + urlencode(receiverID) + "&deviceid=" + urlencode(device.getId());
        this.callServer(query, null, session, RequestIDOnly.class);
    }

    protected abstract byte[] updateEncryptionToken(final byte[] oldSecret, final byte[] update) throws MyJDownloaderException;

    /**
     * Urlencode a String
     * 
     * @param text
     * @return
     * @throws MyJDownloaderException
     */
    abstract public String urlencode(String text) throws MyJDownloaderException;

}
