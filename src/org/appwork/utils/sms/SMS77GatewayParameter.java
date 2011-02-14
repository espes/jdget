/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.sms
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.sms;

/**
 * @author daniel
 *
 */

import java.net.URLEncoder;

public class SMS77GatewayParameter {

    public static SMS77GatewayParameter create(final String key, final Object value) {
        if (value == null) { return null; }

        return new SMS77GatewayParameter(key, value);
    }

    public static SMS77GatewayParameter create(final String key, final Object[] fields) {

        if (fields == null) { return null; }
        final StringBuilder sb = new StringBuilder();
        for (final Object f : fields) {
            if (sb.length() > 0) {
                sb.append(',');
            }

            sb.append(f.toString());
        }
        return new SMS77GatewayParameter(key, sb);

    }

    private final String key;
    private final String value;

    public SMS77GatewayParameter(final String key, final Object value) {
        this.key = key;
        this.value = value.toString();

    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        try {
            return key + "=" + URLEncoder.encode(value, "UTF-8");
        } catch (final Exception e) {
            return key + "=" + value;
        }
    }
}
