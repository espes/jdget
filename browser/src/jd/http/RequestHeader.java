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

package jd.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RequestHeader {
    /**
     * For more header fields see
     * 
     * @link(http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14).
     */
    private boolean            dominant = false;

    private final List<String> keys;

    private final List<String> values;

    public RequestHeader() {
        this.keys = new ArrayList<String>();
        this.values = new ArrayList<String>();
    }

    public RequestHeader(final Map<String, String> headers) {
        this();
        this.putAll(headers);
    }

    public RequestHeader(final RequestHeader requestHeader) {
        this();
        if (requestHeader != null) {
            this.keys.addAll(requestHeader.keys);
            this.values.addAll(requestHeader.values);
            this.dominant = requestHeader.dominant;
        }
    }

    public void clear() {
        this.keys.clear();
        this.values.clear();
    }

    @Override
    public RequestHeader clone() {
        final RequestHeader newObj = new RequestHeader();
        newObj.keys.addAll(this.keys);
        newObj.values.addAll(this.values);
        newObj.dominant = this.dominant;
        return newObj;
    }

    public boolean contains(final String string) {
        return this.keys.contains(string);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (this.getClass() != obj.getClass()) { return false; }
        final RequestHeader other = (RequestHeader) obj;
        if (this.dominant != other.dominant) { return false; }
        if (this.keys == null) {
            if (other.keys != null) { return false; }
        } else if (!this.keys.equals(other.keys)) { return false; }
        if (this.values == null) {
            if (other.values != null) { return false; }
        } else if (!this.values.equals(other.values)) { return false; }
        return true;
    }

    public String get(final String key) {
        final int index = this.keys.indexOf(key);
        return index >= 0 ? this.values.get(index) : null;
    }

    public String getKey(final int index) {
        return this.keys.get(index);
    }

    public String getValue(final int index) {
        return this.values.get(index);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.dominant ? 1231 : 1237);
        result = prime * result + (this.keys == null ? 0 : this.keys.hashCode());
        result = prime * result + (this.values == null ? 0 : this.values.hashCode());
        return result;
    }

    public boolean isDominant() {
        return this.dominant;
    }

    public void put(final String key, final String value) {
        final int keysSize = this.keys.size();
        final String trim = key.trim();
        for (int i = 0; i < keysSize; i++) {
            if (this.keys.get(i).equalsIgnoreCase(trim)) {
                this.keys.set(i, key);
                this.values.set(i, value);
                return;
            }
        }
        this.keys.add(key);
        this.values.add(value);
    }

    public void putAll(final Map<String, String> properties) {
        for (final Entry<String, String> entry : properties.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (value == null) {
                this.remove(key);
            } else {
                this.put(key, value);
            }
        }
    }

    public void putAll(final RequestHeader headers) {
        final int size = headers.size();
        for (int i = 0; i < size; i++) {
            final String key = headers.getKey(i);
            final String value = headers.getValue(i);
            if (value == null) {
                this.remove(key);
            } else {
                this.put(key, value);
            }
        }
    }

    public String remove(final String key) {
        final int index = this.keys.indexOf(key);

        if (index >= 0) {
            this.keys.remove(index);
            return this.values.remove(index);
        }

        return null;
    }

    /**
     * if a header is dominant, it will not get merged with existing headers. It will replace it completely
     * 
     * @param dominant
     */
    public void setDominant(final boolean dominant) {
        this.dominant = dominant;
    }

    public int size() {
        return this.keys.size();
    }
}
