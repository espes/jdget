/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2.sendlogs
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2.sendlogs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.appwork.exceptions.WTFException;
import org.appwork.swing.action.BasicAction;
import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.ProgressDialog;
import org.appwork.utils.swing.dialog.ProgressDialog.ProgressGetter;
import org.appwork.utils.zip.ZipIOException;
import org.appwork.utils.zip.ZipIOWriter;

/**
 * @author Thomas
 * 
 */
public abstract class AbstractLogAction extends BasicAction {

    protected int total;
    protected int current;

    public AbstractLogAction() {

    }

    @Override
    public void actionPerformed(final ActionEvent e) {

        final ProgressDialog p = new ProgressDialog(new ProgressGetter() {

            @Override
            public int getProgress() {
                return -1;

            }

            @Override
            public String getString() {
                return T.T.LogAction_getString_uploading_();
            }

            @Override
            public void run() throws Exception {
                create();
            }

            @Override
            public String getLabelString() {
                return null;
            }
        }, Dialog.BUTTONS_HIDE_OK, T.T.LogAction_actionPerformed_zip_title_(), T.T.LogAction_actionPerformed_wait_(), null, null, null);

        try {
            Dialog.getInstance().showDialog(p);
        } catch (final Throwable e1) {

        }

    }

    protected void create() throws DialogClosedException, DialogCanceledException {

        final File[] logs = Application.getResource("logs").listFiles();
        final java.util.List<LogFolder> folders = new ArrayList<LogFolder>();

        LogFolder latest = null;

        if (logs != null) {
            for (final File f : logs) {
                final String timestampString = new Regex(f.getName(), "(\\d+)_\\d\\d\\.\\d\\d").getMatch(0);
                if (timestampString != null) {
                    final long timestamp = Long.parseLong(timestampString);
                    LogFolder lf;
                    lf = new LogFolder(f, timestamp);
                    if (isCurrentLogFolder(timestamp)) {
                        /*
                         * this is our current logfolder, flush it before we can
                         * upload it
                         */
                        lf.setNeedsFlush(true);
                    }
                    if (Files.getFiles(new FileFilter() {
                        @Override
                        public boolean accept(final File pathname) {
                            return pathname.isFile() && pathname.length() > 0;
                        }
                    }, f).size() == 0) {
                        continue;
                    }

                    folders.add(lf);
                    if (latest == null || lf.getCreated() > latest.getCreated()) {
                        latest = lf;
                    }
                }
            }
        }
        if (latest != null) {
            latest.setSelected(true);
        }

        if (folders.size() == 0) {
            Dialog.getInstance().showExceptionDialog("WTF!", "At Least the current Log should be available", new WTFException());
            return;
        }
        final SendLogDialog d = new SendLogDialog(folders);

        Dialog.getInstance().showDialog(d);

        final java.util.List<LogFolder> selection = d.getSelectedFolders();
        if (selection.size() == 0) { return; }
        total = selection.size();
        current = 0;
        final ProgressDialog p = new ProgressDialog(new ProgressGetter() {

            @Override
            public int getProgress() {
                if (current == 0) { return -1; }
                return current * 100 / total;
            }

            @Override
            public String getString() {
                return T.T.LogAction_getString_uploading_();
            }

            @Override
            public void run() throws Exception {

                try {
                    createPackage(selection);
                } catch (final WTFException e) {
                    throw new InterruptedException();
                }

            }

            @Override
            public String getLabelString() {
                return null;
            }
        }, Dialog.BUTTONS_HIDE_OK, T.T.LogAction_actionPerformed_zip_title_(), T.T.LogAction_actionPerformed_wait_(), null, null, null);

        Dialog.getInstance().showDialog(p);

    }

    /**
     * @param selection
     * 
     */
    protected void createPackage(final List<LogFolder> selection) throws Exception {
        for (final LogFolder lf : selection) {
            final File zip = Application.getResource("tmp/logs/logPackage.zip");
            zip.delete();
            zip.getParentFile().mkdirs();
            ZipIOWriter writer = null;
            
            String name =lf.getFolder().getName()+"-"+ format(lf.getCreated()) + " to " + format(lf.getLastModified());
            File folder = Application.getResource("tmp/logs/" + name);
            try {
                if (lf.isNeedsFlush()) {

                    flushLogs();
                }
                writer = new ZipIOWriter(zip) {
                    public void addFile(final File addFile, final boolean compress, final String fullPath) throws FileNotFoundException, ZipIOException, IOException {
                        if (addFile.getName().endsWith(".lck") || (addFile.isFile() && addFile.length() == 0)) { return; }
                        if (Thread.currentThread().isInterrupted()) { throw new WTFException("INterrupted"); }
                        super.addFile(addFile, compress, fullPath);
                    }
                };
               
                if (folder.exists()) Files.deleteRecursiv(folder);
                IO.copyFolderRecursive(lf.getFolder(), folder, true);
                writer.addDirectory(folder, true, null);
            } finally {
                try {
                    writer.close();
                } catch (final Throwable e) {
                }
            }

            onNewPackage(zip,format(lf.getCreated()) + "-" + format(lf.getLastModified()));

            current++;
        }
    }

    /**
     * @param created
     * @return
     */
    private String format(long created) {
        Date date = new Date(created);
        
        return new SimpleDateFormat("dd.MM.yy HH.mm.ss",Locale.GERMANY).format(date);
    
    }

    /**
     * @param timestamp
     * @return
     */
    abstract protected boolean isCurrentLogFolder(long timestamp);

    /**
     * @param zip
     * @param string 
     * @throws IOException
     */
    abstract protected void onNewPackage(File zip, String string) throws IOException;

    /**
     * 
     */
    abstract protected void flushLogs();

}
