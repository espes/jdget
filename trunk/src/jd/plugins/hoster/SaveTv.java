//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
import jd.config.SubConfiguration;
import jd.controlling.AccountController;
import jd.http.Browser;
import jd.http.Browser.BrowserException;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.appwork.uio.UIOManager;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.swing.dialog.ContainerDialog;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.DomainInfo;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "save.tv" }, urls = { "https?://(www\\.)?(save\\.tv|free\\.save\\.tv)/STV/M/obj/user/usShowVideoArchiveDetail\\.cfm\\?TelecastID=\\d+" }, flags = { 2 })
public class SaveTv extends PluginForHost {

    /* Static information */
    private final String         APIKEY                            = "Q0FFQjZDQ0YtMDdFNC00MDQ4LTkyMDQtOUU5QjMxOEU3OUIz";
    @SuppressWarnings("unused")
    private final String         APIPAGE                           = "http://api.save.tv/v2/Api.svc?wsdl";
    public static final double   QUALITY_HD_MB_PER_MINUTE          = 22;
    public static final double   QUALITY_H264_NORMAL_MB_PER_MINUTE = 12.605;
    public static final double   QUALITY_H264_MOBILE_MB_PER_MINUTE = 4.64;
    private final String         COOKIE_HOST                       = "http://save.tv";
    private static final String  NICE_HOST                         = "save.tv";
    private static final String  NICE_HOSTproperty                 = "savetv";

    /* Regex stuff */
    private final String         INFOREGEX                         = "<h3>([^<>\"]*?)</h2>[\t\n\r ]+<p>([^<>]*?)</p>([\t\n\r ]+<p>([^<>]*?)</p>)?";
    private final String         GENERAL_REGEX                     = "Kategorie:</label>[\t\n\r ]+Info.*?";
    private final String         SERIESINFORMATION                 = "[A-Za-z]+ [A-Za-z]+ (\\d{4} / \\d{4}|\\d{4})";

    /* Settings stuff */
    private static final String  USEORIGINALFILENAME               = "USEORIGINALFILENAME";
    private static final String  PREFERADSFREE                     = "PREFERADSFREE";
    private static final String  PREFERADSFREE_OVERRIDE            = "PREFERADSFREE_OVERRIDE";
    private static final String  PREFERADSFREE_OVERRIDE_MAXRETRIES = "PREFERADSFREE_OVERRIDE_MAXRETRIES";
    private static final String  FAILED_ADSFREE_DIFFERENCE_MINUTES = "FAILED_ADSFREE_DIFFERENCE_MINUTES";
    private static final String  DOWNLOADONLYADSFREE               = "DOWNLOADONLYADSFREE";
    private static final String  DOWNLOADONLYADSFREE_RETRY_HOURS   = "DOWNLOADONLYADSFREE_RETRY_HOURS";
    private final String         ADSFREEAVAILABLETEXT              = JDL.L("plugins.hoster.SaveTv.AdsFreeAvailable", "Video ist werbefrei verfügbar");
    private final String         ADSFREEANOTVAILABLE               = JDL.L("plugins.hoster.SaveTv.AdsFreeNotAvailable", "Video ist nicht werbefrei verfügbar");
    private static final String  PREFERREDFORMATNOTAVAILABLETEXT   = JDL.L("plugins.hoster.SaveTv.Format_unavailable", "Das bevorzugte Format ist (noch) nicht verfügbar. Warte oder ändere die Einstellung!");
    private final String         NOCUTAVAILABLETEXT                = JDL.L("plugins.hoster.SaveTv.noCutAvailable", "Für diese Sendung steht (noch) keine Schnittliste zur Verfügung");
    private final static String  selected_video_format             = "selected_video_format";

    /* The list of server values displayed to the user */
    private final String[]       formats                           = new String[] { "HD [Funktioniert (noch) nicht per API!]", "H.264 HQ", "H.264 MOBILE" };

    private static final String  USEAPI                            = "USEAPI";
    private static final String  CRAWLER_ACTIVATE                  = "CRAWLER_ACTIVATE";
    private static final String  CRAWLER_ENABLE_FASTER             = "CRAWLER_ENABLE_FASTER";
    private static final String  CRAWLER_DISABLE_DIALOGS           = "CRAWLER_DISABLE_DIALOGS";
    private static final String  CRAWLER_LASTDAYS_COUNT            = "CRAWLER_LASTDAYS_COUNT";
    private static final String  DISABLE_LINKCHECK                 = "DISABLE_LINKCHECK";
    private static final String  DELETE_TELECAST_ID_AFTER_DOWNLOAD = "DELETE_TELECAST_ID_AFTER_DOWNLOAD";

    /* Custom filename settings stuff */
    private static final String  CUSTOM_DATE                       = "CUSTOM_DATE";
    private static final String  CUSTOM_FILENAME2                  = "CUSTOM_FILENAME2";
    private static final String  CUSTOM_FILENAME_SERIES2           = "CUSTOM_FILENAME_SERIES2";
    private static final String  CUSTOM_FILENAME_SEPERATION_MARK   = "CUSTOM_FILENAME_SEPERATION_MARK";
    private static final String  CUSTOM_FILENAME_EMPTY_TAG_STRING  = "CUSTOM_FILENAME_EMPTY_TAG_STRING";
    private static final String  FORCE_ORIGINALFILENAME_SERIES     = "FORCE_ORIGINALFILENAME_SERIES";
    private static final String  FORCE_ORIGINALFILENAME_MOVIES     = "FORCE_ORIGINALFILENAME_MOVIES";

    /* Variables */
    private boolean              FORCE_ORIGINAL_FILENAME           = false;
    private boolean              FORCE_LINKCHECK                   = false;
    private boolean              ISADSFREEAVAILABLE                = false;
    /* If this != null, API is in use */
    private String               SESSIONID                         = null;
    private static final String  NORESUME                          = "NORESUME";
    private static final String  NOCHUNKS                          = "NOCHUNKS";
    private DownloadLink         DLINK                             = null;

    /* Other */
    private static Object        LOCK                              = new Object();
    private static final int     MAX_RETRIES_LOGIN                 = 10;
    private static final int     MAX_RETRIES_SAFE_REQUEST          = 3;

    /* Download connections constants */
    private static final boolean ACCOUNT_PREMIUM_RESUME            = true;
    private static final int     ACCOUNT_PREMIUM_MAXCHUNKS         = -2;
    private static final int     ACCOUNT_PREMIUM_MAXDOWNLOADS      = -1;

    /* Other API/site errorhandling constants */
    private static final String  SITE_DL_IMPOSSIBLE                = ">Diese Sendung kann leider nicht heruntergeladen werden, da die Aufnahme fehlerhaft ist";
    private static final String  API_DL_IMPOSSIBLE                 = ">1418</ErrorCodeID>";
    private static final String  DL_IMPOSSIBLE_USER_TEXT           = JDL.L("plugins.hoster.SaveTv.dlImpossible", "Aufnahme fehlerhaft - Download momentan nicht möglich");

    @SuppressWarnings("deprecation")
    public SaveTv(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("https://www.save.tv/stv/s/obj/registration/RegPage1.cfm");
        if (!isJDStable()) {
            setConfigElements();
        }
    }

    @Override
    public String getAGBLink() {
        return "http://free.save.tv/STV/S/misc/miscShowTermsConditionsInMainFrame.cfm";
    }

    private boolean isJDStable() {
        return System.getProperty("jd.revision.jdownloaderrevision") == null;
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) throws Exception {
        link.setUrlDownload(link.getDownloadURL().replaceFirst("https://", "http://").replace("free.save.tv/", "save.tv/"));
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return ACCOUNT_PREMIUM_MAXDOWNLOADS;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return ACCOUNT_PREMIUM_MAXDOWNLOADS;
    }

    /**
     * JD2 CODE. DO NOT USE OVERRIDE FOR JD=) COMPATIBILITY REASONS!
     */
    public boolean isProxyRotationEnabledForLinkChecker() {
        return false;
    }

    /**
     * TODO: Known Bugs in API mode: API cannot differ between different video formats -> Cannot show any error in case user choice is not
     * available. --> NO FATAL bugs ---> Plugin will work fine with them!
     * 
     * @property "category": 0 = undefined, 1 = movies,category: 2 = series, 3 = show, 7 = music
     */

    @SuppressWarnings("deprecation")
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws Exception {
        DLINK = link;
        link.setProperty("is_adsfree_failed", Property.NULL);
        br.setFollowRedirects(true);
        /* Show telecast-ID in case it is offline or plugin is broken */
        if (link.getName() != null && (link.getName().contains(getTelecastId(link)) && !link.getName().endsWith(".mp4") || link.getName().contains("usShowVideoArchiveDetail.cfm"))) {
            link.setName(getTelecastId(link) + ".mp4");
        }
        final Account aa = AccountController.getInstance().getValidAccount(this);
        if (aa == null) {
            link.getLinkStatus().setStatusText("Kann Links ohne gültigen Account nicht überprüfen");
            synchronized (LOCK) {
                checkAccountNeededDialog();
            }
            return AvailableStatus.UNCHECKABLE;
        }
        if (this.getPluginConfig().getBooleanProperty(DISABLE_LINKCHECK, false) && !FORCE_LINKCHECK) {
            link.getLinkStatus().setStatusText("Linkcheck deaktiviert - korrekter Dateiname erscheint erst beim Downloadstart");
            return AvailableStatus.TRUE;
        }
        login(this.br, aa, false);

        final String string_for_empty_tags = this.getPluginConfig().getStringProperty(CUSTOM_FILENAME_EMPTY_TAG_STRING, defaultCustomStringForEmptyTags);
        String site_title = null;
        String filesize = null;
        long datemilliseconds = 0;
        String broadcastTime = null;
        String date = null;
        String category = null;

        /* For series only */
        String episodenumber = null;
        String seriestitle = null;
        String episodename = null;

        String genre = null;
        String producecountry = null;
        String produceyear = null;
        if (SESSIONID != null && is_API_enabled()) {
            String stv_request_selected_format_value = api_get_format_request_value();
            doSoapRequest("http://tempuri.org/IDownload/GetStreamingUrl", "<sessionId i:type=\"d:string\">" + SESSIONID + "</sessionId><telecastId i:type=\"d:int\">" + getTelecastId(link) + "</telecastId><telecastIdSpecified i:type=\"d:boolean\">true</telecastIdSpecified><recordingFormatId i:type=\"d:int\">" + stv_request_selected_format_value + "</recordingFormatId><adFree i:type=\"d:boolean\">1</adFree><adFreeSpecified i:type=\"d:boolean\">true</adFreeSpecified>");
            /* filesize = 0, filename = null - but we know, it is online */
            if (br.containsHTML(API_DL_IMPOSSIBLE)) {
                link.getLinkStatus().setStatusText(DL_IMPOSSIBLE_USER_TEXT);
                return AvailableStatus.TRUE;
            }
            String apifilename = br.getRegex("<a:Filename>([^<>\"]*?)</a").getMatch(0);
            filesize = br.getRegex("<a:SizeMB>(\\d+)</a:SizeMB>").getMatch(0);
            if (apifilename == null || filesize == null) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            final Regex fName = new Regex(apifilename, "((\\d{4}\\-\\d{2}\\-\\d{2})_\\d+_\\d+\\.mp4)");
            final String filenameReplace = fName.getMatch(0);
            if (filenameReplace != null) {
                site_title = apifilename.replace(filenameReplace, "");
            }

            date = fName.getMatch(1);
            /* Test code to correct the date here - not possible (yet) because AM/PM time-state is not given: http://pastebin.com/SZJFYn1W */
            link.setProperty("apiplainfilename", apifilename);
            filesize += " KB";
        } else {
            boolean isSeries = false;
            getPageSafe(link.getDownloadURL(), aa);
            if (br.containsHTML("(Leider ist ein Fehler aufgetreten|Bitte versuchen Sie es später noch einmal)") || !br.getURL().contains("TelecastID=")) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            site_title = br.getRegex("<h2 id=\"archive-detailbox-title\">(.*?)</h2>").getMatch(0);
            if (site_title == null) {
                site_title = br.getRegex("id=\"telecast-detail\">.*?<h3>(.*?)</h2>").getMatch(0);
            }
            filesize = site_get_filesize();
            category = br.getRegex("<label>Kategorie:</label>([^<>\"]*?)</p>").getMatch(0);
            if (site_title == null || (filesize == null && !br.containsHTML(SITE_DL_IMPOSSIBLE)) || category == null) {
                logger.warning("Save.tv: Availablecheck failed!");
                return AvailableStatus.UNCHECKABLE;
            }
            site_title = correctData(site_title);
            category = correctData(category);

            /* Find custom filename stuff */
            date = br.getRegex("<b>Datum:</b>[\t\n\r ]+[A-Za-z]{1,3}\\., (\\d{2}\\.\\d{02}\\.)([\t\n\r ]+)?</p>").getMatch(0);
            if (date != null) {
                date = date.trim();
                date += +Calendar.getInstance().get(Calendar.YEAR);
                datemilliseconds = TimeFormatter.getMilliSeconds(date, "dd.MM.yyyy", Locale.GERMAN);
            }
            broadcastTime = br.getRegex("<b>Ausstrahlungszeitraum:</b>[\t\n\r ]+(\\d{2}:\\d{2}) \\-").getMatch(0);

            if (br.containsHTML(GENERAL_REGEX)) {
                /* Find out if it's a series or if we should handle it like a movie */
                final Regex seriesInfo = br.getRegex(INFOREGEX);
                String testtitle = seriesInfo.getMatch(0);
                if (testtitle != null) {
                    testtitle = testtitle.trim();
                    isSeries = isSeries(testtitle);
                }
            }

            if (category.contains("Serien") || category.contains("Info") || isSeries) {
                /* For series */
                link.setProperty("category", 2);
                episodenumber = br.getRegex("<strong>Folge:</strong> (\\d+)</p>").getMatch(0);
                final Regex seriesInfo = br.getRegex(INFOREGEX);
                seriestitle = seriesInfo.getMatch(0);
                episodename = seriesInfo.getMatch(1);
                if (episodename != null && episodename.contains("Originaltitel")) {
                    episodename = seriesInfo.getMatch(3);
                }
                if (seriestitle != null) {
                    seriestitle = correctData(seriestitle);
                } else {
                    seriestitle = site_title;
                }
                if (episodename == null) {
                    episodename = string_for_empty_tags;
                }
                episodename = correctData(episodename);
                if (episodename.matches(SERIESINFORMATION)) {
                    episodename = string_for_empty_tags;
                } else if (episodename.contains("Für diese Sendung stehen leider keine weiteren Informationen zur Verfügung")) {
                    episodename = string_for_empty_tags;
                }
                String seriesdata = seriesInfo.getMatch(3);
                if (seriesdata == null) {
                    seriesdata = seriesInfo.getMatch(2);
                }
                if (seriesdata == null) {
                    seriesdata = seriesInfo.getMatch(1);
                }
                if (seriesdata != null) {
                    seriesdata = seriesdata.trim();
                    final String[] dataArray = seriesdata.split(" ");
                    if (dataArray != null) {
                        genre = dataArray[0];
                        /* Maybe the media was produced over multiple years */
                        produceyear = new Regex(seriesdata, "(\\d{4} / \\d{4})").getMatch(0);
                        producecountry = getRegexSafe(seriesdata + genre, " ([\\p{L}/]+)", 0);
                        if (dataArray != null) {
                            if (dataArray.length >= 3) {
                                if (producecountry == null) {
                                    producecountry = dataArray[1];
                                }
                                if (produceyear == null) {
                                    produceyear = dataArray[2];
                                }
                            } else if (dataArray.length == 2) {
                                if (producecountry == null) {
                                    producecountry = dataArray[1];
                                }
                            }
                        }

                    }
                }
            } else if (category.contains("Shows") || br.containsHTML("src=\"/STV/IMG/global/TVCategorie/kat3\\.jpg\"")) {
                /* For shows */
                link.setProperty("category", 3);
                final Regex showInfo = br.getRegex(INFOREGEX);
                genre = showInfo.getMatch(3);
                if (genre == null) {
                    genre = showInfo.getMatch(1);
                }
            } else if (category.contains("Musik") || br.containsHTML("src=\"/STV/IMG/global/TVCategorie/kat7\\.jpg\"")) {
                /* For music */
                link.setProperty("category", 7);
                final Regex musicInfo = br.getRegex(INFOREGEX);
                final String music_title = musicInfo.getMatch(0);
                final String music_subtitle = musicInfo.getMatch(1);
                if (music_title != null && music_subtitle != null) {
                    site_title = correctData(music_title) + " - " + correctData(music_subtitle);
                } else if (music_title != null) {
                    site_title = correctData(site_title);
                }
                String musicdata = musicInfo.getMatch(2);
                if (musicdata != null) {
                    musicdata = musicdata.trim();
                    musicdata = musicdata.replace("<p>", "").replace("</p>", "");
                    genre = new Regex(musicdata, "([A-Za-z]+ / [A-Za-z, ]+)").getMatch(0);
                }
            } else if (category.contains("Film") || br.containsHTML("src=\"/STV/IMG/global/TVCategorie/kat1\\.jpg\"") || br.containsHTML(GENERAL_REGEX)) {
                /* For movies */
                try {
                    final Regex movieInfo = br.getRegex(INFOREGEX);
                    String moviesdata = movieInfo.getMatch(1);
                    if (moviesdata != null) {
                        moviesdata = moviesdata.trim();
                        final String[] dataArray = moviesdata.split(" ");
                        if (dataArray != null) {
                            genre = dataArray[0];
                            /* Maybe the media was produced over multiple years */
                            produceyear = new Regex(moviesdata, "(\\d{4} / \\d{4})").getMatch(0);
                            if (produceyear == null) {
                                produceyear = new Regex(moviesdata, "(\\d{4})").getMatch(0);
                            }
                            if (produceyear != null) {
                                producecountry = new Regex(moviesdata, genre + "(.+)" + produceyear).getMatch(0);
                            } else {
                                producecountry = new Regex(moviesdata, genre + "(.+)").getMatch(0);
                            }
                            /* A little errorhandling but this should never happen */
                            if (producecountry != null) {
                                producecountry = correctData(producecountry);
                            }
                            if (producecountry != null && producecountry.equals("")) {
                                producecountry = null;
                            }

                        }
                    }
                } catch (final Throwable e) {
                    logger.warning("produceyear/producecountry handling failed!");
                    produceyear = "PLUGIN_ERROR";
                    producecountry = "PLUGIN_ERROR";
                    site_title += "PLUGIN_ERROR";
                }
                link.setProperty("category", 1);
            } else {
                /* For everything else */
                link.setProperty("category", 0);
            }

            /* Add time to date */
            if (broadcastTime != null) {
                /* Add missing time - Also add one hour because otherwise one is missing */
                datemilliseconds += TimeFormatter.getMilliSeconds(broadcastTime, "HH:mm", Locale.GERMAN) + (60 * 60 * 1000l);
            }
        }
        link.setAvailable(true);

        /* Filesize stuff */
        /* Download impossible --> No filesize given --> Calculate it over bitrate */
        final String site_run_time = get_run_time(this.br);
        if (br.containsHTML(SITE_DL_IMPOSSIBLE)) {
            link.setDownloadSize(calculateFilesize(site_run_time));
        } else {
            filesize = filesize.replace(".", "");
            final long page_size = SizeFormatter.getSize(filesize.replace(".", ""));
            final long run_time_max_difference = getLongProperty(this.getPluginConfig(), FAILED_ADSFREE_DIFFERENCE_MINUTES, 0);
            if (run_time_max_difference > 0) {
                final long page_size_mb = SizeFormatter.getSize(filesize.replace(".", "")) / 1024 / 1024;
                final long run_time_page = Long.parseLong(site_run_time);
                final double run_time_calculated = site_get_calculated_runtime_minutes(page_size_mb);
                long run_time_difference = run_time_page - (long) run_time_calculated;
                if (run_time_difference < 0) {
                    run_time_difference = run_time_difference * (-1);
                }
                if (run_time_difference > run_time_max_difference) {
                    logger.info("Seems like the ads-free (Schnittliste) failed - marking filename if possible.");
                    link.setProperty("is_adsfree_failed", true);
                }
            }
            link.setDownloadSize(page_size);
        }

        /* Set properties which are needed for filenames */
        /* Add series information */
        if (episodenumber != null) {
            link.setProperty("episodenumber", Integer.parseInt(episodenumber));
        }
        link.setProperty("seriestitle", seriestitle);
        link.setProperty("episodename", episodename);

        /* Add movie information */
        if (produceyear != null) {
            produceyear = produceyear.replace("/", "-");
            link.setProperty("produceyear", produceyear);
        }
        if (genre == null) {
            genre = string_for_empty_tags;
        }
        link.setProperty("genre", correctData(genre));
        link.setProperty("producecountry", producecountry);
        link.setProperty("plain_site_category", category);

        /* Add remaining information */
        link.setProperty("plainfilename", site_title);
        link.setProperty("type", ".mp4");
        link.setProperty("originaldate", datemilliseconds);

        /* No custom filename if not all required tags are given */
        final boolean force_original_general = (datemilliseconds == 0 || getPluginConfig().getBooleanProperty(USEORIGINALFILENAME) || SESSIONID != null || getLongProperty(link, "category", 0l) == 0);
        boolean force_original_series = false;
        try {
            if (getLongProperty(link, "category", 0l) == 2 && seriestitle.matches(getPluginConfig().getStringProperty(FORCE_ORIGINALFILENAME_SERIES, null))) {
                force_original_series = true;
            }
        } catch (final Throwable e) {
            logger.info("FORCE_ORIGINALFILENAME_SERIES custom regex failed");
        }
        boolean force_original_movies = false;
        try {
            if (getLongProperty(link, "category", 0l) == 1 && site_title.matches(getPluginConfig().getStringProperty(FORCE_ORIGINALFILENAME_MOVIES, null))) {
                force_original_movies = true;
            }
        } catch (final Throwable e) {
            logger.info("FORCE_ORIGINALFILENAME_MOVIES custom regex failed");
        }
        FORCE_ORIGINAL_FILENAME = (force_original_general || force_original_series || force_original_movies);
        String availablecheck_filename;
        if (FORCE_ORIGINAL_FILENAME) {
            availablecheck_filename = getFakeOriginalFilename(link);
            availablecheck_filename = fixCharIssues(availablecheck_filename);
        } else {
            availablecheck_filename = getFormattedFilename(link);
        }
        /* Reset from previous state so we can use the final filename as final filename later even if it has changed before */
        link.setFinalFileName(null);
        link.setName(availablecheck_filename);
        link.setName(availablecheck_filename);
        return AvailableStatus.TRUE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        final Account aa = AccountController.getInstance().getValidAccount(this);
        if (aa == null) {
            try {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
            } catch (final Throwable e) {
                if (e instanceof PluginException) {
                    throw (PluginException) e;
                }
            }
            throw new PluginException(LinkStatus.ERROR_FATAL, "Only downloadable for premium users");
        }
        // http://svn.jdownloader.org/issues/10306
        logger.warning("Downloading as premium in free mode as a workaround for bug #10306");
        try {
            handlePremium(downloadLink, aa);
        } catch (final PluginException e) {
            /* Catch premium errors - usually the account would be deactivated then -> Wait */
            if (e.getLinkStatus() == LinkStatus.ERROR_PREMIUM) {
                throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "FATAL server error", 30 * 60 * 1000l);
            }
            /* Show other download errors */
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handlePremium(final DownloadLink downloadLink, final Account account) throws Exception {
        synchronized (LOCK) {
            checkFeatureDialogAll();
            checkFeatureDialogCrawler();
            checkFeatureDialogHD();
        }
        final SubConfiguration cfg = SubConfiguration.getConfig("save.tv");
        final boolean preferAdsFree = cfg.getBooleanProperty(PREFERADSFREE);
        FORCE_LINKCHECK = true;
        requestFileInformation(downloadLink);

        if (SESSIONID != null && is_API_enabled()) {
            if (br.containsHTML(API_DL_IMPOSSIBLE)) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, DL_IMPOSSIBLE_USER_TEXT, 30 * 60 * 1000l);
            }
            // doSoapRequest("http://tempuri.org/ITelecast/GetTelecastDetail",
            // "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetTelecastDetail xmlns=\"http://tempuri.org/\"><sessionId>6f33f94f-13bb-4271-ab48-3339d2430d75</sessionId><telecastIds xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"/><detailLevel>1</detailLevel></GetTelecastDetail></s:Body></s:Envelope>");
            // try {
            // doSoapRequest("http://tempuri.org/IDownload/GetSimultaneousDownloadConnectionCount", "<sessionId>" + SESSIONID +
            // "</sessionId>");
            // } catch (final BrowserException e) {
            // logger.warning("FAILED!");
            // }
            /* Check if ads-free version is available */
            doSoapRequest("http://tempuri.org/IVideoArchive/GetAdFreeState", "<sessionId>" + SESSIONID + "</sessionId><telecastId i:type=\"d:int\">" + getTelecastId(downloadLink) + "</telecastId><telecastIdSpecified i:type=\"d:boolean\">true</telecastIdSpecified>");
            if (br.containsHTML("<a:IsAdFreeAvailable>false</a:IsAdFreeAvailable>")) {
                downloadLink.getLinkStatus().setStatusText(ADSFREEANOTVAILABLE);
                ISADSFREEAVAILABLE = false;
            } else {
                downloadLink.getLinkStatus().setStatusText(ADSFREEAVAILABLETEXT);
                ISADSFREEAVAILABLE = true;
            }
        } else {
            if (br.containsHTML(SITE_DL_IMPOSSIBLE)) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, DL_IMPOSSIBLE_USER_TEXT, 30 * 60 * 1000l);
            }
            /* Check if ads-free version is available */
            final DecimalFormat df = new DecimalFormat("0000");
            postPageSafe(this.br, "https://www.save.tv/STV/M/obj/cRecordOrder/croGetAdFreeAvailable.cfm?null.GetAdFreeAvailable", "ajax=true&clientAuthenticationKey=&callCount=1&c0-scriptName=null&c0-methodName=GetAdFreeAvailable&c0-id=" + df.format(new Random().nextInt(1000)) + "_" + System.currentTimeMillis() + "&c0-param0=number:" + getTelecastId(downloadLink) + "&xml=true&extend=function (object) {for (property in object) {this[property] = object[property];}return this;}&");
            if (this.br.containsHTML("= \\'3\\';")) {
                downloadLink.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SaveTv.NoCutListAvailable", NOCUTAVAILABLETEXT));
            } else if (this.br.containsHTML("= \\'1\\';")) {
                downloadLink.getLinkStatus().setStatusText(ADSFREEAVAILABLETEXT);
                ISADSFREEAVAILABLE = true;
            } else {
                downloadLink.getLinkStatus().setStatusText(ADSFREEANOTVAILABLE);
                ISADSFREEAVAILABLE = false;
            }
        }

        String dllink = null;
        /*
         * User wants ads-free but it's not available -> Wait X [User-Defined DOWNLOADONLYADSFREE_RETRY_HOURS] hours, status can still
         * change but probably won't -> If defined by user, force version with ads after a user defined amount of retries.
         */
        if (preferAdsFree && this.getPluginConfig().getBooleanProperty(DOWNLOADONLYADSFREE, false) && !this.ISADSFREEAVAILABLE) {
            final boolean preferadsfreeOverride = cfg.getBooleanProperty(PREFERADSFREE_OVERRIDE, false);
            final long maxRetries = getLongProperty(cfg, PREFERADSFREE_OVERRIDE_MAXRETRIES, defaultIgnoreOnlyAdsFreeAfterRetries_maxRetries);
            long currentTryCount = getLongProperty(downloadLink, "curren_no_ads_free_available_retries", 0);
            final boolean load_with_ads = (preferadsfreeOverride && currentTryCount >= maxRetries);

            if (!load_with_ads) {
                /* Only increase the counter when the option is activated */
                if (preferadsfreeOverride) {
                    currentTryCount++;
                    downloadLink.setProperty("curren_no_ads_free_available_retries", currentTryCount);
                }
                final long userDefinedWaitHours = getLongProperty(cfg, DOWNLOADONLYADSFREE_RETRY_HOURS, SaveTv.defaultNoAdsFreeAvailableRetryWaitHours);
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, NOCUTAVAILABLETEXT, userDefinedWaitHours * 60 * 60 * 1000l);
            }
        }
        String downloadWithoutAds = "false";
        if (preferAdsFree) {
            downloadWithoutAds = "true";
        }

        /* Set download options (ads-free or with ads) and get download url */
        String stv_request_selected_format_value = null;
        if (SESSIONID != null && is_API_enabled()) {
            stv_request_selected_format_value = api_get_format_request_value();
            api_postDownloadPage(downloadLink, stv_request_selected_format_value, downloadWithoutAds);
            dllink = br.getRegex("<a:DownloadUrl>(http://[^<>\"]*?)</a").getMatch(0);
        } else {
            stv_request_selected_format_value = site_get_format_request_value();
            postDownloadPage(downloadLink, stv_request_selected_format_value, downloadWithoutAds);
            if (br.containsHTML("Die Aufnahme liegt nicht im gewünschten Format vor")) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, PREFERREDFORMATNOTAVAILABLETEXT, 4 * 60 * 60 * 1000l);
            }
            /* Ads-Free version not available - handle it */
            if (br.containsHTML("\\'Leider enthält Ihre Aufnahme nur Werbung\\.\\'") && preferAdsFree) {
                this.ISADSFREEAVAILABLE = false;
                if (cfg.getBooleanProperty(DOWNLOADONLYADSFREE, false) && !this.ISADSFREEAVAILABLE) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, NOCUTAVAILABLETEXT, 12 * 60 * 60 * 1000l);
                }
                postDownloadPage(downloadLink, stv_request_selected_format_value, "false");
            }
            dllink = br.getRegex("\\'OK\\',\\'(http://[^<>\"\\']+)\\'").getMatch(0);
            if (dllink == null) {
                dllink = br.getRegex("\\'(http://[^<>\"\\']+/\\?m=dl)\\'").getMatch(0);
            }
        }
        if (dllink == null) {
            logger.warning("Final downloadlink (dllink) is null");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        logger.info("Final downloadlink = " + dllink + " starting download...");
        int maxChunks = ACCOUNT_PREMIUM_MAXCHUNKS;
        boolean resume = ACCOUNT_PREMIUM_RESUME;
        if (downloadLink.getBooleanProperty(NORESUME, false)) {
            resume = false;
        }
        if (downloadLink.getBooleanProperty(SaveTv.NOCHUNKS, false) || resume == false) {
            maxChunks = 1;
        }

        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, resume, maxChunks);
        if (dl.getConnection().getContentType().contains("html")) {
            /* Handle (known) errors */
            logger.warning("Save.tv: Received HTML code instead of the file!");
            br.followConnection();
            if (br.containsHTML(">Die Aufnahme kann zum aktuellen Zeitpunkt nicht vollständig heruntergeladen werden")) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Die Aufnahme kann zum aktuellen Zeitpunkt nicht vollständig heruntergeladen werden");
            }

            /* Handle unknown errors */
            logger.info(NICE_HOST + ": timesfailed_unknown_dlerror");
            int timesFailed = downloadLink.getIntegerProperty(NICE_HOSTproperty + "timesfailed_unknown_dlerror", 0);
            downloadLink.getLinkStatus().setRetryCount(0);
            if (timesFailed <= 2) {
                timesFailed++;
                downloadLink.setProperty(NICE_HOSTproperty + "timesfailed_unknown_dlerror", timesFailed);
                logger.info(NICE_HOST + ": timesfailed_unknown_dlerror -> Retrying");
                throw new PluginException(LinkStatus.ERROR_RETRY, "timesfailed_unknown_dlerror");
            } else {
                downloadLink.setProperty(NICE_HOSTproperty + "timesfailed_unknown_dlerror", Property.NULL);
                logger.info(NICE_HOST + ": timesfailed_unknown_dlerror - disabling current host!");
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Unbekannter Server-Fehler - bitte dem JDownloader Support mit Log melden!", 60 * 60 * 1000l);
            }
        } else if (dl.getConnection().getLongContentLength() <= 1048576) {
            /* Avoid downloading (too small) trash data */
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server-Fehler: Datei vom Server zu klein", 60 * 60 * 1000l);
        }
        String final_filename;
        if (FORCE_ORIGINAL_FILENAME) {
            final_filename = getFileNameFromHeader(dl.getConnection());
            final_filename = fixCharIssues(final_filename);
        } else {
            final_filename = getFormattedFilename(downloadLink);
        }
        downloadLink.setFinalFileName(final_filename);
        try {
            if (!this.dl.startDownload()) {
                try {
                    if (dl.externalDownloadStop()) {
                        return;
                    }
                } catch (final Throwable e) {
                }
                /* Unknown error, we disable multiple chunks */
                if (downloadLink.getLinkStatus().getErrorMessage() != null && downloadLink.getBooleanProperty(SaveTv.NOCHUNKS, false) == false) {
                    downloadLink.setProperty(SaveTv.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
            } else {
                try {
                    if (cfg.getBooleanProperty(DELETE_TELECAST_ID_AFTER_DOWNLOAD, false)) {
                        br.postPage("https://www.save.tv/STV/M/obj/user/usShowVideoArchive.cfm?", "iFilterType=1&sText=&iTextSearchType=1&ChannelID=0&TVCategoryID=0&TVSubCategoryID=0&iRecordingState=1&iPageNumber=1&sSortOrder=&lTelecastID=" + getTelecastId(downloadLink));
                        logger.info("Successfully deleted telecastID:  " + getTelecastId(downloadLink));
                    }
                } catch (final Throwable e) {
                    logger.info("Failed to delete telecastID: " + getTelecastId(downloadLink));
                }
            }
        } catch (final PluginException e) {
            if (e.getLinkStatus() == LinkStatus.ERROR_DOWNLOAD_INCOMPLETE) {
                logger.info("ERROR_DOWNLOAD_INCOMPLETE --> Handling it");
                if (downloadLink.getBooleanProperty(NORESUME, false)) {
                    downloadLink.setProperty(NORESUME, Boolean.valueOf(false));
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Unknown server error", 30 * 60 * 1000l);
                }
                downloadLink.setProperty(NORESUME, Boolean.valueOf(true));
                downloadLink.setChunksProgress(null);
                throw new PluginException(LinkStatus.ERROR_RETRY, "ERROR_DOWNLOAD_INCOMPLETE");
            }
            /* New V2 errorhandling */
            /* unknown error, we disable multiple chunks */
            if (e.getLinkStatus() != LinkStatus.ERROR_RETRY && downloadLink.getBooleanProperty(SaveTv.NOCHUNKS, false) == false) {
                downloadLink.setProperty(SaveTv.NOCHUNKS, Boolean.valueOf(true));
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        final AccountInfo ai = new AccountInfo();
        loginSafe(this.br, account, true);
        /* We're logged in - don't fail here! */
        String acctype = null;
        try {
            if (is_API_enabled()) {
                // doSoapRequest("http://tempuri.org/IUser/GetUserStatus", "<sessionId i:type=\"d:string\">" + SESSIONID + "</sessionId>");
                acctype = "XL Account";
            } else {
                /* Get long lasting login cookie */
                String long_cookie = br.getCookie("http://save.tv/", "SLOCO");
                if (long_cookie == null || long_cookie.trim().equals("bAutoLoginActive=1")) {
                    logger.info("Long session cookie does not exist yet/anymore - enabling it");
                    br.postPage("https://www.save.tv/STV/M/obj/user/usEditAutoLogin.cfm?", "bAutoLoginActive=1");
                    long_cookie = br.getCookie("http://save.tv/", "SLOCO");
                    if (long_cookie == null || long_cookie.trim().equals("")) {
                        logger.info("Failed to get long session cookie");
                    } else {
                        logger.info("Successfully received long session cookie and saved cookies");
                        this.saveCookies(account);
                    }
                } else {
                    logger.info("Long session cookie exists");
                }
                /* Find account details */
                br.getPage("https://www.save.tv/STV/M/obj/user/contract/coUpgrade.cfm");
                if (br.containsHTML(">XL-Status:</label></span>[\t\n\r ]+Ja<br")) {
                    acctype = "XL Account";
                } else {
                    acctype = "Basis Account";
                }
                String expireDate = br.getRegex("<label>Laufzeit:</label></span> [0-9\\.]+ \\- (\\d{2}\\.\\d{2}\\.\\d{4})<br />").getMatch(0);
                if (expireDate != null) {
                    expireDate = Encoding.htmlDecode(expireDate.trim());
                    /* Accounts expire after the last day */
                    expireDate += " 23:59:59";
                    ai.setValidUntil(TimeFormatter.getMilliSeconds(expireDate, "dd.MM.yyyy hh:mm:ss", Locale.GERMANY));
                    account.setProperty("acc_expire", expireDate);
                }
                final String package_name = br.getRegex("<label>Name:</label></span>([^<>\"]*?)<br").getMatch(0);
                final String price = br.getRegex("<label>Preis:</label></span>([^<>\"]*?)<br").getMatch(0);
                final String capacity = br.getRegex("<label>Kapazität Videoarchiv:</label></span>([^<>\"]*?)<br").getMatch(0);
                final String runtime = br.getRegex("<label>Laufzeit:</label></span>([^<>\"]*?)<br").getMatch(0);
                if (package_name != null) {
                    account.setProperty("acc_package", correctData(package_name));
                }
                if (price != null) {
                    account.setProperty("acc_price", correctData(price));
                }
                if (capacity != null) {
                    account.setProperty("acc_capacity", correctData(capacity));
                }
                if (runtime != null) {
                    account.setProperty("acc_runtime", correctData(runtime));
                }

                br.getPage("https://www.save.tv/STV/M/obj/user/usShowVideoArchive.cfm?");
                final String totalLinks = br.getRegex(">Gefundene Sendungen: (\\d+)").getMatch(0);
                if (totalLinks != null) {
                    account.setProperty("acc_count_telecast_ids", totalLinks);
                }
            }
        } catch (final Throwable e) {
            /* Should not happen but it won't hurt */
            logger.info("Extended account check failed");
        }
        ai.setStatus(acctype);
        account.setProperty("acc_type", acctype);
        ai.setUnlimitedTraffic();
        account.setValid(true);
        return ai;
    }

    @SuppressWarnings({ "unchecked" })
    public void login(final Browser br, final Account account, final boolean force) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        final String lang = System.getProperty("user.language");
        if (is_API_enabled()) {
            SESSIONID = account.getStringProperty("sessionid", null);
            final long lastUse = getLongProperty(account, "lastuse", -1l);
            /* Only generate new sessionID if we have none or it's older than 6 hours */
            if (SESSIONID == null || (System.currentTimeMillis() - lastUse) > 360000) {
                api_prepBrowser(br);
                doSoapRequest("http://tempuri.org/ISession/CreateSession", "<apiKey>" + Encoding.Base64Decode(APIKEY) + "</apiKey>");
                SESSIONID = br.getRegex("<a:SessionId>([^<>\"]*?)</a:SessionId>").getMatch(0);
                if (SESSIONID == null) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nPlugin defekt, bitte den JDownloader Support kontaktieren!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nPlugin broken, please contact the JDownloader Support!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                account.setProperty("lastuse", System.currentTimeMillis());
                account.setProperty("sessionid", SESSIONID);
            }
            doSoapRequest("http://tempuri.org/IUser/Login", "<sessionId>" + SESSIONID + "</sessionId><username>" + account.getUser() + "</username><password>" + account.getPass() + "</password>");
            if (!br.containsHTML("<a:HasPremiumStatus>true</a:HasPremiumStatus>")) {
                if ("de".equalsIgnoreCase(lang)) {
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen? Versuche folgendes:\r\n1. Falls dein Passwort Sonderzeichen enthält, ändere es (entferne diese) und versuche es erneut!\r\n2. Gib deine Zugangsdaten per Hand (ohne kopieren/einfügen) ein.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                } else {
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nYou're sure that the username and password you entered are correct? Some hints:\r\n1. If your password contains special characters, change it (remove them) and try again!\r\n2. Type in your username/password by hand without copy & paste.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                }
            }
        } else {
            site_prepBrowser(br);
            synchronized (LOCK) {
                try {
                    /* Load cookies */
                    br.setCookiesExclusive(true);
                    final Object ret = account.getProperty("cookies", null);
                    boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                    if (acmatch) {
                        acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                    }
                    if (acmatch && ret != null && ret instanceof HashMap<?, ?> && !force) {
                        final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                        if (account.isValid()) {
                            for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                                final String key = cookieEntry.getKey();
                                final String value = cookieEntry.getValue();
                                br.setCookie(COOKIE_HOST, key, value);
                            }
                            return;
                        }
                    }
                    final String postData = "sUsername=" + Encoding.urlEncode(account.getUser()) + "&sPassword=" + Encoding.urlEncode(account.getPass()) + "&bAutoLoginActivate=1";
                    br.postPage("https://www.save.tv/STV/M/Index.cfm?sk=PREMIUM", postData);
                    if (br.containsHTML("No htmlCode read")) {
                        br.getPage("https://www.save.tv/STV/M/obj/TVProgCtr/tvctShow.cfm");
                    }
                    if (!br.containsHTML("/STV/M/obj/user/usEdit.cfm") || br.containsHTML("Bitte verifizieren Sie Ihre Logindaten")) {
                        if ("de".equalsIgnoreCase(lang)) {
                            throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen? Versuche folgendes:\r\n1. Falls dein Passwort Sonderzeichen enthält, ändere es (entferne diese) und versuche es erneut!\r\n2. Gib deine Zugangsdaten per Hand (ohne kopieren/einfügen) ein.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                        } else {
                            throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nYou're sure that the username and password you entered are correct? Some hints:\r\n1. If your password contains special characters, change it (remove them) and try again!\r\n2. Type in your username/password by hand without copy & paste.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                        }
                    }
                    final String acc_count_archive_entries = br.getRegex("id=\"archiv_countentries\">(\\d+) Sendungen im Archiv</li>").getMatch(0);
                    if (acc_count_archive_entries != null) {
                        account.setProperty("acc_count_archive_entries", acc_count_archive_entries);
                    }
                    /* Save cookies & account data */
                    saveCookies(account);
                } catch (final PluginException e) {
                    account.setProperty("cookies", Property.NULL);
                    throw e;
                }
            }
        }
    }

    /*
     * Log in, handle all kinds of timeout- and browser errors - as long as we know that the account is valid it should stay active!
     */
    @SuppressWarnings("deprecation")
    private void loginSafe(final Browser br, final Account acc, final boolean force) throws Exception {
        boolean success = false;
        try {
            for (int i = 1; i <= MAX_RETRIES_LOGIN; i++) {
                logger.info("Login try " + i + " of " + MAX_RETRIES_LOGIN);
                try {
                    login(br, acc, true);
                } catch (final Exception e) {
                    if (e instanceof UnknownHostException || e instanceof BrowserException) {
                        logger.info("Login " + i + " of " + MAX_RETRIES_LOGIN + " failed because of server error or timeout");
                        continue;
                    } else {
                        throw e;
                    }
                }
                logger.info("Login " + i + " of " + MAX_RETRIES_LOGIN + " was successful");
                success = true;
                break;
            }
        } catch (final PluginException ep) {
            logger.info("save.tv Account ist ungültig!");
            acc.setValid(false);
            throw ep;
        }
        if (!success) {
            final String lang = System.getProperty("user.language");
            if ("de".equalsIgnoreCase(lang)) {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nLogin wegen Serverfehler oder Timeout fehlgeschlagen!", PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            } else {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nLogin failed because of server error or timeout!", PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
        }
    }

    /* Always compare to the decrypter */
    private void getPageSafe(final String url, final Account acc) throws Exception {
        /*
         * Max 30 logins possible Max 3 accesses of the link possible -> Max 33 total requests
         */
        for (int i = 1; i <= MAX_RETRIES_SAFE_REQUEST; i++) {
            br.getPage(url);
            if (br.getURL().contains("Token=MSG_LOGOUT_B")) {
                logger.info("Refreshing cookies to continue downloading " + i + " of " + MAX_RETRIES_SAFE_REQUEST);
                br.clearCookies(COOKIE_HOST);
                loginSafe(br, acc, true);
                continue;
            }
            if (i > 1) {
                logger.info("Successfully refreshed cookies to access url: " + url);
            }
            break;
        }
    }

    /* Avoud 503 server errors */
    private void postPageSafe(final Browser br, final String url, final String postData) throws IOException, PluginException {
        for (int i = 1; i <= 3; i++) {
            try {
                br.postPage(url, postData);
            } catch (final BrowserException e) {
                if (br.getRequest().getHttpConnection().getResponseCode() == 503) {
                    logger.info("503 BrowserException occured, retry " + i + " of 3");
                    sleep(3000l, DLINK);
                    continue;
                }
                logger.info("Unhandled BrowserException occured...");
                throw e;
            }
            break;
        }
    }

    /**
     * @param dl
     *            DownloadLink
     * @param user_selected_video_quality
     *            : Vom Benutzer bevorzugte Qualitätsstufe
     * @param downloadWithoutAds
     *            : Videos mit angewandter Schnittliste bevorzugen oder nicht
     */
    private void postDownloadPage(final DownloadLink dl, final String user_selected_video_quality, final String downloadWithoutAds) throws IOException {
        br.postPage("https://www.save.tv/STV/M/obj/cRecordOrder/croGetDownloadUrl.cfm?null.GetDownloadUrl", "ajax=true&clientAuthenticationKey=&callCount=1&c0-scriptName=null&c0-methodName=GetDownloadUrl&c0-id=&c0-param0=number:" + getTelecastId(dl) + "&" + user_selected_video_quality + "&c0-param2=boolean:" + downloadWithoutAds + "&xml=true&");
    }

    /**
     * @param dl
     *            DownloadLink
     * @param user_selected_video_quality
     *            : MVom Benutzer bevorzugte Qualitätsstufe
     * @param downloadWithoutAds
     *            : Videos mit angewandter Schnittliste bevorzugen oder nicht
     */
    private void api_postDownloadPage(final DownloadLink dl, final String user_selected_video_quality, final String downloadWithoutAds) throws IOException {
        doSoapRequest("http://tempuri.org/IDownload/GetStreamingUrl", "<sessionId i:type=\"d:string\">" + SESSIONID + "</sessionId><telecastId i:type=\"d:int\">" + getTelecastId(dl) + "</telecastId><telecastIdSpecified i:type=\"d:boolean\">true</telecastIdSpecified><recordingFormatId i:type=\"d:int\">" + user_selected_video_quality + "</recordingFormatId><recordingFormatIdSpecified i:type=\"d:boolean\">true</recordingFormatIdSpecified><adFree i:type=\"d:boolean\">false</adFree><adFreeSpecified i:type=\"d:boolean\">" + downloadWithoutAds + "</adFreeSpecified>");
    }

    private void site_prepBrowser(final Browser br) {
        br.setReadTimeout(3 * 60 * 1000);
        br.setConnectTimeout(3 * 60 * 1000);
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
    }

    private void api_prepBrowser(final Browser br) {
        br.setReadTimeout(3 * 60 * 1000);
        br.setConnectTimeout(3 * 60 * 1000);
        br.getHeaders().put("User-Agent", "kSOAP/2.0");
        br.getHeaders().put("Content-Type", "text/xml");
    }

    public static long calculateFilesize(final String minutes) {
        double calculated_filesize = 0;
        final long duration_minutes = Long.parseLong(minutes);
        final int user_format = getConfiguredFormat();
        switch (user_format) {
        case 0:
            calculated_filesize = QUALITY_HD_MB_PER_MINUTE * duration_minutes * 1024 * 1024;
            break;
        case 1:
            calculated_filesize = QUALITY_H264_NORMAL_MB_PER_MINUTE * duration_minutes * 1024 * 1024;
            break;
        case 2:
            calculated_filesize = QUALITY_H264_MOBILE_MB_PER_MINUTE * duration_minutes * 1024 * 1024;
            break;
        }
        return (long) calculated_filesize;
    }

    /**
     * Returns the format value needed for format specific requests TODO: Once serverside implemented, add support for HD - at the moment,
     * if user selected HD, Normal H.264 will be downloaded instead
     */
    private String api_get_format_request_value() {
        final int selected_video_format = getConfiguredFormat();
        String stv_request_selected_format = null;
        switch (selected_video_format) {
        case 0:
            stv_request_selected_format = "5";
            break;
        case 1:
            stv_request_selected_format = "5";
            break;
        case 2:
            stv_request_selected_format = "4";
            break;
        }
        return stv_request_selected_format;
    }

    /** Returns the format value needed for format specific requests */
    private String site_get_format_request_value() {
        final int selected_video_format = getConfiguredFormat();
        String stv_request_selected_format = null;
        switch (selected_video_format) {
        case 0:
            stv_request_selected_format = "c0-param1=number:2";
            break;
        case 1:
            stv_request_selected_format = "c0-param1=number:0";
            break;
        case 2:
            stv_request_selected_format = "c0-param1=number:1";
            break;
        }
        return stv_request_selected_format;
    }

    /** Gets the filesize from the save.tv telecast-ID page */
    private String site_get_filesize() {
        final int selected_video_format = getConfiguredFormat();
        final String[] filesizes = br.getRegex(">Download</a>[\t\n\r ]+\\(ca\\.([^<>\"/]*?)\\)").getColumn(0);
        int filesizes_length = 0;
        if (filesizes != null) {
            filesizes_length = filesizes.length;
        }
        String filesize = null;

        switch (selected_video_format) {
        case 0:
            if (filesizes_length == 3) {
                filesize = filesizes[2];
            }
            break;
        case 1:
            if (filesizes_length > 0) {
                filesize = filesizes[0];
            }
            break;
        case 2:
            if (filesizes_length >= 2) {
                filesize = filesizes[1];
            }
            break;
        }
        return filesize;
    }

    private static int getConfiguredFormat() {
        switch (SubConfiguration.getConfig("save.tv").getIntegerProperty(selected_video_format, -1)) {
        case 0:
            return 0;
        case 1:
            return 1;
        case 2:
            return 2;
        default:
            return 0;
        }
    }

    private String get_run_time(final Browser br) {
        String run_time_site = br.getRegex("<b>Aufnahmezeitraum:</b>[\t\n\r ]+\\d{2}:\\d{2} \\-[\t\n\r ]+\\d{2}:\\d{2} \\((\\d+) Min\\.\\)").getMatch(0);
        if (run_time_site == null) {
            run_time_site = "0";
        }
        return run_time_site;
    }

    private double site_get_calculated_runtime_minutes(final long page_size_mb) {
        double run_time_calculated = 0;
        final int selected_video_format = getConfiguredFormat();
        switch (selected_video_format) {
        case 0:
            run_time_calculated = page_size_mb / QUALITY_HD_MB_PER_MINUTE;
            break;
        case 1:
            run_time_calculated = page_size_mb / QUALITY_H264_NORMAL_MB_PER_MINUTE;
            break;
        case 2:
            run_time_calculated = page_size_mb / QUALITY_H264_MOBILE_MB_PER_MINUTE;
            break;
        }
        return run_time_calculated;
    }

    /**
     * @param soapAction
     *            : The soap link which should be accessed
     * @param soapPost
     *            : The soap post data
     */
    private void doSoapRequest(final String soapAction, final String soapPost) throws IOException {
        final String method = new Regex(soapAction, "([A-Za-z0-9]+)$").getMatch(0);
        br.getHeaders().put("SOAPAction", soapAction);
        br.getHeaders().put("Content-Type", "text/xml");
        final String postdata = "<?xml version=\"1.0\" encoding=\"utf-8\"?><v:Envelope xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\"><v:Header /><v:Body><" + method + " xmlns=\"http://tempuri.org/\" id=\"o0\" c:root=\"1\">" + soapPost + "</" + method + "></v:Body></v:Envelope>";
        br.postPageRaw("http://api.save.tv/v2/Api.svc", postdata);
    }

    private void saveCookies(final Account acc) {
        /* Save cookies */
        final HashMap<String, String> cookies = new HashMap<String, String>();
        final Cookies add = br.getCookies(COOKIE_HOST);
        for (final Cookie c : add.getCookies()) {
            cookies.put(c.getKey(), c.getValue());
        }
        acc.setProperty("name", Encoding.urlEncode(acc.getUser()));
        acc.setProperty("pass", Encoding.urlEncode(acc.getPass()));
        acc.setProperty("cookies", cookies);
    }

    private boolean is_API_enabled() {
        return this.getPluginConfig().getBooleanProperty(USEAPI);
    }

    /* (Bad) workaround for issues with brackets in String that we regex */
    private String getRegexSafe(final String input, final String regex, final int match) {
        final String regexFixedInput = input.replace("(", "65788jdclipopenjd4684").replace(")", "65788jdclipclosejd4684");
        String result = new Regex(regexFixedInput, regex).getMatch(match);
        if (result != null) {
            result = result.replace("65788jdclipopenjd4684", "(").replace("65788jdclipclosejd4684", ")");
        }
        return result;
    }

    /* Advanced check to find out if what we want to download is a series or something else. */
    private boolean isSeries(final String testTitle) {
        final String otherEpisodes = br.getRegex("Weitere Sendungen aus dieser Unterkategorie[\t\n\r ]+</h3>(.*?)</ul>").getMatch(0);
        if (otherEpisodes != null) {
            if (otherEpisodes.contains(testTitle)) {
                return true;
            }
        }
        return false;
    }

    /* Corrects all kinds of data which Stv provides, makes filenames look better */
    public static String correctData(final String input) {
        String output = Encoding.htmlDecode(input);
        output = output.replace("_", " ");
        output = output.trim();
        output = output.replaceAll("(\r|\n)", "");
        output = output.replace("/", SubConfiguration.getConfig("save.tv").getStringProperty(CUSTOM_FILENAME_SEPERATION_MARK, defaultCustomSeperationMark));

        /* Correct spaces */
        final String[] unneededSpaces = new Regex(output, ".*?([ ]{2,}).*?").getColumn(0);
        if (unneededSpaces != null && unneededSpaces.length != 0) {
            for (String unneededSpace : unneededSpaces) {
                output = output.replace(unneededSpace, " ");
            }
        }
        return output;
    }

    private static String getTelecastId(final DownloadLink link) {
        return new Regex(link.getDownloadURL(), "TelecastID=(\\d+)").getMatch(0);
    }

    private static String getRandomNumber() {
        final DecimalFormat df = new DecimalFormat("0000");
        return df.format(new Random().nextInt(10000));
    }

    public static String getFormattedFilename(final DownloadLink downloadLink) throws ParseException {
        final SubConfiguration cfg = SubConfiguration.getConfig("save.tv");
        final String customStringForEmptyTags = cfg.getStringProperty(CUSTOM_FILENAME_EMPTY_TAG_STRING, defaultCustomStringForEmptyTags);
        final String ext = downloadLink.getStringProperty("type", ".mp4");
        final String genre = downloadLink.getStringProperty("genre", customStringForEmptyTags);
        final String producecountry = downloadLink.getStringProperty("producecountry", customStringForEmptyTags);
        final String produceyear = downloadLink.getStringProperty("produceyear", customStringForEmptyTags);
        final String randomnumber = getRandomNumber();
        final String telecastid = getTelecastId(downloadLink);
        final String tv_station = downloadLink.getStringProperty("plain_tv_station", customStringForEmptyTags);
        final String site_category = downloadLink.getStringProperty("plain_site_category", customStringForEmptyTags);

        final long date = getLongProperty(downloadLink, "originaldate", 0l);
        String formattedDate = null;
        final String userDefinedDateFormat = cfg.getStringProperty(CUSTOM_DATE, "dd.MM.yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date theDate = new Date(date);
        if (userDefinedDateFormat != null) {
            try {
                formatter = new SimpleDateFormat(userDefinedDateFormat);
                formattedDate = formatter.format(theDate);
            } catch (Exception e) {
                /* prevent user error killing plugin */
                formattedDate = "";
            }
        }

        String formattedFilename = null;
        if (belongsToCategoryMovie(downloadLink)) {
            /* For all links except series */
            final String title = downloadLink.getStringProperty("plainfilename", null);
            formattedFilename = cfg.getStringProperty(CUSTOM_FILENAME2, defaultCustomFilename);
            if (formattedFilename == null || formattedFilename.equals("")) {
                formattedFilename = defaultCustomFilename;
            }
            formattedFilename = formattedFilename.toLowerCase();
            if (!formattedFilename.contains("*endung*") || (!formattedFilename.contains("*videotitel*") && !formattedFilename.contains("*zufallszahl*") && !formattedFilename.contains("*telecastid*") && !formattedFilename.contains("*sendername*"))) {
                formattedFilename = defaultCustomFilename;
            }

            formattedFilename = formattedFilename.replace("*zufallszahl*", randomnumber);
            formattedFilename = formattedFilename.replace("*telecastid*", telecastid);
            formattedFilename = formattedFilename.replace("*datum*", formattedDate);
            formattedFilename = formattedFilename.replace("*produktionsjahr*", produceyear);
            formattedFilename = formattedFilename.replace("*endung*", ext);
            formattedFilename = formattedFilename.replace("*sendername*", tv_station);
            formattedFilename = formattedFilename.replace("*kategorie*", site_category);
            formattedFilename = formattedFilename.replace("*genre*", genre);
            formattedFilename = formattedFilename.replace("*produktionsland*", producecountry);
            /* Insert actual filename at the end to prevent errors with tags */
            formattedFilename = formattedFilename.replace("*videotitel*", title);
        } else {
            /* For series */
            formattedFilename = cfg.getStringProperty(CUSTOM_FILENAME_SERIES2, defaultCustomSeriesFilename);
            if (formattedFilename == null || formattedFilename.equals("")) {
                formattedFilename = defaultCustomFilename;
            }
            formattedFilename = formattedFilename.toLowerCase();
            if (!formattedFilename.contains("*endung*") || (!formattedFilename.contains("*serientitel*") && !formattedFilename.contains("*episodenname*") && !formattedFilename.contains("*episodennummer*") && !formattedFilename.contains("*zufallszahl*") && !formattedFilename.contains("*telecastid*") && !formattedFilename.contains("*sendername*"))) {
                formattedFilename = defaultCustomFilename;
            }

            final String seriestitle = downloadLink.getStringProperty("seriestitle", null);
            final String episodename = downloadLink.getStringProperty("episodename", null);
            final String episodenumber = getEpisodeNumber(downloadLink);

            formattedFilename = formattedFilename.replace("*zufallszahl*", randomnumber);
            formattedFilename = formattedFilename.replace("*telecastid*", telecastid);
            formattedFilename = formattedFilename.replace("*datum*", formattedDate);
            formattedFilename = formattedFilename.replace("*produktionsjahr*", produceyear);
            formattedFilename = formattedFilename.replace("*episodennummer*", episodenumber);
            formattedFilename = formattedFilename.replace("*endung*", ext);
            formattedFilename = formattedFilename.replace("*sendername*", tv_station);
            formattedFilename = formattedFilename.replace("*kategorie*", site_category);
            formattedFilename = formattedFilename.replace("*genre*", genre);
            formattedFilename = formattedFilename.replace("*produktionsland*", producecountry);
            /* Insert filename at the end to prevent errors with tags */
            formattedFilename = formattedFilename.replace("*serientitel*", seriestitle);
            formattedFilename = formattedFilename.replace("*episodenname*", episodename);
        }
        formattedFilename = encodeUnicode(formattedFilename);
        if (downloadLink.getBooleanProperty("is_adsfree_failed", false)) {
            formattedFilename = "¡" + formattedFilename;
        }

        formattedFilename = fixCharIssues(formattedFilename);
        return formattedFilename;
    }

    /* Stable workaround */
    public static long getLongProperty(final Property link, final String key, final long def) {
        try {
            return link.getLongProperty(key, def);
        } catch (final Throwable e) {
            try {
                Object r = link.getProperty(key, def);
                if (r instanceof String) {
                    r = Long.parseLong((String) r);
                } else if (r instanceof Integer) {
                    r = ((Integer) r).longValue();
                }
                final Long ret = (Long) r;
                return ret;
            } catch (final Throwable e2) {
                return def;
            }
        }
    }

    /* Attempt to build filenames which look like the original save.tv server-filenames */
    private String getFakeOriginalFilename(final DownloadLink downloadLink) throws ParseException {
        String ext = downloadLink.getStringProperty("type", null);
        if (ext == null) {
            ext = ".mp4";
        }

        final long date = getLongProperty(downloadLink, "originaldate", 0l);
        String formattedDate = null;
        final String userDefinedDateFormat = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date theDate = new Date(date);
        if (userDefinedDateFormat != null) {
            try {
                formatter = new SimpleDateFormat(userDefinedDateFormat);
                formattedDate = formatter.format(theDate);
            } catch (Exception e) {
                /* prevent user error killing plugin */
                formattedDate = "";
            }
        }

        final DecimalFormat df = new DecimalFormat("0000");
        final int random1 = new Random().nextInt(1000);
        final DecimalFormat df2 = new DecimalFormat("000000");
        final int random2 = new Random().nextInt(100000);
        String formattedFilename = downloadLink.getStringProperty("apiplainfilename", null);
        if (formattedFilename != null) {
        } else {
            if (belongsToCategoryMovie(downloadLink) || getLongProperty(downloadLink, "category", 0l) == 0l) {
                final String title = downloadLink.getStringProperty("plainfilename", null);
                formattedFilename = title.replace(" ", "_") + "_";
                formattedFilename += formattedDate + "_";
                formattedFilename += df.format(random1) + "_" + df2.format(random2);
            } else {
                final String seriestitle = downloadLink.getStringProperty("seriestitle", null);
                final String episodename = downloadLink.getStringProperty("episodename", null);
                final String episodenumber = getEpisodeNumber(downloadLink);
                /* For series */
                formattedFilename = seriestitle.replace(" ", "_") + "_";
                formattedFilename += episodename.replace(" ", "_") + "_";
                formattedFilename += "Folge" + episodenumber + "_" + formattedDate + "_";
                formattedFilename += df.format(random1) + "_" + df2.format(random2);
            }
            formattedFilename += ".mp4";
            formattedFilename = encodeUnicode(formattedFilename);
        }

        return formattedFilename;
    }

    private static String getEpisodeNumber(final DownloadLink dl) {
        final long episodenumber = getLongProperty(dl, "episodenumber", 0l);
        if (episodenumber == 0) {
            return SubConfiguration.getConfig("save.tv").getStringProperty(CUSTOM_FILENAME_EMPTY_TAG_STRING, defaultCustomStringForEmptyTags);
        } else {
            return Long.toString(episodenumber);
        }
    }

    /* Several catregories are internally handled as category movie */
    private static boolean belongsToCategoryMovie(final DownloadLink dl) {
        long cat = getLongProperty(dl, "category", 0l);
        final boolean belongsToCategoryMovie = (cat == 0 || cat == 1 || cat == 3 || cat == 7);
        return belongsToCategoryMovie;
    }

    /* Correct characters of serverside encoding failures */
    private static String fixCharIssues(final String input) {
        String output = input;
        /* Part 1 */
        output = output.replace("ÃŸ", "ß");
        output = output.replace("Ã„", "Ä");
        output = output.replace("Ãœ", "Ü");
        output = output.replace("Ã–", "Ö");

        /* Part 2 */
        output = output.replace("Ã¶", "ö");
        output = output.replace("Ã¤", "ä");
        output = output.replace("Ã¼", "ü");
        // output = output.replace("Ã?", "");
        return output;
    }

    /* Avoid chars which are not allowed in filenames under certain OS' */
    private static String encodeUnicode(final String input) {
        String output = input;
        output = output.replace(":", ";");
        output = output.replace("|", "¦");
        output = output.replace("<", "[");
        output = output.replace(">", "]");
        output = output.replace("/", "⁄");
        output = output.replace("\\", "∖");
        output = output.replace("*", "#");
        output = output.replace("?", "¿");
        output = output.replace("!", "¡");
        output = output.replace("\"", "'");
        return output;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public String getDescription() {
        return "JDownloader's Save.tv Plugin helps downloading videoclips from Save.tv. Save.tv provides different settings for its downloads.";
    }

    private final static String defaultCustomFilename                             = "*videotitel**telecastid**endung*";
    private final static String defaultCustomSeriesFilename                       = "*serientitel* ¦ *episodennummer* ¦ *episodenname**endung*";
    private final static String defaultCustomSeperationMark                       = "+";
    private final static String defaultCustomStringForEmptyTags                   = "-";
    private final static int    defaultCrawlLastdays                              = 0;
    private final static int    defaultNoAdsFreeAvailableRetryWaitHours           = 12;
    private final static int    defaultIgnoreOnlyAdsFreeAfterRetries_maxRetries   = 2;
    private final static int    defaultImarkAdsFreeFailedLinks_difference_minutes = 0;

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Allgemeine Einstellungen:"));
        final ConfigEntry useAPI = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.USEAPI, JDL.L("plugins.hoster.SaveTv.UseAPI", "API verwenden?\r\nINFO: Aktiviert man die API, entfallen folgende Features:\r\n-Benutzerdefinierte Dateinamen\r\n-Archiv-Crawler\r\n-Nur Aufnahmen mit angewandter Schnittliste laden\r\n-Anzeigen der Account Details in der Account-Verwaltung (Account Typ, Ablaufdatum, ...)\r\nAus technischen Gründen ist es (noch) nicht möglich, alle dann Inaktiven Einstellungen bei\r\naktivierter API auszugrauen um dem Benutzer visuelles Feedback zu geben, sorry!")).setDefaultValue(false);
        getConfig().addEntry(useAPI);
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.DISABLE_LINKCHECK, JDL.L("plugins.hoster.SaveTv.DisableLinkcheck", "Linkcheck deaktivieren?\r\nVorteile:\r\n-Links landen schneller im Linkgrabber und können auch bei Serverproblemen oder wenn die save.tv Seite komplett offline ist gesammelt werden\r\nNachteile:\r\n-Im Linkgrabber werden zunächst nur die telecast-IDs als Dateinamen angezeigt\r\n-Die endgültigen Dateinamen werden erst beim Downloadstart angezeigt")).setDefaultValue(false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));

        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Archiv-Crawler Einstellungen:"));
        final ConfigEntry grabArchives = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.CRAWLER_ACTIVATE, JDL.L("plugins.hoster.SaveTv.grabArchive", "Archiv-Crawler aktivieren?\r\nINFO: Fügt das komplette Archiv oder Teile davon beim Einfügen dieses Links ein:\r\n'http://www.save.tv/STV/M/obj/user/usShowVideoArchive.cfm'?\r\n")).setDefaultValue(false).setEnabledCondidtion(useAPI, false);
        getConfig().addEntry(grabArchives);
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SPINNER, getPluginConfig(), SaveTv.CRAWLER_LASTDAYS_COUNT, JDL.L("plugins.hoster.SaveTv.grabArchive.LastDaysCount", "Nur Aufnahmen der letzten X Tage crawlen??\r\nAnzahl der Tage, die gecrawlt werden sollen [0 = komplettes Archiv = alle Tage]:"), 0, 32, 1).setDefaultValue(defaultCrawlLastdays).setEnabledCondidtion(useAPI, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.CRAWLER_ENABLE_FASTER, JDL.L("plugins.hoster.SaveTv.grabArchiveFaster", "Aktiviere schnellen Linkcheck für Archiv-Crawler?\r\nVorteil: Über den Archiv-Crawler hinzugefügte Links landen viel schneller im Linkgrabber\r\nNachteil: Es sind nicht alle Informationen (z.B. Produktionsjahr) verfügbar - erst beim Download oder späterem Linkcheck\r\n")).setDefaultValue(false).setEnabledCondidtion(grabArchives, true));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.CRAWLER_DISABLE_DIALOGS, JDL.L("plugins.hoster.SaveTv.crawlerDisableDialogs", "Info Dialoge des Archiv-Crawlers (nach dem Crawlen oder im Fehlerfall) deaktivieren?")).setDefaultValue(false).setEnabledCondidtion(grabArchives, true));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));

        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Format & Qualitäts-Einstellungen:"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_COMBOBOX_INDEX, getPluginConfig(), selected_video_format, formats, JDL.L("plugins.hoster.SaveTv.prefer_format", "Bevorzugtes Format")).setDefaultValue(0));
        final ConfigEntry preferAdsFree = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.PREFERADSFREE, JDL.L("plugins.hoster.SaveTv.PreferAdFreeVideos", "Aufnahmen mit angewandter Schnittliste bevorzugen?")).setDefaultValue(true);
        getConfig().addEntry(preferAdsFree);
        final ConfigEntry downloadOnlyAdsFree = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.DOWNLOADONLYADSFREE, JDL.L("plugins.hoster.SaveTv.downloadOnlyAdsFree", "Nur Aufnahmen mit angewandter Schnittliste laden?\r\nINFO: Zur Nutzung muss die Option\r\n'Aufnahmen mit angewandter Schnittliste bevorzugen' aktiviert sein.")).setDefaultValue(false).setEnabledCondidtion(preferAdsFree, true);
        getConfig().addEntry(downloadOnlyAdsFree);
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SPINNER, getPluginConfig(), SaveTv.DOWNLOADONLYADSFREE_RETRY_HOURS, JDL.L("plugins.hoster.SaveTv.downloadOnlyAdsFreeRetryHours", "Zeit [in stunden] bis zum Neuversuch für Aufnahmen, die (noch) keine Schnittliste haben.\r\nINFO: Der Standardwert beträgt 12 Stunden, um die Server nicht unnötig zu belasten.\r\n"), 1, 24, 1).setDefaultValue(defaultNoAdsFreeAvailableRetryWaitHours).setEnabledCondidtion(downloadOnlyAdsFree, true));
        final ConfigEntry ignoreOnlyAdsFreeAfterRetries = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.PREFERADSFREE_OVERRIDE, JDL.L("plugins.hoster.SaveTv.forceDownloadWithzAdsAfterXretries", "Download OHNE Schnittliste erzwingen, falls nach X versuchen noch immer nicht verfügbar?\r\nINFO: Ein Versuch = Keine Schnittliste verfügbar & die oben angegebene Wartezeit wird einmal abgewartet")).setDefaultValue(false).setEnabledCondidtion(preferAdsFree, true);
        getConfig().addEntry(ignoreOnlyAdsFreeAfterRetries);
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SPINNER, getPluginConfig(), SaveTv.PREFERADSFREE_OVERRIDE_MAXRETRIES, JDL.L("plugins.hoster.SaveTv.ignoreOnlyAdsFreeAfterRetries_maxRetries", "Max Anzahl Neuversuche bis der Download ohne Schnittliste erzwungen wird:\r\nINFO: Diese Einstellungen hat nur Auswirkungen, solange die Einstellung darüber aktiviert ist!"), 1, 100, 1).setDefaultValue(defaultIgnoreOnlyAdsFreeAfterRetries_maxRetries).setEnabledCondidtion(ignoreOnlyAdsFreeAfterRetries, true));

        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Dateiname Einstellungen:"));
        final ConfigEntry origName = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.USEORIGINALFILENAME, JDL.L("plugins.hoster.SaveTv.UseOriginalFilename", "Original (Server) Dateinamen verwenden? [erst beim Downloadstart sichtbar]")).setDefaultValue(false).setEnabledCondidtion(useAPI, false);
        getConfig().addEntry(origName);
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_DATE, JDL.L("plugins.hoster.savetv.customdate", "Setze das Datumsformat:\r\nWichtige Information dazu:\r\nDas Datum erscheint im angegebenen Format im Dateinamen, allerdings nur,\r\nwenn man das *datum* Tag auch verwendet (siehe Benutzerdefinierte Dateinamen für Filme und Serien unten)")).setDefaultValue("dd.MM.yyyy").setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        final StringBuilder sbinfo = new StringBuilder();
        sbinfo.append("Erklärung zur Nutzung eigener Dateinamen:\r\n");
        sbinfo.append("Eigene Dateinamen lassen sich unten über ein Tag-System (siehe weiter unten) nutzen.\r\n");
        sbinfo.append("Das bedeutet, dass man die Struktur seiner gewünschten Dateinamen definieren kann.\r\n");
        sbinfo.append("Dabei hat man Tags wie z.B. *telecastid*, die dann durch Daten ersetzt werden.\r\n");
        sbinfo.append("Wichtig dabei ist, dass Tags immer mit einem Stern starten und enden.\r\n");
        sbinfo.append("Man darf nichts zwischen ein Tag schreiben z.B. *-telecastid-*, da das\r\n");
        sbinfo.append("zu unschönen Dateinamen führt und das Tag nicht die Daten ersetzt werden kann.\r\n");
        sbinfo.append("Wenn man die Tags trennen will muss man die anderen Zeichen zwischen Tags\r\n");
        sbinfo.append("z.B. '-*telecastid*-*endung*' -> Der Dateiname würde dann in etwa so aussehen: '-7573789-.mp4' (ohne die '')\r\n");
        sbinfo.append("WICHTIG: Tags, zu denen die Daten fehlen, werden standardmäßig durch '-' (Bindestrich) ersetzt!\r\n");
        sbinfo.append("Fehlen z.B. die Daten zu *genre*, steht statt statt dem Genre dann ein Bindestrich ('-') an dieser Stelle im Dateinamen.\r\n");
        sbinfo.append("Gut zu wissen: Statt dem Bindestrich lässt sich hierfür unten auch ein anderes Zeichen bzw. Zeichenfolge definieren.\r\n");
        sbinfo.append("Außerdem: Für Filme und Serien gibt es unterschiedliche Tags.\r\n");
        sbinfo.append("Kaputtmachen kannst du mit den Einstellungen prinzipiell nichts also probiere es einfach aus ;)\r\n");
        sbinfo.append("Tipp: Die Save.tv PluginEinstellungen lassen sich rechts oben wieder auf ihre Standardwerte zurücksetzen!\r\n");
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, sbinfo.toString()).setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_FILENAME2, JDL.L("plugins.hoster.savetv.customfilenamemovies", "Eigener Dateiname für Filme/Shows:")).setDefaultValue(defaultCustomFilename).setEnabledCondidtion(origName, false));
        final StringBuilder sb = new StringBuilder();
        sb.append("Erklärung der verfügbaren Tags:\r\n");
        sb.append("*datum* = Datum der Ausstrahlung der aufgenommenen Sendung\r\n[Erscheint im oben definierten Format, wird von der save.tv Seite ausgelesen]\r\n");
        sb.append("*genre* = Das Genre\r\n");
        sb.append("*produktionsland* = Name des Produktionslandes\r\n");
        sb.append("*produktionsjahr* = Produktionsjahr\r\n");
        sb.append("*sendername* = Name des TV-Senders auf dem die Sendung ausgestrahlt wurde\r\nWICHTIG: Ist nur bei Links verfügbar, die über den Crawler eingefügt wurden!\r\n");
        sb.append("*kategorie* = Kategorie, siehe telecast-ID Seite\r\n");
        sb.append("*videotitel* = Name des Videos ohne Dateiendung\r\n");
        sb.append("*zufallszahl* = Eine vierstellige Zufallszahl\r\n[Nützlich um Dateinamenkollisionen zu vermeiden]\r\n");
        sb.append("*telecastid* = Die id, die in jedem save.tv Link steht: TelecastID=XXXXXXX\r\n[Nützlich um Dateinamenkollisionen zu vermeiden]\r\n");
        sb.append("*endung* = Die Dateiendung, in diesem Fall immer '.mp4'");
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, sb.toString()).setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_FILENAME_SERIES2, JDL.L("plugins.hoster.savetv.customseriesfilename", "Eigener Dateiname für Serien:")).setDefaultValue(defaultCustomSeriesFilename).setEnabledCondidtion(origName, false));
        final StringBuilder sbseries = new StringBuilder();
        sbseries.append("Erklärung der verfügbaren Tags:\r\n");
        sbseries.append("*datum* = Datum der Ausstrahlung der aufgenommenen Sendung\r\n[Erscheint im oben definierten Format, wird von der save.tv Seite ausgelesen]\r\n");
        sbseries.append("*genre* = Das Genre\r\n");
        sbseries.append("*produktionsland* = Name des Produktionslandes\r\n");
        sbseries.append("*produktionsjahr* = Produktionsjahr\r\n");
        sbseries.append("*sendername* = Name des TV-Senders auf dem die Sendung ausgestrahlt wurde\r\nWICHTIG: Ist nur bei Links verfügbar, die über den Crawler eingefügt wurden!\r\n");
        sbseries.append("*kategorie* = Kategorie, siehe telecast-ID Seite\r\n");
        sbseries.append("*serientitel* = Name der Serie\r\n");
        sbseries.append("*episodenname* = Name der Episode\r\n");
        sbseries.append("*episodennummer* = Episodennummer\r\n");
        sbseries.append("*zufallszahl* = Eine vierstellige Zufallszahl - nützlich um Dateinamenkollisionen zu vermeiden\r\n");
        sbseries.append("*telecastid* = Die id, die in jedem save.tv Link steht: TelecastID=XXXXXXX\r\n[Nützlich um Dateinamenkollisionen zu vermeiden]\r\n");
        sbseries.append("*endung* = Die Dateiendung, immer '.mp4'");
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, sbseries.toString()).setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));

        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Erweiterte Einstellungen:"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), SaveTv.DELETE_TELECAST_ID_AFTER_DOWNLOAD, JDL.L("plugins.hoster.SaveTv.deleteFromArchiveAfterDownload", "Erfolgreich geladene telecastIDs aus dem save.tv Archiv löschen?\r\n Warnung: Gelöschte telecast-IDs können nicht wiederhergestellt werden!\r\nFalls diese Funktion einen Fehler beinhaltet, ist Datenverlust möglich!\r\nWICHTIG: Bei aktivierter API kann man diese Einstellung nicht verwenden!")).setDefaultValue(false).setEnabledCondidtion(useAPI, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_FILENAME_SEPERATION_MARK, JDL.L("plugins.hoster.savetv.customFilenameSeperationmark", "Trennzeichen als Ersatz für '/'  (da ungültig in Dateinamen):")).setDefaultValue(defaultCustomSeperationMark).setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_FILENAME_EMPTY_TAG_STRING, JDL.L("plugins.hoster.savetv.customEmptyTagsString", "Zeichen, mit dem Tags ersetzt werden sollen, deren Daten fehlen:")).setDefaultValue(defaultCustomStringForEmptyTags).setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SPINNER, getPluginConfig(), SaveTv.FAILED_ADSFREE_DIFFERENCE_MINUTES, JDL.L("plugins.hoster.SaveTv.markAdsFreeFailedLinks", "Markiere telecast-IDs bei denen die Schnittliste vermutlich fehlgeschlagen ist mit einem '¡' am Anfang vom Dateinamen, sobald die\r\nberechnete Aufnahmezeit mehr als X Minuten von der angegebenen Aufnahmezeit abweicht [0 = deaktiviert]\r\nWICHTIG: Dieses Feature ist nur aktiv, wenn auch die eigenen Dateinamen genutzt werden!"), 0, 30, 1).setDefaultValue(defaultImarkAdsFreeFailedLinks_difference_minutes).setEnabledCondidtion(useAPI, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        final StringBuilder sbmore = new StringBuilder();
        sbmore.append("Definiere Filme oder Serien, für die trotz obiger Einstellungen die Originaldateinamen\r\n");
        sbmore.append("verwendet werden sollen.\r\n");
        sbmore.append("Manche mehrteiligen Filme haben dieselben Titel und bei manchen Serien fehlen die Episodennamen,\r\n");
        sbmore.append("wodurch sie alle dieselben Dateinamen bekommen -> JDownloader denkt es seien Duplikate/Mirrors und lädt nur\r\n");
        sbmore.append("einen der scheinbar gleichen Dateien.\r\n");
        sbmore.append("Um dies zu verhindern, kann man in den Eingabefeldern Namen solcher Filme/Serien eintragen,\r\n");
        sbmore.append("für die trotz obiger Einstellungen der Original Dateiname verwendet werden soll.\r\n");
        sbmore.append("Beispiel: 'serienname 1|serienname 2|usw.' (ohne die '')\r\n");
        sbmore.append("Die Eingabe erfolgt als RegEx. Wer nicht weiß was das ist -> Google");
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, sbmore.toString()).setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), FORCE_ORIGINALFILENAME_SERIES, JDL.L("plugins.hoster.savetv.forceoriginalnameforspecifiedseries", "Original Dateinamen für folgende Serien erzwingen [Eingabe erfolgt in RegEx]:")).setDefaultValue("").setEnabledCondidtion(origName, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), FORCE_ORIGINALFILENAME_MOVIES, JDL.L("plugins.hoster.savetv.forcefilenameforspecifiedmovies", "Original Dateinamen für folgende Filme erzwingen [Eingabe erfolgt in RegEx]:")).setDefaultValue("").setEnabledCondidtion(origName, false));
    }

    private static String getMessageEnd() {
        String message = "";
        message += "\r\n\r\n";
        message += "Falls du Fehler findest oder Fragen hast, melde dich jederzeit gerne bei uns: board.jdownloader.org.\r\n";
        message += "\r\n";
        message += "Dieses Fenster wird nur einmal angezeigt.\r\nAlle wichtigen Informationen stehen auch in den save.tv Plugin Einstellungen.\r\n";
        message += "\r\n";
        message += "- Das JDownloader Team wünscht weiterhin viel Spaß mit JDownloader und save.tv! -";
        return message;
    }

    private void checkAccountNeededDialog() {
        SubConfiguration config = null;
        try {
            config = getPluginConfig();
            if (config.getBooleanProperty("accNeededShown", Boolean.FALSE) == false) {
                if (config.getProperty("accNeededShown2") == null) {
                    showAccNeededDialog();
                } else {
                    config = null;
                }
            } else {
                config = null;
            }
        } catch (final Throwable e) {
        } finally {
            if (config != null) {
                config.setProperty("accNeededShown", Boolean.TRUE);
                config.setProperty("accNeededShown2", "shown");
                config.save();
            }
        }
    }

    private static void showAccNeededDialog() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        String message = "";
                        String title = null;
                        title = "Save.tv - Account benötigt";
                        message += "Hallo lieber save.tv Nutzer.\r\n";
                        message += "Um über JDownloader Videos aus deinem save.tv Archiv herunterladen zu können musst du\r\nzunächst deinen save.tv Account in JDownloader eintragen.";
                        message += "\r\n";
                        message += "In JDownloader 0.9.581 geht das unter:\r\n";
                        message += "Einstellungen -> Anbieter -> -> Premium -> Account hinzufügen save.tv\r\n";
                        message += "\r\n";
                        message += "In der JDownloader 2 BETA geht das unter:\r\n";
                        message += "Einstellungen -> Accountverwaltung -> Hinzufügen -> save.tv\r\n";
                        message += "\r\n";
                        message += "Sobald du deinen Account eingetragen hast kannst du aus deinem save.tv Archiv\r\n";
                        message += "Links dieses Formats in JDownloader einfügen und herunterladen:\r\n";
                        message += "save.tv/STV/M/obj/user/usShowVideoArchiveDetail.cfm?TelecastID=XXXXXXX";
                        message += getMessageEnd();
                        JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null);
                    } catch (Throwable e) {
                    }
                }
            });
        } catch (Throwable e) {
        }
    }

    private void checkFeatureDialogAll() {
        SubConfiguration config = null;
        try {
            config = getPluginConfig();
            if (config.getBooleanProperty("featuredialog_all_Shown", Boolean.FALSE) == false) {
                if (config.getProperty("featuredialog_all_Shown2") == null) {
                    showFeatureDialogAll();
                } else {
                    config = null;
                }
            } else {
                config = null;
            }
        } catch (final Throwable e) {
        } finally {
            if (config != null) {
                config.setProperty("featuredialog_all_Shown", Boolean.TRUE);
                config.setProperty("featuredialog_all_Shown2", "shown");
                config.save();
            }
        }
    }

    private static void showFeatureDialogAll() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        String message = "";
                        String title = null;
                        title = "Save.tv Plugin - Features";
                        message += "Hallo lieber save.tv Nutzer/liebe save.tv Nutzerin\r\n";
                        message += "Das save.tv Plugin bietet folgende Features:\r\n";
                        message += "- Automatisierter Download von save.tv Links (telecast-IDs)\r\n";
                        message += "- Laden des kompletten save.tv Archivs über wenige Klicks\r\n";
                        message += "-- > Oder wahlweise nur alle Links der letzten X Tage\r\n";
                        message += "- Benutzerdefinierte Dateinamen über ein Tag-System mit vielen Möglichkeiten\r\n";
                        message += "- Alles unter beachtung der Schnittlisten-Einstellungen und des Formats\r\n";
                        message += "- Und viele mehr...\r\n";
                        message += "\r\n";
                        message += "Diese einstellungen sind nur in der Version JDownloader 2 BETA verfügbar unter:\r\nEinstellungen -> Plugin Einstellungen -> save.tv";
                        message += getMessageEnd();
                        JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null);
                    } catch (Throwable e) {
                    }
                }
            });
        } catch (Throwable e) {
        }
    }

    private void checkFeatureDialogCrawler() {
        SubConfiguration config = null;
        try {
            config = getPluginConfig();
            if (config.getBooleanProperty("featuredialog_crawler_Shown", Boolean.FALSE) == false) {
                if (config.getProperty("featuredialog_crawler_Shown2") == null) {
                    showFeatureDialogCrawler();
                } else {
                    config = null;
                }
            } else {
                config = null;
            }
        } catch (final Throwable e) {
        } finally {
            if (config != null) {
                config.setProperty("featuredialog_crawler_Shown", Boolean.TRUE);
                config.setProperty("featuredialog_crawler_Shown2", "shown");
                config.save();
            }
        }
    }

    private static void showFeatureDialogCrawler() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        String message = "";
                        String title = null;
                        title = "Save.tv Plugin - Crawler Features";
                        message += "Hallo lieber save.tv Nutzer/liebe save.tv NutzerIn\r\n";
                        message += "Das save.tv Crawler Plugin bietet folgende Features:\r\n";
                        message += "- Info-Dialoge\r\n";
                        message += "- Viele Dateiinfos trotz aktiviertem schnellen Linkcheck\r\n";
                        message += "- Eigene Dateinamen auch bei aktiviertem schnellen Linkcheck (eingeschränkt)\r\n";
                        message += "- Die Möglichkeit, wahlweise alle oder nur Aufnahmen der letzten X Tage zu crawlen\r\n";
                        message += "\r\n";
                        message += "Die Crawler Einstellungen sind nur in der Version JDownloader 2 BETA verfügbar unter:\r\nEinstellungen -> Plugin Einstellungen -> save.tv";
                        message += getMessageEnd();
                        JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null);
                    } catch (Throwable e) {
                    }
                }
            });
        } catch (Throwable e) {
        }
    }

    private void checkFeatureDialogHD() {
        SubConfiguration config = null;
        try {
            config = getPluginConfig();
            if (config.getBooleanProperty("featuredialog_hd_Shown", Boolean.FALSE) == false) {
                if (config.getProperty("featuredialog_hd_Shown2") == null) {
                    showFeatureDialogHD();
                } else {
                    config = null;
                }
            } else {
                config = null;
            }
        } catch (final Throwable e) {
        } finally {
            if (config != null) {
                config.setProperty("featuredialog_hd_Shown", Boolean.TRUE);
                config.setProperty("featuredialog_hd_Shown2", "shown");
                config.save();
            }
        }
    }

    private static void showFeatureDialogHD() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        String message = "";
                        String title = null;
                        title = "Save.tv Plugin - 16.06.14 - HD verfügbar";
                        message += "Hallo lieber save.tv Nutzer/liebe save.tv NutzerIn\r\n";
                        message += "Ab sofort kannst du HD als bevorzugtes Format in den Plugin Einstellungen auswählen (standardmäßig bevorzugt).\r\n";
                        message += "Bedenkt bitte, dass dies über die API (noch) nicht funktioniert!\r\n";
                        message += getMessageEnd();
                        JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null);
                    } catch (Throwable e) {
                    }
                }
            });
        } catch (Throwable e) {
        }
    }

    public void showAccountDetailsDialog(final Account account) {
        final AccountInfo ai = account.getAccountInfo();
        if (ai != null) {
            final String windowTitleLangText = "Account Zusatzinformationen";
            final String accType = account.getStringProperty("acc_type", "Premium Account");
            final String acc_expire = account.getStringProperty("acc_expire", "?");
            final String acc_package = account.getStringProperty("acc_package", "?");
            final String acc_price = account.getStringProperty("acc_price", "?");
            final String acc_capacity = account.getStringProperty("acc_capacity", "?");
            final String acc_runtime = account.getStringProperty("acc_runtime", "?");
            final String acc_count_archive_entries = account.getStringProperty("acc_count_archive_entries", "?");
            final String acc_count_telecast_ids = account.getStringProperty("acc_count_telecast_ids", "?");

            /* it manages new panel */
            final PanelGenerator panelGenerator = new PanelGenerator();

            JLabel hostLabel = new JLabel("<html><b>" + account.getHoster() + "</b></html>");
            hostLabel.setIcon(DomainInfo.getInstance(account.getHoster()).getFavIcon());
            panelGenerator.addLabel(hostLabel);

            String revision = "$Revision$";
            try {
                String[] revisions = revision.split(":");
                revision = revisions[1].replace('$', ' ').trim();
            } catch (final Exception e) {
                logger.info("save.tv revision number error: " + e);
            }

            panelGenerator.addCategory("Account");
            panelGenerator.addEntry("Name:", account.getUser());
            panelGenerator.addEntry("Account Typ:", accType);
            panelGenerator.addEntry("Paket:", acc_package);
            panelGenerator.addEntry("Laufzeit:", acc_runtime);
            panelGenerator.addEntry("Ablaufdatum:", acc_expire + " Uhr");
            panelGenerator.addEntry("Preis:", acc_price);
            panelGenerator.addEntry("Aufnahmekapazität:", acc_capacity);
            panelGenerator.addEntry("Sendungen im Archiv:", acc_count_archive_entries);
            panelGenerator.addEntry("Ladbare Sendungen im Archiv (telecast-IDs):", acc_count_telecast_ids);

            panelGenerator.addCategory("Download");
            panelGenerator.addEntry("Max. Anzahl gleichzeitiger Downloads:", "20");
            panelGenerator.addEntry("Max. Anzahl Verbindungen pro Datei (Chunks):", "2");
            panelGenerator.addEntry("Abgebrochene Downloads fortsetzbar:", "Ja");

            panelGenerator.addEntry("Plugin Revision:", revision);

            ContainerDialog dialog = new ContainerDialog(UIOManager.BUTTONS_HIDE_CANCEL + UIOManager.LOGIC_COUNTDOWN, windowTitleLangText, panelGenerator.getPanel(), null, "Schließen", "");
            try {
                Dialog.getInstance().showDialog(dialog);
            } catch (DialogNoAnswerException e) {
            }
        }

    }

    public class PanelGenerator {
        private JPanel panel = new JPanel();
        private int    y     = 0;

        public PanelGenerator() {
            panel.setLayout(new GridBagLayout());
            panel.setMinimumSize(new Dimension(270, 200));
        }

        public void addLabel(JLabel label) {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridwidth = 2;
            c.gridy = y;
            c.insets = new Insets(0, 5, 0, 5);
            panel.add(label, c);
            y++;
        }

        public void addCategory(String categoryName) {
            JLabel category = new JLabel("<html><u><b>" + categoryName + "</b></u></html>");

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridwidth = 2;
            c.gridy = y;
            c.insets = new Insets(10, 5, 0, 5);
            panel.add(category, c);
            y++;
        }

        public void addEntry(String key, String value) {
            GridBagConstraints c = new GridBagConstraints();
            JLabel keyLabel = new JLabel(key);
            // keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.9;
            c.gridx = 0;
            c.gridy = y;
            c.insets = new Insets(0, 5, 0, 5);
            panel.add(keyLabel, c);

            JLabel valueLabel = new JLabel(value);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            panel.add(valueLabel, c);

            y++;
        }

        public void addTextField(JTextArea textfield) {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridwidth = 2;
            c.gridy = y;
            c.insets = new Insets(0, 5, 0, 5);
            panel.add(textfield, c);
            y++;
        }

        public JPanel getPanel() {
            return panel;
        }

    }

}