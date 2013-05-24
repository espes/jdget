package org.jdownloader.myjdownloader.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.appwork.exceptions.WTFException;
import org.appwork.shutdown.ShutdownController;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.jackson.JacksonMapper;
import org.appwork.utils.encoding.Base64;
import org.appwork.utils.net.BasicHTTP.BasicHTTP;
import org.appwork.utils.net.httpconnection.HTTPConnection;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.appwork.utils.swing.dialog.LoginDialog;
import org.appwork.utils.swing.dialog.LoginDialog.LoginData;
import org.jdownloader.myjdownloader.client.AbstractMyJDClient;
import org.jdownloader.myjdownloader.client.SessionInfo;
import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.EmailNotValidatedException;
import org.jdownloader.myjdownloader.client.exceptions.ExceptionResponse;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.exceptions.TokenException;
import org.jdownloader.myjdownloader.client.json.CaptchaChallenge;
import org.jdownloader.myjdownloader.client.json.DeviceData;
import org.jdownloader.myjdownloader.client.json.DeviceList;

public class Test {
    private static final String LIST_DEVICES      = "List Devices";
    private static final String RESTORE_SESSION   = "Restore Session";
    private static final String REGISTER          = "Register";
    private static final String LOGIN             = "Login";
    private static final String WRITE_SESSION     = "Write Session & Exit";
    private static final String CALL_UPTIME       = "Call Uptime";
    private static final String REGAIN_TOKEN      = "Regain Token";
    private static final String CAPTCHA_CHALLENGE = "Captcha Challenge";
    private static final String DISCONNECT        = "Disconnect";
    private static final String CHANGE_PASSWORD   = "Change Password";

    /**
     * @param args
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws APIException
     * @throws MyJDownloaderException
     * @throws DialogCanceledException
     * @throws DialogClosedException
     * @throws IOException
     */
    public static void main(final String[] args) throws APIException, MyJDownloaderException, DialogClosedException, DialogCanceledException, IOException {
        final BasicHTTP br = new BasicHTTP();
        JSonStorage.setMapper(new JacksonMapper());
        br.putRequestHeader("Content-Type", "application/json; charset=utf-8");
        br.setAllowedResponseCodes(200, 503, 401, 407, 403, 500, 429);

        final AbstractMyJDClient api = new AbstractMyJDClient("Java Test Application") {

            @Override
            protected String post(final String query, final String object) throws ExceptionResponse {
                try {

                    System.out.println(object);
                    final String ret = br.postPage(new URL(getServerRoot() + query), object == null ? "" : object);
                    System.out.println(br.getConnection());
                    System.out.println(ret);
                    final HTTPConnection con = br.getConnection();

                    if (con != null && con.getResponseCode() > 0 && con.getResponseCode() != 200) {

                    throw new ExceptionResponse(ret, con.getResponseCode());

                    }

                    return ret;
                } catch (final ExceptionResponse e) {
                    throw e;
                } catch (final Exception e) {
                    throw new ExceptionResponse(e);

                } finally {

                }

            }

            @Override
            protected byte[] base64decode(final String base64encodedString) {
                return Base64.decode(base64encodedString);
            }

            @Override
            protected String base64Encode(final byte[] encryptedBytes) {
                return Base64.encodeToString(encryptedBytes, false);
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
            public String urlencode(final String text) {
                try {
                    return URLEncoder.encode(text, "UTF-8");
                } catch (final UnsupportedEncodingException e) {
                    throw new WTFException(e);

                }
            }

        };
        final Storage config = JSonStorage.getPlainStorage("APiClientTest");

        api.setServerRoot("http://192.168.2.110:10101");
        // api.setServerRoot("http://localhost:10101");

        try {

            String lastOption = LOGIN;
            while (true) {
                try {
                    lastOption = (String) Dialog.getInstance().showComboDialog(0, "Please Choose", "Choose Test", new String[] { LOGIN, REGISTER, LIST_DEVICES, RESTORE_SESSION, CHANGE_PASSWORD, DISCONNECT, CAPTCHA_CHALLENGE, REGAIN_TOKEN, CALL_UPTIME, WRITE_SESSION }, lastOption, null, null, null, null);
                    if (CHANGE_PASSWORD == lastOption) {
                        api.requestPasswordChangeEmail();

                        api.changePassword(Dialog.getInstance().showInputDialog(0, "New Password", "Enter", null, null, null, null), Dialog.getInstance().showInputDialog(0, "Current Password", "Enter", null, null, null, null), Dialog.getInstance().showInputDialog(0, "Confirmal Key", "Enter", null, null, null, null));
                    } else if (LIST_DEVICES == lastOption) {

                        final DeviceList list = api.listDevices();
                        Dialog.getInstance().showMessageDialog(JSonStorage.toString(list));
                    } else if (DISCONNECT == lastOption) {
                        api.disconnect();
                        try {
                            api.listDevices();

                            Dialog.getInstance().showExceptionDialog("Error!!", "This SHould throw a TokenException", new WTFException("This SHould throw a TokenException"));
                        } catch (final TokenException e) {
                            Dialog.getInstance().showMessageDialog("Disconnect OK");
                            System.exit(1);
                        }

                    } else if (CAPTCHA_CHALLENGE == lastOption) {

                        final CaptchaChallenge challenge = api.getChallenge();
                        final String response = Dialog.getInstance().showInputDialog(0, "Enter Captcha to register", "Enter", null, createImage(challenge), null, null);
                        challenge.setCaptchaResponse(response);

                    } else if (REGAIN_TOKEN == lastOption) {
                        api.reconnect();

                        Dialog.getInstance().showMessageDialog("Done. New SessionToken: " + JSonStorage.toString(api.getSessionInfo()));
                    } else if (CALL_UPTIME == lastOption) {
                        final DeviceList list = api.listDevices();
                        if (list.getList().size() == 0) { throw new RuntimeException("No Device Connected"); }
                        final int device = Dialog.getInstance().showComboDialog(0, "Choose Device", "Choose Device", list.getList().toArray(new DeviceData[] {}), 0, null, null, null, null);

                        final Long uptime = api.callAction(list.getList().get(device).getId(), "/jd/uptime", long.class);

                        Dialog.getInstance().showMessageDialog("Uptime: " + uptime);
                    } else if (WRITE_SESSION == lastOption) {

                        config.put("session", JSonStorage.toString(api.getSessionInfo()));
                        ShutdownController.getInstance().requestShutdown();

                    } else if (LOGIN == lastOption) {
                        final LoginDialog login = new LoginDialog(0);
                        login.setMessage("MyJDownloader Account Logins");
                        login.setRememberDefault(true);
                        login.setUsernameDefault(config.get("email", ""));
                        login.setPasswordDefault(config.get("password", ""));
                        final LoginData li = Dialog.getInstance().showDialog(login);
                        if (li.isSave()) {
                            config.put("email", li.getUsername());

                            config.put("password", li.getPassword());
                        }
                        try {
                            api.connect(li.getUsername(), li.getPassword());
                        } catch (final EmailNotValidatedException e) {
                            api.requestConfirmationEmail(li.getUsername(), li.getPassword());

                            api.confirmEmail(Dialog.getInstance().showInputDialog(0, "Email Confirmal Key", "Enter", null, null, null, null), li.getUsername(), li.getPassword());
                            api.connect(li.getUsername(), li.getPassword());

                        }

                    } else if (REGISTER == lastOption) {
                        final LoginDialog login = new LoginDialog(0);
                        login.setMessage("MyJDownloader Account Register");
                        login.setRememberDefault(true);
                        login.setUsernameDefault(config.get("email", ""));
                        login.setPasswordDefault(config.get("password", ""));
                        final LoginData li = Dialog.getInstance().showDialog(login);
                        if (li.isSave()) {
                            config.put("email", li.getUsername());

                            config.put("password", li.getPassword());
                        }
                        try {
                            final CaptchaChallenge challenge = api.getChallenge();

                            final String response = Dialog.getInstance().showInputDialog(0, "Enter Captcha to register", "Enter", null, createImage(challenge), null, null);
                            challenge.setCaptchaResponse(response);

                            api.register(challenge, li.getUsername(), li.getPassword(), null);
                            api.confirmEmail(Dialog.getInstance().showInputDialog(0, "Email Confirmal Key", "Enter", null, null, null, null), li.getUsername(), li.getPassword());

                        } catch (final EmailNotValidatedException e) {
                            api.requestConfirmationEmail(li.getUsername(), li.getPassword());

                            api.confirmEmail(Dialog.getInstance().showInputDialog(0, "Email Confirmal Key", "Enter", null, null, null, null), li.getUsername(), li.getPassword());
                            api.connect(li.getUsername(), li.getPassword());

                        }

                    } else if (RESTORE_SESSION == lastOption) {
                        final String session = config.get("session", "");
                        final SessionInfo sessioninfo = JSonStorage.restoreFromString(session, SessionInfo.class);
                        api.setSessionInfo(sessioninfo);

                        api.reconnect();
                        Dialog.getInstance().showMessageDialog("Done. New SessionToken: " + JSonStorage.toString(api.getSessionInfo()));
                    }
                } catch (final DialogNoAnswerException e) {
                    System.exit(1);
                } catch (final Exception e) {

                    Dialog.getInstance().showExceptionDialog("Error!!", e.getClass().getSimpleName() + ": " + e, e);
                }
            }

        } catch (final Exception e) {

            Dialog.getInstance().showExceptionDialog("Error!!", e.getClass().getSimpleName() + ": " + e, e);
        }
        // List<FilePackageAPIStorable> ret = api.link(DownloadsAPI.class, "downloads").queryPackages(new APIQuery());
        // List<CaptchaJob> list = api.link(CaptchaAPI.class, "captcha").list();
        // System.out.println(list);
        //
        // Long uptime = api.callAction("/jd/uptime", long.class);
        // System.out.println(uptime);
        // System.out.println(ret);
        // api.disconnect();
        // System.out.println(jdapi.uptime());
    }

    private static ImageIcon createImage(final CaptchaChallenge challenge) throws IOException {
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.decode(challenge.getImage().substring(challenge.getImage().lastIndexOf(",")))));

        return new ImageIcon(image);
    }
}
