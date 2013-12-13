package org.jdownloader.myjdownloader.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public abstract class AbstractMyJDClientForDesktopJVM extends AbstractMyJDClientForBasicJVM {

    public AbstractMyJDClientForDesktopJVM(final String appKey) {
        super(appKey);

    }
    @Override
    public String urlencode(final String text) throws MyJDownloaderException{
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw MyJDownloaderException.get(e);

        }
    }

}
