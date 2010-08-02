/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.LogRecord;

import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public class ExceptionLogHandler extends java.util.logging.Handler {

    private File file;
    private BufferedWriter writer;

    public ExceptionLogHandler() {
        super();
        try {
            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(new Date().getTime());

            file = Application.getRessource("logs/error_" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + System.currentTimeMillis() + ".log");
            file.getParentFile().mkdirs();
            file.deleteOnExit();
            if (!file.isFile()) file.createNewFile();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF8"));
        } catch (Exception e) {
            Log.exception(e);
        }

    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            org.appwork.utils.logging.Log.exception(e);
        }
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {

            org.appwork.utils.logging.Log.exception(e);
        }
    }

    public void publish(LogRecord logRecord) {
        try {
            writer.write(this.getFormatter().format(logRecord));
        } catch (IOException e) {
            org.appwork.utils.logging.Log.exception(e);
        }
    }

    /**
     * @return
     */
    public File getFile() {
        return file;
    }
}
