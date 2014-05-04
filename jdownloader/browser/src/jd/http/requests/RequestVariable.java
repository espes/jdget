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

package jd.http.requests;

import java.util.Map.Entry;

/**
 * class holding key value of variable for Request
 */
public class RequestVariable {

    /** The key */
    final private String key;

    /** The value */
    final private String value;

    /**
     * constructor
     * 
     * @param keyValueEntry
     *            {@link java.util.Map.Entry} containing key and value to use
     */
    public RequestVariable(final Entry<String, String> keyValueEntry) {
        this.key = keyValueEntry.getKey();
        this.value = keyValueEntry.getValue();
    }

    /**
     * constructor
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public RequestVariable(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }
}
