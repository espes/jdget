//jDownloader - Downloadmanager
//Copyright (C) 2010  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.logging2.LogSource;

/** Works exactly like sockshare.com */
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "firedrive.com", "putlocker.com" }, urls = { "https?://(www\\.)?(m\\.)?(putlocker|firedrive)\\.com/(file|embed|mobile/file)/[A-Z0-9]+", "dg56i8zg3ufgrheiugrio9gh59zjder9gjKILL_ME_V2_frh6ujtzj" }, flags = { 2, 0 })
public class PutLockerCom extends PluginForHost {

    // TODO: fix premium, it's broken because of domainchange
    private final String                   MAINPAGE           = "http://www.firedrive.com";
    private static Object                  LOCK               = new Object();
    private static AtomicReference<String> agent              = new AtomicReference<String>(null);
    private static final String            NORESUME           = "NORESUME";
    private static final String            NOCHUNKS           = "NOCHUNKS";
    private final String[]                 servers            = new String[] { "Original format (bigger size, better quality)", "Stream format [.flv] (smaller size, less quality)" };
    private final String                   formats            = "formats";

    private static final String            PASSWORD_PROTECTED = "id=\"file_password_container\"";

    public PutLockerCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("https://auth.firedrive.com/signup");
        this.setConfigElements();
    }

    public boolean isPremiumEnabled() {
        return "firedrive.com".equalsIgnoreCase(getHost());
    }

    public Boolean rewriteHost(Account acc) {
        if ("putlocker.com".equals(getHost())) {
            if (acc != null && "putlocker.com".equals(acc.getHoster())) {
                acc.setHoster("firedrive.com");
                return true;
            }
            return false;
        }
        return null;
    }

    public Boolean rewriteHost(DownloadLink link) {
        if ("putlocker.com".equals(getHost())) {
            if (link != null && "putlocker.com".equals(link.getHost())) {
                link.setHost("firedrive.com");
                return true;
            }
            return false;
        }
        return null;
    }

    public void correctDownloadLink(DownloadLink link) throws PluginException {
        // Correct all because firedrive doesn't have the embed links though users use them anyways
        // respect users protocol import
        final String prot = new Regex(link.getDownloadURL(), "https?://").getMatch(-1);
        final String fuid = new Regex(link.getDownloadURL(), "/([A-Z0-9]+)$").getMatch(0);
        if (prot == null || fuid == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setUrlDownload(prot + "www.firedrive.com/file/" + fuid);
    }

    /* Don't use it as it doesn't work for private links */
    // @Override
    // public boolean checkLinks(final DownloadLink[] urls) {
    // if (urls == null || urls.length == 0) { return false; }
    // try {
    // final Browser br = new Browser();
    // prepBrowser();
    // br.setCookiesExclusive(true);
    // final StringBuilder sb = new StringBuilder();
    // final ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
    // int index = 0;
    // while (true) {
    // links.clear();
    // while (true) {
    // /* we test 50 links at once - limit is unknown */
    // if (index == urls.length || links.size() > 50) {
    // break;
    // }
    // links.add(urls[index]);
    // index++;
    // }
    // sb.delete(0, sb.capacity());
    // for (final DownloadLink dl : links) {
    // sb.append(getFID(dl) + "-");
    // }
    // /*
    // * They use this to show many IDs as a folder - we use this to check links. This way, we can even get filename & size for
    // * password protected links!
    // */
    // br.getPage("http://www.firedrive.com/share/" + sb.toString());
    // for (final DownloadLink dllink : links) {
    // final String fid = getFID(dllink);
    // final Regex finfo = br.getRegex("public=\\'" + fid +
    // "\\' data\\-name=\"([^<>\"]*?)\" data\\-type=\"([^<>\"]*?)\" data\\-size=\"(\\d+)\"");
    // final String filename = finfo.getMatch(0);
    // final String filesize = finfo.getMatch(2);
    // if (filename == null || filesize == null) {
    // dllink.setName(fid);
    // dllink.setAvailable(false);
    // } else {
    // dllink.setName(Encoding.htmlDecode(filename.trim()));
    // dllink.setDownloadSize(Long.parseLong(filesize));
    // dllink.setAvailable(true);
    // }
    // }
    // if (index == urls.length) {
    // break;
    // }
    // }
    // } catch (final Exception e) {
    // return false;
    // }
    // return true;
    // }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        correctDownloadLink(link);
        prepBrowser();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        // http://svn.jdownloader.org/issues/27819
        if (br.containsHTML("No htmlCode read|This file is temporarily unavailable due to maintenance\\.")) return AvailableStatus.UNCHECKABLE;
        if (br.containsHTML("This file might have been moved, replaced or deleted")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.containsHTML(PASSWORD_PROTECTED)) {
            link.getLinkStatus().setStatusText("This link is password protected");
            return AvailableStatus.TRUE;
        } else if (br.containsHTML("This file is private and only viewable by the owner")) {
            link.getLinkStatus().setStatusText("Private file - only downloadable by the owner");
            return AvailableStatus.TRUE;
        }
        String filename = br.getRegex("<b>Name:</b>([^<>\"]*?)<br>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>([^<>\"]*?)\\| Firedrive</title>").getMatch(0);
        final String filesize = br.getRegex("<b>Size:</b>([^<>\"]*?)<br>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        // User sometimes adds random stuff to filenames when downloading so we
        // better set the final name here
        link.setName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) link.setDownloadSize(SizeFormatter.getSize(filesize.trim()));
        return AvailableStatus.TRUE;
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (final PluginException e) {
            account.setValid(false);
            throw e;
        }
        br.getPage("http://www.firedrive.com/my_settings?_=" + System.currentTimeMillis());
        final String validUntil = br.getRegex("Pro features end on: ([^<>\"]*?)</span>").getMatch(0);
        if (validUntil != null) {
            ai.setValidUntil(TimeFormatter.getMilliSeconds(validUntil, "MMMM dd, yyyy", Locale.ENGLISH));
            account.setProperty("freeacc", false);
            ai.setStatus("Premium User");
        } else {
            account.setProperty("freeacc", true);
            ai.setStatus("Registered (free) user");
        }
        ai.setUnlimitedTraffic();
        account.setValid(true);
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.firedrive.com/page/terms";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    private static AtomicInteger ERROR_COUNTER = new AtomicInteger();

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        AvailableStatus availableStatus = requestFileInformation(downloadLink);
        if (availableStatus != null) {
            switch (availableStatus) {
            case FALSE:
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            case UNCHECKABLE:
            case UNCHECKED:
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "File temporarily not available", 15 * 60 * 1000l);
            case TRUE:
                doFree(downloadLink);

            }
        } else {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "File temporarily not available", 15 * 60 * 1000l);
        }

    }

    public void doFree(final DownloadLink downloadLink) throws Exception, PluginException {
        if (br.containsHTML("This file is private and only viewable by the owner")) throw new PluginException(LinkStatus.ERROR_FATAL, "Private file - only downloadable by the owner");
        br.setFollowRedirects(false);
        String passCode = null;
        // 10 MB trash-testfile: http://www.firedrive.com/file/54F8207A5D669183 PW: 12345
        if (br.containsHTML(PASSWORD_PROTECTED)) {
            passCode = handlePassword(downloadLink);
        }
        // HTTP/1.0 302 Moved
        // Location: http://10.251.45.****:15871/cgi-bin/blockpage.cgi?ws-session=****

        // unknown HTTP response

        /* Not always needed */
        final Form freeform = br.getFormbyKey("confirm");
        if (freeform != null) br.submitForm(freeform);

        if (br.containsHTML("prepare_continue_btn")) {
            sleep(5000, downloadLink, "Prepare File...");
            br.submitForm(freeform);

        }
        if (br.containsHTML("prepare_continue_btn")) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        checkForErrors();

        String dllink;
        if (getConfiguredServer() == 0) {
            dllink = getDllink_General(downloadLink);
        } else {
            dllink = getStreamDl();
            if (dllink == null) {
                logger.info("Configured type stream was not found, falling back to original format");
                dllink = getFileDllink();
            }
        }
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);

        boolean resume = true;
        if (downloadLink.getBooleanProperty(NORESUME, false)) resume = false;
        int chunks = 0;
        if (downloadLink.getBooleanProperty(PutLockerCom.NOCHUNKS, false) || !resume) {
            chunks = 1;
        }
        br.setFollowRedirects(true);
        logger.info("firedrive.com: Download will start soon");
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, resume, chunks);
        if (dl.getConnection().getContentType().contains("html")) {
            if (dl.getConnection().getResponseCode() == 416) {
                /* unknown error, we disable multiple chunks */
                if (downloadLink.getBooleanProperty(PutLockerCom.NOCHUNKS, false) == false) {
                    downloadLink.setProperty(PutLockerCom.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Unknown server error 416", calculateDynamicWaittime(10));
            }
            logger.warning("firedrive.com: final link leads to html code...");
            br.followConnection();

            if (br.getRequest() != null && br.getRequest().getHttpConnection() != null && br.getRequest().getHttpConnection().getResponseCode() == 403) {
                // HTTP/1.1 403 Forbidden
                // Server: cloudflare-nginx
                // Date: Sat, 05 Apr 2014 13:05:07 GMT
                // Content-Type: text/html
                // Transfer-Encoding: chunked
                // Connection: close
                // Access-Control-Allow-Methods: GET, POST, OPTIONS
                // Access-Control-Allow-Headers:
                // Origin,Referer,Accept-Encoding,Accept-Language,Accept,DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type
                // Access-Control-Allow-Origin: *
                // CF-RAY: *****-MIA
                // Content-Encoding: gzip

                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server Error. Try again later", 5 * 60 * 1000l);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        fixFilename(downloadLink);
        downloadLink.setProperty("pass", passCode);
        try {
            if (!this.dl.startDownload()) {
                try {
                    if (dl.externalDownloadStop()) return;
                } catch (final Throwable e) {
                }
                /* unknown error, we disable multiple chunks */
                if (downloadLink.getBooleanProperty(PutLockerCom.NOCHUNKS, false) == false) {
                    downloadLink.setProperty(PutLockerCom.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
                logger.warning("firedrive.com: Unknown error1");
                int timesFailed = downloadLink.getIntegerProperty("timesfailedfiredrivecom_unknown1", 0);
                downloadLink.getLinkStatus().setRetryCount(0);
                if (timesFailed <= 2) {
                    timesFailed++;
                    downloadLink.setProperty("timesfailedfiredrivecom_unknown1", timesFailed);
                    throw new PluginException(LinkStatus.ERROR_RETRY, "Unknown error1");
                } else {
                    downloadLink.setProperty("timesfailedfiredrivecom_unknown1", Property.NULL);
                    logger.info("firedrive.com: Unknown error1 - plugin broken!");
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
            }
        } catch (final PluginException e) {
            LogSource.exception(logger, e);
            // New V2 errorhandling
            /* unknown error, we disable multiple chunks */
            if (e.getLinkStatus() != LinkStatus.ERROR_RETRY && downloadLink.getBooleanProperty(PutLockerCom.NOCHUNKS, false) == false) {
                downloadLink.setProperty(PutLockerCom.NOCHUNKS, Boolean.valueOf(true));
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }

            if (e.getLinkStatus() == LinkStatus.ERROR_DOWNLOAD_INCOMPLETE) {
                logger.info("ERROR_DOWNLOAD_INCOMPLETE --> Handling it");
                int timesFailed = downloadLink.getIntegerProperty("timesfailedfiredrivecom_download_incomplete", 0);
                downloadLink.getLinkStatus().setRetryCount(0);
                if (timesFailed <= 2) {
                    timesFailed++;
                    downloadLink.setProperty("timesfailedfiredrivecom_download_incomplete", timesFailed);
                    throw new PluginException(LinkStatus.ERROR_RETRY, "download_incomplete");
                } else {
                    downloadLink.setProperty("timesfailedfiredrivecom_download_incomplete", Property.NULL);
                    logger.info("firedrive.com: timesfailedfiredrivecom_download_incomplete");
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Fatal server error");
                }

            }

            logger.warning("firedrive.com: Unknown error2");
            int timesFailed = downloadLink.getIntegerProperty("timesfailedfiredrivecom_unknown2", 0);
            downloadLink.getLinkStatus().setRetryCount(0);
            if (timesFailed <= 2) {
                timesFailed++;
                downloadLink.setProperty("timesfailedfiredrivecom_unknown2", timesFailed);
                /* unknown error, we disable multiple chunks */
                if (downloadLink.getBooleanProperty(PutLockerCom.NOCHUNKS, false) == false) {
                    logger.warning("firedrive.com: Unknown error2 -> Retrying without chunkload");
                    downloadLink.setProperty(PutLockerCom.NOCHUNKS, Boolean.valueOf(true));
                }
                throw new PluginException(LinkStatus.ERROR_RETRY, "Unknown error2");
            } else {
                downloadLink.setProperty("timesfailedfiredrivecom_unknown2", Property.NULL);
                logger.info("firedrive.com: Unknown error2 - plugin broken!");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }

        } catch (final InterruptedException e) {
            logger.warning("firedrive.com: Unknown error3");
            int timesFailed = downloadLink.getIntegerProperty("timesfailedfiredrivecom_unknown3", 0);
            downloadLink.getLinkStatus().setRetryCount(0);
            if (timesFailed <= 2) {
                timesFailed++;
                downloadLink.setProperty("timesfailedfiredrivecom_unknown3", timesFailed);
                throw new PluginException(LinkStatus.ERROR_RETRY, "Unknown error3");
            } else {
                downloadLink.setProperty("timesfailedfiredrivecom_unknown3", Property.NULL);
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Unknown error3", calculateDynamicWaittime(30));
            }
        }
    }

    private String handlePassword(final DownloadLink dl) throws IOException, PluginException {
        String passCode = dl.getStringProperty("pass");
        if (passCode == null) passCode = Plugin.getUserInput("Password?", dl);
        br.postPage(br.getURL(), "item_pass=" + Encoding.urlEncode(passCode));
        if (br.containsHTML(PASSWORD_PROTECTED)) {
            dl.setProperty("pass", Property.NULL);
            throw new PluginException(LinkStatus.ERROR_RETRY, "Wrong password");
        }
        return passCode;
    }

    protected void checkForErrors() throws PluginException {
        if (br.containsHTML("This file is temporarily unavailable due to maintenance")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "File temporarily not available", 5 * 60 * 1000l); }
        // Add firedrive errorhandling here
    }

    protected long calculateDynamicWaittime(int maxMinutes) {
        return (long) (Math.min(maxMinutes * 60, (10 + Math.pow(2, ERROR_COUNTER.incrementAndGet()))) * 1000l);
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("No htmlCode read")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 5 * 60 * 1000l);
        if (account.getBooleanProperty("freeacc", false)) {
            doFree(link);
        } else {
            if (br.containsHTML("This file is private and only viewable by the owner")) throw new PluginException(LinkStatus.ERROR_FATAL, "Private file - only downloadable by the owner");
            String passCode = null;
            // 10 MB trash-testfile: http://www.firedrive.com/file/54F8207A5D669183 PW: 12345
            if (br.containsHTML(PASSWORD_PROTECTED)) {
                passCode = handlePassword(link);
            }
            br.setFollowRedirects(true);
            final String dlURL = getFileDllink();
            if (dlURL == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, dlURL, true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                br.followConnection();
                checkForErrors();
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            link.setProperty("pass", passCode);
            fixFilename(link);
            dl.startDownload();
        }
    }

    public void prepBrowser() {
        // define custom browser headers and language settings.
        br.getHeaders().put("Accept-Language", "en-us;q=0.7,en;q=0.3");
        br.getHeaders().put("Accept-Charset", null);
        if (agent.get() == null) {
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            agent.set(jd.plugins.hoster.MediafireCom.stringUserAgent());
        }
        br.getHeaders().put("User-Agent", agent.get());
    }

    private void login(final Account account, final boolean fetchInfo) throws Exception {
        synchronized (LOCK) {
            try {
                /** Load cookies */
                prepBrowser();
                br.getHeaders().put("Accept-Charset", null);
                br.setCookiesExclusive(true);
                final Object ret = account.getProperty("cookies", null);
                boolean cookiesSet = false;
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                if (acmatch && ret != null && ret instanceof Map<?, ?>) {
                    final Map<String, String> cookies = (Map<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            this.br.setCookie(MAINPAGE, key, value);
                            cookiesSet = true;
                        }
                    }
                }
                if (!fetchInfo && cookiesSet) return;
                br.setFollowRedirects(true);
                br.postPage("https://auth.firedrive.com/", "remember=1&json=1&user=" + Encoding.urlEncode(account.getUser()) + "&pass=" + Encoding.urlEncode(account.getPass()));
                // no auth = not logged / invalid account.
                if (br.getCookie(MAINPAGE, "auth") == null) { throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password wrong!\r\nUngültiger Benutzername oder ungültiges Passwort!", PluginException.VALUE_ID_PREMIUM_DISABLE); }
                /** Save cookies */
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = this.br.getCookies(MAINPAGE);
                for (final Cookie c : add.getCookies()) {
                    cookies.put(c.getKey(), c.getValue());
                }
                account.setProperty("name", Encoding.urlEncode(account.getUser()));
                account.setProperty("pass", Encoding.urlEncode(account.getPass()));
                account.setProperty("cookies", cookies);
            } catch (final PluginException e) {
                account.setProperty("cookies", Property.NULL);
                throw e;
            }
        }
    }

    private String getDllink_General(DownloadLink downloadLink) throws IOException, PluginException {
        String dllink = null;
        // get file_dllink
        dllink = getFileDllink();
        if (dllink == null) {
            // check if there is a video stream... this is generally of lesser quality!
            dllink = getStreamDl();
        }
        return dllink;
    }

    private String getStreamDl() throws IOException {
        String dllink = null;
        final String stream_dl = br.getRegex("('|\")(http://dl\\.firedrive\\.com/\\?stream=[^<>\"]*?)\\'").getMatch(1);
        if (stream_dl != null) {
            final Browser br2 = br.cloneBrowser();
            br2.postPage(stream_dl, "");
            dllink = br2.toString();
        }
        if (dllink != null && (!dllink.startsWith("http") || dllink.length() > 500)) dllink = null;
        if (dllink != null) dllink = dllink.replace("&amp;", "&");
        return dllink;
    }

    private String getFileDllink() {
        String dllink = br.getRegex("\"(https?://dl\\.firedrive\\.com/[^<>\"]+)\"").getMatch(0);
        if (dllink != null) dllink = dllink.replace("&amp;", "&");
        return dllink;
    }

    private String getFID(final DownloadLink dl) {
        return new Regex(dl.getDownloadURL(), "([A-Z0-9]+)$").getMatch(0);
    }

    private void fixFilename(final DownloadLink downloadLink) {
        String oldName = downloadLink.getFinalFileName();
        if (oldName == null) oldName = downloadLink.getName();
        final String serverFilename = Encoding.htmlDecode(getFileNameFromHeader(dl.getConnection()));
        String newExtension = null;
        // some streaming sites do not provide proper file.extension within
        // headers (Content-Disposition or the fail over getURL()).
        if (serverFilename.contains(".")) {
            newExtension = serverFilename.substring(serverFilename.lastIndexOf("."));
        } else {
            logger.info("HTTP headers don't contain filename.extension information");
        }
        if (newExtension != null && !oldName.endsWith(newExtension)) {
            String oldExtension = null;
            if (oldName.contains(".")) oldExtension = oldName.substring(oldName.lastIndexOf("."));
            if (oldExtension != null && oldExtension.length() <= 5)
                downloadLink.setFinalFileName(oldName.replace(oldExtension, newExtension));
            else
                downloadLink.setFinalFileName(oldName + newExtension);
        }
    }

    private int getConfiguredServer() {
        switch (getPluginConfig().getIntegerProperty(formats, -1)) {
        case 0:
            logger.fine("Original format is configured");
            return 0;
        case 1:
            logger.fine("Stream format is configured");
            return 1;
        default:
            logger.fine("No format is cunfigured, returning default format (original format)");
            return 0;
        }
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_COMBOBOX_INDEX, getPluginConfig(), formats, servers, JDL.L("plugins.host.firedrivecom.preferredformats", "Format selection - select your prefered format:\r\nBy default, JDownloader will download the original format if possible.\r\nIf the desired format isn't available, JDownloader will download the other one.\r\n\rPremium users can only download the original format.")).setDefaultValue(0));
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean hasCaptcha(DownloadLink link, jd.plugins.Account acc) {
        if (acc == null) {
            /* no account, yes we can expect captcha */
            return true;
        }
        if (Boolean.TRUE.equals(acc.getBooleanProperty("freeacc"))) {
            /* free accounts also have captchas */
            return true;
        }
        return false;
    }
}