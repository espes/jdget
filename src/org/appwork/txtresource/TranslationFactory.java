package org.appwork.txtresource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

public class TranslationFactory {

    private static final HashMap<String, TranslateInterface> CACHE    = new HashMap<String, TranslateInterface>();
    private static String                                    language = System.getProperty("user.language").toLowerCase();

    public static <T extends TranslateInterface> T create(final Class<T> class1) {
        return TranslationFactory.create(class1, TranslationFactory.getDesiredLanguage());
    }

    /**
     * do not call this directly for each translationrequest. use a static cache
     * instead!
     */
    @SuppressWarnings("unchecked")
    public static <T extends TranslateInterface> T create(final Class<T> class1, final String... lookup) {

        synchronized (TranslationFactory.CACHE) {
            final StringBuilder sb = new StringBuilder();
            sb.append(class1.getName());
            for (final String c : lookup) {
                sb.append(c + ";");
            }
            final String id = sb.toString();
            T ret = (T) TranslationFactory.CACHE.get(id);
            if (ret == null) {
               
                ret = (T) Proxy.newProxyInstance( class1.getClassLoader(), new Class[] { class1 }, new TranslationHandler(class1, lookup));
                TranslationFactory.CACHE.put(id, ret);
            }

            return ret;
        }

    }

    /**
     * 
     */
    public static ArrayList<TranslateInterface> getCachedInterfaces() {
        final ArrayList<TranslateInterface> ret = new ArrayList<TranslateInterface>();
        Entry<String, TranslateInterface> next;
        for (final Iterator<Entry<String, TranslateInterface>> it = TranslationFactory.CACHE.entrySet().iterator(); it.hasNext();) {
            next = it.next();
            ret.remove(next.getValue());
            ret.add(next.getValue());
        }
        return ret;

    }

    public static String getDesiredLanguage() {
        return TranslationFactory.language;
    }

    /**
     * @return
     */
    public static Locale getDesiredLocale() {
        final String lng = TranslationFactory.getDesiredLanguage();

        return TranslationFactory.stringToLocale(lng);

    }

    public static ArrayList<String> listAvailableTranslations(final Class<? extends TranslateInterface> class1) {

        final String path = class1.getPackage().getName().replace(".", "/");

        final ArrayList<String> ret = new ArrayList<String>();
        final Defaults defs = class1.getAnnotation(Defaults.class);
        if (defs != null) {
            for (final String s : defs.lngs()) {
                ret.add(s);
            }
        }

        // first look out for all translations in filesystem
        String[] files;
        final FilenameFilter namefilter = new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                return name.startsWith(class1.getSimpleName() + ".") && name.endsWith(".lng");
            }

        };

        files = Application.getResource(path).list(namefilter);
        String name, jarPath, internPath, p;
        int index;
        if (files != null) {
            for (final String file : files) {
                name = file.substring(class1.getSimpleName().length() + 1, file.length() - 4);
                ret.remove(name);
                ret.add(name);
            }
        }

        // Search in jar:
        try {

            Enumeration<URL> resources;

            resources = Thread.currentThread().getContextClassLoader().getResources(path);

            while (resources.hasMoreElements()) {

                final URL url = resources.nextElement();
                if (url.getProtocol().equalsIgnoreCase("jar")) {
                    p = url.getPath();
                    index = p.lastIndexOf('!');
                    jarPath = p.substring(0, index);
                    internPath = p.substring(index + 2);

                    final JarInputStream jarFile = new JarInputStream(new FileInputStream(new File(new URL(jarPath).toURI())));
                    JarEntry e;

                    String jarName;
                    while ((e = jarFile.getNextJarEntry()) != null) {
                        jarName = e.getName();
                        if (jarName.startsWith(internPath) && jarName.endsWith(".loc")) {
                            name = new File(jarName).getName();
                            name = name.substring(0, name.length() - 4);
                            ret.remove(name);
                            ret.add(name);
                        }
                    }
                } else {
                    files = new File(url.toURI()).list(namefilter);

                    if (files != null) {
                        for (final String file : files) {
                            name = file.substring(class1.getSimpleName().length() + 1, file.length() - 4);
                            ret.remove(name);
                            ret.add(name);
                        }
                    }
                }

            }
        } catch (final Exception e) {
            Log.exception(e);
        }
        return ret;
    }

    /**
     * @return
     */
    public static String localeToString(final Locale l) {
        final StringBuilder sb = new StringBuilder();
        sb.append(l.getLanguage());
        String c = l.getCountry();
        if (c != null && c.trim().length() > 0) {
            sb.append("-");
            sb.append(l.getCountry());
            c = l.getVariant();
            if (c != null && c.trim().length() > 0) {
                sb.append("-");
                sb.append(l.getCountry());
            }
        }
        return sb.toString();
    }

    public static void main(final String[] args) {
        // Locale.setDefault(TranslationFactory.getDesiredLocale());
        // System.out.println(TranslationFactory.getDesiredLocale().getDisplayCountry());
        // final Translate t = TranslationFactory.create(Translate.class);
        // System.out.println(t.getTestText());
        // System.out.println(t.getOrderedText(1, 7, 23, 5));
        // System.out.println(t._getTranslation("en", "getOrderedText", 1, 3, 5,
        // 8));
        // System.err.println(t._createFile("en", true));

        System.out.println(new Locale("zh", "DE", "hans").getDisplayName());
        System.out.println(Locale.TRADITIONAL_CHINESE.getDisplayName());
    }

    public static boolean setDesiredLanguage(final String loc) {
        if (TranslationFactory.getDesiredLanguage().equals(loc)) { return false; }
        TranslationFactory.language = loc;

        for (final TranslateInterface i : TranslationFactory.CACHE.values()) {
            i._getHandler().setLanguage(loc);
        }
        return true;
    }

    /**
     * @param lng
     * @return
     */
    public static Locale stringToLocale(final String lng) {
        final String[] split = lng.split("[\\-\\_]");
        switch (split.length) {
        case 1:
            return new Locale(split[0]);
        case 2:
            return new Locale(split[0], split[1]);

        default:
            return new Locale(split[0], split[1], split[2]);
        }
    }

}
