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
    private ArrayList<TranslateResource>              lookup;

    private final HashMap<Method, String>             cache;
    private final Method[]                            methods;
    private final HashMap<String, TranslateResource>  resourceCache;

    public static final String                        DEFAULT = "en";

    /**
     * @param class1
     * @param lookup
     * @throws IOException
     */
    public TranslationHandler(final Class<? extends TranslateInterface> class1, final String[] lookup) {
        tInterface = class1;
        methods = tInterface.getDeclaredMethods();
        cache = new HashMap<Method, String>();
        resourceCache = new HashMap<String, TranslateResource>();
        this.lookup = fillLookup(lookup);

    }

    /**
     * @param m
     * @param types
     * @return
     */
    private boolean checkTypes(final Method m, final Class<?>[] types) {
        final Class<?>[] parameters = m.getParameterTypes();
        if (parameters.length != types.length) { return false; }
        if (types.length == 0) { return true; }
        for (int i = 0; i < types.length; i++) {
            if (types[i] != parameters[i]) {
                if (Number.class.isAssignableFrom(types[i])) {
                    if (parameters[i] == int.class || parameters[i] == long.class || parameters[i] == double.class || parameters[i] == float.class || parameters[i] == byte.class || parameters[i] == char.class) {
                        continue;
                    } else {
                        return false;
                    }

                } else if (types[i] == Boolean.class && parameters[i] == boolean.class) { return true; }
                return false;
            }

        }
        return true;
    }

    /**
     * @param string
     * @param addComments
     * @return
     */
    private String createFile(final String string, final boolean addComments) {

        final TranslateData map = new TranslateData();
        cache.clear();
        lookup = fillLookup(string);
        for (final Method m : tInterface.getDeclaredMethods()) {
            try {
                map.put(m.getName(), invoke(null, m, null).toString());

            } catch (final Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String ret = JSonStorage.serializeToJson(map);
        if (addComments) {
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
        }

        return ret;
    }

    /**
     * @param string
     * @return
     * @throws IOException
     */
    private TranslateResource createTranslationResource(final String string) throws IOException {
        TranslateResource ret = resourceCache.get(string);
        if (ret != null) { return ret; }
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
        ret = new TranslateResource(url, string);
        resourceCache.put(string, ret);
        return ret;

    }

    /**
     * @param lookup2
     * @return
     */
    private ArrayList<TranslateResource> fillLookup(final String... lookup) {

        final ArrayList<TranslateResource> ret = new ArrayList<TranslateResource>();
        TranslateResource res;
        boolean containsDefault = false;
        for (final String o : lookup) {
            try {
                if (TranslationHandler.DEFAULT.equals(o)) {
                    containsDefault = true;
                }
                res = createTranslationResource(o);
                ret.add(res);
            } catch (final Throwable e) {
                Log.exception(Level.WARNING, e);
            }
        }
        if (!containsDefault) {
            try {
                res = createTranslationResource(TranslationHandler.DEFAULT);
                ret.add(res);
            } catch (final Throwable e) {
                Log.exception(Level.WARNING, e);
            }
        }
        return ret;
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

    public String getValue(final Method method, final ArrayList<TranslateResource> lookup) {

        String ret = null;
        TranslateResource res;

        for (final Iterator<TranslateResource> it = lookup.iterator(); it.hasNext();) {
            res = it.next();
            try {
                ret = res.getEntry(method);
                return ret;
            } catch (final Throwable e) {
                Log.L.warning("Exception in translation: " + tInterface.getName() + "." + res.getName());
                Log.exception(Level.WARNING, e);
                it.remove();
            }

        }
        if (ret == null) {
            ret = tInterface.getSimpleName() + "." + method.getName().substring(3);

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

        final ArrayList<TranslateResource> lookup = this.lookup;
        // for speed reasons let all controller methods (@see
        // TRanslationINterface.java) start with _
        if (method.getName().startsWith("_")) {
            if (method.getName().equals("_createFile")) { return createFile(args[0] + "", (Boolean) args[1]); }
            if (method.getName().equals("_getTranslation")) {
                final String methodname = args[1] + "";
                final Object[] params = (Object[]) args[2];
                final Class<?>[] types = new Class<?>[params.length];
                for (int i = 0; i < params.length; i++) {

                    types[i] = params[i].getClass();
                }
                for (final Method m : methods) {
                    if (m.getName().equals(methodname)) {
                        if (checkTypes(m, types)) {
                            final String ret = getValue(m, fillLookup(args[0] + ""));
                            return format(ret, params);
                        }
                    }
                }

            }
        }

        String ret = cache.get(method);
        if (ret == null) {
            ret = getValue(method, lookup);
            if (ret != null) {
                cache.put(method, ret);
            }
        }
        return format(ret, args);

    }
}
