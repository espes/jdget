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

    private volatile File      file;
    private BufferedWriter     writer;
    private OutputStreamWriter osw = null;
    private FileOutputStream   fos = null;

    public LogToFileHandler() throws IOException {
        super();
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        this.file = Application.getResource("logs/" + cal.get(Calendar.YEAR) + "-" + (1 + cal.get(Calendar.MONTH)) + "-" + cal.get(Calendar.DATE) + ".log");
        this.file.getParentFile().mkdirs();
        this.file.deleteOnExit();
        if (!this.file.isFile()) {
            this.file.createNewFile();
        }
        try {
            this.writer = new BufferedWriter(this.osw = new OutputStreamWriter(this.fos = new FileOutputStream(this.file, true), "UTF8"));
        } catch (final IOException e) {
            e.printStackTrace();
            this.close();
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            if (this.writer != null) {
                this.writer.close();
            }
        } catch (final Throwable e) {
        } finally {
            this.writer = null;
        }
        try {
            if (this.osw != null) {
                this.osw.close();
            }
        } catch (final Throwable e) {
        } finally {
            this.osw = null;
        }
        try {
            if (this.fos != null) {
                this.fos.close();
            }
        } catch (final Throwable e) {
        } finally {
            this.fos = null;
        }
        final File lfile = this.file;
        this.file = null;
        if (lfile != null && lfile.exists() && lfile.length() == 0) {
            lfile.delete();
        }
    }

    @Override
    public void flush() {
        try {
            final BufferedWriter lwriter = this.writer;
            if (lwriter != null) {
                lwriter.flush();
            }
        } catch (final IOException e) {
            org.appwork.utils.logging.Log.exception(e);
        }
    }

    @Override
    public void publish(final LogRecord logRecord) {
        if (logRecord.getLevel() == Level.INFO) {
            try {
                final BufferedWriter lwriter = this.writer;
                if (lwriter != null) {
                    lwriter.write(this.getFormatter().format(logRecord));
                }
            } catch (final IOException e) {
                if (e.getMessage().contains("not enough")) {
                    org.appwork.utils.logging.Log.L.severe("Cannot write log, Disk is full!");
                } else {
                    org.appwork.utils.logging.Log.exception(e);
                }
            }

        }

    }
}
