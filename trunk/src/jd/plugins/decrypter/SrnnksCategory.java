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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.nutils.DiffMatchPatch;
import jd.nutils.encoding.Encoding;
import jd.parser.html.HTMLParser;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "serienjunkies.org", "serienjunkies.org" }, urls = { "http://[\\w\\.]*?serienjunkies\\.org/\\?(cat|p)=\\d+", "http://[\\w\\.]{0,4}serienjunkies\\.org/(?!safe).*?/.+" }, flags = { 0, 0 })
public class SrnnksCategory extends PluginForDecrypt {

    public SrnnksCategory(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink parameter, final ProgressController progress) throws Exception {
        Browser.setRequestIntervalLimitGlobal("serienjunkies.org", 400);
        Browser.setRequestIntervalLimitGlobal("download.serienjunkies.org", 400);
        if (!UserIO.isOK(UserIO.getInstance().requestConfirmDialog(UserIO.DONT_SHOW_AGAIN, JDL.L("plugins.decrypter.srnkscategory.AddCategory", "Kategorie Decrypter!\r\nWillst du wirklich eine ganze Kategorie hinzufügen?")))) { return new ArrayList<DownloadLink>(); }
        br.setFollowRedirects(true);
        br.getPage(parameter.getCryptedUrl());
        if (br.containsHTML("<FRAME SRC")) {
            // progress.setStatusText("Lade Downloadseitenframe");
            br.getPage(br.getRegex("<FRAME SRC=\"(.*?)\"").getMatch(0));
        }
        if (br.containsHTML("Error 503")) {
            UserIO.getInstance().requestMessageDialog(JDL.L("plugins.decrypter.srnks.overloaded", "Serienjunkies ist überlastet. Bitte versuche es später erneut."));
            return new ArrayList<DownloadLink>();
        }

        IdNamePair selectedCategory = letTheUserSelectCategory();
        if (selectedCategory == null) return new ArrayList<DownloadLink>();

        String page = br.getPage("http://serienjunkies.org/" + selectedCategory.getId() + "/");

        Format selectedFormat = letTheUserSelectFormat(page);
        if (selectedFormat == null) return new ArrayList<DownloadLink>();

        List<String> links = letTheUserSelectMirrors(selectedFormat);
        if (links == null) return new ArrayList<DownloadLink>();

        return confirmSelectedLinks(links);

    }

    private ArrayList<DownloadLink> confirmSelectedLinks(List<String> links) {
        final ArrayList<DownloadLink> ret = new ArrayList<DownloadLink>();

        String linksAsSingleString = convertListOfLinksToString(links);
        String linklist = UserIO.getInstance().requestInputDialog(UserIO.STYLE_LARGE | UserIO.NO_COUNTDOWN, JDL.L("plugins.decrypter.srnkscategory.RemoveUnwantedLinks", "Entferne ungewollte Links"), linksAsSingleString);
        if (linklist == null) return new ArrayList<DownloadLink>();

        String[] urls = HTMLParser.getHttpLinks(linklist, null);
        for (String url : urls) {
            ret.add(this.createDownloadlink(url));
        }
        if (UserIO.isOK(UserIO.getInstance().requestConfirmDialog(0, String.format(JDL.L("plugins.decrypter.srnkscategory.DecryptLinks", "Jetzt %s Links Decrypten? Für Jeden Link muss ein Captcha eingegeben werden!"), ret.size())))) {
            return ret;
        } else {
            return new ArrayList<DownloadLink>();
        }
    }

    private List<String> letTheUserSelectMirrors(Format selectedFormat) {
        String[] mirrors = selectedFormat.getMirrors();
        int[] selectedMirrorsIndices = null;
        try {
            selectedMirrorsIndices = UserIO.getInstance().requestMultiSelectionDialog(0, JDL.L("plugins.decrypter.srnkscategory.SelectHostersHeadline", "Bitte Mirrors auswählen"), JDL.L("plugins.decrypter.srnkscategory.SelectHosters", "Bitte die gewünschten Anbieter auswählen."), mirrors, null, null, null, null);
        } catch (Throwable e) {
            /* this function DOES NOT exist in 09581 stable */
            // TODO Get rid of this catch section once MultiSelectionDialog
            // makes its way into stable
            int selectedMirror = UserIO.getInstance().requestComboDialog(0, JDL.L("plugins.decrypter.srnkscategory.SelectHostersHeadline", "Bitte Mirrors auswählen"), JDL.L("plugins.decrypter.srnkscategory.SelectHosters", "Bitte die gewünschten Anbieter auswählen."), mirrors, 0, null, null, null, null);
            if (selectedMirror < 0) return null;
            selectedMirrorsIndices = new int[] { selectedMirror };
        }
        if (selectedMirrorsIndices == null) return null;

        List<String> links = selectedFormat.getLinks(selectedMirrorsIndices);
        return links;
    }

    private Format letTheUserSelectFormat(String page) {
        Format selectedFormat = null;
        Format[] formats = parseFormats(page);
        int res;
        if (formats.length > 1) {
            res = UserIO.getInstance().requestComboDialog(UserIO.NO_COUNTDOWN, JDL.L("plugins.decrypter.srnkscategory.SelectFormatHeadline", "Bitte Format auswählen"), JDL.L("plugins.decrypter.srnkscategory.SelectFormat", "Bitte das gewünsche Format auswählen."), formats, 0, null, null, null, null);
            if (res < 0) {
                return null;
            } else {
                selectedFormat = formats[res];
            }
        } else if (formats.length == 1) {
            selectedFormat = formats[0];
        }

        if (selectedFormat == null) return null;
        return selectedFormat;
    }

    private IdNamePair letTheUserSelectCategory() {
        IdNamePair[] categories = parseCategories();
        if (categories.length == 0) return null;
        int res = UserIO.getInstance().requestComboDialog(UserIO.NO_COUNTDOWN, JDL.L("plugins.decrypter.srnkscategory.SelectSeasonHeadline", "Bitte Stabbel auswählen"), JDL.L("plugins.decrypter.srnkscategory.SelectSeason", "Bitte die gewünschte Staffel auswählen"), categories, 0, null, null, null, null);
        if (res < 0) return null;

        IdNamePair selectedCategory = categories[res];
        return selectedCategory;
    }

    private String convertListOfLinksToString(List<String> links) {
        StringBuilder sb = new StringBuilder();
        for (String url : links) {
            sb.append(url);
            sb.append("\r\n");
        }
        return sb.toString();
    }

    private IdNamePair[] parseCategories() {
        String[] ids = br.getRegex("\\&nbsp\\;<a href=\"http://serienjunkies.org/(.*?)/\">(.*?)</a><br").getColumn(0);

        String[] names = br.getRegex("\\&nbsp\\;<a href=\"http://serienjunkies.org/(.*?)/\">(.*?)</a><br").getColumn(1);

        if (ids.length != names.length) throw new IllegalStateException("Found " + ids.length + " ids and " + names.length + " names");

        IdNamePair[] idNames = new IdNamePair[names.length];
        for (int i = 0; i < names.length; i++) {
            idNames[i] = new IdNamePair(ids[i], names[i]);
        }

        // May ignore Season/Staffel Difference when sorting
        Arrays.sort(idNames);
        return idNames;
    }

    private static final Pattern MIRROR_PATTERN = Pattern.compile(".*href=\"([^\"]*)\".*\">(part.*?|hier)</a> \\| ([^< ]+).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // FIXME is the old format still used anywhere?
    // /* old format */
    // urls = br.getRegex("Download:</strong>(.*?)\\| " + mirrors[res])
    // .getColumn(0);
    // for (String url : urls) {
    // String matches[] = new Regex(url, "<a href=\"([^<]*?)\"")
    // .getColumn(0);
    // for (String match : matches) {
    // sb.append(match);
    // sb.append("\r\n");
    // }
    // }

    private Format[] parseFormats(String page) {
        List<Format> result = new ArrayList<Format>();

        String[] lines = page.split("\n");

        Format currentFormat = new Format("");
        String normalName = "";
        for (String line : lines) {
            if (line.contains(Format.DURATION_KEY) || line.contains(Format.FORMAT_KEY) || line.contains(Format.LANGUAGE_KEY)) {
                if (!currentFormat.isEmpty()) {
                    result.add(currentFormat);
                }
                String description = removeHTMLTags(line);
                currentFormat = new Format(description);
            } else if (line.contains("Download:")) {
                String mirror = null;
                String link = null;
                Matcher matcher = MIRROR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    link = matcher.group(1);
                    mirror = matcher.group(3);
                }
                currentFormat.add(link, mirror, normalName);
            } else if (line.contains("<strong>")) {
                normalName = removeHTMLTags(line);
            }
        }
        if (!currentFormat.isEmpty()) {
            result.add(currentFormat);
        }
        return (Format[]) result.toArray(new Format[result.size()]);
    }

    private static String removeHTMLTags(String line) {
        return line.replace("<strong>", "").replace("</strong>", "").replace("<p>", "").replace("</p>", "").replace("<br />", "");
    }

    private static class IdNamePair implements Comparable<IdNamePair> {
        private static final Collator collator = Collator.getInstance(Locale.GERMAN);

        private final String          id;
        private final String          name;

        public IdNamePair(String id, String name) {
            this.id = id;
            this.name = Encoding.htmlDecode(name).replace("-", " ");
        }

        public String getId() {
            return id;
        }

        public int compareTo(IdNamePair o) {
            return collator.compare(name, o.name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class Link {

        private final String url;

        private final String mirror;

        private final String name;

        public Link(String url, String mirror, String name) {
            this.url = url;
            this.mirror = mirror;
            this.name = Encoding.htmlDecode(name);
        }

        public String getMirror() {
            return mirror;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return name + " (" + mirror + ")";
        }

    }

    private static class Format {

        private static final int    MIN_SUFFIX_LENGTH = 2;

        private static final String UPLOADER_KEY      = "Uploader";

        private static final String LANGUAGE_KEY      = "Sprache";

        private static final String DURATION_KEY      = "Dauer";

        private static final String FORMAT_KEY        = "Format";

        private final Properties    descriptions      = new Properties();

        private final Set<String>   mirrors           = new HashSet<String>();
        private final Set<String>   names             = new HashSet<String>();

        private final List<Link>    links             = new ArrayList<Link>();

        public Format(String description) {
            parseDescription(Encoding.htmlDecode(description));
        }

        private void parseDescription(String description) {
            String[] split = description.split("\\|");
            for (String s : split) {
                s = s.trim();
                int index = s.indexOf(":");
                if (index != -1) {
                    String key = s.substring(0, index).trim();
                    String value = s.substring(index + 1).trim();
                    descriptions.put(key, value);
                }
            }
        }

        public void add(String url, String mirror, String name) {
            mirrors.add(mirror);
            names.add(name.replace(".", " "));
            links.add(new Link(url, mirror, name));
        }

        public boolean isEmpty() {
            return links.isEmpty();
        }

        /**
         * Gets all the links for all of given mirrors
         */
        public List<String> getLinks(Set<String> mirrors) {
            List<String> result = new ArrayList<String>();

            for (Link link : links) {
                if (mirrors.contains(link.getMirror())) {
                    result.add(link.getUrl());
                }
            }
            return result;
        }

        private List<String> getLinks(int[] selectedMirrorsIndices) {
            Set<String> selectedMirrors = this.fetchMirrorsByTheirIndices(selectedMirrorsIndices);
            List<String> links = this.getLinks(selectedMirrors);
            return links;
        }

        public String[] getMirrors() {
            return mirrors.toArray(new String[mirrors.size()]);
        }

        protected Set<String> fetchMirrorsByTheirIndices(int[] mirrorIndices) {
            Set<String> ret = new HashSet<String>(mirrorIndices.length);
            String[] mirrorsAsArray = this.getMirrors();
            for (int index : mirrorIndices) {
                ret.add(mirrorsAsArray[index]);
            }
            return ret;
        }

        private Set<String> findCommonSuffix(Set<String> values) {
            Set<String> suffixes = new HashSet<String>();
            DiffMatchPatch matcher = new DiffMatchPatch();
            for (String val1 : values) {
                for (String val2 : values) {
                    if (!val1.equals(val2)) {
                        int length = matcher.diffCommonSuffix(val1, val2);
                        if (length > 0) {
                            String match = val1.substring(val1.length() - length).trim();
                            if (match.length() > MIN_SUFFIX_LENGTH) {
                                suffixes.add(match);
                            }
                        }
                    }
                }
            }
            if (suffixes.size() > 1) {
                suffixes = findCommonSuffix(suffixes);
            }
            return suffixes;
        }

        private void appendIfNotEmpty(StringBuilder b, String key) {
            Object value = descriptions.get(key);
            appendKeyValue(b, key, value);
        }

        private void appendKeyValue(StringBuilder b, String key, Object value) {
            if (value != null) {
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(key).append("=").append(value);
            }
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            appendIfNotEmpty(b, FORMAT_KEY);
            Set<String> substr = findCommonSuffix(names);
            if (!substr.isEmpty()) {
                appendKeyValue(b, "ID", substr.iterator().next());
            }
            appendIfNotEmpty(b, DURATION_KEY);
            appendIfNotEmpty(b, LANGUAGE_KEY);
            appendIfNotEmpty(b, UPLOADER_KEY);

            return b.toString();
        }

    }

}
