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
        this.tInterface = class1;
        this.methods = this.tInterface.getDeclaredMethods();
        this.cache = new HashMap<Method, String>();
        this.resourceCache = new HashMap<String, TranslateResource>();
        this.lookup = this.fillLookup(lookup);

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
    public String createFile(final String string, final boolean addComments) {

        final TranslateData map = new TranslateData();
        this.cache.clear();
        this.lookup = this.fillLookup(string);
        for (final Method m : this.tInterface.getDeclaredMethods()) {
            try {
                map.put(m.getName(), this.invoke(null, m, null).toString());

            } catch (final Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String ret = JSonStorage.serializeToJson(map);
        if (addComments) {
            for (final Method m : this.tInterface.getDeclaredMethods()) {
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
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private TranslateResource createTranslationResource(final String string) throws IOException, InstantiationException, IllegalAccessException {
        TranslateResource ret = this.resourceCache.get(string);
        if (ret != null) { return ret; }
        DynamicResourcePath rPath = tInterface.getAnnotation(DynamicResourcePath.class);
        final String path = rPath != null ? rPath.value().newInstance().getPath() + "." + string + ".lng" : this.tInterface.getName().replace(".", "/") + "." + string + ".lng";
        final URL url = Application.getRessourceURL(path, false);
        miss: if (url == null) {
            final Defaults ann = this.tInterface.getAnnotation(Defaults.class);
            if (ann != null) {
                for (final String d : ann.lngs()) {
                    if (d.equals(string)) {
                        // defaults
                        Log.L.info("Translation file missing:" + path + "Use Annotation Dev fallback");
                        break miss;
                    }
                }

            }
            // throw new NullPointerException("Missing Translation: " + path);
        }
        ret = new TranslateResource(url, string);
        this.resourceCache.put(string, ret);
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
                res = this.createTranslationResource(o);
                ret.add(res);
            } catch (final NullPointerException e) {
                Log.L.warning(e.getMessage());

            } catch (final Throwable e) {
                Log.exception(Level.WARNING, e);
            }
        }
        if (!containsDefault) {
            try {
                res = this.createTranslationResource(TranslationHandler.DEFAULT);
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
                ret = ret.replace("%s" + i, o == null ? "null" : o.toString());
            }
        }
        return ret;
    }

    /**
     * @param method
     * @return
     */
    public String getDefault(final Method method) {
        final TranslateResource res = this.resourceCache.get(TranslationHandler.DEFAULT);

        return res.readDefaults(method);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */

    /**
     * @return
     */
    public Method[] getMethods() {
        return this.methods;
    }

    /**
     * @param method
     * @return
     */
    public String getTranslation(final Method method, final Object... params) {
        return this.getValue(method, this.lookup);
    }

    /**
     * @param string
     * @param string2
     * @return
     */
    public String getTranslation(final String languageKey, final String methodname, final Object... params) {

        final Class<?>[] types = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {

            types[i] = params[i].getClass();
        }
        for (final Method m : this.methods) {
            if (m.getName().equals(methodname)) {
                if (this.checkTypes(m, types)) {
                    final String ret = this.getValue(m, this.fillLookup(languageKey));
                    return this.format(ret, params);
                }
            }
        }
        return null;

    }

    public String getValue(final Method method, final ArrayList<TranslateResource> lookup) {

        String ret = null;
        TranslateResource res;

        for (final Iterator<TranslateResource> it = lookup.iterator(); it.hasNext();) {
            res = it.next();
            try {
                ret = res.getEntry(method);
                if (ret != null) { return ret; }
            } catch (final Throwable e) {
                Log.L.warning("Exception in translation: " + this.tInterface.getName() + "." + res.getName());
                Log.exception(Level.WARNING, e);
            }

        }

        if (ret == null) {
            ret = this.tInterface.getSimpleName() + "." + method.getName();

        }

        return ret;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final ArrayList<TranslateResource> lookup = this.lookup;
        // for speed reasons let all controller methods (@see
        // TRanslationINterface.java) start with _
        if (method.getDeclaringClass() == Object.class) { return method.invoke(this, args); }
        // else if (method.getName().startsWith("_")) {
        if (method.getName().equals("_getHandler")) { return this; }
        // if (method.getName().equals("_getSupportedLanguages")) {
        //
        // return TranslationFactory.listAvailableTranslations(this.tInterface);
        // }
        // if (method.getName().equals("_setLanguage")) {

        //
        // if (method.getName().equals("_getTranslation")) {

        String ret = this.cache.get(method);
        if (ret == null) {
            ret = this.getValue(method, lookup);
            if (ret != null) {
                this.cache.put(method, ret);
            }
        }
        return this.format(ret, args);

    }

    /**
     * Tells the TranslationHandler to use this language from now on.clears
     * 
     * cache.
     * 
     * for speed reasons, cache access is not synchronized
     * 
     * @param loc
     */
    public void setLanguage(final String loc) {
        this.cache.clear();
        this.resourceCache.clear();
        this.lookup = this.fillLookup(loc);

    }
}
