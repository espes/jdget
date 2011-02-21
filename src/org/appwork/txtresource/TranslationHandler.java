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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.appwork.storage.JSonStorage;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class TranslationHandler implements InvocationHandler {

    private final Class<? extends TranslateInterface> tInterface;
    private final ArrayList<TranslateResource>        lookup;

    private final HashMap<Method, String>             cache;

    /**
     * @param class1
     * @param lookup
     * @throws IOException
     */
    public TranslationHandler(final Class<? extends TranslateInterface> class1, final String[] lookup) {
        tInterface = class1;

        this.lookup = new ArrayList<TranslateResource>();
        cache = new HashMap<Method, String>();
        TranslateResource res;

        for (final String o : lookup) {
            try {
                res = createTranslationResource(o);
                this.lookup.add(res);
            } catch (final Throwable e) {
                Log.exception(Level.WARNING, e);
            }
        }
        try {
            res = createTranslationResource("en");
            this.lookup.add(res);
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
        }

    }

    /**
     * @return
     */
    private String createFile() {

        final TranslateData map = new TranslateData();
        for (final Method m : tInterface.getDeclaredMethods()) {
            try {
                map.put(m.getName(), invoke(null, m, null).toString());

            } catch (final Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String ret = JSonStorage.serializeToJson(map);

        for (final Method m : tInterface.getDeclaredMethods()) {
            final Default def = m.getAnnotation(Default.class);
            final Description desc = m.getAnnotation(Description.class);

            String comment = "";
            if (desc != null) {
                final String d = desc.value().replaceAll("[\\\r\\\n]+", "\r\n//    ");
                comment += "\r\n// Description:\r\n//    " + d;
            }
            if (def != null) {

                comment += "\r\n// Defaultvalue:\r\n//    " + def.toString().replaceAll("[\\\r\\\n]+", "\r\n//    ");

            }

            //

            if (comment.length() > 0) {
                ret = ret.replace("\"" + m.getName() + "\" : \"", comment + "\r\n\r\n     " + "\"" + m.getName() + "\" : \"");
            }
        }

        return ret;
    }

    /**
     * @param string
     * @return
     * @throws IOException
     */
    private TranslateResource createTranslationResource(final String string) throws IOException {
        final String path = tInterface.getName().replace(".", "/") + "." + string + ".lng";
        final URL url = Application.getRessourceURL(path, false);
        miss: if (url == null) {
            final Defaults ann = tInterface.getAnnotation(Defaults.class);
            if (ann != null) {
                for (final String d : ann.lngs()) {
                    if (d.equals(string)) {
                        // defaults
                        Log.L.warning("Translation file missing:" + path + "Use Annotation Dev fallback");
                        break miss;
                    }
                }

            }
            throw new NullPointerException("Missing Translation: " + path);
        }

        return new TranslateResource(url, string);

    }

    /**
     * @param ret
     * @param args
     * @return
     */
    private String format(String ret, final Object[] args) {
        if (args != null) {
            int i = 0;
            for (final Object o : args) {
                i++;
                ret = ret.replace("%s" + i, o.toString());
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getName().equals("createFile")) { return createFile(); }
        String ret = cache.get(method);
        if (ret == null) {
            TranslateResource res;

            for (final Iterator<TranslateResource> it = lookup.iterator(); it.hasNext();) {
                res = it.next();
                try {
                    ret = res.getEntry(method);
                } catch (final Throwable e) {
                    Log.L.warning("Exception in translation: " + tInterface.getName() + "." + res.getName());
                    Log.exception(Level.WARNING, e);
                    it.remove();
                }
                if (ret == null) {

                }
                if (ret != null) {
                    cache.put(method, ret);
                    break;

                }
            }
            if (ret == null) {
                ret = tInterface.getSimpleName() + "." + method.getName().substring(3);
                cache.put(method, ret);

            }
        }
        return format(ret, args);

    }
}
