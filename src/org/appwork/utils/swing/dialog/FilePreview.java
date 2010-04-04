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

import javax.imageio.ImageIO;
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
 * 
 */
public class FilePreview extends JPanel implements PropertyChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = 68064282036848471L;
    private File file;
    private JLabel label;
    private JPanel panel;
    private JFileChooser fileChooser;
    private JScrollPane sp;

    public FilePreview(JFileChooser fc) {
        this.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        add(sp = new JScrollPane(panel = new JPanel(new MigLayout("ins 5", "[grow,fill]", "[grow,fill]"))), "hidemode 3,gapleft 5");
        panel.add(label = new JLabel());
        this.setPreferredSize(new Dimension(200, 100));
        fc.addPropertyChangeListener(this);
        this.fileChooser = fc;

    }

    public void propertyChange(PropertyChangeEvent e) {

        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            file = (File) e.getNewValue();

        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            file = (File) e.getNewValue();

        }
        new Thread() {
            public void run() {
                update();
            }
        }.start();

    }

    /**
     * 
     */
    private void update() {
        if (file != null && file.isFile()) {

            try {
                String ext = Files.getExtension(file.getName());
                BufferedImage image = null;

                if (ext.equalsIgnoreCase("png")) {

                    image = ImageIO.read(file);

                } else if (ext.equalsIgnoreCase("jpg")) {
                    image = ImageIO.read(file);
                } else if (ext.equalsIgnoreCase("gif")) {
                    image = ImageIO.read(file);
                }
                final ImageIcon ii = new ImageIcon(ImageProvider.scaleBufferedImage(image, 160, 160));
                if (image != null) {
                    new EDTHelper<Object>() {

                        @Override
                        public Object edtRun() {
                            label.setIcon(ii);
                            // sp.setVisible(true);
                            int w = fileChooser.getWidth() / 3;
                            setPreferredSize(new Dimension(w, 100));
                            fileChooser.revalidate();
                            return null;
                        }

                    }.start();
                    return;

                }
            } catch (Exception e) {

            }
        }
        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                label.setIcon(null);
                label.setText("");
                setPreferredSize(new Dimension(0, 0));
                fileChooser.revalidate();
                return null;
            }

        }.start();
    }
}
