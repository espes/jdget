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

    private File file;
    private BufferedWriter writer;

    public LogToFileHandler() throws IOException {
        super();
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(new Date().getTime());

        file = Application.getRessource("logs/" + cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + ".log");
        file.getParentFile().mkdirs();
        if (!file.isFile()) file.createNewFile();
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF8"));

    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void publish(LogRecord logRecord) {
        if (logRecord.getLevel() == Level.INFO) {

            try {
                writer.write(this.getFormatter().format(logRecord));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
}
