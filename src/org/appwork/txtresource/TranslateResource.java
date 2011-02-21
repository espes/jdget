/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.txtresource
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.txtresource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;

import org.appwork.storage.JSonStorage;

/**
 * @author thomas
 * 
 */
public class TranslateResource {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final URL         url;
    private TranslateData     data;
    private final String      name;

    /**
     * @param url
     */
    public TranslateResource(final URL url, final String name) {
        this.url = url;
        this.name = name;

    }

    public TranslateData getData() throws UnsupportedEncodingException, IOException {
        synchronized (this) {
            if (data == null) {
                if (url != null) {
                    final String txt = read(url);
                    data = JSonStorage.restoreFromString(txt, TranslateData.class);
                }
            }
            return data;

        }
    }

    /**
     * @param method
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public String getEntry(final Method method) throws UnsupportedEncodingException, IOException {
        final String ret = getData().get(method.getName());
        if (ret == null) {
            // check Annotations. this is slow, but ok for dev enviroment.
            // for release, we always should create real translation files
            // instead of using the annotations
            final Default lngAn = method.getAnnotation(Default.class);
            for (int i = 0; i < lngAn.lngs().length; i++) {
                if (lngAn.lngs()[i].equals(name)) { return lngAn.values()[i]; }
            }
        }
        return ret;

    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param url
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private String read(final URL url) throws UnsupportedEncodingException, IOException {

        BufferedReader f = null;
        InputStreamReader isr = null;
        try {
            f = new BufferedReader(isr = new InputStreamReader(url.openStream(), "UTF8"));
            String line;
            final StringBuilder ret = new StringBuilder();
            final String sep = System.getProperty("line.separator");
            while ((line = f.readLine()) != null) {
                // ignore comments
                if (line.trim().startsWith("//")) {
                    continue;
                }
                if (ret.length() > 0) {
                    ret.append(sep);
                }

                ret.append(line);
            }
            return ret.toString();
        } finally {
            try {
                f.close();
            } catch (final Throwable e) {
            }
            try {
                isr.close();
            } catch (final Throwable e) {
            }
        }
    }
}
