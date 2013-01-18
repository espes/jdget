/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * @author Thomas
 * 
 */
public class InputStreamLogger extends Thread {

    private InputStream stream;
    private LogSource   logger;

    /**
     * @param is
     * @param logSource
     * @param b
     */
    public InputStreamLogger(InputStream is, LogSource logSource) {
        super();
        stream = is;
        logger = logSource;
    }

    public void run() {
        try {
            readInputStreamToString(stream, logger);
        } catch (Exception e) {
            logger.log(e);
        }
    }

    public void readInputStreamToString(final InputStream fis, LogSource logger) throws UnsupportedEncodingException, IOException, InterruptedException {
        BufferedReader f = null;

        try {
            f = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            String line;

            while ((line = f.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    /*
                     * Workaround for this bug:
                     * http://bugs.sun.com/view_bug.do?bug_id=4508058
                     * http://bugs.sun.com/view_bug.do?bug_id=6378911
                     */

                    line = line.substring(1);
                }
                logger.info(line);

            }

        } catch (final IOException e) {

            throw e;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Error e) {
            throw e;
        } finally {

            // don't close streams this might ill the process
        }
    }
}
