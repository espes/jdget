/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.swing.ExtJDialog;
import org.appwork.utils.swing.EDTRunner;

public class InternDialog<T> extends ExtJDialog {

    /**
     * 
     */
    private final AbstractDialog<T> dialogModel;
    /**
     * 
     */
    private static final long       serialVersionUID = 1L;

    public InternDialog(final AbstractDialog<T> abstractDialog, final ModalityType modality) {
        super(abstractDialog.getOwner(), modality);
        dialogModel = abstractDialog;

        setLayout(new MigLayout("ins 5", "[]", "[fill,grow][]"));
        // JPanel contentPane;
        // setContentPane(contentPane = new JPanel());
        List<? extends Image> iconlist = Dialog.getInstance().getIconList();
        if (iconlist == null) {
            iconlist = dialogModel.getIconList();
        }
        if (iconlist != null) {
            setIconImages(iconlist);
        } else {
            if (getOwner() == null) {
                final ArrayList<Image> l = new ArrayList<Image>();
                l.add(AWUTheme.I().getImage("info", 16));
                l.add(AWUTheme.I().getImage("info", 32));

                setIconImages(l);
            }
        }

    }

    public void setTitle(final String title) {
        super.setTitle(title);
        // if(getName()==null) {
        setName(title);
        // }
    }

    public AbstractDialog<T> getDialogModel() {
        return dialogModel;
    }

    @Override
    public void dispose() {

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                InternDialog.this.dialogModel.setDisposed(true);
                dialogModel.dispose();
                InternDialog.super.dispose();
            }
        }.waitForEDT();

    }

    @Override
    public Dimension getPreferredSize() {
        return dialogModel.getPreferredSize();

    }

    public Dimension getRawPreferredSize() {
       
        return super.getPreferredSize();

    }

    /**
     * 
     */
    public void realDispose() {
        super.dispose();

    }

    // @Override
    // public void setLayout(final LayoutManager manager) {
    // super.setLayout(manager);
    // }
}