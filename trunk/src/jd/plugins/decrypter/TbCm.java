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

package jd.plugins.decrypter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.config.SubConfiguration;
import jd.controlling.AccountController;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.http.Request;
import jd.nutils.encoding.Encoding;
import jd.nutils.io.JDIO;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.Form.MethodType;
import jd.plugins.Account;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;
import de.savemytube.flv.FLV;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "youtube.com" }, urls = { "https?://[\\w\\.]*?youtube\\.com/(embed/|.*?watch.*?v=|.*?watch.*?v%3D|view_play_list\\?p=|playlist\\?(p|list)=|.*?g/c/|.*?grid/user/|v/)[a-z\\-_A-Z0-9]+(.*?page=\\d+)?" }, flags = { 0 })
public class TbCm extends PluginForDecrypt {
	private static AtomicBoolean PLUGIN_DISABLED = new AtomicBoolean(false);

	static {
		String installerSource = null;
		try {

			installerSource = JDIO.readFileToString(JDUtilities
					.getResourceFile("src.dat"));
			PLUGIN_DISABLED.set(installerSource.contains("\"PS\""));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public TbCm(PluginWrapper wrapper) {
		super(wrapper);
	};

	public static enum DestinationFormat {
		AUDIOMP3("Audio (MP3)", new String[] { ".mp3" }), VIDEOFLV(
				"Video (FLV)", new String[] { ".flv" }), VIDEOMP4(
				"Video (MP4)", new String[] { ".mp4" }), VIDEOWEBM(
				"Video (Webm)", new String[] { ".webm" }), VIDEO3GP(
				"Video (3GP)", new String[] { ".3gp" }), UNKNOWN(
				"Unknown (unk)", new String[] { ".unk" }),

		VIDEOIPHONE("Video (IPhone)", new String[] { ".mp4" });

		private String text;
		private String[] ext;

		DestinationFormat(final String text, final String[] ext) {
			this.text = text;
			this.ext = ext;
		}

		public String getExtFirst() {
			return this.ext[0];
		}

		public String getText() {
			return this.text;
		}

		@Override
		public String toString() {
			return this.text;
		}

	}

	static class Info {
		public String link;
		public long size;
		public int fmt;
		public String desc;
	}

	private final Pattern StreamingShareLink = Pattern
			.compile(
					"\\< streamingshare=\"youtube\\.com\" name=\"(.*?)\" dlurl=\"(.*?)\" brurl=\"(.*?)\" convertto=\"(.*?)\" comment=\"(.*?)\" \\>",
					Pattern.CASE_INSENSITIVE);

	static public final Pattern YT_FILENAME_PATTERN = Pattern
			.compile("<meta name=\"title\" content=\"(.*?)\">",
					Pattern.CASE_INSENSITIVE);
	private static final String UNSUPPORTEDRTMP = "itag%2Crtmpe%2";

	HashMap<DestinationFormat, ArrayList<Info>> possibleconverts = null;

	private static final String TEMP_EXT = ".tmp$";

	ArrayList<String> done = new ArrayList<String>();
	private boolean pluginloaded = false;
	private boolean verifyAge = false;

	public static boolean ConvertFile(final DownloadLink downloadlink,
			final DestinationFormat InType, final DestinationFormat OutType) {
		System.out.println("Convert " + downloadlink.getName() + " - "
				+ InType.getText() + " - " + OutType.getText());
		if (InType.equals(OutType)) {
			System.out.println("No Conversion needed, renaming...");
			final File oldone = new File(downloadlink.getFileOutput());
			final File newone = new File(downloadlink.getFileOutput()
					.replaceAll(TbCm.TEMP_EXT, OutType.getExtFirst()));
			downloadlink.setFinalFileName(downloadlink.getName().replaceAll(
					TbCm.TEMP_EXT, OutType.getExtFirst()));
			oldone.renameTo(newone);
			return true;
		}

		downloadlink.getLinkStatus().setStatusText(
				JDL.L("convert.progress.convertingto", "convert to") + " "
						+ OutType.toString());

		switch (InType) {
		case VIDEOFLV:
			// Inputformat FLV
			switch (OutType) {
			case AUDIOMP3:
				System.out.println("Convert FLV to mp3...");
				new FLV(downloadlink.getFileOutput(), true, true);

				// FLV löschen
				if (!new File(downloadlink.getFileOutput()).delete()) {
					new File(downloadlink.getFileOutput()).deleteOnExit();
				}
				// AVI löschen
				if (!new File(downloadlink.getFileOutput().replaceAll(
						TbCm.TEMP_EXT, ".avi")).delete()) {
					new File(downloadlink.getFileOutput().replaceAll(
							TbCm.TEMP_EXT, ".avi")).deleteOnExit();
				}

				return true;
			default:
				System.out.println("Don't know how to convert "
						+ InType.getText() + " to " + OutType.getText());
				downloadlink.getLinkStatus().setErrorMessage(
						JDL.L("convert.progress.unknownintype",
								"Unknown format"));
				return false;
			}
		default:
			System.out.println("Don't know how to convert " + InType.getText()
					+ " to " + OutType.getText());
			downloadlink.getLinkStatus().setErrorMessage(
					JDL.L("convert.progress.unknownintype", "Unknown format"));
			return false;
		}
	}

	/**
	 * Converts the Google Closed Captions subtitles to SRT subtitles. It runs
	 * after the completed download.
	 * 
	 * @param downloadlink
	 *            . The finished link to the Google CC subtitle file.
	 * @return The success of the conversion.
	 */
	public static boolean convertSubtitle(final DownloadLink downloadlink) {
		final File source = new File(downloadlink.getFileOutput());

		BufferedWriter dest;
		try {
			dest = new BufferedWriter(new FileWriter(new File(source
					.getAbsolutePath().replace(".xml", ".srt"))));
		} catch (IOException e1) {
			return false;
		}

		final StringBuilder xml = new StringBuilder();
		int counter = 1;
		final String lineseparator = System.getProperty("line.separator");

		Scanner in = null;
		try {
			in = new Scanner(new FileReader(source));
			while (in.hasNext()) {
				xml.append(in.nextLine() + lineseparator);
			}
		} catch (Exception e) {
			return false;
		} finally {
			in.close();
		}

		String[][] matches = new Regex(xml.toString(),
				"<text start=\"(.*?)\" dur=\"(.*?)\">(.*?)</text>")
				.getMatches();

		try {
			for (String[] match : matches) {
				dest.write(counter++ + lineseparator);

				Double start = Double.valueOf(match[0]);
				Double end = start + Double.valueOf(match[1]);
				dest.write(convertSubtitleTime(start) + " --> "
						+ convertSubtitleTime(end) + lineseparator);

				String text = match[2].trim();
				text = text.replaceAll(lineseparator, " ");
				text = text.replaceAll("&amp;", "&");
				text = text.replaceAll("&quot;", "\"");
				text = text.replaceAll("&#39;", "'");
				dest.write(text + lineseparator + lineseparator);
			}
		} catch (Exception e) {
			return false;
		} finally {
			try {
				dest.close();
			} catch (IOException e) {
			}
		}

		source.delete();

		return true;
	}

	/**
	 * Converts the the time of the Google format to the SRT format.
	 * 
	 * @param time
	 *            . The time from the Google XML.
	 * @return The converted time as String.
	 */
	private static String convertSubtitleTime(Double time) {
		String hour = "00";
		String minute = "00";
		String second = "00";
		String millisecond = "0";

		Integer itime = Integer.valueOf(time.intValue());

		// Hour
		Integer timeHour = Integer.valueOf(itime.intValue() / 3600);
		if (timeHour < 10) {
			hour = "0" + timeHour.toString();
		} else {
			hour = timeHour.toString();
		}

		// Minute
		Integer timeMinute = Integer.valueOf((itime.intValue() % 3600) / 60);
		if (timeMinute < 10) {
			minute = "0" + timeMinute.toString();
		} else {
			minute = timeMinute.toString();
		}

		// Second
		Integer timeSecond = Integer.valueOf(itime.intValue() % 60);
		if (timeSecond < 10) {
			second = "0" + timeSecond.toString();
		} else {
			second = timeSecond.toString();
		}

		// Millisecond
		millisecond = String.valueOf(time - itime).split("\\.")[1];
		if (millisecond.length() == 1)
			millisecond = millisecond + "00";
		if (millisecond.length() == 2)
			millisecond = millisecond + "0";
		if (millisecond.length() > 2)
			millisecond = millisecond.substring(0, 3);

		// Result
		String result = hour + ":" + minute + ":" + second + "," + millisecond;

		return result;
	}

	private void gsProxy(boolean b) {
		SubConfiguration cfg = SubConfiguration.getConfig("youtube.com");
		if (cfg != null && cfg.getBooleanProperty("PROXY_ACTIVE")) {
			String PROXY_ADDRESS = cfg.getStringProperty("PROXY_ADDRESS");
			int PROXY_PORT = cfg.getIntegerProperty("PROXY_PORT");
			if (isEmpty(PROXY_ADDRESS) || PROXY_PORT < 0)
				return;
			PROXY_ADDRESS = new Regex(PROXY_ADDRESS, "^[0-9a-zA-Z]+://")
					.matches() ? PROXY_ADDRESS : "http://" + PROXY_ADDRESS;
			org.appwork.utils.net.httpconnection.HTTPProxy proxy = org.appwork.utils.net.httpconnection.HTTPProxy
					.parseHTTPProxy(PROXY_ADDRESS + ":" + PROXY_PORT);
			if (b && proxy != null && proxy.getHost() != null) {
				br.setProxy(proxy);
				return;
			}
		}
		br.setProxy(br.getThreadProxy());
	}

	private boolean isEmpty(String ip) {
		return ip == null || ip.trim().length() == 0;
	}

	private void addtopos(final DestinationFormat mode, final String link,
			final long size, final String desc, final int fmt) {
		ArrayList<Info> info = this.possibleconverts.get(mode);
		if (info == null) {
			info = new ArrayList<Info>();
			this.possibleconverts.put(mode, info);
		}
		final Info tmp = new Info();
		tmp.link = link;
		tmp.size = size;
		tmp.desc = desc;
		tmp.fmt = fmt;
		info.add(tmp);
	}

	private void addVideosCurrentPage(final ArrayList<DownloadLink> links,
			String playlistID) {
		final String[] videos = this.br
				.getRegex(
						"<a href=\"(/watch\\?v=[a-z\\-_A-Z0-9]+\\&amp;list=[a-z\\-_A-Z0-9]+\\&amp;index=\\d+\\&amp;feature=[a-z\\-_A-Z0-9]+)\"")
				.getColumn(0);
		for (String video : videos) {
			video = Encoding.htmlDecode(video);
			if (done.contains(video))
				continue;
			done.add(video);
			video = new Regex(video, "(/watch\\?v=[a-z\\-_A-Z0-9]+)\\&")
					.getMatch(0);
			if (video == null)
				continue;
			links.add(this.createDownloadlink("http://www.youtube.com" + video));
		}
	}

	public boolean canHandle(final String data) {
		if (PLUGIN_DISABLED.get() == true)
			return false;
		return super.canHandle(data);
	}

	public ArrayList<DownloadLink> decryptIt(final CryptedLink param,
			final ProgressController progress) throws Exception {

		this.possibleconverts = new HashMap<DestinationFormat, ArrayList<Info>>();
		final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
		if (PLUGIN_DISABLED.get() == true)
			return decryptedLinks;
		String parameter = param.toString().replace("watch#!v", "watch?v");
		parameter = parameter.replaceFirst("(verify_age\\?next_url=\\/?)", "");
		parameter = parameter.replaceFirst("(%3Fv%3D)", "?v=");
		parameter = parameter.replaceFirst("(watch\\?.*?v)", "watch?v");
		parameter = parameter.replaceFirst("/embed/", "/watch?v=");
		parameter = parameter.replaceFirst("https", "http");

		this.br.setFollowRedirects(true);
		this.br.setCookiesExclusive(true);
		this.br.clearCookies("youtube.com");
		if (parameter.contains("watch#")) {
			parameter = parameter.replace("watch#", "watch?");
		}
		if (parameter.contains("v/")) {
			String id = new Regex(parameter, "v/([a-z\\-_A-Z0-9]+)")
					.getMatch(0);
			if (id != null)
				parameter = "http://www.youtube.com/watch?v=" + id;
		}
		if (parameter.contains("view_play_list")
				|| parameter.contains("playlist") || parameter.contains("g/c/")
				|| parameter.contains("grid/user/")) {
			if (parameter.contains("g/c/") || parameter.contains("grid/user/")) {
				String id = new Regex(parameter, "g/c/([a-z\\-_A-Z0-9]+)")
						.getMatch(0);
				if (id == null) {
					id = new Regex(parameter, "grid/user/([a-z\\-_A-Z0-9]+)")
							.getMatch(0);
					if (id == null) {
						id = new Regex(parameter,
								"youtube\\.com/playlist\\?list=(.+)")
								.getMatch(0);
					}
				}
				if (id != null)
					parameter = "http://www.youtube.com/view_play_list?p=" + id;
			}
			String playlistID = new Regex(parameter, "\\?list=([a-zA-Z0-9]+)")
					.getMatch(0);
			if (playlistID == null)
				playlistID = new Regex(parameter, "list\\?p=([a-zA-Z0-9]+)")
						.getMatch(0);
			parameter = parameter
					.replaceFirst("playlist\\?", "view_play_list?");
			this.br.getPage(parameter);
			this.addVideosCurrentPage(decryptedLinks, playlistID);
			if (!parameter.contains("page=")) {
				final String[] pages = this.br
						.getRegex(
								"<a href=(\"|')(http://www.youtube.com/view_play_list\\?p=.*?page=\\d+)(\"|')")
						.getColumn(1);
				for (int i = 0; i < pages.length - 1; i++) {
					this.br.getPage(pages[i]);
					this.addVideosCurrentPage(decryptedLinks, playlistID);
				}
			}
		} else {
			boolean prem = false;
			final ArrayList<Account> accounts = AccountController.getInstance()
					.getAllAccounts("youtube.com");
			if (accounts != null && accounts.size() != 0) {
				Iterator<Account> it = accounts.iterator();
				while (it.hasNext()) {
					Account n = it.next();
					if (n.isEnabled() && n.isValid()) {
						prem = this.login(n);
						break;
					}
				}
			}

			try {
				if (this.StreamingShareLink.matcher(parameter).matches()) {
					// StreamingShareLink

					final String[] info = new Regex(parameter,
							this.StreamingShareLink).getMatches()[0];

					for (final String debug : info) {
						logger.info(debug);
					}
					final DownloadLink thislink = this
							.createDownloadlink(info[1]);
					thislink.setProperty("ALLOW_DUPE", true);
					thislink.setBrowserUrl(info[2]);
					thislink.setFinalFileName(info[0]);
					thislink.setProperty("convertto", info[3]);

					decryptedLinks.add(thislink);
					return decryptedLinks;
				}
				verifyAge = false;
				final HashMap<Integer, String[]> LinksFound = this.getLinks(
						parameter, prem, this.br, 0);
				String error = br
						.getRegex(
								"<div id=\"unavailable\\-message\" class=\"\">[\t\n\r ]+<span class=\"yt\\-alert\\-vertical\\-trick\"></span>[\t\n\r ]+<div class=\"yt\\-alert\\-message\">([^<>\"]*?)</div>")
						.getMatch(0);
				if (error == null)
					error = br.getRegex(
							"<div class=\"yt\\-alert\\-message\">(.*?)</div>")
							.getMatch(0);
				if (error == null)
					error = br.getRegex("\\&reason=([^<>\"/]*?)\\&")
							.getMatch(0);
				if (br.containsHTML(UNSUPPORTEDRTMP))
					error = "RTMP video download isn't supported yet!";
				if ((LinksFound == null || LinksFound.isEmpty())
						&& error != null) {
					error = Encoding.urlDecode(error, false);
					logger.info("Video unavailable: " + parameter);
					logger.info("Reason: " + error.trim());
					return decryptedLinks;
				}
				if (LinksFound == null || LinksFound.isEmpty()) {
					if (verifyAge
							|| this.br.getURL().toLowerCase()
									.indexOf("youtube.com/get_video_info?") != -1
							&& !prem) {
						throw new DecrypterException(DecrypterException.ACCOUNT);
					}
					throw new DecrypterException("Video no longer available");
				}

				/* First get the filename */
				String YT_FILENAME = "";
				if (LinksFound.containsKey(-1)) {
					YT_FILENAME = LinksFound.get(-1)[0];
					LinksFound.remove(-1);
				}
				/* prefer videoID als filename? */
				SubConfiguration cfg = SubConfiguration
						.getConfig("youtube.com");
				if (cfg.getBooleanProperty("ISASFILENAME", false)) {
					String id = new Regex(parameter, "v=([a-z\\-_A-Z0-9]+)")
							.getMatch(0);
					if (id != null)
						YT_FILENAME = id.toUpperCase(Locale.ENGLISH);
				}
				final boolean fast = cfg.getBooleanProperty("FAST_CHECK2",
						false);
				final boolean mp3 = cfg.getBooleanProperty("ALLOW_MP3", true);
				final boolean mp4 = cfg.getBooleanProperty("ALLOW_MP4", true);
				final boolean webm = cfg.getBooleanProperty("ALLOW_WEBM", true);
				final boolean flv = cfg.getBooleanProperty("ALLOW_FLV", true);
				final boolean threegp = cfg.getBooleanProperty("ALLOW_3GP",
						true);

				final boolean q240p = cfg
						.getBooleanProperty("ALLOW_240P", true);
				final boolean q360p = cfg
						.getBooleanProperty("ALLOW_360P", true);
				final boolean q480p = cfg
						.getBooleanProperty("ALLOW_480P", true);
				final boolean q720p = cfg
						.getBooleanProperty("ALLOW_720P", true);
				final boolean q1080p = cfg.getBooleanProperty("ALLOW_1080P",
						true);
				final boolean qOriginal = cfg.getBooleanProperty(
						"ALLOW_ORIGINAL", true);
				/* http://en.wikipedia.org/wiki/YouTube */
				final HashMap<Integer, Object[]> ytVideo = new HashMap<Integer, Object[]>() {
					/**
                     * 
                     */
					private static final long serialVersionUID = -3028718522449785181L;

					{
						// **** FLV *****
						if (mp3) {
							this.put(0, new Object[] {
									DestinationFormat.AUDIOMP3, "H.263", "MP3",
									"Mono" });
							this.put(5, new Object[] {
									DestinationFormat.AUDIOMP3, "H.263", "MP3",
									"Stereo" });
							this.put(6, new Object[] {
									DestinationFormat.AUDIOMP3, "H.263", "MP3",
									"Mono" });
						}
						if (flv) {
							if (q240p) {
								this.put(5, new Object[] {
										DestinationFormat.VIDEOFLV, "H.263",
										"MP3", "Stereo", "240p" });
							}
							if (q360p) {
								this.put(34, new Object[] {
										DestinationFormat.VIDEOFLV, "H.264",
										"AAC", "Stereo", "360p" });
							}
							if (q480p) {
								this.put(35, new Object[] {
										DestinationFormat.VIDEOFLV, "H.264",
										"AAC", "Stereo", "480p" });
							}
						}

						// **** 3GP *****
						if (threegp && q240p) {
							this.put(13, new Object[] {
									DestinationFormat.VIDEO3GP, "H.263", "AMR",
									"Mono", "240p" });
							this.put(17, new Object[] {
									DestinationFormat.VIDEO3GP, "H.264", "AAC",
									"Stereo", "240p" });
						}

						// **** MP4 *****
						if (mp4) {
							if (q360p) {
								this.put(18, new Object[] {
										DestinationFormat.VIDEOMP4, "H.264",
										"AAC", "Stereo", "360p" });
							}
							if (q720p) {
								this.put(22, new Object[] {
										DestinationFormat.VIDEOMP4, "H.264",
										"AAC", "Stereo", "720p" });
							}
							if (q1080p) {
								this.put(37, new Object[] {
										DestinationFormat.VIDEOMP4, "H.264",
										"AAC", "Stereo", "1080" });
							}
							if (qOriginal) {
								this.put(38, new Object[] {
										DestinationFormat.VIDEOMP4, "H.264",
										"AAC", "Stereo", "Original" });
							}
						}

						// **** WebM *****
						if (webm) {
							if (q360p) {
								this.put(43, new Object[] {
										DestinationFormat.VIDEOWEBM, "VP8",
										"Vorbis", "Stereo", "360p" });
							}
							if (q720p) {
								this.put(45, new Object[] {
										DestinationFormat.VIDEOWEBM, "VP8",
										"Vorbis", "Stereo", "720p" });
							}
						}
					}
				};

				/* check for wished formats first */
				String dlLink = "";
				String vQuality = "";
				DestinationFormat cMode = null;

				for (final Integer format : LinksFound.keySet()) {
					if (ytVideo.containsKey(format)) {
						cMode = (DestinationFormat) ytVideo.get(format)[0];
						vQuality = "(" + LinksFound.get(format)[1] + "_"
								+ ytVideo.get(format)[1] + "-"
								+ ytVideo.get(format)[2] + ")";
					} else {
						cMode = DestinationFormat.UNKNOWN;
						vQuality = "(" + LinksFound.get(format)[1] + "_"
								+ format + ")";
						/*
						 * we do not want to download unknown formats at the
						 * moment
						 */
						continue;
					}
					dlLink = LinksFound.get(format)[0];
					try {
						if (fast) {
							this.addtopos(cMode, dlLink, 0, vQuality, format);
						} else if (this.br.openGetConnection(dlLink)
								.getResponseCode() == 200) {
							this.addtopos(cMode, dlLink,
									this.br.getHttpConnection()
											.getLongContentLength(), vQuality,
									format);
						}
					} catch (final Throwable e) {
						e.printStackTrace();
					} finally {
						try {
							this.br.getHttpConnection().disconnect();
						} catch (final Throwable e) {
						}
					}
					if (format == 0 || format == 5 || format == 6) {
						try {
							if (fast) {
								this.addtopos(DestinationFormat.AUDIOMP3,
										dlLink, 0, "", format);
							} else if (this.br.openGetConnection(dlLink)
									.getResponseCode() == 200) {
								this.addtopos(DestinationFormat.AUDIOMP3,
										dlLink, this.br.getHttpConnection()
												.getLongContentLength(), "",
										format);
							}
						} catch (final Throwable e) {
							e.printStackTrace();
						} finally {
							try {
								this.br.getHttpConnection().disconnect();
							} catch (final Throwable e) {
							}
						}
					}
				}

				for (final Entry<DestinationFormat, ArrayList<Info>> next : this.possibleconverts
						.entrySet()) {
					final DestinationFormat convertTo = next.getKey();
					// create a package, for each quality.
					final FilePackage filePackage = FilePackage.getInstance();
					filePackage.setProperty("ALLOW_MERGE", true);
					filePackage.setName("YouTube " + convertTo.getText());
					for (final Info info : next.getValue()) {
						final DownloadLink thislink = this
								.createDownloadlink(info.link.replaceFirst(
										"http", "httpJDYoutube"));
						thislink.setProperty("ALLOW_DUPE", true);
						filePackage.add(thislink);
						thislink.setBrowserUrl(parameter);
						thislink.setFinalFileName(YT_FILENAME + info.desc
								+ convertTo.getExtFirst());
						thislink.setProperty("size", info.size);
						String name = null;
						if (convertTo != DestinationFormat.AUDIOMP3) {
							name = YT_FILENAME + info.desc
									+ convertTo.getExtFirst();
							thislink.setProperty("name", name);
						} else {
							/*
							 * because demuxer will fail when mp3 file already
							 * exists
							 */
							name = YT_FILENAME + info.desc + ".tmp";
							thislink.setProperty("name", name);
						}
						thislink.setProperty("convertto", convertTo.name());
						thislink.setProperty("videolink", parameter);
						thislink.setProperty("valid", true);
						thislink.setProperty("fmtNew", info.fmt);
						thislink.setProperty("LINKDUPEID", name);
						decryptedLinks.add(thislink);
					}
				}

				final String VIDEOID = new Regex(parameter,
						"watch\\?v=([\\w_\\-]+)").getMatch(0);

				// Grab Subtitles
				if (cfg.getBooleanProperty("ALLOW_SUBTITLES", true)) {
					br.getPage("http://video.google.com/timedtext?type=list&v="
							+ VIDEOID);

					final FilePackage filePackage = FilePackage.getInstance();
					filePackage.setProperty("ALLOW_MERGE", true);
					filePackage.setName("YouTube subtitles");

					String[][] matches = br
							.getRegex(
									"<track id=\"(.*?)\" name=\"(.*?)\" lang_code=\"(.*?)\" lang_original=\"(.*?)\".*?/>")
							.getMatches();

					for (String[] track : matches) {
						String link = "http://video.google.com/timedtext?type=track&name="
								+ URLEncoder.encode(track[1], "UTF-8")
								+ "&lang="
								+ URLEncoder.encode(track[2], "UTF-8")
								+ "&v="
								+ URLEncoder.encode(VIDEOID, "UTF-8");

						DownloadLink dlink = this.createDownloadlink(link
								.replaceFirst("http", "httpJDYoutube"));
						dlink.setProperty("ALLOW_DUPE", true);
						filePackage.add(dlink);
						dlink.setBrowserUrl(parameter);

						String name = YT_FILENAME + " (" + track[3] + ").xml";
						dlink.setFinalFileName(name);
						dlink.setProperty("name", name);
						dlink.setProperty("subtitle", true);

						decryptedLinks.add(dlink);
					}
				}

				// Grab thumbnails
				FilePackage filePackage = null;
				if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_HQ", false)
						|| cfg.getBooleanProperty("ALLOW_THUMBNAIL_MQ", false)
						|| cfg.getBooleanProperty("ALLOW_THUMBNAIL_DEFAULT",
								false)) {
					filePackage = FilePackage.getInstance();
					filePackage.setProperty("ALLOW_MERGE", true);
					filePackage.setName("YouTube thumbnails");
				}

				if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_HQ", false)) {
					decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME
							+ " (HQ).jpg", "http://img.youtube.com/vi/"
							+ VIDEOID + "/hqdefault.jpg", parameter,
							filePackage));
				}

				if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_MQ", false)) {
					decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME
							+ " (MQ).jpg", "http://img.youtube.com/vi/"
							+ VIDEOID + "/mqdefault.jpg", parameter,
							filePackage));
				}

				if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_DEFAULT", false)) {
					decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME
							+ ".jpg", "http://img.youtube.com/vi/" + VIDEOID
							+ "/default.jpg", parameter, filePackage));
				}
			} catch (final IOException e) {
				this.br.getHttpConnection().disconnect();
				logger.log(java.util.logging.Level.SEVERE,
						"Exception occurred", e);
				return null;
			}
		}

		return decryptedLinks;
	}

	private DownloadLink createThumbnailDownloadLink(String name, String link,
			String browserurl, FilePackage filePackage) {
		DownloadLink dlink = this.createDownloadlink(link.replaceFirst("http",
				"httpJDYoutube"));
		dlink.setProperty("ALLOW_DUPE", true);
		filePackage.add(dlink);
		dlink.setBrowserUrl(browserurl);

		dlink.setFinalFileName(name);
		dlink.setProperty("name", name);
		dlink.setProperty("thumbnail", true);

		return dlink;
	}

	public HashMap<Integer, String[]> getLinks(final String video,
			final boolean prem, Browser br, int retrycount) throws Exception {
		if (retrycount > 2) {
			// do not retry more often than 2 time
			return null;
		}
		if (br == null) {
			br = this.br;
		}

		try {
			gsProxy(true);
		} catch (Throwable e) {
			/* does not exist in 09581 */
		}
		br.setFollowRedirects(true);
		/* this cookie makes html5 available and skip controversy check */
		br.setCookie("youtube.com", "PREF", "f2=40100000");
		br.getHeaders().put("User-Agent", "Wget/1.12");
		br.getPage(video);
		if (br.containsHTML("id=\"unavailable-submessage\" class=\"watch-unavailable-submessage\"")) {
			return null;
		}
		final String VIDEOID = new Regex(video, "watch\\?v=([\\w_\\-]+)")
				.getMatch(0);
		boolean fileNameFound = false;
		String YT_FILENAME = VIDEOID;
		if (br.containsHTML("&title=")) {
			YT_FILENAME = Encoding.htmlDecode(br.getRegex("&title=([^&$]+)")
					.getMatch(0).replaceAll("\\+", " ").trim());
			fileNameFound = true;
		}
		final String url = br.getURL();
		boolean ythack = false;
		if (url != null && !url.equals(video)) {
			/* age verify with activated premium? */
			if (url.toLowerCase(Locale.ENGLISH).indexOf(
					"youtube.com/verify_age?next_url=") != -1) {
				verifyAge = true;
			}
			if (url.toLowerCase(Locale.ENGLISH).indexOf(
					"youtube.com/verify_age?next_url=") != -1
					&& prem) {
				final String session_token = br.getRegex(
						"onLoadFunc.*?gXSRF_token = '(.*?)'").getMatch(0);
				final LinkedHashMap<String, String> p = Request.parseQuery(url);
				final String next = p.get("next_url");
				final Form form = new Form();
				form.setAction(url);
				form.setMethod(MethodType.POST);
				form.put("next_url", "%2F" + next.substring(1));
				form.put("action_confirm", "Confirm+Birth+Date");
				form.put("session_token", Encoding.urlEncode(session_token));
				br.submitForm(form);
				if (br.getCookie("http://www.youtube.com", "is_adult") == null) {
					return null;
				}
			} else if (url.toLowerCase(Locale.ENGLISH).indexOf(
					"youtube.com/index?ytsession=") != -1
					|| url.toLowerCase(Locale.ENGLISH).indexOf(
							"youtube.com/verify_age?next_url=") != -1 && !prem) {
				ythack = true;
				br.getPage("http://www.youtube.com/get_video_info?video_id="
						+ VIDEOID);
				if (br.containsHTML("&title=") && fileNameFound == false) {
					YT_FILENAME = Encoding.htmlDecode(br
							.getRegex("&title=([^&$]+)").getMatch(0)
							.replaceAll("\\+", " ").trim());
					fileNameFound = true;
				}
			} else if (url.toLowerCase(Locale.ENGLISH).indexOf(
					"google.com/accounts/servicelogin?") != -1) {
				// private videos
				return null;
			}
		}
		/* html5_fmt_map */
		if (br.getRegex(TbCm.YT_FILENAME_PATTERN).count() != 0
				&& fileNameFound == false) {
			YT_FILENAME = Encoding.htmlDecode(br
					.getRegex(TbCm.YT_FILENAME_PATTERN).getMatch(0).trim());
			fileNameFound = true;
		}
		final HashMap<Integer, String[]> links = new HashMap<Integer, String[]>();
		String html5_fmt_map = br.getRegex("\"html5_fmt_map\": \\[(.*?)\\]")
				.getMatch(0);

		if (html5_fmt_map != null) {
			String[] html5_hits = new Regex(html5_fmt_map, "\\{(.*?)\\}")
					.getColumn(0);
			if (html5_hits != null) {
				for (String hit : html5_hits) {
					String hitUrl = new Regex(hit, "url\": \"(http:.*?)\"")
							.getMatch(0);
					String hitFmt = new Regex(hit, "itag\": (\\d+)")
							.getMatch(0);
					String hitQ = new Regex(hit, "quality\": \"(.*?)\"")
							.getMatch(0);
					if (hitUrl != null && hitFmt != null && hitQ != null) {
						hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
						links.put(
								Integer.parseInt(hitFmt),
								new String[] {
										Encoding.htmlDecode(Encoding.urlDecode(
												hitUrl, true)), hitQ });
					}
				}
			}
		} else {
			/* new format since ca. 1.8.2011 */
			html5_fmt_map = br.getRegex(
					"\"url_encoded_fmt_stream_map\": \"(.*?)\"").getMatch(0);
			if (html5_fmt_map == null) {
				html5_fmt_map = br.getRegex(
						"url_encoded_fmt_stream_map=(.*?)(&|$)").getMatch(0);
				if (html5_fmt_map != null) {
					html5_fmt_map = html5_fmt_map.replaceAll("%2C", ",");
					if (!html5_fmt_map.contains("url=")) {
						html5_fmt_map = html5_fmt_map.replaceAll("%3D", "=");
						html5_fmt_map = html5_fmt_map.replaceAll("%26", "&");
					}
				}
			}
			if (html5_fmt_map != null && !html5_fmt_map.contains("signature")
					&& !html5_fmt_map.contains("sig")) {
				Thread.sleep(5000);
				br.clearCookies(getHost());
				return getLinks(video, prem, br, retrycount + 1);
			}
			if (html5_fmt_map != null) {
				if (html5_fmt_map.contains(UNSUPPORTEDRTMP)) {
					return null;
				}
				String[] html5_hits = new Regex(html5_fmt_map, "(.*?)(,|$)")
						.getColumn(0);
				if (html5_hits != null) {
					for (String hit : html5_hits) {
						hit = unescape(hit);
						String hitUrl = new Regex(hit, "url=(http.*?)(\\&|$)")
								.getMatch(0);
						String sig = new Regex(hit,
								"url=http.*?(\\&|$)(sig|signature)=(.*?)(\\&|$)")
								.getMatch(2);
						String hitFmt = new Regex(hit, "itag=(\\d+)")
								.getMatch(0);
						String hitQ = new Regex(hit, "quality=(.*?)(\\&|$)")
								.getMatch(0);
						if (hitUrl != null && hitFmt != null && hitQ != null) {
							hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
							if (hitUrl.startsWith("http%253A")) {
								hitUrl = Encoding.htmlDecode(hitUrl);
							}
							String[] inst = null;
							if (hitUrl.contains("sig")) {
								inst = new String[] {
										Encoding.htmlDecode(Encoding.urlDecode(
												hitUrl, true)), hitQ };
							} else {
								inst = new String[] {
										Encoding.htmlDecode(Encoding.urlDecode(
												hitUrl, true)
												+ "&signature="
												+ sig), hitQ };
							}
							links.put(Integer.parseInt(hitFmt), inst);
						}
					}
				}
			}
		}

		/* normal links */
		final HashMap<String, String> fmt_list = new HashMap<String, String>();
		String fmt_list_str = "";
		if (ythack) {
			fmt_list_str = (br.getMatch("&fmt_list=(.+?)&") + ",").replaceAll(
					"%2F", "/").replaceAll("%2C", ",");
		} else {
			fmt_list_str = (br.getMatch("\"fmt_list\":\\s+\"(.+?)\",") + ",")
					.replaceAll("\\\\/", "/");
		}
		final String fmt_list_map[][] = new Regex(fmt_list_str,
				"(\\d+)/(\\d+x\\d+)/\\d+/\\d+/\\d+,").getMatches();
		for (final String[] fmt : fmt_list_map) {
			fmt_list.put(fmt[0], fmt[1]);
		}
		if (links.size() == 0 && ythack) {
			/* try to find fallback links */
			String urls[] = br.getRegex("url%3D(.*?)($|%2C)").getColumn(0);
			int index = 0;
			for (String vurl : urls) {
				String hitUrl = new Regex(vurl, "(.*?)%26").getMatch(0);
				String hitQ = new Regex(vurl, "%26quality%3D(.*?)%")
						.getMatch(0);
				if (hitUrl != null && hitQ != null) {
					hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
					if (fmt_list_map.length >= index) {
						links.put(
								Integer.parseInt(fmt_list_map[index][0]),
								new String[] {
										Encoding.htmlDecode(Encoding.urlDecode(
												hitUrl, false)), hitQ });
						index++;
					}
				}
			}
		}
		for (Integer fmt : links.keySet()) {
			String fmt2 = fmt + "";
			if (fmt_list.containsKey(fmt2)) {
				String Videoq = links.get(fmt)[1];
				final Integer q = Integer.parseInt(fmt_list.get(fmt2)
						.split("x")[1]);
				if (fmt == 40) {
					Videoq = "240p Light";
				} else if (q > 1080) {
					Videoq = "Original";
				} else if (q > 720) {
					Videoq = "1080p";
				} else if (q > 576) {
					Videoq = "720p";
				} else if (q > 360) {
					Videoq = "480p";
				} else if (q > 240) {
					Videoq = "360p";
				} else {
					Videoq = "240p";
				}
				links.get(fmt)[1] = Videoq;
			}
		}
		if (YT_FILENAME != null && links != null && !links.isEmpty()) {
			links.put(-1, new String[] { YT_FILENAME });
		}
		return links;
	}

	private synchronized String unescape(final String s) {
		/* we have to make sure the youtube plugin is loaded */
		if (pluginloaded == false) {
			final PluginForHost plugin = JDUtilities
					.getPluginForHost("youtube.com");
			if (plugin == null)
				throw new IllegalStateException("youtube plugin not found!");
			pluginloaded = true;
		}
		return jd.plugins.hoster.Youtube.unescape(s);
	}

	@Override
	public void init() {
		Browser.setRequestIntervalLimitGlobal(this.getHost(), 100);
	}

	private boolean login(final Account account) throws Exception {
		this.setBrowserExclusive();
		final PluginForHost plugin = JDUtilities
				.getPluginForHost("youtube.com");
		try {
			if (plugin != null) {
				((jd.plugins.hoster.Youtube) plugin).login(account, this.br,
						false, false);
			} else {
				return false;
			}
		} catch (final PluginException e) {
			account.setEnabled(false);
			account.setValid(false);
			return false;
		}
		return true;
	}

}
