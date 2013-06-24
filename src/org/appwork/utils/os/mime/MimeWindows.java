/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.os.mime
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.os.mime;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.appwork.utils.Application;
import org.appwork.utils.ImageProvider.ImageProvider;

import sun.awt.shell.ShellFolder;

public class MimeWindows extends MimeDefault {

    @Override
    public ImageIcon getFileIcon(final String extension, final int width, final int height) throws IOException {
        final String iconKey = super.getIconKey(extension, width, height);
        ImageIcon ret = super.getCacheIcon(iconKey);
        if (ret == null) {
            final File path = Application.getResource("tmp/images/" + extension + ".png");
            if (path.getParentFile().isDirectory()) {
                // woraround a bug we had until 24.06.2013.. created folders
                // instead of files
                path.getParentFile().delete();
            }
            if (!path.getParentFile().exists()) {
                path.getParentFile().mkdirs();
            }
            try {
                if (path.exists() && path.isFile()) {
                    ret = new ImageIcon(ImageProvider.read(path));
                } else {
                    File file = null;
                    FileOutputStream fos = null;
                    try {
                        file = File.createTempFile("icon", "." + extension);
                        final ShellFolder shellFolder = ShellFolder.getShellFolder(file);
                        ret = new ImageIcon(shellFolder.getIcon(true));
                        fos = new FileOutputStream(path);
                        ImageIO.write((RenderedImage) ret.getImage(), "png", fos);
                    } catch (final Throwable e) {
                        ret = ImageProvider.toImageIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
                    } finally {
                        try {
                            fos.close();
                        } catch (final Throwable e) {
                        }
                        if (file != null) {
                            file.delete();
                        }
                    }
                    if (ret == null || ret.getIconWidth() < width || ret.getIconHeight() < height) {
                        ret = super.getFileIcon(extension, width, height);
                    }
                }
            } catch (final Throwable e) {
                return null;
            }
        }
        ret = ImageProvider.scaleImageIcon(ret, width, height);
        super.saveIconCache(iconKey, ret);
        return ret;
    }
}