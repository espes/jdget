//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package jd.plugins.decrypter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import jd.PluginWrapper;
import jd.controlling.JDLogger;
import jd.controlling.ProgressController;
import jd.controlling.reconnect.Reconnecter;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.JDHash;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.EditDistance;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "serienjunkies.org", "dokujunkies.org" }, urls = { "http://[\\w\\.]*?serienjunkies\\.org/.*?/(df|st|so|hf|fs|mu|rc|rs|nl|u[tl]|ff|fc|wu)[_-].*", "http://[\\w\\.]*?dokujunkies\\.org/[\\w\\-/]+.*\\.html" }, flags = { 0, 0 })
public class Srnnks extends PluginForDecrypt {
    class DecryptRunnable implements Runnable {

        private final String                  action;
        private final Browser                 br;
        private final ArrayList<DownloadLink> results;

        public DecryptRunnable(final String action, final Browser br, final ArrayList<DownloadLink> results) {
            this.action = action;
            this.br = br;
            this.results = results;
        }

        public void run() {

            // sj heuristic detection.

            // this makes the jobber useless... but we have to use the 300 ms to
            // work around sj's firewall
            try {
                Thread.sleep(Srnnks.FW_WAIT);
                this.br.getPage(this.action);
                if (this.br.getRedirectLocation() != null) {
                    this.results.add(Srnnks.this.createDownloadlink(this.br.getRedirectLocation()));
                } else {
                    // not sure if there are stil pages that use this old system

                    final String link = this.br.getRegex("SRC=\"(http://download\\.serienjunkies\\.org.*?)\"").getMatch(0);

                    if (link != null) {
                        Thread.sleep(Srnnks.FW_WAIT);
                        this.br.getPage(link);
                        final String loc = this.br.getRedirectLocation();
                        if (loc != null) {
                            this.results.add(Srnnks.this.createDownloadlink(loc));
                            return;
                        } else {
                            throw new Exception("no Redirect found");
                        }
                    } else {

                        throw new Exception("no Frame found");

                    }
                }

            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private final static ArrayList<String> passwords           = new ArrayList<String>(Arrays.asList(new String[] { "serienjunkies.dl.am", "serienjunkies.org", "dokujunkies.org" }));
    private static long                    LATEST_BLOCK_DETECT = 0;

    private static long                    LATEST_RECONNECT    = 0;
    private static Object                  GLOBAL_LOCK         = new Object();

    // seems like sj does block ips if requestlimit is not ok
    private static final long              FW_WAIT             = 300;
    private static final Object            LOCK                = new Object();
    private static AtomicInteger           RUNNING             = new AtomicInteger(0);

    private synchronized static boolean limitsReached(final Browser br) throws IOException {
        int ret = -100;
        if (br == null) {
            ret = UserIO.RETURN_OK;
        } else {
            if (br.containsHTML("Error 503")) {
                UserIO.getInstance().requestMessageDialog(JDL.L("plugins.decrypter.srnks.overloaded", "Serienjunkies ist überlastet. Bitte versuche es später erneut."));
                return true;
            }

            if (br.containsHTML("Du hast zu oft das Captcha falsch")) {

                if (System.currentTimeMillis() - Srnnks.LATEST_BLOCK_DETECT < 60000) { return true; }
                if (System.currentTimeMillis() - Srnnks.LATEST_RECONNECT < 15000) {
                    // redo the request
                    br.loadConnection(br.openRequestConnection(br.getRequest().cloneRequest()));
                    return false;
                }
                ret = UserIO.getInstance().requestConfirmDialog(0, "Captchalimit", JDL.L("plugins.decrypter.srnks.CaptchaLimitReached", "Sie haben zu oft das Captcha falsch eingegeben sie müssen entweder warten oder einen Reconnect durchführen"), null, "Reconnect", JDL.L("plugins.decrypter.srnks.canceldecrypt", "Decryptvorgang abbrechen"));

            }
            if (br.containsHTML("Download-Limit")) {
                if (System.currentTimeMillis() - Srnnks.LATEST_BLOCK_DETECT < 60000) { return true; }
                if (System.currentTimeMillis() - Srnnks.LATEST_RECONNECT < 15000) {
                    // redo the request
                    br.loadConnection(br.openRequestConnection(br.getRequest().cloneRequest()));
                    return false;
                }
                ret = UserIO.getInstance().requestConfirmDialog(0, "Downloadlimit", JDL.L("plugins.decrypter.srnks.DownloadLimitReached", "Das Downloadlimit wurde erreicht sie müssen entweder warten oder einen Reconnect durchführen"), null, "Reconnect", JDL.L("plugins.decrypter.srnks.canceldecrypt", "Decryptvorgang abbrechen"));

            }
        }
        if (ret != -100) {
            if (UserIO.isOK(ret)) {
                try {
                    jd.controlling.reconnect.ipcheck.IPController.getInstance().invalidate();
                } catch (Throwable e) {
                    /* not in 9580 stable */
                }

                if (Reconnecter.waitForNewIP(15000, false)) {

                    // redo the request
                    br.loadConnection(br.openRequestConnection(br.getRequest().cloneRequest()));
                    Srnnks.LATEST_RECONNECT = System.currentTimeMillis();
                    return false;
                }
            } else {
                Srnnks.LATEST_BLOCK_DETECT = System.currentTimeMillis();
            }
            return true;
        }
        return false;
    }

    private String         crawlStatus;

    private static boolean CAPTCHA = false;

    public Srnnks(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    protected DownloadLink createDownloadlink(final String link) {
        final DownloadLink dlink = super.createDownloadlink(link);
        dlink.setSourcePluginPasswordList(Srnnks.passwords);
        try {
            this.distribute(dlink);
        } catch (Throwable t) {

        }
        return dlink;
    }

    public String getCrawlerStatusString() {
        return crawlStatus;
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(final CryptedLink parameter, final ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> SjDj = new ArrayList<DownloadLink>();

        // crude importation method from doju -> serien
        if (parameter.getCryptedUrl().contains("dokujunkies.org")) {
            ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
            String parm = parameter.toString();
            br.setFollowRedirects(false);
            br.setCookiesExclusive(true);
            br.getPage(parm);
            if (br.containsHTML("<h2>Error 404 \\- Page not found\\!</h2>")) {
                logger.warning("Invalid URL: " + parameter);
                return decryptedLinks;
            }
            String grab = br.getRegex("<p><strong>[\\w\\-\\.]+</strong><br />(.*?)<div class=\"post\\_details\">").getMatch(0);
            String[] links = new Regex(grab, "href=\"(http://[\\w\\.]*?serienjunkies\\.org/.*?)\" target").getColumn(0);
            if (links != null && links.length != 0) {
                for (String link : links)
                    decryptedLinks.add(createDownloadlink(link));
            }
            return decryptedLinks;

        } else if (parameter.getCryptedUrl().contains("serienjunkies.org")) {
            try {
                // Browser.setRequestIntervalLimitGlobal("serienjunkies.org",
                // 400);
                // Browser.setRequestIntervalLimitGlobal("download.serienjunkies.org",
                // 400);

                final ArrayList<DownloadLink> ret = new ArrayList<DownloadLink>();
                Form[] forms;
                // progress.setStatusText("Lade Downloadseite");

                for (int i = 0; i < 5; i++) {
                    progress.setRange(100);

                    try {
                        RUNNING.incrementAndGet();
                        synchronized (LOCK) {

                            System.out.println(parameter.getCryptedUrl());
                            synchronized (Srnnks.GLOBAL_LOCK) {
                                Thread.sleep(Srnnks.FW_WAIT);
                                this.br.getPage(parameter.getCryptedUrl());
                            }
                            if (Srnnks.limitsReached(this.br)) { return new ArrayList<DownloadLink>(ret); }

                            if (this.br.containsHTML("<FRAME SRC")) {
                                // progress.setStatusText("Lade Downloadseitenframe");
                                synchronized (Srnnks.GLOBAL_LOCK) {
                                    Thread.sleep(Srnnks.FW_WAIT);
                                    this.br.getPage(this.br.getRegex("<FRAME SRC=\"(.*?)\"").getMatch(0));
                                }
                            }
                            if (Srnnks.limitsReached(this.br)) { return new ArrayList<DownloadLink>(ret); }
                            progress.increase(5);

                            // linkendung kommt auch im action der form vor
                            final String sublink = parameter.getCryptedUrl().substring(parameter.getCryptedUrl().indexOf("org/") + 3);

                            // try captcha max 5 times

                            progress.setRange(100);
                            progress.setStatus(5);

                            // suche wahrscheinlichste form
                            // progress.setStatusText("Suche Captcha Form");
                            Form form = null;

                            forms = this.br.getForms();
                            int bestdist = Integer.MAX_VALUE;
                            for (final Form form1 : forms) {
                                final int dist = EditDistance.damerauLevenshteinDistance(sublink, form1.getAction());
                                if (dist < bestdist) {
                                    form = form1;
                                    bestdist = dist;
                                }
                            }
                            if (form.getRegex("img.*?src=\"([^\"]*?secure)").matches()) {
                                /*
                                 * this form contains captcha image, so it must be valid
                                 */
                            } else if (bestdist > 100) {
                                form = null;
                            }

                            if (form == null) { throw new Exception("Serienjunkies Captcha Form konnte nicht gefunden werden!"); }
                            progress.increase(5);

                            // das bild in der Form ist das captcha
                            String captchaLink = new Regex(form.getHtmlCode(), "<IMG SRC=\"(.*?)\"").getMatch(0);
                            // if (captchaLink == null) { throw new
                            // Exception("Serienjunkies Captcha konnte nicht gefunden werden!");
                            // }
                            if (captchaLink != null) {
                                System.out.println("CAPTCHA!!!");
                                // only each 5 link needs captchas
                                if (!captchaLink.toLowerCase().startsWith("http://")) {
                                    String base = new Regex(br.getURL(), "(http.*?\\.org)").getMatch(0);
                                    if (base != null) {
                                        captchaLink = base + captchaLink;
                                    } else {
                                        captchaLink = "http://download.serienjunkies.org" + captchaLink;
                                    }
                                }

                                crawlStatus = br.getRegex("<TITLE>.* \\- (.*?)</TITLE>").getMatch(0);
                                if (crawlStatus == null) crawlStatus = "";
                                crawlStatus += "(" + RUNNING.intValue() + " pending)";
                                final File captcha = this.getLocalCaptchaFile(".png");
                                // captcha laden
                                synchronized (Srnnks.GLOBAL_LOCK) {
                                    Thread.sleep(Srnnks.FW_WAIT);
                                    final URLConnectionAdapter urlc = this.br.cloneBrowser().openGetConnection(captchaLink);
                                    Browser.download(captcha, urlc);
                                }
                                if ("7ebca510a6a18c1e8f6e8d98c3118874".equals(JDHash.getMD5(captcha))) {
                                    // dummy captcha without content.. wait
                                    // before
                                    // reloading
                                    JDLogger.getLogger().warning("Dummy Captcha. wait 3 seconds");
                                    Thread.sleep(3000);
                                    continue;
                                }
                                String code;
                                // wenn es ein Einzellink ist soll die
                                // Captchaerkennung
                                // benutzt
                                // werden

                                if (captchaLink.contains(".gif")) {
                                    code = this.getCaptchaCode("einzellinks.serienjunkies.org", captcha, parameter);
                                } else {
                                    code = this.getCaptchaCode(captcha, parameter);
                                }

                                if (code == null) { return ret; }
                                if (code.length() != 3) {
                                    progress.setStatus(30);
                                    Thread.sleep(1100);
                                    continue;
                                }
                                progress.increase(5);

                                form.getInputFieldByType("text").setValue(code);
                                // System.out.println(code);
                                synchronized (Srnnks.GLOBAL_LOCK) {
                                    Thread.sleep(Srnnks.FW_WAIT);
                                    this.br.submitForm(form);
                                }

                            } else {
                                System.out.println("CAPTCHA SKIP!!!");
                            }

                        }
                    } finally {
                        System.out.println(RUNNING.decrementAndGet());
                    }

                    if (Srnnks.limitsReached(this.br)) { return new ArrayList<DownloadLink>(ret); }
                    if (this.br.getRedirectLocation() != null) {
                        ret.add(this.createDownloadlink(this.br.getRedirectLocation()));
                        progress.doFinalize();
                        return new ArrayList<DownloadLink>(ret);
                    } else {
                        progress.setStatus(0);
                        forms = this.br.getForms();
                        // suche die downloadlinks
                        final ArrayList<String> actions = new ArrayList<String>();
                        for (final Form frm : forms) {
                            if (frm.getAction().contains("download.serienjunkies.org") && !frm.getAction().contains("firstload") && !frm.getAction().equals("http://mirror.serienjunkies.org")) {
                                actions.add(frm.getAction());
                            }
                        }
                        // es wurden keine Links gefunden also wurde das Captcha
                        // falsch eingegeben
                        if (actions.size() == 0) {
                            progress.setStatus(10);
                            // progress.setStatusText("Captcha code falsch");
                            continue;
                        }
                        // we need the 300 ms gap between two requests..
                        progress.setRange(10 + actions.size());
                        progress.setStatus(10);
                        synchronized (Srnnks.GLOBAL_LOCK) {
                            for (int d = 0; d < actions.size(); d++) {
                                this.new DecryptRunnable(actions.get(d), this.br.cloneBrowser(), ret).run();
                                progress.increase(1);
                            }

                        }
                        progress.doFinalize();

                        // wenn keine links drinnen sind ist bestimmt was mit
                        // dem
                        // captcha
                        // schief gegangen einfach nochmal versuchen
                        if (ret.size() != 0) { return ret; }
                    }

                }
                return new ArrayList<DownloadLink>(ret);

            } catch (final Exception e) {
                throw e;
            }
        }
        return new ArrayList<DownloadLink>(SjDj);
    }
}
