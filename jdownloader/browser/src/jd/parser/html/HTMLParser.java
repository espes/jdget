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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;

import org.appwork.utils.encoding.Hex;
import org.appwork.utils.logging.Log;

public class HTMLParser {

    private static class HtmlParserCharSequence implements CharSequence {
        private final static boolean offsetValueCountAvailable;
        private static Field         offsetField;
        private static Field         valueField;
        private static Field         countField;
        static {
            boolean ret = false;
            try {
                HtmlParserCharSequence.offsetField = String.class.getDeclaredField("offset");
                HtmlParserCharSequence.countField = String.class.getDeclaredField("count");
                HtmlParserCharSequence.valueField = String.class.getDeclaredField("value");
                HtmlParserCharSequence.offsetField.setAccessible(true);
                HtmlParserCharSequence.valueField.setAccessible(true);
                HtmlParserCharSequence.countField.setAccessible(true);
                ret = true;
            } catch (final Throwable e) {
            }
            offsetValueCountAvailable = ret;
        }

        char[]                       chars;
        int                          start;
        int                          end;

        private HtmlParserCharSequence(final HtmlParserCharSequence source, final int start, final int end) {
            this.chars = source.chars;
            this.start = start;
            this.end = end;
        }

        public HtmlParserCharSequence(final String input) {
            if (HtmlParserCharSequence.offsetValueCountAvailable) {
                /* this avoids copy of charArray from String to this HtmlParserCharSequence */
                try {
                    if (HtmlParserCharSequence.offsetField != null && HtmlParserCharSequence.valueField != null && HtmlParserCharSequence.countField != null) {
                        this.chars = (char[]) HtmlParserCharSequence.valueField.get(input);
                        this.start = HtmlParserCharSequence.offsetField.getInt(input);
                        this.end = this.start + HtmlParserCharSequence.countField.getInt(input);
                        return;
                    }
                } catch (final Throwable e) {
                }
            }
            /* seems we are java >=1.7_6 */
            /* http://java-performance.info/changes-to-string-java-1-7-0_06/ */
            this.chars = new char[input.length()];
            input.getChars(0, input.length(), this.chars, 0);
            this.start = 0;
            this.end = input.length();
        }

        @Override
        public char charAt(int index) {
            index = index + this.getStartIndex();
            if (index > this.getStopIndex()) {
                throw new IndexOutOfBoundsException("index " + index + " > end " + this.getStopIndex());
            }
            return this.chars[index];
        }

        public boolean contains(final CharSequence s) {
            return this.indexOf(s.toString()) > -1;
        }

        @Override
        public boolean equals(final Object anObject) {
            if (this == anObject) {
                return true;
            }
            if (anObject instanceof CharSequence) {
                final CharSequence anotherString = (CharSequence) anObject;
                int n = this.length();
                if (n == anotherString.length()) {
                    int i = 0;
                    while (n-- != 0) {
                        if (this.charAt(i) != anotherString.charAt(i)) {
                            return false;
                        }
                        i++;
                    }
                    return true;
                }
            }
            return false;
        }

        public int getStartIndex() {
            return this.start;
        }

        public int getStopIndex() {
            return this.end;
        }

        /**
         * create a String from this HtmlParserCharSequence and avoid copy if possible , see offsetValueCountAvailable
         * 
         * @return
         */
        public String getStringAvoidCopy() {
            if (HtmlParserCharSequence.offsetValueCountAvailable) {
                try {
                    final String ret = new String();
                    HtmlParserCharSequence.valueField.set(ret, this.chars);
                    HtmlParserCharSequence.offsetField.setInt(ret, this.start);
                    HtmlParserCharSequence.countField.setInt(ret, this.length());
                    return ret;
                } catch (final Throwable e) {
                }
            }
            return this.getStringCopy();
        }

        public String getStringCopy() {
            return new String(this.chars, this.getStartIndex(), this.length());
        }

        private int indexOf(final char[] source, final int sourceOffset, final int sourceCount, final CharSequence target, final int targetOffset, final int targetCount, int fromIndex) {
            if (fromIndex >= sourceCount) {
                return targetCount == 0 ? sourceCount : -1;
            }
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            if (targetCount == 0) {
                return fromIndex;
            }

            final char first = target.charAt(targetOffset);
            final int max = sourceOffset + sourceCount - targetCount;

            for (int i = sourceOffset + fromIndex; i <= max; i++) {
                /* Look for first character. */
                if (source[i] != first) {
                    while (++i <= max && source[i] != first) {
                        ;
                    }
                }

                /* Found first character, now look at the rest of v2 */
                if (i <= max) {
                    int j = i + 1;
                    final int end = j + targetCount - 1;
                    for (int k = targetOffset + 1; j < end && source[j] == target.charAt(k); j++, k++) {
                        ;
                    }

                    if (j == end) {
                        /* Found whole string. */
                        return i - sourceOffset;
                    }
                }
            }
            return -1;
        }

        public int indexOf(final CharSequence str) {
            return this.indexOf(str, 0);
        }

        public int indexOf(final CharSequence indexOf, final int fromIndex) {
            return this.indexOf(this.chars, this.getStartIndex(), this.length(), indexOf, 0, indexOf.length(), fromIndex);
        }

        @Override
        public int length() {
            return this.getStopIndex() - this.getStartIndex();
        }

        public boolean matches(final Pattern regex) {
            return regex.matcher(this).matches();
        }

        public HtmlParserCharSequence replaceAll(final Pattern regex, final String replacement) {
            final String ret = regex.matcher(this).replaceAll(replacement);
            if (this.equals(ret)) {
                return this;
            }
            return new HtmlParserCharSequence(ret);
        }

        public HtmlParserCharSequence replaceFirst(final Pattern regex, final String replacement) {
            final String ret = regex.matcher(this).replaceFirst(replacement);
            if (this.equals(ret)) {
                return this;
            }
            return new HtmlParserCharSequence(ret);
        }

        public boolean startsWith(final CharSequence prefix) {
            return this.startsWith(prefix, 0);
        }

        public boolean startsWith(final CharSequence prefix, final int toffset) {
            int to = toffset;
            int po = 0;
            int pc = prefix.length();
            // Note: toffset might be near -1>>>1.
            if (toffset < 0 || toffset > this.length() - pc) {
                return false;
            }
            while (--pc >= 0) {
                if (this.charAt(to++) != prefix.charAt(po++)) {
                    return false;
                }
            }
            return true;
        }

        public HtmlParserCharSequence subSequence(final int start) {
            return new HtmlParserCharSequence(this, this.getStartIndex() + start, this.getStopIndex());
        }

        @Override
        public HtmlParserCharSequence subSequence(final int start, final int end) {
            return new HtmlParserCharSequence(this, this.getStartIndex() + start, this.getStartIndex() + end);
        }

        @Override
        public String toString() {
            return this.getStringAvoidCopy();
        }

        public HtmlParserCharSequence trim() {
            int len = this.length();
            int st = 0;

            while (st < len && this.charAt(st) <= ' ') {
                st++;
            }
            while (st < len && this.charAt(st) <= ' ') {
                len--;
            }
            return st > 0 || len < this.length() ? this.subSequence(st, len) : this;
        }

    }

    private static class HtmlParserResultSet extends LinkedHashSet<String> {
        /**
         * 
         */
        private static final long         serialVersionUID           = -8661894478609472993L;
        protected final ArrayList<String> results                    = new ArrayList<String>();
        protected AtomicInteger           HttpLinksDeepWalkerCounter = new AtomicInteger(0);
        protected AtomicInteger           HttpLinksFinderCounter     = new AtomicInteger(0);
        protected AtomicInteger           HttpLinksWalkerCounter     = new AtomicInteger(0);

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
            if (super.add(e)) {
                this.results.add(e);
                return true;
            }
            return false;
        }

        public int getDeepWalkerCounter() {
            return this.HttpLinksDeepWalkerCounter.get();
        }

        public int getFinderCounter() {
            return this.HttpLinksFinderCounter.get();
        }

        public int getLastResultIndex() {
            return this.results.size();
        }

        public List<String> getResults() {
            return this.results;
        }

        public List<String> getResultsSublist(final int index) {
            return this.results.subList(index, this.results.size());
        }

        public int getWalkerCounter() {
            return this.HttpLinksWalkerCounter.get();
        }
    }

    final static class Httppattern {
        public Pattern p;
        public int     group;

        public Httppattern(final Pattern p, final int group) {
            this.p = p;
            this.group = group;
        }
    }

    final private static Httppattern[]          linkAndFormPattern          = new Httppattern[] { new Httppattern(Pattern.compile("src.*?=.*?('|\")(.*?)(\\1)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 2), new Httppattern(Pattern.compile("(<[ ]?a[^>]*?href=|<[ ]?form[^>]*?action=)('|\")(.*?)(\\2)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 3), new Httppattern(Pattern.compile("(<[ ]?a[^>]*?href=|<[ ]?form[^>]*?action=)([^'\"][^\\s]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 2), new Httppattern(Pattern.compile("\\[(link|url)\\](.*?)\\[/(link|url)\\]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), 2) };
    final private static String                 protocolPattern             = "(mega://|directhttp://https?://|flashget://|https?viajd://|https?://|ccf://|dlc://|ftp://|jd://|rsdf://|jdlist://|file://)";
    final private static Pattern[]              basePattern                 = new Pattern[] { Pattern.compile("href=('|\")(.*?)(\\1)", Pattern.CASE_INSENSITIVE), Pattern.compile("src=('|\")(.*?)(\\1)", Pattern.CASE_INSENSITIVE), Pattern.compile("(?s)<[ ]?base[^>]*?href=('|\")(.*?)\\1", Pattern.CASE_INSENSITIVE), Pattern.compile("(?s)<[ ]?base[^>]*?(href)=([^'\"][^\\s]*)", Pattern.CASE_INSENSITIVE) };
    final private static Pattern                pat1                        = Pattern.compile("(" + HTMLParser.protocolPattern + "|(?<!://)www\\.)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                protocols                   = Pattern.compile("(" + HTMLParser.protocolPattern + ")");
    final private static Pattern                LINKPROTOCOL                = Pattern.compile("^" + HTMLParser.protocolPattern, Pattern.CASE_INSENSITIVE);

    final private static Pattern                mergePattern_Root           = Pattern.compile("(.*?\\..*?)(/|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                mergePattern_Path           = Pattern.compile("(.*?\\.[^?#]+/)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                mergePattern_FileORPath     = Pattern.compile("(.*?\\..*?/.*?)($|#|\\?)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static Pattern                      mp                          = null;

    static {
        try {
            HTMLParser.mp = Pattern.compile("(\"|')?((" + HTMLParser.protocolPattern + "|www\\.).+?(?=((\\s*" + HTMLParser.protocolPattern + ")|<|>|\r|\n|\f|\t|$|\\1|';|'\\)|'\\+)))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        } catch (final Throwable e) {
            Log.exception(e);
        }
    }

    final private static Pattern                unescapePattern             = Pattern.compile("unescape\\(('|\")(.*?)(\\1)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                checkPatternHREFUNESCAPESRC = Pattern.compile(".*?(href|unescape|src=).+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                checkPatternHREFSRC         = Pattern.compile(".*?(href|src=).+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                unhexPattern                = Pattern.compile("(([0-9a-fA-F]{2}| )+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    final private static Pattern                paramsCut1                  = Pattern.compile("://[^\r\n]*?/[^\r\n]+\\?.[^\r\n]*?=(.*?)($|\r|\n)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    final private static Pattern                paramsCut2                  = Pattern.compile("://[^\r\n]*?/[^\r\n]*?\\?(.*?)($|\r|\n)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern                inTagsPattern               = Pattern.compile("<([^<]*?)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    final private static Pattern                endTagPattern               = Pattern.compile("^(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    final private static Pattern                taglessPattern              = Pattern.compile("^(.*?)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    final private static HtmlParserCharSequence directHTTP                  = new HtmlParserCharSequence("directhttp://");
    final private static HtmlParserCharSequence httpviajd                   = new HtmlParserCharSequence("httpviajd://");
    final private static HtmlParserCharSequence httpsviajd                  = new HtmlParserCharSequence("httpsviajd://");

    final private static Pattern                tagsPattern                 = Pattern.compile(".*<.*>.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    final private static Pattern                spacePattern                = Pattern.compile("\\s", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                singleSpacePattern          = Pattern.compile(" ", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                space2Pattern               = Pattern.compile(".*\\s.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                hdotsPattern                = Pattern.compile("h.{2,3}://", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                specialReplacePattern       = Pattern.compile("'", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                specialReplace2Pattern      = Pattern.compile("%21", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                missingHTTPPattern          = Pattern.compile("^www\\.", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    final private static Pattern                removeTagsPattern           = Pattern.compile("[<>\"]*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    final private static Pattern                urlEncodedProtocol      = Pattern.compile("(%3A%2F%2F|%253A%252F%252F)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static HtmlParserResultSet _getHttpLinksDeepWalker(HtmlParserCharSequence data, final String url, HtmlParserResultSet results) {
        if (results == null) {
            results = new HtmlParserResultSet();
        }
        results.HttpLinksDeepWalkerCounter.incrementAndGet();
        if (data == null || (data = data.trim()).length() < 13) {
            return results;
        }
        if ((data.startsWith(HTMLParser.directHTTP) || data.startsWith(HTMLParser.httpviajd) || data.startsWith(HTMLParser.httpsviajd)) && results.contains(data)) {
            /* we don't have to further check urls with those prefixes */
            return results;
        }
        final int indexBefore = results.getLastResultIndex();
        /* find reversed */
        String reversedata = new StringBuilder(data).reverse().toString();
        if (!data.equals(reversedata)) {
            HTMLParser._getHttpLinksFinder(new HtmlParserCharSequence(reversedata), url, results);
        }
        reversedata = null;
        /* find base64'ed */
        final HtmlParserCharSequence urlDecoded = HTMLParser.decodeURLParamEncodedURL(data);
        String urlDecodedString = urlDecoded.getStringAvoidCopy();
        String base64Data = Encoding.Base64Decode(urlDecodedString);
        if (urlDecodedString.equals(base64Data)) {
            /* no base64 content found */
            base64Data = null;
        }
        urlDecodedString = null;
        if (base64Data != null && !data.equals(base64Data)) {
            HTMLParser._getHttpLinksFinder(new HtmlParserCharSequence(base64Data), url, results);
        }
        base64Data = null;
        /* parse escaped js stuff */
        if (data.length() > 23 && data.contains("unescape")) {
            String unescaped[] = new Regex(data, HTMLParser.unescapePattern).getColumn(1);
            if (unescaped != null && unescaped.length > 0) {
                for (String unescape : unescaped) {
                    unescape = Encoding.htmlDecode(unescape);
                    HTMLParser._getHttpLinksFinder(new HtmlParserCharSequence(unescape), url, results);
                }
                unescaped = null;
            }
        }
        /* find hex'ed */
        if (HTMLParser.deepWalkCheck(results, indexBefore) && data.length() >= 24) {
            String hex = new Regex(data, HTMLParser.unhexPattern).getMatch(0);
            if (hex != null && hex.length() >= 24) {
                try {
                    /* remove spaces from hex-coded string */
                    hex = hex.replaceAll(" ", "");
                    String hexString = Hex.hex2String(hex);
                    hex = null;
                    HTMLParser._getHttpLinksFinder(new HtmlParserCharSequence(hexString), url, results);
                    hexString = null;
                } catch (final Throwable e) {
                    Log.exception(e);
                }
            }
        }
        if (HTMLParser.deepWalkCheck(results, indexBefore)) {
            /* no changes in results size, and data contains urlcoded http://, so lets urldecode it */
            HTMLParser._getHttpLinksFinder(urlDecoded, url, results);
        }
        return results;
    }

    private static HtmlParserResultSet _getHttpLinksFinder(HtmlParserCharSequence data, String url, HtmlParserResultSet results) {
        if (results == null) {
            results = new HtmlParserResultSet();
        }
        results.HttpLinksFinderCounter.incrementAndGet();
        if (data == null || (data = data.trim()).length() == 0) {
            return results;
        }
        if (!data.matches(HTMLParser.tagsPattern)) {
            final int c = new Regex(data, HTMLParser.pat1).count();
            String protocol = null;
            if (c == 0) {
                if (!data.matches(HTMLParser.checkPatternHREFSRC)) {
                    /* no href inside */
                    return results;
                }
            } else if (c == 1 && data.length() < 256) {
                if ((protocol = HTMLParser.getProtocol(data.toString())) != null) {
                    if (protocol.startsWith("file://")) {
                        results.add(data.replaceAll(HTMLParser.spacePattern, "%20").getStringCopy());
                        return results;
                    } else {
                        final HtmlParserCharSequence link = data.replaceAll(HTMLParser.removeTagsPattern, "");
                        if (!link.matches(HTMLParser.space2Pattern)) {
                            results.add(HTMLParser.correctURL(link).getStringCopy());
                            return results;
                        }
                    }
                } else {
                    final HtmlParserCharSequence link = data.replaceFirst(HTMLParser.hdotsPattern, "http://").replaceFirst(HTMLParser.missingHTTPPattern, "http://www.").replaceAll(HTMLParser.removeTagsPattern, "");
                    if (!link.matches(HTMLParser.space2Pattern)) {
                        results.add(HTMLParser.correctURL(link).getStringCopy());
                        return results;
                    }
                }
            }
        }
        if ("about:blank".equals(url)) {
            // remove about:blank
            url = null;
        }
        final String baseURL = url;
        // AVOID recheck every time: if (HTMLParser.getProtocol(url) != null) baseURL = url;
        url = null;
        Matcher m;
        String link;

        for (final Pattern element : HTMLParser.basePattern) {
            m = element.matcher(data);
            if (m.find()) {
                final String found = m.group(2);
                if ("about:blank".equals(found)) {
                    continue;
                }
                url = found;
                break;
            }
        }

        if (HTMLParser.getProtocol(url) == null) {
            if (baseURL != null && url != null) {
                url = HTMLParser.mergeUrl(baseURL, url);
            } else {
                /* no baseURL available, we are unable to try mergeURL */
                url = null;
            }
        }
        if (HTMLParser.getProtocol(url) != null) {
            /* found a valid url with recognized protocol */
            results.add(HTMLParser.correctURL(url).getStringCopy());
        }

        for (final Httppattern element : HTMLParser.linkAndFormPattern) {
            m = element.p.matcher(data);
            while (m.find()) {
                link = m.group(element.group);
                if (HTMLParser.getProtocol(link) == null) {
                    link = link.replaceAll("h.{2,3}://", "http://");
                }
                if (HTMLParser.getProtocol(link) != null) {
                    results.add(HTMLParser.correctURL(link).getStringCopy());
                } else if (baseURL != null) {
                    link = HTMLParser.mergeUrl(baseURL, link);
                    if (HTMLParser.getProtocol(link) != null) {
                        results.add(HTMLParser.correctURL(link).getStringCopy());
                    }
                }
            }
        }
        if (HTMLParser.mp != null) {
            m = HTMLParser.mp.matcher(data);
            while (m.find()) {
                link = m.group(2);
                link = link.trim();
                if (HTMLParser.getProtocol(link) == null) {
                    link = link.replaceFirst("^www\\.", "http://www\\.");
                }
                final Matcher mlinks = HTMLParser.protocols.matcher(link);
                int start = -1;
                /*
                 * special handling if we have multiple links without newline separation
                 */
                while (mlinks.find()) {
                    if (start == -1) {
                        start = mlinks.start();
                    } else {
                        results.add(HTMLParser.correctURL(link.substring(start, mlinks.start())).getStringCopy());
                        start = mlinks.start();
                    }
                }
                if (start != -1) {
                    final String check = link.substring(start);
                    results.add(HTMLParser.correctURL(check).getStringCopy());
                    if (data.equals(check)) {
                        /* data equals check, so we can leave this loop */
                        break;
                    }
                }
            }
        }
        return results;
    }

    private static HtmlParserResultSet _getHttpLinksWalker(HtmlParserCharSequence data, final String url, HtmlParserResultSet results, Pattern tagRegex) {
        // System.out.println("Call: "+data.length());
        if (results == null) {
            results = new HtmlParserResultSet();
        }
        results.HttpLinksWalkerCounter.incrementAndGet();
        if (data == null || (data = data.trim()).length() < 13) {
            return results;
        }
        /* filtering tags, recursion command me ;) */
        while (true) {
            if (tagRegex == null) {
                tagRegex = HTMLParser.inTagsPattern;
            }
            final String nexttag = new Regex(data, tagRegex).getMatch(0);
            if (nexttag == null || nexttag.length() == 0) {
                /* no further tag found, lets continue */
                break;
            } else {
                /* lets check if tag contains links */
                HTMLParser._getHttpLinksWalker(new HtmlParserCharSequence(nexttag), url, results, HTMLParser.inTagsPattern);
                final int tagOpen = data.indexOf('<' + nexttag);
                int tagClose = -1;
                if (tagOpen >= 0) {
                    tagClose = tagOpen + nexttag.length() + 1;
                }
                if (tagClose >= 0 && data.length() >= tagClose + 1) {
                    if (tagOpen > 0) {
                        /*
                         * there might be some data left before the tag, do not remove that data
                         */
                        final HtmlParserCharSequence dataLeft = data.subSequence(0, tagOpen);
                        final HtmlParserCharSequence dataLeft2 = data.subSequence(tagClose + 1);
                        data = null;
                        if (dataLeft.contains(">")) {
                            HTMLParser._getHttpLinksWalker(dataLeft, url, results, HTMLParser.endTagPattern);
                        } else {
                            HTMLParser._getHttpLinksWalker(dataLeft, url, results, HTMLParser.taglessPattern);
                        }
                        data = dataLeft2;
                    } else {
                        /* remove tag at begin of data */
                        data = data.subSequence(tagClose + 1);
                        if (data.length() == 0) {
                            return results;
                        }
                    }
                    // System.out.println("SubCall: "+data.length());
                } else {
                    if (tagClose < 0) {
                        data = data.subSequence(nexttag.length());
                        if (data.length() == 0) {
                            return results;
                        }
                    } else {
                        /* remove tag at begin of data */
                        data = data.subSequence(tagClose + 1);
                        if (data.length() == 0) {
                            return results;
                        }
                    }
                }
            }
        }
        /* find normal */
        if (data.length() < 13) {
            //
            return results;
        }
        if (!data.contains("://") && !new Regex(data, HTMLParser.urlEncodedProtocol).matches() && !data.contains("www.")) {
            /* data must contain at least the protocol separator */
            if (!data.matches(HTMLParser.checkPatternHREFUNESCAPESRC)) {
                /* maybe easy encrypted website or a href */
                return results;
            }
        }
        final int indexBefore = results.getResults().size();
        HTMLParser._getHttpLinksFinder(data, url, results);
        if (HTMLParser.deepWalkCheck(results, indexBefore)) {
            HTMLParser._getHttpLinksDeepWalker(data, url, results);
            /* cut of ?xy= parts if needed */
            String newdata = new Regex(data, HTMLParser.paramsCut1).getMatch(0);
            if (newdata != null && !data.equals(newdata)) {
                HTMLParser._getHttpLinksDeepWalker(new HtmlParserCharSequence(newdata), url, results);
            }
            /* use of ?xy parts if available */
            newdata = new Regex(data, HTMLParser.paramsCut2).getMatch(0);
            if (newdata != null && !data.equals(newdata)) {
                HTMLParser._getHttpLinksDeepWalker(new HtmlParserCharSequence(newdata), url, results);
            }
        }
        return results;
    }

    private static HtmlParserCharSequence correctURL(HtmlParserCharSequence input) {
        int specialCutOff = input.indexOf("', ");
        if (specialCutOff < 0) {
            specialCutOff = input.indexOf("',");
        }
        if (specialCutOff >= 0) {
            input = input.subSequence(0, specialCutOff);
        }
        final int indexofa = input.indexOf("&");
        if (indexofa > 0 && input.indexOf("?") == -1) {
            final int indexofb = input.indexOf("#");
            if (indexofb < 0 || indexofb > indexofa) {
                /**
                 * this can happen when we found a link as urlparameter
                 * 
                 * eg test.com/?u=http%3A%2F%2Fwww...&bla=
                 * 
                 * then we get
                 * 
                 * http://www...&bla
                 * 
                 * we cut of the invalid urlParameter &bla
                 * 
                 * check if we really have &x=y format following
                 * 
                 * also pay attention about anchor
                 */
                final HtmlParserCharSequence check = input.subSequence(indexofa);
                final int indexChecka = check.indexOf("=");
                if (indexChecka > 0) {
                    final HtmlParserCharSequence check2 = check.subSequence(1, indexChecka);
                    if (check2.matches(Pattern.compile("[a-zA-Z0-9%]+"))) {
                        /* we have found &x=y pattern, so it is okay to cut it off */
                        input = input.subSequence(0, indexofa);
                    }
                }
            }
        }
        /* ! is allowed so we convert back %21 to ! */
        input = input.replaceAll(HTMLParser.specialReplace2Pattern, "!");
        /* ' must be %27 encoded */
        input = input.replaceAll(HTMLParser.specialReplacePattern, "%27");
        /* spaces must be %20 encoded */
        return input.replaceAll(HTMLParser.singleSpacePattern, "%20");
    }

    private static HtmlParserCharSequence correctURL(final String input) {
        return HTMLParser.correctURL(new HtmlParserCharSequence(input));
    }

    public static HtmlParserCharSequence decodeURLParamEncodedURL(HtmlParserCharSequence content) {
        if (content != null && new Regex(content.toString(), HTMLParser.urlEncodedProtocol).matches()) {
            String inputString = content.getStringAvoidCopy();
            content = new HtmlParserCharSequence(decodeURLParamEncodedURL(inputString));
        }
        return content;
    }
    
    public static String decodeURLParamEncodedURL(String content) {
        // has to be first. to allow for multiple double encode of % eg. %253A%252F%252F
        content = content.replaceAll("%25", "%");
        // the rest
        content = content.replaceAll("%2F", "/");
        content = content.replaceAll("%3A", ":");
        content = content.replaceAll("%3F", "?");
        content = content.replaceAll("%3D", "=");
        content = content.replaceAll("%26", "&");
        content = content.replaceAll("%23", "#");
        return content;
    }

    private static boolean deepWalkCheck(final HtmlParserResultSet results, final int indexBefore) {
        final int latestIndex = results.getLastResultIndex();
        final boolean ret = latestIndex == indexBefore;
        if (!ret) {
            final List<String> subList = results.getResultsSublist(indexBefore);
            for (final String check : subList) {
                if (new Regex(check, HTMLParser.urlEncodedProtocol).matches()) {
                    return true;
                }
            }
        }
        return ret;
    }

    /**
     * Diese Methode sucht nach passwÃ¶rtern in einem Datensatz
     * 
     * @param data
     * @return
     */
    public static ArrayList<String> findPasswords(String data) {
        if (data == null) {
            return new ArrayList<String>();
        }
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
        if (links == null || links.size() == 0) {
            return new String[0];
        }
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
            // this finds a URLencoded URL within 'link'. We only want to find URLEncoded link, and not a value belonging to 'link'
            final String urlEncodedLink = new Regex(link, "(?:https?|ftp)" + HTMLParser.urlEncodedProtocol + "[^&]+").getMatch(-1);
            if (urlEncodedLink != null) {
                tmplinks.add(decodeURLParamEncodedURL(urlEncodedLink));
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
        if (data.length() == 0) {
            return null;
        }
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
        data = data.replaceAll("(?s)\\[(url|link)\\](.*?)\\[/(\\2)\\]", "<$2>");
        final HtmlParserResultSet results = new HtmlParserResultSet();
        HTMLParser._getHttpLinksWalker(new HtmlParserCharSequence(data), url, results, null);
        /* we don't want baseurl to be included in result set */
        results.remove(url);
        // System.out.println("Walker:" + results.getWalkerCounter() + "|DeepWalker:" + results.getDeepWalkerCounter() + "|Finder:" +
        // results.getFinderCounter() + "|Found:" + results.size());
        if (results.isEmpty()) {
            return null;
        }
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
        if (url == null) {
            return null;
        }
        return new Regex(url, HTMLParser.LINKPROTOCOL).getMatch(0);
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

    private static String mergeUrl(final String baseURL, final String path) {
        if (path == null || baseURL == null || path.length() == 0) {
            return null;
        }
        String merged = null;
        final char first = path.charAt(0);
        if (first == '/') {
            /* absolut path relative to baseURL */
            final String base = new Regex(baseURL, HTMLParser.mergePattern_Root).getMatch(0);
            if (base != null) {
                merged = base + path;
            }
        } else if (first == '.' && (path.charAt(1) == '.' || path.charAt(1) == '/')) {
            /* relative path relative to baseURL */
            String base = new Regex(baseURL, HTMLParser.mergePattern_Path).getMatch(0);
            if (base != null) {
                /* relative to current path */
                merged = base + path;
            } else {
                base = new Regex(baseURL, HTMLParser.mergePattern_Root).getMatch(0);
                if (base != null) {
                    /* relative to root */
                    merged = base + "/" + path;
                }
            }
        } else if (first == '#' || first == '?') {
            /* append query/anchor to baseURL */
            String base = new Regex(baseURL, HTMLParser.mergePattern_FileORPath).getMatch(0);
            if (base != null) {
                /* append query/anchor to current path/file */
                merged = base + path;
            } else {
                base = new Regex(baseURL, HTMLParser.mergePattern_Root).getMatch(0);
                if (base != null) {
                    /* append query/anchor to root */
                    merged = base + "/" + path;
                }
            }
        } else {
            /* relative path relative to baseURL */
            String base = new Regex(baseURL, HTMLParser.mergePattern_Path).getMatch(0);
            if (base != null) {
                /* relative to current path */
                merged = base + path;
            } else {
                base = new Regex(baseURL, HTMLParser.mergePattern_Root).getMatch(0);
                if (base != null) {
                    /* relative to root */
                    merged = base + "/" + path;
                }
            }
        }
        return Browser.correctURL(merged, true);
    }
}
