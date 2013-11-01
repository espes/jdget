package jd.plugins.hoster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.http.Browser;
import jd.http.Request;
import jd.nutils.encoding.Base64;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.PluginProgress;
import jd.plugins.download.RAFDownload;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.jdownloader.images.NewTheme;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "mega.co.nz" }, urls = { "https?://(www\\.)?mega\\.co\\.nz/#N?(!|%21)[a-zA-Z0-9]+(!|%21)[a-zA-Z0-9_,\\-]+" }, flags = { 0 })
public class MegaConz extends PluginForHost {
    private static AtomicLong CS        = new AtomicLong(System.currentTimeMillis());
    private final String      USE_SSL   = "USE_SSL_V2";
    private final String      USE_TMP   = "USE_TMP_V2";
    private final String      encrypted = ".encrypted";

    public MegaConz(PluginWrapper wrapper) {
        super(wrapper);
        setConfigElements();
    }

    @Override
    public String getAGBLink() {
        return "https://mega.co.nz/#terms";
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceAll("%21", "!"));
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws Exception {
        setBrowserExclusive();
        boolean publicFile = true;
        String fileID = getPublicFileID(link);
        String keyString = getPublicFileKey(link);
        if (fileID == null) {
            fileID = getNodeFileID(link);
            keyString = getNodeFileKey(link);
            publicFile = false;
        }
        if (fileID == null || keyString == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.getHeaders().put("APPID", "JDownloader");
        if (publicFile) {
            br.postPageRaw("https://eu.api.mega.co.nz/cs?id=" + CS.incrementAndGet(), "[{\"a\":\"g\",\"ssl\":" + useSSL() + ",\"p\":\"" + fileID + "\"}]");
        } else {
            br.postPageRaw("https://eu.api.mega.co.nz/cs?id=" + CS.incrementAndGet(), "[{\"a\":\"g\",\"ssl\":" + useSSL() + ",\"n\":\"" + fileID + "\"}]");
        }
        String fileSize = br.getRegex("\"s\"\\s*?:\\s*?(\\d+)").getMatch(0);
        if (fileSize == null) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        } else {
            link.setDownloadSize(Long.parseLong(fileSize));
            try {
                link.setVerifiedFileSize(Long.parseLong(fileSize));
            } catch (final Throwable e) {
            }
        }
        String at = br.getRegex("\"at\"\\s*?:\\s*?\"(.*?)\"").getMatch(0);
        if (at == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String fileInfo = decrypt(at, keyString);
        String fileName = new Regex(fileInfo, "\"n\"\\s*?:\\s*?\"(.*?)\"").getMatch(0);
        if (fileName == null) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        link.setFinalFileName(fileName);
        try {
            if (link.getCustomFileOutputFilename() == null) {
                link.setCustomFileOutputFilenameAppend(encrypted);
            }
        } catch (final Throwable e) {
        }
        link.setProperty("ALLOW_HASHCHECK", false);
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        requestFileInformation(link);
        boolean publicFile = true;
        String fileID = getPublicFileID(link);
        String keyString = getPublicFileKey(link);
        if (fileID == null) {
            fileID = getNodeFileID(link);
            keyString = getNodeFileKey(link);
            publicFile = false;
        }
        if (fileID == null || keyString == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (publicFile) {
            br.postPageRaw("https://eu.api.mega.co.nz/cs?id=" + CS.incrementAndGet(), "[{\"a\":\"g\",\"g\":\"1\",\"ssl\":" + useSSL() + ",\"p\":\"" + fileID + "\"}]");
        } else {
            br.postPageRaw("https://eu.api.mega.co.nz/cs?id=" + CS.incrementAndGet(), "[{\"a\":\"g\",\"g\":\"1\",\"ssl\":" + useSSL() + ",\"n\":\"" + fileID + "\"}]");
        }
        String downloadURL = br.getRegex("\"g\"\\s*?:\\s*?\"(https?.*?)\"").getMatch(0);
        if (downloadURL == null) {
            String error = getError(br);
            if ("-18".equals(error)) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 5 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (oldStyle()) {
            /* old 09581 stable only */
            dl = createHackedDownloadInterface(this, br, link, downloadURL);
        } else {
            /* mega does not like much connections! */
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, downloadURL, true, -10);
        }
        if (dl.getConnection().getContentType() != null && dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (dl.startDownload()) {
            if (link.getLinkStatus().hasStatus(LinkStatus.FINISHED) && link.getDownloadCurrent() > 0) {
                decrypt(link, keyString);
            }
        }
    }

    private String decrypt(String input, String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, PluginException {
        byte[] b64Dec = b64decode(keyString);
        int[] intKey = aByte_to_aInt(b64Dec);
        if (intKey.length < 8) {
            /* key is not given in link */
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        byte[] key = aInt_to_aByte(intKey[0] ^ intKey[4], intKey[1] ^ intKey[5], intKey[2] ^ intKey[6], intKey[3] ^ intKey[7]);
        byte[] iv = aInt_to_aByte(0, 0, 0, 0);
        final IvParameterSpec ivSpec = new IvParameterSpec(iv);
        final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/nopadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        byte[] unPadded = b64decode(input);
        int len = 16 - ((unPadded.length - 1) & 15) - 1;
        byte[] payLoadBytes = new byte[unPadded.length + len];
        System.arraycopy(unPadded, 0, payLoadBytes, 0, unPadded.length);
        payLoadBytes = cipher.doFinal(payLoadBytes);
        String ret = new String(payLoadBytes, "UTF-8");
        if (ret != null && !ret.startsWith("MEGA{")) {
            /* verify if the keyString is correct */
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        return ret;
    }

    public String getError(Browser br) {
        if (br == null) return null;
        return br.getRegex("\"e\"\\s*?:\\s*?(-?\\d+)").getMatch(0);
    }

    private boolean oldStyle() {
        String style = System.getProperty("ftpStyle", null);
        if ("new".equalsIgnoreCase(style)) return false;
        String prev = JDUtilities.getRevision();
        if (prev == null || prev.length() < 3) {
            prev = "0";
        } else {
            prev = prev.replaceAll(",|\\.", "");
        }
        int rev = Integer.parseInt(prev);
        if (rev < 10000) return true;
        return false;
    }

    private RAFDownload createHackedDownloadInterface2(final PluginForHost plugin, final DownloadLink downloadLink, final Request request) throws IOException, PluginException {
        final RAFDownload dl = new RAFDownload(plugin, downloadLink, request);
        plugin.setDownloadInterface(dl);
        dl.setResume(true);
        dl.setChunkNum(1);
        return dl;
    }

    /* Workaround for Bug in old 09581 Downloadsystem bug */
    private RAFDownload createHackedDownloadInterface(final PluginForHost plugin, final Browser br, final DownloadLink downloadLink, final String url) throws IOException, PluginException, Exception {
        Request r = br.createRequest(url);
        RAFDownload dl = this.createHackedDownloadInterface2(plugin, downloadLink, r);
        try {
            r.getHeaders().remove("Accept-Encoding");
            dl.connect(br);
        } catch (final PluginException e) {
            if (e.getValue() == -1) {

                int maxRedirects = 10;
                while (maxRedirects-- > 0) {
                    dl = this.createHackedDownloadInterface2(plugin, downloadLink, r = br.createGetRequestRedirectedRequest(r));
                    try {
                        r.getHeaders().remove("Accept-Encoding");
                        dl.connect(br);
                        break;
                    } catch (final PluginException e2) {
                        continue;
                    }
                }
                if (maxRedirects <= 0) { throw new PluginException(LinkStatus.ERROR_FATAL, "Redirectloop"); }

            }
        }
        if (plugin.getBrowser() == br) {
            plugin.setDownloadInterface(dl);
        }
        return dl;
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), USE_SSL, JDL.L("plugins.hoster.megaconz.usessl", "Use SSL?")).setDefaultValue(false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), USE_TMP, JDL.L("plugins.hoster.megaconz.usetmp", "Use tmp decrypting file?")).setDefaultValue(false));
    }

    private void decrypt(DownloadLink link, String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        byte[] b64Dec = b64decode(keyString);
        int[] intKey = aByte_to_aInt(b64Dec);
        int[] keyNOnce = new int[] { intKey[0] ^ intKey[4], intKey[1] ^ intKey[5], intKey[2] ^ intKey[6], intKey[3] ^ intKey[7], intKey[4], intKey[5] };
        byte[] key = aInt_to_aByte(keyNOnce[0], keyNOnce[1], keyNOnce[2], keyNOnce[3]);
        int[] iiv = new int[] { keyNOnce[4], keyNOnce[5], 0, 0 };
        byte[] iv = aInt_to_aByte(iiv);
        final IvParameterSpec ivSpec = new IvParameterSpec(iv);
        final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        File dst = null;
        File src = null;
        File tmp = null;
        String path = link.getFileOutput();
        if (path.endsWith(encrypted)) {
            src = new File(path);
            String path2 = path.substring(0, path.length() - encrypted.length());
            if (useTMP()) {
                tmp = new File(path2 + ".decrypted");
            }
            dst = new File(path2);
        } else {
            src = new File(path);
            tmp = new File(path + ".decrypted");
            dst = new File(path);
        }
        if (tmp != null) {
            if (tmp.exists() && tmp.delete() == false) throw new IOException("Could not delete " + tmp);
        } else {
            if (dst.exists() && dst.delete() == false) throw new IOException("Could not delete " + dst);
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        boolean deleteDst = true;
        try {
            long total = src.length();
            final PluginProgress progress = new PluginProgress(0, total, null) {
                long lastCurrent    = -1;
                long startTimeStamp = -1;

                public String getMessage(Object requestor) {
                    return "Decrypting";
                }

                @Override
                public void updateValues(long current, long total) {
                    super.updateValues(current, total);
                    if (lastCurrent == -1 || lastCurrent > current) {
                        lastCurrent = current;
                        startTimeStamp = System.currentTimeMillis();
                        this.setETA(-1);
                        return;
                    }
                    long currentTimeDifference = System.currentTimeMillis() - startTimeStamp;
                    if (currentTimeDifference <= 0) return;
                    long speed = (current * 10000) / currentTimeDifference;
                    if (speed == 0) return;
                    long eta = ((total - current) * 10000) / speed;
                    this.setETA(eta);
                }

            };
            progress.setProgressSource(this);
            progress.setIcon(NewTheme.I().getIcon("lock", 16));
            link.setPluginProgress(progress);
            fis = new FileInputStream(src);
            if (tmp != null) {
                fos = new FileOutputStream(tmp);
            } else {
                fos = new FileOutputStream(dst);
            }

            Cipher cipher = Cipher.getInstance("AES/CTR/nopadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            final CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            int read = 0;
            final byte[] buffer = new byte[32767];
            while ((read = fis.read(buffer)) != -1) {
                if (read > 0) {
                    progress.updateValues(progress.getCurrent() + read, total);
                    cos.write(buffer, 0, read);
                }
            }
            cos.close();
            try {
                fos.close();
            } catch (final Throwable e) {
            }
            try {
                fis.close();
            } catch (final Throwable e) {
            }
            deleteDst = false;
            link.getLinkStatus().setStatusText("Finished");
            try {
                link.setFinalFileOutput(dst.getAbsolutePath());
                link.setCustomFileOutputFilenameAppend(null);
                link.setCustomFileOutputFilename(null);
            } catch (final Throwable e) {
            }
            if (tmp == null) {
                src.delete();
            } else {
                src.delete();
                tmp.renameTo(dst);
            }
        } finally {
            link.setPluginProgress(null);
            try {
                fis.close();
            } catch (final Throwable e) {
            }
            try {
                fos.close();
            } catch (final Throwable e) {
            }
            if (deleteDst) {
                if (tmp != null) {
                    tmp.delete();
                } else {
                    dst.delete();
                }
            }
        }

    }

    private String getPublicFileID(DownloadLink link) {
        return new Regex(link.getDownloadURL(), "#\\!([a-zA-Z0-9]+)\\!").getMatch(0);
    }

    private String getPublicFileKey(DownloadLink link) {
        return new Regex(link.getDownloadURL(), "#\\![a-zA-Z0-9]+\\!([a-zA-Z0-9_,\\-]+)").getMatch(0);
    }

    private String getNodeFileID(DownloadLink link) {
        return new Regex(link.getDownloadURL(), "#N\\!([a-zA-Z0-9]+)\\!").getMatch(0);
    }

    private String getNodeFileKey(DownloadLink link) {
        return new Regex(link.getDownloadURL(), "#N\\![a-zA-Z0-9]+\\!([a-zA-Z0-9_,\\-]+)").getMatch(0);
    }

    private byte[] b64decode(String data) {
        data += "==".substring((2 - data.length() * 3) & 3);
        data = data.replace("-", "+").replace("_", "/").replace(",", "");
        return Base64.decode(data);
    }

    private byte[] aInt_to_aByte(int... intKey) {
        byte[] buffer = new byte[intKey.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        for (int i = 0; i < intKey.length; i++) {
            bb.putInt(intKey[i]);
        }
        return bb.array();
    }

    private int[] aByte_to_aInt(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int[] res = new int[bytes.length / 4];
        for (int i = 0; i < res.length; i++) {
            res[i] = bb.getInt(i * 4);
        }
        return res;
    }

    private String useSSL() {
        if (getPluginConfig().getBooleanProperty(USE_SSL, false)) {
            return "1";
        } else {
            return "0";
        }
    }

    private boolean useTMP() {
        if (getPluginConfig().getBooleanProperty(USE_TMP, false)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 20;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}
