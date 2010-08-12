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
            final Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(new Date().getTime());

            this.file = Application.getRessource("logs/error_" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + System.currentTimeMillis() + ".log");
            this.file.getParentFile().mkdirs();
            this.file.deleteOnExit();
            if (!this.file.isFile()) {
                this.file.createNewFile();
            }
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file, true), "UTF8"));
        } catch (final Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    public void close() {
        try {
            this.writer.close();
        } catch (final IOException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void flush() {
        try {
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
            this.writer.write(this.getFormatter().format(logRecord));
        } catch (final IOException e) {
            e.printStackTrace();

        }
    }
}
