package org.appwork.utils.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

import org.appwork.utils.Application;

public class ErrRedirect extends Thread {
    private final File       file;
    private FileOutputStream outStr      = null;
    private PrintStream      printStream = null;

    public ErrRedirect() {
        this.setDaemon(true);
        final Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(new Date().getTime());

        this.file = Application.getResource("logs/error_cerr_" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + System.currentTimeMillis() + ".log");

        try {
            this.file.getParentFile().mkdirs();
            this.file.deleteOnExit();
            if (!this.file.isFile()) {
                this.file.createNewFile();
            }
            this.outStr = new FileOutputStream(this.file, true);
            this.printStream = new PrintStream(this.outStr);
            System.setErr(this.printStream);
            this.start();
        } catch (final IOException e) {
            Log.exception(e);
        }
    }

    public void close() throws IOException {
        try {
            this.printStream.close();
        } catch (final Throwable e) {
        }
        try {
            this.outStr.close();
        } catch (final Throwable e) {
        }
        this.printStream = null;
        this.outStr = null;
    }

    public void flush() throws IOException {
        if (this.printStream == null) { return; }
        this.printStream.flush();
        this.outStr.flush();
    }

    /**
     * @return the {@link ErrRedirect#file}
     * @see ErrRedirect#file
     */
    public File getFile() {
        return this.file;
    }

    @Override
    public void run() {
        // flushes the log every 60 seconds and writes it to file
        while (true) {
            if (this.printStream == null) {
                break;
            }
            try {
                Thread.sleep(60000);
            } catch (final InterruptedException e) {
                // e.printStackTrace();
            }
            try {
                if (this.printStream == null) {
                    break;
                }
                this.printStream.flush();
            } catch (final Throwable e) {
                Log.exception(e);
                break;
            }
        }
        try {
            this.close();
        } catch (final IOException e) {
        }
    }
}
