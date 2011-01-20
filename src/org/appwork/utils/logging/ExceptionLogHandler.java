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

    private File               file;
    private BufferedWriter     writer = null;
    private OutputStreamWriter osw    = null;
    private FileOutputStream   fos    = null;

    public ExceptionLogHandler() {
        super();
        try {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(new Date().getTime());
            this.file = Application.getResource("logs/error_" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + System.currentTimeMillis() + ".log");
            this.file.getParentFile().mkdirs();
            this.file.deleteOnExit();
            if (!this.file.isFile()) {
                this.file.createNewFile();
            }
            this.writer = new BufferedWriter(osw = new OutputStreamWriter(fos = new FileOutputStream(this.file, true), "UTF8"));
        } catch (final Exception e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (Throwable e) {
        } finally {
            this.writer = null;
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

    @Override
    public void flush() {
        try {
            if (this.writer == null) return;
            this.writer.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    public File getFile() {
        return this.file;
    }

    @Override
    public void publish(final LogRecord logRecord) {
        try {
            if (this.writer == null) return;
            this.writer.write(this.getFormatter().format(logRecord));
        } catch (final IOException e) {
            /*
             * in case write does not work, we close the file and no further
             * logging to file
             */
            e.printStackTrace();
            try {
                close();
            } catch (Throwable e2) {
            }
        }
    }
}
