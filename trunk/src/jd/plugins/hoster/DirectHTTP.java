//    jDownloader - Downloadmanager
//    Copyright (C) 2012  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import jd.PluginWrapper;
import jd.config.Property;
import jd.controlling.HTACCESSController;
import jd.http.Browser;
import jd.http.Cookies;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.FilePackage;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

/**
 * TODO: Remove after next big update of core to use the public static methods!
 */
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "DirectHTTP", "http links" }, urls = { "directhttp://.+", "https?viajd://[\\w\\.:\\-@]*/.*\\.(jdeatme|3gp|7zip|7z|abr|ac3|aiff|aifc|aif|ai|au|avi|apk|bin|bmp|bat|bz2|cbr|cbz|ccf|chm|cr2|cso|cue|cvd|dta|deb|divx|djvu|dlc|dmg|doc|docx|dot|eps|epub|exe|ff|flv|flac|f4v|gsd|gif|gpg|gz|iwd|idx|iso|ipa|ipsw|java|jar|jpg|jpeg|load|m2ts|mws|mv|m4v|m4a|mkv|mp2|mp3|mp4|mobi|mov|movie|mpeg|mpe|mpg|mpq|msi|msu|msp|nfo|npk|oga|ogg|ogv|otrkey|par2|pkg|png|pdf|pptx|ppt|ppsx|ppsx|ppz|pot|psd|qt|rmvb|rm|rar|ram|ra|rev|rnd|[r-z]\\d{2}|r\\d+|rpm|run|rsdf|reg|rtf|shnf|sh(?!tml)|ssa|smi|sub|srt|snd|sfv|sfx|swf|swc|tar\\.gz|tar\\.bz2|tar\\.xz|tar|tgz|tiff|tif|ts|txt|viv|vivo|vob|vtt|webm|wav|wmv|wma|xla|xls|xpi|zeno|zip|z\\d+|_[_a-z]{2}|\\d+(?=\\?|$|\"|\r|\n))" }, flags = { 2, 0 })
public class DirectHTTP extends PluginForHost {

    public static class Recaptcha {

        private static final int MAX_TRIES    = 5;
        private final Browser    br;
        private String           challenge;
        private String           server;
        private String           captchaAddress;
        private String           id;
        private Browser          rcBr;
        private Form             form;
        private int              tries        = 0;
        private boolean          clearReferer = true;

        public Recaptcha(final Browser br) {
            this.br = br;
        }

        public File downloadCaptcha(final File captchaFile) throws IOException, PluginException {
            /* follow redirect needed as google redirects to another domain */
            if (this.getTries() > 0) {
                this.reload();
            }
            this.rcBr.setFollowRedirects(true);
            try {
                Browser.download(captchaFile, this.rcBr.openGetConnection(this.captchaAddress));
            } catch (final IOException e) {
                captchaFile.delete();
                throw e;
            }
            return captchaFile;
        }

        public void findID() throws PluginException {
            this.id = this.br.getRegex("\\?k=([A-Za-z0-9%_\\+\\- ]+)\"").getMatch(0);
            if (this.id == null) {
                this.id = this.br.getRegex("Recaptcha\\.create\\((\"|\\')([A-Za-z0-9%_\\+\\- ]+)(\"|\\')").getMatch(1);
            }
            if (this.id == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        }

        public String getCaptchaAddress() {
            return this.captchaAddress;
        }

        public String getChallenge() {
            return this.challenge;
        }

        public Form getForm() {
            return this.form;
        }

        public String getId() {
            return this.id;
        }

        public String getServer() {
            return this.server;
        }

        public int getTries() {
            return this.tries;
        }

        public synchronized void handleAuto(final Plugin plg, final DownloadLink downloadLink) throws Exception {

            if (this.form == null || this.id == null) {
                this.parse();
            }

            this.load();
            while (!this.isSolved()) {

                int count = 1;

                File dest = null;
                while (dest == null || dest.exists()) {
                    dest = JDUtilities.getResourceFile("captchas/recaptcha_" + plg.getHost() + "_" + count + ".jpg", true);
                    dest.deleteOnExit();
                    count++;
                }
                dest.deleteOnExit();

                this.downloadCaptcha(dest);
                // workaround
                final String code = new DirectHTTP(PluginWrapper.getWrapper(plg.getClass().getName())).getCaptchaCode(plg.getHost(), dest, 0, downloadLink, "", "Please enter both words");

                if (code == null || code.length() == 0) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Recaptcha failed", 10 * 1000l);
                }
                this.setCode(code);

            }
        }

        public boolean isSolved() throws PluginException {
            if (this.tries > Recaptcha.MAX_TRIES) {
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            }
            try {
                this.parse();
                this.findID();
                // recaptcha still found. so it is not solved yet
                return false;
            } catch (final Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        public void load() throws IOException, PluginException {
            this.rcBr = this.br.cloneBrowser();
            // recaptcha works off API key, and javascript. The imported browser session isn't actually needed.
            /*
             * Randomise user-agent to prevent tracking by google, each time we load(). Without this they could make the captchas images
             * harder read, the more a user requests captcha'. Also algos could track captcha requests based on user-agent globally, which
             * means JD default user-agent been very old (firefox 3.x) negatively biased to JD clients! Tracking takes place on based on IP
             * address, User-Agent, and APIKey of request (site of APIKey), cookies session submitted, and combinations of those.
             * Effectively this can all be done with a new browser, with regex tasks from source browser (ids|keys|submitting forms).
             */
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            this.rcBr.getHeaders().put("User-Agent", jd.plugins.hoster.MediafireCom.stringUserAgent());

            // this prevents google/recaptcha group from seeing referrer
            try {
                if (this.clearReferer) {
                    this.rcBr.setCurrentURL(null);
                }
            } catch (final Throwable e) {
                /* 09581 will break here */
            }

            // end of privacy protection

            /* follow redirect needed as google redirects to another domain */
            this.rcBr.setFollowRedirects(true);
            this.rcBr.getPage("http://api.recaptcha.net/challenge?k=" + this.id);
            this.challenge = this.rcBr.getRegex("challenge.*?:.*?'(.*?)',").getMatch(0);
            this.server = this.rcBr.getRegex("server.*?:.*?'(.*?)',").getMatch(0);
            if (this.challenge == null || this.server == null) {
                System.out.println("Recaptcha Module fails: " + this.rcBr.getHttpConnection());
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            this.captchaAddress = this.server + "image?c=" + this.challenge;
        }

        public void parse() throws IOException, PluginException {
            this.form = null;
            this.id = null;
            if (this.br.containsHTML("Recaptcha\\.create\\(\".*?\"\\,\\s*\".*?\"\\,.*?\\)")) {
                this.id = this.br.getRegex("Recaptcha\\.create\\(\"(.*?)\"").getMatch(0);
                final String div = this.br.getRegex("Recaptcha\\.create\\(\"(.*?)\"\\,\\s*\"(.*?)\"").getMatch(1);

                // find form that contains the found div id
                if (div == null || this.id == null) {
                    System.out.println("reCaptcha ID or div couldn't be found...");

                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
                for (final Form f : this.br.getForms()) {
                    if (f.containsHTML("id\\s*?=\\s*?\"" + div + "\"")) {
                        this.form = f;
                        break;
                    }
                }

            } else {
                final Form[] forms = this.br.getForms();
                this.form = null;
                for (final Form f : forms) {
                    if (f.getInputField("recaptcha_challenge_field") != null) {
                        this.form = f;
                        break;
                    }
                }
                if (this.form == null) {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                } else {
                    this.id = this.form.getRegex("k=(.*?)\"").getMatch(0);
                    if (this.id == null || this.id.equals("") || this.id.contains("\\")) {
                        this.findID();
                    }
                    if (this.id == null || this.id.equals("")) {
                        System.out.println("reCaptcha ID couldn't be found...");
                        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                    } else {
                        this.id = this.id.replace("&amp;error=1", "");
                    }
                }
            }
            if (this.id == null || this.id.equals("")) {
                System.out.println("reCaptcha ID couldn't be found...");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (this.form == null) {
                System.out.println("reCaptcha form couldn't be found...");

                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }

        }

        /* do not use for plugins at the moment */
        private void prepareForm(final String code) throws PluginException {
            if (this.challenge == null || code == null) {
                System.out.println("Recaptcha Module fail: challenge or code equals null!");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            this.form.put("recaptcha_challenge_field", this.challenge);
            this.form.put("recaptcha_response_field", Encoding.urlEncode(code));
        }

        public void reload() throws IOException, PluginException {

            this.rcBr.getPage("http://www.google.com/recaptcha/api/reload?c=" + this.challenge + "&k=" + this.id + "&reason=r&type=image&lang=en");
            this.challenge = this.rcBr.getRegex("Recaptcha\\.finish\\_reload\\(\\'(.*?)\\'\\, \\'image\\'").getMatch(0);

            if (this.challenge == null) {
                System.out.println("Recaptcha Module fails: " + this.rcBr.getHttpConnection());
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            this.captchaAddress = this.server + "image?c=" + this.challenge;
        }

        public void setCaptchaAddress(final String captchaAddress) {
            this.captchaAddress = captchaAddress;
        }

        public void setChallenge(final String challenge) {
            this.challenge = challenge;
        }

        public void setClearReferer(final boolean clearReferer) {
            this.clearReferer = clearReferer;
        }

        public Browser setCode(final String code) throws Exception {
            // <textarea name="recaptcha_challenge_field" rows="3"
            // cols="40"></textarea>\n <input type="hidden"
            // name="recaptcha_response_field" value="manual_challenge"/>
            this.prepareForm(code);
            this.br.submitForm(this.form);
            this.tries++;
            return this.br;
        }

        public void setForm(final Form form) {
            this.form = form;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setServer(final String server) {
            this.server = server;
        }
    }

    private static final String JDL_PREFIX        = "jd.plugins.hoster.DirectHTTP.";

    public static final String  ENDINGS           = "\\.(jdeatme|3gp|7zip|7z|abr|ac3|aiff|aifc|aif|ai|au|avi|apk|bin|bmp|bat|bz2|cbr|cbz|ccf|chm|cr2|cso|cue|cvd|dta|deb|divx|djvu|dlc|dmg|doc|docx|dot|eps|epub|exe|ff|flv|flac|f4v|gsd|gif|gpg|gz|iwd|idx|iso|ipa|ipsw|java|jar|jpg|jpeg|load|m2ts|mws|mv|m4v|m4a|mkv|mp2|mp3|mp4|mobi|mov|movie|mpeg|mpe|mpg|mpq|msi|msu|msp|nfo|npk|oga|ogg|ogv|otrkey|par2|pkg|png|pdf|pptx|ppt|ppsx|ppsx|ppz|pot|psd|qt|rmvb|rm|rar|ram|ra|rev|rnd|[r-z]\\d{2}|r\\d+|rpm|run|rsdf|reg|rtf|shnf|sh(?!tml)|ssa|smi|sub|srt|snd|sfv|sfx|swf|swc|tar\\.gz|tar\\.bz2|tar\\.xz|tar|tgz|tiff|tif|ts|txt|viv|vivo|vob|vtt|webm|wav|wmv|wma|xla|xls|xpi|zeno|zip|z\\d+|_[_a-z]{2}|\\d+)";
    public static final String  NORESUME          = "nochunkload";
    public static final String  NOCHUNKS          = "nochunk";
    public static final String  FORCE_NORESUME    = "forcenochunkload";
    public static final String  FORCE_NOCHUNKS    = "forcenochunk";
    public static final String  TRY_ALL           = "tryall";
    public static final String  POSSIBLE_URLPARAM = "POSSIBLE_GETPARAM";

    @Override
    public ArrayList<DownloadLink> getDownloadLinks(final String data, final FilePackage fp) {
        final ArrayList<DownloadLink> ret = super.getDownloadLinks(data, fp);
        try {
            if (ret != null && ret.size() == 1) {
                String modifiedData = null;
                if (data.startsWith("directhttp://")) {
                    modifiedData = data.replace("directhttp://", "");
                } else {
                    modifiedData = data.replace("httpsviajd://", "https://");
                    modifiedData = modifiedData.replace("httpviajd://", "http://");
                    modifiedData = modifiedData.replace(".jdeatme", "");
                }
                /* single link parsing in svn/jd2 */
                final String url = ret.get(0).getDownloadURL();
                final int idx = modifiedData.indexOf(url);
                if (idx >= 0 && modifiedData.length() >= idx + url.length()) {
                    String param = modifiedData.substring(idx + url.length());
                    if (param != null) {
                        param = new Regex(param, "(.*?)(\r|\n|$)").getMatch(0);
                        if (param != null && param.trim().length() != 0) {
                            ret.get(0).setProperty(DirectHTTP.POSSIBLE_URLPARAM, new String(param));
                        }
                    }
                }
            }
        } catch (final Throwable e) {
            this.logger.severe(e.getMessage());
        }
        return ret;
    }

    @Override
    public boolean isValidURL(String URL) {
        URL = URL.toLowerCase(Locale.ENGLISH);
        if (URL.contains("facebook.com/l.php")) {
            return false;
        }
        if (URL.contains("facebook.com/ajax/sharer/")) {
            return false;
        }
        if (URL.contains("youtube.com/videoplayback") && URL.startsWith("http")) {
            return false;
        }
        return true;
    }

    /**
     * TODO: Remove with next major-update!
     */
    public static ArrayList<String> findUrls(final String source) {
        /* TODO: better parsing */
        /* remove tags!! */
        final ArrayList<String> ret = new ArrayList<String>();
        try {

            for (final String link : new Regex(source, "((https?|ftp):((//)|(\\\\\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)(\n|\r|$|<|\")").getColumn(0)) {
                try {
                    new URL(link);
                    if (!ret.contains(link)) {
                        ret.add(link);
                    }
                } catch (final MalformedURLException e) {

                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return DirectHTTP.removeDuplicates(ret);
    }

    /**
     * TODO: Remove with next major-update!
     */
    public static ArrayList<String> removeDuplicates(final ArrayList<String> links) {
        final ArrayList<String> tmplinks = new ArrayList<String>();
        if (links == null || links.size() == 0) {
            return tmplinks;
        }
        for (final String link : links) {
            if (link.contains("...")) {
                final String check = link.substring(0, link.indexOf("..."));
                String found = link;
                for (final String link2 : links) {
                    if (link2.startsWith(check) && !link2.contains("...")) {
                        found = link2;
                        break;
                    }
                }
                if (!tmplinks.contains(found)) {
                    tmplinks.add(found);
                }
            } else {
                tmplinks.add(link);
            }
        }
        return tmplinks;
    }

    private String               contentType       = "";

    private String               customFavIconHost = null;

    private static AtomicBoolean hotFixSynthethica = new AtomicBoolean(true);

    public DirectHTTP(final PluginWrapper wrapper) {
        super(wrapper);
        if (DirectHTTP.hotFixSynthethica.get()) {
            try {
                /*
                 * hotfix for synthetica license issues, as some java versions have broken aes support
                 */
                /*
                 * NOTE: This Licensee Information may only be used by AppWork UG. If you like to create derived creation based on this
                 * sourcecode, you have to remove this license key. Instead you may use the FREE Version of synthetica found on javasoft.de
                 */
                final String[] li = { "Licensee=AppWork UG", "LicenseRegistrationNumber=289416475", "Product=Synthetica", "LicenseType=Small Business License", "ExpireDate=--.--.----", "MaxVersion=2.999.999" };
                javax.swing.UIManager.put("Synthetica.license.info", li);
                javax.swing.UIManager.put("Synthetica.license.key", "C1410294-61B64AAC-4B7D3039-834A82A1-37E5D695");
            } catch (final Throwable e) {
            }
            DirectHTTP.hotFixSynthethica.set(false);
        }
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) {
        if (link.getDownloadURL().startsWith("directhttp")) {
            link.setUrlDownload(link.getDownloadURL().replaceAll("^directhttp://", ""));
        } else {
            link.setUrlDownload(link.getDownloadURL().replaceAll("httpviajd://", "http://").replaceAll("httpsviajd://", "https://"));
            /* this extension allows to manually add unknown extensions */
            link.setUrlDownload(link.getDownloadURL().replaceAll("\\.jdeatme$", ""));
        }
    }

    @Override
    public String getAGBLink() {
        return "";
    }

    private String[] getBasicAuth(final DownloadLink link) throws PluginException {
        String username = null;
        String password = null;

        try {
            // jd2

            org.jdownloader.auth.Login logins = requestLogins(org.jdownloader.translate._JDT._.DirectHTTP_getBasicAuth_message(), link);
            return new String[] { logins.toBasicAuth(), logins.getUsername(), logins.getPassword() };
        } catch (PluginException e) {
            throw e;
        } catch (Throwable t) {
            // jd1
            String url;
            if (link.getLinkType() == DownloadLink.LINKTYPE_CONTAINER) {
                url = link.getHost();
            } else {
                url = link.getBrowserUrl();
            }

            try {
                username = Plugin.getUserInput(JDL.LF(DirectHTTP.JDL_PREFIX + "username", "Username (BasicAuth) for %s", url), link);
                password = Plugin.getUserInput(JDL.LF(DirectHTTP.JDL_PREFIX + "password", "Password (BasicAuth) for %s", url), link);
            } catch (final Exception e) {
                return null;
            }
            return new String[] { "Basic " + Encoding.Base64Encode(username + ":" + password), username, password };
        }
    }

    @Override
    public String getCustomFavIconURL(final DownloadLink link) {
        if (link != null) {
            final String domain = Browser.getHost(link.getDownloadURL(), true);
            if (domain != null) {
                return domain;
            }
        }
        if (this.customFavIconHost != null) {
            return this.customFavIconHost;
        }
        return null;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    /**
     * TODO: can be removed with next major update cause of recaptcha change
     */
    public Recaptcha getReCaptcha(final Browser br) {
        return new Recaptcha(br);
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        if (this.requestFileInformation(downloadLink) == AvailableStatus.UNCHECKABLE) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 15 * 60 * 1000l);
        }
        final String auth = this.br.getHeaders().get("Authorization");
        /*
         * replace with br.setCurrentURL(null); in future (after 0.9)
         */
        this.br = new Browser();/* needed to clean referer */
        if (auth != null) {
            this.br.getHeaders().put("Authorization", auth);
        }
        /* workaround to clear referer */
        this.br.setFollowRedirects(true);
        this.br.setDebug(true);
        boolean resume = true;
        int chunks = 0;

        if (downloadLink.getBooleanProperty(DirectHTTP.NORESUME, false) || downloadLink.getBooleanProperty(DirectHTTP.FORCE_NORESUME, false)) {
            resume = false;
        }
        if (downloadLink.getBooleanProperty(DirectHTTP.NOCHUNKS, false) || downloadLink.getBooleanProperty(DirectHTTP.FORCE_NOCHUNKS, false) || resume == false) {
            chunks = 1;
        }
        this.setCustomHeaders(this.br, downloadLink);
        if (downloadLink.getStringProperty("post", null) != null) {
            this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, downloadLink, downloadLink.getDownloadURL(), downloadLink.getStringProperty("post", null), resume, chunks);
        } else {
            this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, downloadLink, downloadLink.getDownloadURL(), resume, chunks);
        }
        if (this.dl.getConnection().getResponseCode() == 503) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 15 * 60 * 1000l);
        }
        try {
            if (!this.dl.startDownload()) {
                try {
                    if (this.dl.externalDownloadStop()) {
                        return;
                    }
                } catch (final Throwable e) {
                    // not stable compatible
                }
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            } else if (downloadLink.getLinkStatus().getErrorMessage() != null && downloadLink.getLinkStatus().getErrorMessage().startsWith(JDL.L("download.error.message.rangeheaders", "Server does not support chunkload")) || this.dl.getConnection().getResponseCode() == 400 && this.br.getRequest().getHttpConnection().getHeaderField("server").matches("HFS.+")) {
                if (downloadLink.getBooleanProperty(DirectHTTP.NORESUME, false) == false) {
                    downloadLink.setChunksProgress(null);
                    downloadLink.setProperty(DirectHTTP.NORESUME, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
            } else if (downloadLink.getLinkStatus().hasStatus(1 << 13)) {
                return;
            } else {
                /* unknown error, we disable multiple chunks */
                if (downloadLink.getBooleanProperty(DirectHTTP.NOCHUNKS, false) == false) {
                    downloadLink.setProperty(DirectHTTP.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
            }
            throw e;
        }
    }

    private URLConnectionAdapter prepareConnection(final Browser br, final DownloadLink downloadLink) throws IOException {
        URLConnectionAdapter urlConnection = null;
        this.setCustomHeaders(br, downloadLink);
        if (downloadLink.getStringProperty("post", null) != null) {
            urlConnection = br.openPostConnection(downloadLink.getDownloadURL(), downloadLink.getStringProperty("post", null));
        } else {
            urlConnection = br.openGetConnection(downloadLink.getDownloadURL());
        }
        return urlConnection;
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        this.handleFree(link);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws PluginException {
        if (downloadLink.getBooleanProperty("OFFLINE", false)) {
            // used to make offline links for decrypters. To prevent 'Checking online status' and/or prevent downloads of downloadLink.
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        // if (true) throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, 60 * 1000l);
        this.setBrowserExclusive();
        /* disable gzip, because current downloadsystem cannot handle it correct */
        this.br.getHeaders().put("Accept-Encoding", "identity");
        final String authinURL = new Regex(downloadLink.getDownloadURL(), "https?://(.+)@.*?($|/)").getMatch(0);
        String authSaved = null;
        String authProperty = null;
        final ArrayList<String> pwTries = new ArrayList<String>();
        final String preAuth = downloadLink.getStringProperty("auth", null);
        if (preAuth != null) {
            pwTries.add(preAuth);
        }
        pwTries.add("");
        if (authinURL != null) {
            /* take auth from url */
            pwTries.add("Basic " + Encoding.Base64Encode(authinURL));
        }
        if ((authProperty = downloadLink.getStringProperty("pass", null)) != null) {
            /* convert property to auth */
            pwTries.add("Basic " + Encoding.Base64Encode(authProperty));
        }

        try {
            // jd2 only

            for (org.jdownloader.auth.Login l : org.jdownloader.auth.AuthenticationController.getInstance().getSortedLoginsList(downloadLink.getDownloadURL())) {
                pwTries.add(l.toBasicAuth());
            }
        } catch (Throwable e) {

            // /jd1
            if ((authSaved = HTACCESSController.getInstance().get(downloadLink.getDownloadURL())) != null) {
                /* use auth from saved ones */
                pwTries.add("Basic " + Encoding.Base64Encode(authSaved));
            }
        }
        this.br.setFollowRedirects(true);
        URLConnectionAdapter urlConnection = null;
        try {
            String basicauth = null;
            for (final String pw : pwTries) {
                if (pw != null && pw.length() > 0) {
                    basicauth = pw;
                    this.br.getHeaders().put("Authorization", pw);
                } else {
                    basicauth = null;
                    this.br.getHeaders().remove("Authorization");
                }
                urlConnection = this.prepareConnection(this.br, downloadLink);
                String urlParams = null;
                if ((urlConnection.getResponseCode() == 401 || urlConnection.getResponseCode() == 400 || urlConnection.getResponseCode() == 404 || urlConnection.getResponseCode() == 403) && (urlParams = downloadLink.getStringProperty(DirectHTTP.POSSIBLE_URLPARAM, null)) != null) {
                    /* check if we need the URLPARAMS to download the file */
                    urlConnection.disconnect();
                    final String newURL = downloadLink.getDownloadURL() + urlParams;
                    downloadLink.setProperty(DirectHTTP.POSSIBLE_URLPARAM, Property.NULL);
                    downloadLink.setUrlDownload(newURL);
                    urlConnection = this.prepareConnection(this.br, downloadLink);
                }
                if (urlConnection.getResponseCode() == 401) {
                    if (urlConnection.getHeaderField("WWW-Authenticate") == null) {
                        /* no basic auth */
                        throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                    }
                    urlConnection.disconnect();
                    invalidateLogins(pw, downloadLink);
                } else {
                    validateLogins(downloadLink, pw);
                    break;
                }
            }

            if (urlConnection.getResponseCode() == 401) {
                final String[] basicauthInfo = this.getBasicAuth(downloadLink);
                if (basicauthInfo == null) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND, JDL.L("plugins.hoster.httplinks.errors.basicauthneeded", "BasicAuth needed"));
                }
                basicauth = basicauthInfo[0];
                this.br.getHeaders().put("Authorization", basicauth);
                urlConnection = this.prepareConnection(this.br, downloadLink);
                if (urlConnection.getResponseCode() == 401) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND, JDL.L("plugins.hoster.httplinks.errors.basicauthneeded", "BasicAuth needed"));
                }
                HTACCESSController.getInstance().addValidatedAuthentication(downloadLink.getDownloadURL(), basicauthInfo[1], basicauthInfo[2]);
                validateLogins(downloadLink, basicauthInfo[1], basicauthInfo[2]);

            }
            if (urlConnection.getResponseCode() == 404 || !urlConnection.isOK()) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            downloadLink.setProperty("auth", basicauth);

            if (urlConnection.getResponseCode() == 503) {
                return AvailableStatus.UNCHECKABLE;
            }
            this.contentType = urlConnection.getContentType();
            if (this.contentType != null && this.contentType.startsWith("application/pls") && downloadLink.getName().endsWith("mp3")) {
                this.br.followConnection();
                final String mp3URL = this.br.getRegex("(https?://.*?\\.mp3)").getMatch(0);
                if (downloadLink.getBooleanProperty("htmlRedirect", false)) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                }
                if (mp3URL != null) {
                    downloadLink.setUrlDownload(mp3URL);
                    /* we set property here to avoid loops */
                    downloadLink.setProperty("htmlRedirect", true);
                    return this.requestFileInformation(downloadLink);
                }
            }
            if (this.contentType != null && this.contentType.startsWith("text/html") && urlConnection.isContentDisposition() == false && downloadLink.getBooleanProperty(DirectHTTP.TRY_ALL, false) == false) {
                /* jd does not want to download html content! */
                /* if this page does redirect via js/html, try to follow */
                this.br.followConnection();
                /* search urls */
                /*
                 * TODO: Change to org.appwork.utils.parser.HTMLParser.findUrls with next major-update
                 */
                final ArrayList<String> follow = DirectHTTP.findUrls(this.br.toString());
                /*
                 * if we already tried htmlRedirect or not exactly one link found, throw File not available
                 */
                if (follow.size() != 1 || downloadLink.getBooleanProperty("htmlRedirect", false)) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                }
                /* found one valid url */
                downloadLink.setUrlDownload(follow.get(0).trim());
                /* we set property here to avoid loops */
                downloadLink.setProperty("htmlRedirect", true);
                return this.requestFileInformation(downloadLink);
            } else {
                urlConnection.disconnect();
            }
            /* if final filename already set, do not change */
            if (downloadLink.getFinalFileName() == null) {
                /* restore filename from property */
                String fileName = downloadLink.getStringProperty("fixName", null);
                if (fileName == null && downloadLink.getBooleanProperty("MOVIE2K", false)) {
                    final String ext = new Regex(this.contentType, "(audio|video)/(x\\-)?(.*?)$").getMatch(2);
                    fileName = downloadLink.getName() + "." + ext;
                }
                if (fileName == null) {
                    fileName = Plugin.getFileNameFromHeader(urlConnection);
                    if (fileName != null && downloadLink.getBooleanProperty("urlDecodeFinalFileName", false)) {
                        fileName = Encoding.urlDecode(fileName, false);
                    }
                }
                if (fileName == null) {
                    fileName = downloadLink.getName();
                }
                if (fileName != null) {
                    if (fileName.indexOf(".") < 0) {
                        final String ext = this.getExtensionFromMimeType(this.contentType);
                        if (ext != null) {
                            fileName = fileName + "." + ext;
                        }
                    }
                    downloadLink.setFinalFileName(fileName);
                    /* save filename in property so we can restore in reset case */
                    downloadLink.setProperty("fixName", fileName);
                }
            }
            final long length = urlConnection.getLongContentLength();
            if (length >= 0) {
                downloadLink.setDownloadSize(length);
                final String contentEncoding = urlConnection.getHeaderField("Content-Encoding");
                if (urlConnection.getHeaderField("X-Mod-H264-Streaming") == null && (contentEncoding == null || "none".equalsIgnoreCase(contentEncoding))) {
                    final String contentMD5 = urlConnection.getHeaderField("Content-MD5");
                    final String contentSHA1 = urlConnection.getHeaderField("Content-SHA1");
                    if (downloadLink.getSha1Hash() == null) {
                        if (contentSHA1 != null) {
                            downloadLink.setSha1Hash(contentSHA1);
                        }
                    } else if (downloadLink.getMD5Hash() == null) {
                        if (contentMD5 != null) {
                            downloadLink.setMD5Hash(contentMD5);
                        }
                    }
                    downloadLink.setProperty("VERIFIEDFILESIZE", length);
                }
            }
            return AvailableStatus.TRUE;
        } catch (final PluginException e2) {
            /* try referer set by flashgot and check if it works then */
            if (downloadLink.getBooleanProperty("tryoldref", false) == false && downloadLink.getStringProperty("referer", null) != null) {
                downloadLink.setProperty("tryoldref", true);
                return this.requestFileInformation(downloadLink);
            } else {
                throw e2;
            }
        } catch (IOException e) {
            if (e instanceof java.net.ConnectException || e.getCause() instanceof java.net.ConnectException) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            if (e instanceof java.net.UnknownHostException || e.getCause() instanceof java.net.UnknownHostException) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Network problem: " + e.getMessage(), 5 * 60 * 1000l);
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            try {
                urlConnection.disconnect();
            } catch (final Throwable e) {
            }
        }
        throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
    }

    private void validateLogins(DownloadLink downloadLink, String basicAuth) {
        try {

            org.jdownloader.auth.AuthenticationController.getInstance().validate(new org.jdownloader.auth.BasicAuth(basicAuth), downloadLink.getDownloadURL());
        } catch (org.jdownloader.auth.InvalidBasicAuthFormatException e) {

        } catch (Throwable e) {
            // jd1
        }
    }

    private void validateLogins(DownloadLink downloadLink, String username, String password) {
        try {

            org.jdownloader.auth.AuthenticationController.getInstance().validate(new org.jdownloader.auth.BasicAuth(username, password), downloadLink.getDownloadURL());
            // } catch (org.jdownloader.auth.InvalidBasicAuthFormatException e) {

        } catch (Throwable e) {
            // jd1
        }
    }

    private void invalidateLogins(String basicAuth, DownloadLink downloadLink) {
        try {

            org.jdownloader.auth.AuthenticationController.getInstance().invalidate(new org.jdownloader.auth.BasicAuth(basicAuth), downloadLink.getDownloadURL());
        } catch (org.jdownloader.auth.InvalidBasicAuthFormatException e) {

        } catch (Throwable e) {
            // jd1
        }
    }

    /**
     * update this map to your needs
     * 
     * @param mimeType
     * @return
     */
    public String getExtensionFromMimeType(final String mimeType) {
        if (mimeType == null) {
            return null;
        }
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("image/gif", "gif");
        map.put("image/jpeg", "jpeg");
        map.put("image/png", "png");
        map.put("image/tiff", "tiff");
        map.put("application/gzip", "gz");
        map.put("application/pdf", "pdf");
        return map.get(mimeType.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
        link.setProperty(DirectHTTP.NORESUME, Property.NULL);
        link.setProperty(DirectHTTP.NOCHUNKS, Property.NULL);
        if (link.getStringProperty("fixName", null) != null) {
            link.setFinalFileName(link.getStringProperty("fixName", null));
        }
    }

    @Override
    public void resetPluginGlobals() {
    }

    private void setCustomHeaders(final Browser br, final DownloadLink downloadLink) throws IOException {
        /* allow customized headers, eg useragent */
        final Object customRet = downloadLink.getProperty("customHeader");
        List<String[]> custom = null;
        if (customRet != null && customRet instanceof List) {
            custom = (List<String[]>) customRet;
        }
        // Bla
        if (custom != null && custom.size() > 0) {
            for (final Object header : custom) {
                /*
                 * this is needed because we no longer serialize the stuff, we use json as storage and it does not differ between String[]
                 * and ArrayList<String>
                 */
                if (header instanceof ArrayList) {
                    br.getHeaders().put((String) ((ArrayList<?>) header).get(0), (String) ((ArrayList<?>) header).get(1));
                } else if (header.getClass().isArray()) {
                    br.getHeaders().put(((String[]) header)[0], ((String[]) header)[1]);
                }
            }
        }
        /*
         * seems like flashgot catches the wrong referer and some downloads do not work then, we do not set referer as a workaround
         */
        if (downloadLink.getStringProperty("refURL", null) != null) {
            /* refURL is for internal use */
            br.getHeaders().put("Referer", downloadLink.getStringProperty("refURL", null));
        }
        /*
         * try the referer set by flashgot, maybe it works
         */
        if (downloadLink.getBooleanProperty("tryoldref", false) && downloadLink.getStringProperty("referer", null) != null) {
            /* refURL is for internal use */
            br.getHeaders().put("Referer", downloadLink.getStringProperty("referer", null));
        }
        if (downloadLink.getStringProperty("cookies", null) != null) {
            br.getCookies(downloadLink.getDownloadURL()).add(Cookies.parseCookies(downloadLink.getStringProperty("cookies", null), Browser.getHost(downloadLink.getDownloadURL()), null));
        }
        this.downloadWorkaround(br, downloadLink);
    }

    private void downloadWorkaround(final Browser br, final DownloadLink downloadLink) throws IOException {
        // we shouldn't potentially over right setCustomHeaders..
        if (br.getHeaders().get("Referer") == null) {
            final String link = downloadLink.getDownloadURL();
            if (link.contains("fileplanet.com")) {
                /*
                 * it seems fileplanet firewall checks referer and ip must have called the page lately
                 */
                // br.getPage("http://www.fileplanet.com/");
                br.getHeaders().put("Referer", "http://fileplanet.com/");
            } else if (link.contains("sites.google.com")) {
                /*
                 * it seems google checks referer and ip must have called the page lately
                 */
                br.getHeaders().put("Referer", "https://sites.google.com");
            } else if (link.contains("fastpic.ru")) {
                br.getHeaders().put("Referer", "https://fastpic.ru");
            } else if (link.contains("tinypic.com/")) {
                // they seem to block direct link access
                br.getHeaders().put("Referer", link);
            } else if (link.contains("photobucket.com")) {
                br.getHeaders().put("Referer", link);
            }
        }
    }

    @Override
    public void setDownloadLink(final DownloadLink link) {
        try {
            super.setDownloadLink(link);
            this.customFavIconHost = Browser.getHost(new URL(link.getDownloadURL()));
        } catch (final Throwable e) {
        }
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    @Override
    public boolean hasCaptcha(final DownloadLink link, final jd.plugins.Account acc) {
        return false;
    }
}