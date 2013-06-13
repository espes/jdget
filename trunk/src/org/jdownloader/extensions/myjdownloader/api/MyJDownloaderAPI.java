package org.jdownloader.extensions.myjdownloader.api;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import jd.nutils.encoding.Encoding;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.IO;
import org.appwork.utils.encoding.Base64;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.net.Base64InputStream;
import org.appwork.utils.net.BasicHTTP.BasicHTTP;
import org.appwork.utils.net.httpconnection.HTTPConnection;
import org.jdownloader.extensions.myjdownloader.MyDownloaderExtensionConfig;
import org.jdownloader.extensions.myjdownloader.MyJDownloaderExtension;
import org.jdownloader.myjdownloader.client.AbstractMyJDClient;
import org.jdownloader.myjdownloader.client.exceptions.ExceptionResponse;

public class MyJDownloaderAPI extends AbstractMyJDClient {

    private BasicHTTP br;
    private LogSource logger;

    @Override
    protected byte[] base64decode(String base64encodedString) {

        return Base64.decode(base64encodedString);

    }

    @Override
    protected String base64Encode(byte[] encryptedBytes) {
        return Base64.encodeToString(encryptedBytes, false);
    }

    @Override
    public String urlencode(String text) {
        return Encoding.urlEncode(text);
    }

    @Override
    protected String objectToJSon(final Object payload) {
        return JSonStorage.toString(payload);
    }

    @Override
    protected <T> T jsonToObject(final String dec, final Type clazz) {
        return JSonStorage.restoreFromString(dec, new TypeRef<T>(clazz) {
        });
    }

    @Override
    protected String post(final String query, final String object, final byte[] keyAndIV) throws ExceptionResponse {
        HTTPConnection con = null;
        String ret = null;
        try {
            if (keyAndIV != null) {
                br.putRequestHeader("Accept-Encoding", "gzip_aes");
                final byte[] sendBytes = (object == null ? "" : object).getBytes("UTF-8");
                final HashMap<String, String> header = new HashMap<String, String>();
                header.put(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, "" + sendBytes.length);
                con = br.openPostConnection(new URL(this.getServerRoot() + query), null, new ByteArrayInputStream(sendBytes), header);
                final String content_Encoding = con.getHeaderField(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING);
                if (con.getResponseCode() == 200) {
                    if ("gzip_aes".equals(content_Encoding)) {
                        final byte[] aes = IO.readStream(-1, con.getInputStream());
                        final byte[] decrypted = this.decrypt(aes, keyAndIV);
                        ret = IO.readInputStreamToString(new GZIPInputStream(new ByteArrayInputStream(decrypted)));
                    } else {
                        final byte[] aes = IO.readStream(-1, new Base64InputStream(con.getInputStream()));
                        final byte[] decrypted = this.decrypt(aes, keyAndIV);
                        ret = new String(decrypted, "UTF-8");
                    }
                } else {
                    ret = IO.readInputStreamToString(con.getInputStream());
                }
            } else {
                br.putRequestHeader("Accept-Encoding", null);
                ret = br.postPage(new URL(this.getServerRoot() + query), object == null ? "" : object);
                con = br.getConnection();
            }
            System.out.println(con);
            if (con != null && con.getResponseCode() > 0 && con.getResponseCode() != 200) { throw new ExceptionResponse(ret, con.getResponseCode()); }
            return ret;
        } catch (final ExceptionResponse e) {
            throw e;
        } catch (final Exception e) {
            throw new ExceptionResponse(e);
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
    }

    protected AtomicLong                        TIMESTAMP    = new AtomicLong(System.currentTimeMillis());
    protected volatile String                   connectToken = null;

    protected final MyDownloaderExtensionConfig config;

    private MyJDownloaderExtension              extension;
    private HashMap<String, RIDArray>           rids;

    public MyJDownloaderAPI(MyJDownloaderExtension myJDownloaderExtension) {
        super("JD_V1");
        extension = myJDownloaderExtension;

        this.config = extension.getSettings();
        setServerRoot("http://" + config.getAPIServerURL() + ":" + config.getAPIServerPort());
        logger = extension.getLogger();
        br = new BasicHTTP();
        br.setAllowedResponseCodes(200, 503, 401, 407, 403, 500, 429);
        br.putRequestHeader("Content-Type", "application/json; charset=utf-8");
        rids = new HashMap<String, RIDArray>();

    }

    public LogSource getLogger() {
        return logger;
    }

    /* TODO: add session support, currently all sessions share the same validateRID */
    public synchronized boolean validateRID(long rid, String sessionToken) {

        // TODO CLeanup
        RIDArray ridList = rids.get(sessionToken);
        if (ridList == null) {
            ridList = new RIDArray();
            rids.put(sessionToken, ridList);
        }

        // lowest RID
        long lowestRid = Long.MIN_VALUE;
        RIDEntry next;
        for (Iterator<RIDEntry> it = ridList.iterator(); it.hasNext();) {
            next = it.next();
            if (next.getRid() == rid) {
                // dupe rid is always bad
                logger.warning("received an RID Dupe. Possible Replay Attack avoided");
                return false;
            }
            if (System.currentTimeMillis() - next.getTimestamp() > 15000) {
                it.remove();
                if (next.getRid() > lowestRid) {
                    lowestRid = next.getRid();
                }

            }
        }
        if (lowestRid > ridList.getMinAcceptedRID()) {
            ridList.setMinAcceptedRID(lowestRid);
        }
        if (rid <= ridList.getMinAcceptedRID()) {
            // rid too low
            logger.warning("received an outdated RID. Possible Replay Attack avoided");
            return false;
        }
        RIDEntry ride = new RIDEntry(rid);
        ridList.add(ride);

        return true;
    }

    // protected String getConnectToken(String username, String password) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
    // NoSuchPaddingException, InvalidAlgorithmParameterException {
    // logger.info("Login " + username + ":" + password);
    // String url = "/my/deviceconnect?email=" + Encoding.urlEncode(config.getUsername()) + "&deviceID=" +
    // Encoding.urlEncode(config.getUniqueDeviceID()) + "&type=JD&name=" + Encoding.urlEncode(config.getDeviceName());
    // Browser br = new Browser();
    // long timeStamp = TIMESTAMP.incrementAndGet();
    // url = url + "&rid=" + timeStamp;
    // byte[] loginSecret = getLoginSecret(config.getUsername(), config.getPassword());
    // String signature = getSignature(loginSecret, url.getBytes("UTF-8"));
    // String completeurl = "http://" + config.getAPIServerURL() + ":" + config.getAPIServerPort() + url + "&signature=" + signature;
    // URLConnectionAdapter con = null;
    // try {
    // logger.info("GET " + completeurl);
    // con = br.openGetConnection(completeurl);
    // if (con.getResponseCode() == 403) throw new InvalidConnectException();
    // if (con.isOK()) {
    // byte[] response = null;
    //
    // final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    // final byte[] IV = Arrays.copyOfRange(loginSecret, 0, 16);
    // final IvParameterSpec ivSpec = new IvParameterSpec(IV);
    // final byte[] KEY = Arrays.copyOfRange(loginSecret, 16, 32);
    // final SecretKeySpec skeySpec = new SecretKeySpec(KEY, "AES");
    // cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
    //
    // response = IO.readStream(-1, new CipherInputStream(new Base64InputStream(con.getInputStream()), cipher));
    //
    // String ret = new String(response, "UTF-8");
    // DeviceConnectResponse responseObject = JSonStorage.restoreFromString(ret, DeviceConnectResponse.class);
    // config.setUniqueDeviceID(responseObject.getDeviceid());
    //
    // logger.info("RESPONSE(plain): " + completeurl + "\r\n" + ret);
    // String token = new Regex(ret, "\"token\"\\s*?:\\s*?\"([a-fA-F0-9]+)\"").getMatch(0);
    // if (token == null) throw new IOException("Unknown Response: " + response);
    // logger.info("Login OK: " + token);
    // return token;
    // // }
    // }
    // } finally {
    // try {
    // con.disconnect();
    // } catch (final Throwable e) {
    // }
    // }
    // throw new IOException("Unknown IOException");
    //
    // }

    // protected String getSignature(byte[] secret, byte[] content) throws InvalidKeyException, NoSuchAlgorithmException {
    // return HexFormatter.byteArrayToHex(AWSign.HMACSHA256(secret, content));
    // }
}
