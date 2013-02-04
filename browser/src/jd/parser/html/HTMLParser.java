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

package jd.parser.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;

import org.appwork.utils.StringUtils;
import org.appwork.utils.encoding.Hex;
import org.appwork.utils.logging.Log;

public class HTMLParser {

    final static class Httppattern {
        public Pattern p;
        public int     group;

        public Httppattern(final Pattern p, final int group) {
            this.p = p;
            this.group = group;
        }
    }

    final private static Httppattern[] linkAndFormPattern = new Httppattern[] { new Httppattern(Pattern.compile("src.*?=.*?('|\")(.*?)('|\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 2), new Httppattern(Pattern.compile("src.*?=(.*?)[ |>]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 1), new Httppattern(Pattern.compile("(<[ ]?a[^>]*?href=|<[ ]?form[^>]*?action=)('|\")(.*?)('|\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 3), new Httppattern(Pattern.compile("(<[ ]?a[^>]*?href=|<[ ]?form[^>]*?action=)([^'\"][^\\s]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 2), new Httppattern(Pattern.compile("\\[(link|url)\\](.*?)\\[/(link|url)\\]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 2) };
    final private static String        protocolPattern    = "(directhttp://https?://|flashget://|https?viajd://|https?://|ccf://|dlc://|ftp://|jd://|rsdf://|jdlist://|file://)";
    final private static Pattern[]     basePattern        = new Pattern[] { Pattern.compile("href=('|\")(.*?)('|\")", Pattern.CASE_INSENSITIVE), Pattern.compile("src=('|\")(.*?)('|\")", Pattern.CASE_INSENSITIVE), Pattern.compile("(?s)<[ ]?base[^>]*?href=('|\")(.*?)\\1", Pattern.CASE_INSENSITIVE), Pattern.compile("(?s)<[ ]?base[^>]*?(href)=([^'\"][^\\s]*)", Pattern.CASE_INSENSITIVE) };
    final private static Pattern       pat1               = Pattern.compile("(" + HTMLParser.protocolPattern + "|(?<!://)www\\.)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern       protocols          = Pattern.compile("(" + HTMLParser.protocolPattern + ")");
    private static Pattern             mp                 = null;

    static {
        try {
            HTMLParser.mp = Pattern.compile("(" + HTMLParser.protocolPattern + "|www\\.)[^\\r\\t\\n\\v\\f<>'\"]*(((?!\\s" + HTMLParser.protocolPattern + "|\\swww\\.)[^<>'\"]){0,20}([\\?|\\&][^<>'^\\r\\t\\n\\v\\f\"]{1,10}\\=[^<>'^\\r\\t\\n\\v\\f\"]+|\\.([a-zA-Z0-9]{2,4})[^<>'^\\r\\t\\n\\v\\f\"]*))?", Pattern.CASE_INSENSITIVE);
        } catch (final Throwable e) {
            Log.exception(e);
        }
    }

    private static HashSet<String> _getHttpLinksDeepWalker(String data, final String url, HashSet<String> results) {
        if (results == null) {
            results = new HashSet<String>();
        }
        if (data == null || (data = data.trim()).length() == 0) { return results; }
        if ((data.startsWith("directhttp://") || data.startsWith("httpviajd://") || data.startsWith("httpsviajd://")) && results.contains(data)) {
            /* we don't have to further check urls with those prefixes */
            return results;
        }
        final int sizeBefore = results.size();
        /* find reversed */
        String reversedata = new StringBuilder(data).reverse().toString();
        HTMLParser._getHttpLinksFinder(reversedata, url, results);
        reversedata = null;
        /* find base64'ed */
        String urlDecoded = Encoding.urlDecode(data, true);
        String base64Data = Encoding.Base64Decode(urlDecoded);
        if (base64Data.equals(urlDecoded)) {
            /* no base64 content found */
            base64Data = null;
        }
        urlDecoded = null;
        HTMLParser._getHttpLinksFinder(base64Data, url, results);
        base64Data = null;
        /* parse escaped js stuff */
        String unescaped[] = new Regex(data, "unescape\\(('|\")(.*?)('|\")").getColumn(1);
        if (unescaped != null) {
            for (String unescape : unescaped) {
                unescape = Encoding.htmlDecode(unescape);
                HTMLParser._getHttpLinksFinder(unescape, url, results);
            }
            unescaped = null;
        }
        /* find hex'ed */
        String hex = new Regex(data, "(([0-9a-fA-F]{2}| )+)").getMatch(0);
        if (hex != null && hex.length() > 24) {
            try {
                /* remove spaces from hex-coded string */
                hex = hex.replaceAll(" ", "");
                String hexString = Hex.hex2String(hex);
                hex = null;
                HTMLParser._getHttpLinksFinder(hexString, url, results);
                hexString = null;
            } catch (final Throwable e) {
                Log.exception(e);
            }
        }
        if (results.size() == sizeBefore && data.contains("%3A%2F%2")) {
            /* no changes in results size, and data contains urlcoded http://, so lets urldecode it */
            HTMLParser._getHttpLinksFinder(Encoding.urlDecode(data, true), url, results);
        }
        return results;
    }

    private static HashSet<String> _getHttpLinksFinder(String data, String url, HashSet<String> results) {
        final String baseUrl = url;
        if (results == null) {
            results = new HashSet<String>();
        }
        if (data == null || (data = data.trim()).length() == 0) { return results; }
        if (!data.matches(".*<.*>.*")) {
            final int c = new Regex(data, HTMLParser.pat1).count();
            if (c == 0) {
                if (!data.contains("href") && !data.contains("src=")) {
                    /* no href inside */
                    return results;
                }
            } else if (c == 1 && data.length() < 100 && data.matches("^\"?(" + HTMLParser.protocolPattern + "://|www\\.).*")) {
                if (data.startsWith("file://")) {
                    results.add(new String(data.replaceAll("\\s", "%20")));
                } else {
                    final String link = data.replaceFirst("h.{2,3}://", "http://").replaceFirst("^www\\.", "http://www.").replaceAll("[<>\"]*", "");
                    if (!link.matches(".*\\s.*")) {
                        results.add(new String(HTMLParser.correctURL(link)));
                    }
                }
            }
        }

        url = url == null ? "" : url;
        Matcher m;
        String link;
        final String basename = "";
        final String host = "";

        for (final Pattern element : HTMLParser.basePattern) {
            m = element.matcher(data);
            if (m.find()) {
                url = m.group(2);
                break;
            }
        }
        /* get protocol of the url */
        String pro = HTMLParser.getProtocol(url);
        if (baseUrl != null && url != null && (url.startsWith("./") || pro == null)) {
            /* combine baseURL and href url */
            if (pro == null) {
                final String base = new Regex(baseUrl, "(.*?\\..*?(/|$))").getMatch(0);
                if (url.startsWith("/") /* || url.startsWith("#") */) {
                    /* absolute from root url or anchor from root */
                    if (base != null) {
                        url = HTMLParser.mergeUrl(base, url);
                    } else {
                        url = HTMLParser.mergeUrl(baseUrl, url);
                    }
                } else /* if (url.startsWith("./")) */{
                    /* relative url */
                    url = HTMLParser.mergeUrl(baseUrl, url);
                }
            }
            pro = HTMLParser.getProtocol(url);
        }

        if (url != null && url.trim().length() > 0) {
            if (pro != null) {
                results.add(new String(HTMLParser.correctURL(url)));
            }
        } else {
            url = "";
        }

        for (final Httppattern element : HTMLParser.linkAndFormPattern) {
            m = element.p.matcher(data);
            while (m.find()) {
                link = m.group(element.group);
                link = link.replaceAll("h.{2,3}://", "http://");
                if (!(link.length() > 3 && link.matches("^" + HTMLParser.protocolPattern + "://.*")) && link.length() > 0) {
                    if (link.length() > 2 && link.startsWith("www")) {
                        link = pro + "://" + link;
                    }
                    if (link.charAt(0) == '/') {
                        link = host + link;
                    } else if (link.charAt(0) == '#') {
                        link = url + link;
                    } else {
                        link = basename + link;
                    }
                }
                link = link.trim();
                results.add(new String(HTMLParser.correctURL(link)));
            }
        }
        if (HTMLParser.mp != null) {
            /* replace ' with %27 */
            data = data.replaceAll("'", "%27");
            m = HTMLParser.mp.matcher(data);
            while (m.find()) {
                link = m.group(0);
                link = link.trim();
                final Matcher mlinks = HTMLParser.protocols.matcher(link);
                int start = -1;
                /*
                 * special handling if we have multiple links without newline separation
                 */
                while (mlinks.find()) {
                    if (start == -1) {
                        start = mlinks.start();
                    } else {
                        results.add(new String(HTMLParser.correctURL(link.substring(start, mlinks.start()))));
                        start = mlinks.start();
                    }
                }
                if (start != -1) {
                    results.add(new String(HTMLParser.correctURL(link.substring(start))));
                }
                link = link.replaceAll("^h.{2,3}://", "http://");
                link = link.replaceFirst("^www\\.", "http://www\\.");
                results.add(new String(HTMLParser.correctURL(link)));

            }
        }
        return results;
    }

    private static HashSet<String> _getHttpLinksWalker(String data, final String url, HashSet<String> results) {
        // System.out.println("Call: "+data.length());
        if (results == null) {
            results = new HashSet<String>();
        }
        if (data == null || (data = data.trim()).length() == 0) { return results; }
        /* filtering tags, recursion command me ;) */
        while (true) {
            final String nexttag = new Regex(data, "<(.*?)>").setMemoryOptimized(false).getMatch(0);
            if (nexttag == null || nexttag.length() == 0) {
                /* no further tag found, lets continue */
                break;
            } else {
                /* lets check if tag contains links */
                HTMLParser._getHttpLinksWalker(nexttag, url, results);
                int tagClose = data.indexOf('<' + nexttag);
                if (tagClose >= 0) {
                    tagClose = tagClose + nexttag.length() + 1;
                }
                if (tagClose >= 0 && data.length() >= tagClose + 1) {
                    final int tagOpen = data.indexOf('<' + nexttag);
                    if (tagOpen > 0) {
                        /*
                         * there might be some data left before the tag, do not remove that data
                         */
                        String dataLeft = data.substring(0, tagOpen);
                        String dataLeft2 = data.substring(tagClose + 1);
                        data = null;
                        StringBuilder sb = new StringBuilder(dataLeft.length() + dataLeft2.length() + 10);
                        if (dataLeft.contains(">")) {
                            sb.append("<");
                            sb.append(dataLeft);
                        } else {
                            sb.append("<");
                            sb.append(dataLeft);
                            sb.append(">");
                        }
                        dataLeft = null;
                        sb.append(" ");
                        sb.append(dataLeft2);
                        dataLeft2 = null;
                        data = sb.toString();
                        sb = null;
                    } else {
                        /* remove tag at begin of data */
                        data = data.substring(tagClose + 1);
                        if (data.length() == 0) { return results; }
                    }
                    // System.out.println("SubCall: "+data.length());
                } else {
                    /* remove tag at begin of data */
                    data = data.substring(tagClose + 1);
                    if (data.length() == 0) { return results; }
                }
            }
        }
        /* find normal */
        if (!data.contains("://") && !data.contains("%3A%2F%2") || data.length() < 10) {
            /* data must contain at least the protocol separator */
            /* a://b.c/d == minimum 10 length */
            if (!data.contains("href") && !data.contains("unescape") && !data.contains("src=")) {
                /* maybe easy encrypted website or a href */
                return results;
            }
        }
        HTMLParser._getHttpLinksFinder(data, url, results);
        HTMLParser._getHttpLinksDeepWalker(data, url, results);
        /* cut of ?xy= parts if needed */
        String newdata = new Regex(data, "://[^\r\n]*?/[^\r\n]+\\?.[^\r\n]*?=(.*?)($|\r|\n)").setMemoryOptimized(false).getMatch(0);
        HTMLParser._getHttpLinksDeepWalker(newdata, url, results);
        /* use of ?xy parts if available */
        newdata = new Regex(data, "://[^\r\n]*?/[^\r\n]*?\\?(.*?)($|\r|\n)").setMemoryOptimized(false).getMatch(0);
        HTMLParser._getHttpLinksDeepWalker(newdata, url, results);
        return results;
    }

    private static String correctURL(final String input) {
        /* spaces must be %20 encoded */
        return input.replaceAll(" ", "%20");
    }

    /**
     * Diese Methode sucht nach passwÃ¶rtern in einem Datensatz
     * 
     * @param data
     * @return
     */
    public static ArrayList<String> findPasswords(String data) {
        if (data == null) { return new ArrayList<String>(); }
        final ArrayList<String> ret = new ArrayList<String>();
        data = data.replaceAll("(?s)<!-- .*? -->", "").replaceAll("(?s)<script .*?>.*?</script>", "").replaceAll("(?s)<.*?>", "").replaceAll("Spoiler:", "").replaceAll("(no.{0,2}|kein.{0,8}|ohne.{0,8}|nicht.{0,8})(pw|passwort|password|pass)", "").replaceAll("(pw|passwort|password|pass).{0,12}(nicht|falsch|wrong)", "");

        Pattern pattern = Pattern.compile("(пароль|пасс|pa?s?w|passwort|password|passw?)[\\s][\\s]*?[\"']([[^\\:\"'\\s]][^\"'\\s]*)[\"']?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            final String pass = matcher.group(2);
            if (pass != null && pass.length() > 2 && !pass.matches(".*(rar|zip|jpg|gif|png|html|php|avi|mpg)$") && !ret.contains(pass)) {
                ret.add(pass);
            }
        }
        pattern = Pattern.compile("(пароль|пасс|pa?s?w|passwort|password|passw?)[\\s][\\s]*?([[^\\:\"'\\s]][^\"'\\s]*)[\\s]?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(data);
        while (matcher.find()) {
            final String pass = matcher.group(2);
            if (pass != null && pass.length() > 4 && !pass.matches(".*(rar|zip|jpg|gif|png|html|php|avi|mpg)$") && !ret.contains(pass)) {
                ret.add(pass);
            }
        }
        pattern = Pattern.compile("(пароль|пасс|pa?s?w|passwort|password|passw?)[\\s]?(\\:|=)[\\s]*?[\"']([^\"']+)[\"']?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(data);
        while (matcher.find()) {
            final String pass = matcher.group(2);
            if (pass != null && pass.length() > 2 && !pass.matches(".*(rar|zip|jpg|gif|png|html|php|avi|mpg)$") && !ret.contains(pass)) {
                ret.add(pass);
            }
        }
        pattern = Pattern.compile("(пароль|пасс|pa?s?w|passwort|password|passw?)[\\s]?(\\:|=[\\s]*?)([^\"'\\s]+)[\\s]?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(data);
        while (matcher.find()) {
            final String pass = matcher.group(3);
            if (pass != null && pass.length() > 2 && !pass.matches(".*(rar|zip|jpg|gif|png|html|php|avi|mpg)$") && !ret.contains(pass)) {
                ret.add(pass);
            }
        }
        return ret;
    }

    /**
     * Diese Methode sucht die vordefinierten input type="hidden" und formatiert sie zu einem poststring z.b. wÃ¼rde bei:
     * 
     * <input type="hidden" name="f" value="f50b0f" /> <input type="hidden" name="h" value="390b4be0182b85b0" /> <input type="hidden"
     * name="b" value="9" />
     * 
     * f=f50b0f&h=390b4be0182b85b0&b=9 ausgegeben werden
     * 
     * @param data
     *            Der zu durchsuchende Text
     * 
     * @return ein String, der als POST Parameter genutzt werden kann und alle Parameter des Formulars enthÃ¤lt
     */
    public static String getFormInputHidden(final String data) {
        return HTMLParser.joinMap(HTMLParser.getInputHiddenFields(data), "=", "&");
    }

    /* do not use in 09581 stable */
    public static String[] getHttpLinks(final String data) {
        return HTMLParser.getHttpLinks(data, null);
    }

    public static String[] getHttpLinks(final String data, final String url) {
        HashSet<String> links = HTMLParser.getHttpLinksIntern(data, url);
        if (links == null || links.size() == 0) { return new String[0]; }
        /*
         * in case we have valid and invalid (...) urls for the same link, we only use the valid one
         */
        final HashSet<String> tmplinks = new HashSet<String>(links.size());
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
                tmplinks.add(found);
            } else {
                tmplinks.add(link);
            }
        }
        links = null;
        return tmplinks.toArray(new String[tmplinks.size()]);
    }

    /*
     * return tmplinks.toArray(new String[tmplinks.size()]); }
     * 
     * /* parses data for available links and returns a string array which does not contain any duplicates
     */
    public static HashSet<String> getHttpLinksIntern(String data, final String url) {
        data = data.trim();
        if (data.length() == 0) { return null; }
        /*
         * replace urlencoded br tags, so we can find all links separated by those
         */
        /* find a better solution for this html codings */
        data = data.replaceAll("&lt;", ">");
        data = data.replaceAll("&gt;", "<");
        data = data.replaceAll("&amp;", "&");
        data = data.replaceAll("&quot;", "\"");
        /* place all replaces here that separates links */
        /* replace <br> tags with space so we we can separate the links */
        /* we replace the complete br tag with a newline */
        data = data.replaceAll("<br.*?>", "\r\n");
        /* remove word breaks */
        data = data.replaceAll("<wbr>", "");
        /* remove HTML Tags */
        data = data.replaceAll("</?(i|b|u|s)>", "");
        /*
         * remove all span because they can break url parsing (eg when google-code-prettify is used)
         */
        // not needed here because our filter below will take care of them
        // data = data.replaceAll("(?i)<span.*?>", "");
        // data = data.replaceAll("(?i)</span.*?>", "");
        data = data.replaceAll("(?s)\\[(url|link)\\](.*?)\\[/(url|link)\\]", "<$2>");
        final HashSet<String> results = new HashSet<String>() {

            @Override
            public boolean add(String e) {
                if (e != null) {
                    int index = e.indexOf("\r");
                    if (index < 0) {
                        index = e.indexOf("\n");
                    }
                    if (index < 0) {
                        index = e.indexOf("\t");
                    }
                    if (index > 0) {
                        e = e.substring(0, index);
                    }
                    if (e.contains("\"")) {
                        e = e.replaceAll("\"", "");
                    }
                }
                return super.add(e);
            }

        };
        HTMLParser._getHttpLinksWalker(data, url, results);
        /* we don't want baseurl to be included in result set */
        results.remove(url);
        if (results.isEmpty()) { return null; }
        return results;
    }

    /**
     * Gibt alle Hidden fields als hasMap zurÃ¼ck
     * 
     * @param data
     * @return hasmap mit allen hidden fields variablen
     */
    public static HashMap<String, String> getInputHiddenFields(final String data) {
        final Pattern intput1 = Pattern.compile("(?s)<[ ]?input([^>]*?type=['\"]?hidden['\"]?[^>]*?)[/]?>", Pattern.CASE_INSENSITIVE);
        final Pattern intput2 = Pattern.compile("name=['\"]([^'\"]*?)['\"]", Pattern.CASE_INSENSITIVE);
        final Pattern intput3 = Pattern.compile("value=['\"]([^'\"]*?)['\"]", Pattern.CASE_INSENSITIVE);
        final Pattern intput4 = Pattern.compile("name=([^\\s]*)", Pattern.CASE_INSENSITIVE);
        final Pattern intput5 = Pattern.compile("value=([^\\s]*)", Pattern.CASE_INSENSITIVE);
        final Matcher matcher1 = intput1.matcher(data);
        Matcher matcher2;
        Matcher matcher3;
        Matcher matcher4;
        Matcher matcher5;
        final HashMap<String, String> ret = new HashMap<String, String>();
        boolean iscompl;
        while (matcher1.find()) {
            matcher2 = intput2.matcher(matcher1.group(1) + " ");
            matcher3 = intput3.matcher(matcher1.group(1) + " ");
            matcher4 = intput4.matcher(matcher1.group(1) + " ");
            matcher5 = intput5.matcher(matcher1.group(1) + " ");
            iscompl = false;
            String key, value;
            key = value = null;
            if (matcher2.find()) {
                iscompl = true;
                key = matcher2.group(1);
            } else if (matcher4.find()) {
                iscompl = true;
                key = matcher4.group(1);
            }
            if (matcher3.find() && iscompl) {
                value = matcher3.group(1);
            } else if (matcher5.find() && iscompl) {
                value = matcher5.group(1);
            } else {
                iscompl = false;
            }
            ret.put(key, value);
        }
        return ret;
    }

    private static String getProtocol(final String url) {
        if (url == null) { return null; }
        String pro = null;
        if (url.startsWith("directhttp://")) {
            pro = "directhttp";
        } else if (url.startsWith("https://")) {
            pro = "https";
        } else if (url.startsWith("jd://")) {
            pro = "jd";
        } else if (url.startsWith("rsdf://")) {
            pro = "rsdf";
        } else if (url.startsWith("ccf://")) {
            pro = "ccf";
        } else if (url.startsWith("dlc://")) {
            pro = "dlc";
        } else if (url.startsWith("jdlist://")) {
            pro = "jdlist";
        } else if (url.startsWith("ftp://")) {
            pro = "ftp";
        } else if (url.startsWith("flashget://")) {
            pro = "flashget";
        } else if (url.startsWith("http://")) {
            pro = "http";
        }
        return pro;
    }

    /**
     * @author olimex FÃ¼gt Map als String mit Trennzeichen zusammen TODO: auslagern
     * @param map
     *            Map
     * @param delPair
     *            Trennzeichen zwischen Key und Value
     * @param delMap
     *            Trennzeichen zwischen Map-EintrÃ¤gen
     * @return Key-value pairs
     */
    public static String joinMap(final Map<String, String> map, final String delPair, final String delMap) {
        final StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                buffer.append(delMap);
            }
            buffer.append(entry.getKey());
            buffer.append(delPair);
            buffer.append(entry.getValue());
        }
        return buffer.toString();
    }

    private static String mergeUrl(final String base, final String path) {
        final StringBuilder sb = new StringBuilder();
        int end = 0;
        if (StringUtils.isEmpty(base) == false) {
            end = base.length();
            while (end >= 0 && base.charAt(end - 1) == '/') {
                end--;
            }

            if (end > 0) {
                sb.append(base, 0, end);
            }
        } else {
            sb.append(base);
        }
        sb.append("/");
        if (StringUtils.isEmpty(path) == false) {
            end = 0;
            if (path.startsWith("./")) {
                end = 2;
            }
            while (path.length() <= end && path.charAt(end) == '/') {
                end++;
            }
            sb.append(path, end, path.length());
        }

        return Browser.correctURL(sb.toString());
    }

}
