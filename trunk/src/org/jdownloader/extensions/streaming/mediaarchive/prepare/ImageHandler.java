package org.jdownloader.extensions.streaming.mediaarchive.prepare;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import jd.plugins.DownloadLink;

import org.jdownloader.controlling.UniqueAlltimeID;
import org.jdownloader.extensions.streaming.StreamingExtension;
import org.jdownloader.extensions.streaming.mediaarchive.ImageMediaItem;

public class ImageHandler extends ExtensionHandler<ImageMediaItem> {

    @Override
    public ImageMediaItem handle(StreamingExtension extension, DownloadLink dl) {
        ImageMediaItem ret = new ImageMediaItem(dl);

        String id = new UniqueAlltimeID().toString();
        String streamurl = extension.createStreamUrl(id, "imagereader", null);
        try {
            extension.addDownloadLink(id, dl);

            BufferedImage image = ImageIO.read(new URL(streamurl).openStream());
            System.out.println(image.getWidth() + " - " + image.getHeight());
            ret.setWidth(image.getWidth());
            ret.setHeight(image.getHeight());

            return ret;
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            extension.removeDownloadLink(id);
        }
        return null;

    }

}
