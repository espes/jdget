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
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public class LogToFileHandler extends java.util.logging.Handler {

    private File               file;
    private BufferedWriter     writer;
    private OutputStreamWriter osw = null;
    private FileOutputStream   fos = null;

    public LogToFileHandler() throws IOException {
        super();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());

        file = Application.getResource("logs/" + cal.get(Calendar.YEAR) + "-" + (1 + cal.get(Calendar.MONTH)) + "-" + cal.get(Calendar.DATE) + ".log");
        file.getParentFile().mkdirs();
        file.deleteOnExit();
        if (!file.isFile()) file.createNewFile();
        try {
            writer = new BufferedWriter(osw = new OutputStreamWriter(fos = new FileOutputStream(file, true), "UTF8"));
        } catch (Throwable e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (Throwable e) {
        }
        try {
            osw.close();
        } catch (Throwable e) {
        }
        try {
            fos.close();
        } catch (Throwable e) {
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
        if (logRecord.getLevel() == Level.INFO) {
            try {
                writer.write(this.getFormatter().format(logRecord));
            } catch (IOException e) {
                if (e.getMessage().contains("not enough")) {
                    org.appwork.utils.logging.Log.L.severe("Cannot write log, Disk is full!");
                } else {
                    org.appwork.utils.logging.Log.exception(e);
                }
            }

        }

    }
}
