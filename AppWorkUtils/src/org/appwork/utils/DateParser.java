/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author daniel
 * 
 */
public class DateParser {

    private java.util.List<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();

    public DateParser() {
    }

    public DateParser(String[] dateFormats) {
        if (dateFormats != null) {
            for (String format : dateFormats) {
                try {
                    this.dateFormats.add(new SimpleDateFormat(format));
                } catch (Exception e) {
                }
            }
        }
    }

    public Date parseDate(String date) {
        if (date != null) {
            for (SimpleDateFormat format : dateFormats) {
                try {
                    return format.parse(date);
                } catch (ParseException e) {
                }
            }
        }
        return null;
    }

}
