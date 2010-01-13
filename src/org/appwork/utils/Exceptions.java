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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author $Author: unknown$
 * 
 */
public class Exceptions {
    /**
     * returns the Exceptions Stacktrace as String
     * 
     * @param thrown
     * @return
     */
    public static String getStackTrace(Throwable thrown) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        thrown.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

}
