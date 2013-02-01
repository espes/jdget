//    jDownloader - Downloadmanager
//    Copyright (C) 2013  JD-Team support@jdownloader.org
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jd.nutils.encoding.Encoding;
import jd.parser.Regex;

public class InputField extends HashMap<String, String> {

    private static final long serialVersionUID = 7859094911920903660L;

    public static InputField parse(String data) {
       // lets make all quotation marks within 'data' the same. As it's hard to make consistent regex 'matches' when quote marks are not the same, without using lazy regex!.
        ArrayList<String> cleanupRegex = new ArrayList<String>();
        cleanupRegex.add("(\\w+\\s*=\\s*\"[^\"]+\")");
        cleanupRegex.add("(\\w+\\s*=\\s*'[^']+')");
        for (String reg : cleanupRegex) {
            String results[] = new Regex(data, reg).getColumn(0);
            if (results != null) {
                String quote = new Regex(reg, "(\"|')").getMatch(0);
                for (String result : results) {
                    String cleanedResult = result.replaceAll(quote, "\\\"");
                    data = data.replaceAll(result, cleanedResult);
                }
            }
        }
        
        // no longer have to worry about forms with miss matched quotation marks!
        String[][] matches = new Regex(data, "[\"' ](\\w+?)\\s*=\\s*\"(.*?)\"").getMatches();
        // find values when quotation marks are not used! don't forget to exit on space!
        final String[][] matches2 = new Regex(data, "[\"' ](\\w+?)\\s*=\\s*([^ >\"']+)").getMatches();
        final InputField ret = new InputField();

        for (final String[] match : matches) {
            if (match[0].equalsIgnoreCase("type")) {
                ret.setType(match[1]);
            } else if (match[0].equalsIgnoreCase("name")) {
                ret.setKey(Encoding.formEncoding(match[1]));
            } else if (match[0].equalsIgnoreCase("value")) {
                ret.setValue(Encoding.formEncoding(match[1]));
            } else {
                ret.put(Encoding.formEncoding(match[0]), Encoding.formEncoding(match[1]));
            }
        }

        for (final String[] match : matches2) {
            if (match[0].equalsIgnoreCase("type")) {
                ret.setType(match[1]);
            } else if (match[0].equalsIgnoreCase("name")) {
                ret.setKey(Encoding.formEncoding(match[1]));
            } else if (match[0].equalsIgnoreCase("value")) {
                ret.setValue(Encoding.formEncoding(match[1]));
            } else {
                ret.put(Encoding.formEncoding(match[0]), Encoding.formEncoding(match[1]));
            }
        }
        return ret;
    }

    private String key = null;
    private String value = null;

    private String type = null;

    public InputField() {
        // TODO Auto-generated constructor stub
    }

    public InputField(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public File getFileToPost() {
        if (!type.equalsIgnoreCase("file")) { throw new IllegalStateException("No file post field"); }

        return new File(value);
    }

    public String getKey() {
        return key;
    }

    public String getProperty(final String key, final String defValue) {
        final String ret = get(key);
        return ret == null ? ret : defValue;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setFileToPost(final File file) {
        if (!type.equalsIgnoreCase("file")) { throw new IllegalStateException("No file post field"); }
        value = file.getAbsolutePath();
    }

    public void setKey(String string) {
        if (string != null) {
            string = string.trim();
        }
        key = string;
    }

    public void setType(String string) {
        if (string != null) {
            string = string.trim();
        }
        type = string;
    }

    public void setValue(String value) {
        if (value != null) {
            value = value.trim();
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return "Field: " + key + "(" + type + ")" + " = " + value + " [" + super.toString() + "]";
    }

}