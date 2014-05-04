/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog.test;

import java.util.logging.Level;

import org.appwork.resources.AWUTheme;
import org.appwork.uio.UIOManager;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.ProgressDialog;
import org.appwork.utils.swing.dialog.ProgressDialog.ProgressGetter;

/**
 * @author Thomas
 * 
 */
public class TEstProgressDialog {
    public static void main(String[] args) {
        Log.L.setLevel(Level.ALL);
        new EDTRunner() {

            @Override
            protected void runInEDT() {
         
                final ProgressGetter pg = new ProgressGetter() {

                    private long loaded = 0;
                    private long total  = 100;

                    @Override
                    public int getProgress() {
                 
                        if (this.total == 0) { return 0; }
                        return (int) (this.loaded * 100 / this.total);
                    }

                    @Override
                    public String getString() {
               
                        if (this.total <= 0) { return _AWU.T.connecting(); }
                        String ret = _AWU.T.progress(SizeFormatter.formatBytes(this.loaded), SizeFormatter.formatBytes(this.total), (this.loaded * 10000 / this.total) / 100.0);
                        return ret;
                    }

                    @Override
                    public void run() throws Exception {
                        for (int i = 0; i < 100; i++) {
                            Thread.sleep(1000);
                            loaded++;
                        }

                    }

                    @Override
                    public String getLabelString() {
                        return getProgress()+" %";
                    }

                };
                final ProgressDialog dialog = new ProgressDialog(pg, UIOManager.BUTTONS_HIDE_CANCEL | UIOManager.BUTTONS_HIDE_OK, _AWU.T.download_title(), _AWU.T.download_msg(), AWUTheme.getInstance().getIcon("download", 32)) {
                    /**
             * 
             */
                    protected boolean isLabelEnabled() {
                        // TODO Auto-generated method stub
                        return true;
                    }
                };
                try {
                    Dialog.getInstance().showDialog(dialog);
                } catch (DialogCanceledException ee) {

                } catch (DialogClosedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        };
    }
}
