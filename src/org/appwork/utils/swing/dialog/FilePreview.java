/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.Files;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.swing.EDTHelper;

/**
 * @author thomas
 */
public class FilePreview extends JPanel implements PropertyChangeListener {

    private static final long  serialVersionUID = 68064282036848471L;

    private final JFileChooser fileChooser;
    private final JPanel       panel;
    private final JLabel       label;

    private File               file;

    public FilePreview(final JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        this.fileChooser.addPropertyChangeListener(this);

        this.panel = new JPanel(new MigLayout("ins 5", "[grow,fill]", "[grow,fill]"));
        this.panel.add(this.label = new JLabel());

        this.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        this.add(new JScrollPane(this.panel), "hidemode 3,gapleft 5");
        this.setPreferredSize(new Dimension(200, 100));
    }

    public void propertyChange(final PropertyChangeEvent e) {
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            this.file = (File) e.getNewValue();
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            this.file = (File) e.getNewValue();
        }
        new Thread() {
            @Override
            public void run() {
                FilePreview.this.update();
            }
        }.start();
    }

    private void update() {
        if (this.file != null && this.file.isFile()) {
            try {
                final String ext = Files.getExtension(this.file.getName());
                if (ext != null) {
                    BufferedImage image = null;

                    if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("gif")) {
                        image = ImageProvider.read(this.file);
                    }

                    if (image != null) {
                        final ImageIcon ii = new ImageIcon(ImageProvider.scaleBufferedImage(image, 160, 160));
                        new EDTHelper<Object>() {

                            @Override
                            public Object edtRun() {
                                FilePreview.this.label.setIcon(ii);
                                final int w = FilePreview.this.fileChooser.getWidth() / 3;
                                FilePreview.this.setPreferredSize(new Dimension(w, 100));
                                FilePreview.this.fileChooser.revalidate();
                                return null;
                            }

                        }.start();
                        return;

                    }
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }

        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                FilePreview.this.label.setIcon(null);
                FilePreview.this.label.setText("");
                FilePreview.this.setPreferredSize(new Dimension(0, 0));
                FilePreview.this.fileChooser.revalidate();
                return null;
            }

        }.start();
    }
}
