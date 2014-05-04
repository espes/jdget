package org.appwork.txtresource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.logging.Log;

public class TranslationFactory {

    private static final HashMap<String, TranslateInterface> CACHE    = new HashMap<String, TranslateInterface>();
    private static String                                    language = System.getProperty("user.language").toLowerCase();

    /**
     * @param string
     * @param ret
     */
    private static void collectByPath(final File path, final HashSet<String> ret) {
        final java.util.List<File> files = Files.getFiles(new FileFilter() {

            @Override
            public boolean accept(final File pathname) {
                return pathname.getName().endsWith(".lng");
            }
        }, path);
        String name;

        if (files != null) {
            for (final File file : files) {
                try {
                    name = file.getName();
                    final int index = name.indexOf(".");
                    if (index < 0 || index >= name.length() - 4) {
                        continue;
                    }
                    name = name.substring(index + 1, name.length() - 4);

                    if (ret.add(name)) {
                        Log.L.info(name + " found in " + file);
                    }
                } catch (final Throwable e) {
                    // Invalid LanguageFile nameing
                }
            }
        }
    }

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
                ret = (T) Proxy.newProxyInstance(class1.getClassLoader(), new Class[] { class1 }, new TranslationHandler(class1, lookup));
                TranslationFactory.CACHE.put(id, ret);
            }

            return ret;
        }

    }

    /**
     * @param ret2
     * @param string
     * @return
     */
    private static void findInClassPath(final String path, final HashSet<String> ret) {

        // Search in jar:
        try {

            Enumeration<URL> resources;

            resources = Thread.currentThread().getContextClassLoader().getResources(path);
            String name, p, jarPath, internPath;
            while (resources.hasMoreElements()) {

                final URL url = resources.nextElement();
                if (url.getProtocol().equalsIgnoreCase("jar")) {
                    p = url.getPath();
                    int index = p.lastIndexOf('!');
                    jarPath = p.substring(0, index);
                    internPath = p.substring(index + 2);

                    final JarInputStream jarFile = new JarInputStream(new FileInputStream(new File(new URL(jarPath).toURI())));
                    JarEntry e;

                    String jarName;
                    while ((e = jarFile.getNextJarEntry()) != null) {
                        jarName = e.getName();
                        if (jarName.startsWith(internPath) && jarName.endsWith(".lng")) {
                            name = new File(jarName).getName();
                            index = name.indexOf(".");
                            if (index < 0 || index >= name.length() - 4) {
                                continue;
                            }
                            name = name.substring(index + 1, name.length() - 4);

                            if (ret.add(name)) {
                                Log.L.finer(name + " found in " + new File(jarName));
                            }
                        }
                    }
                } else {
                    TranslationFactory.collectByPath(new File(url.toURI()), ret);

                }

            }
        } catch (final Exception e) {
            Log.exception(e);
        }

    }

    /**
     * 
     */
    public static java.util.List<TranslateInterface> getCachedInterfaces() {
        final HashSet<TranslateInterface> ret = new HashSet<TranslateInterface>();
        synchronized (TranslationFactory.CACHE) {
            for (final TranslateInterface intf : TranslationFactory.CACHE.values()) {
                if (intf != null) {
                    ret.add(intf);
                }
            }
        }
        return new ArrayList<TranslateInterface>(ret);

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

    public static List<String> listAvailableTranslations(final Class<? extends TranslateInterface>... classes) {

        final HashSet<String> ret = new HashSet<String>();

        TranslationFactory.collectByPath(Application.getResource("translations"), ret);
        TranslationFactory.findInClassPath("translations", ret);
        for (final Class<? extends TranslateInterface> clazz : classes) {
            TranslationFactory.collectByPath(Application.getResource(clazz.getPackage().getName().replace(".", "/")), ret);
            TranslationFactory.findInClassPath(clazz.getPackage().getName().replace(".", "/"), ret);
            final Defaults defs = clazz.getAnnotation(Defaults.class);
            if (defs != null) {
                for (final String s : defs.lngs()) {

                    if (ret.add(s)) {
                        Log.L.finer(s + " src: " + clazz + " Defaults");
                    }
                }
            }

        }

        return new ArrayList<String>(ret);
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

        synchronized (TranslationFactory.CACHE) {
            for (final TranslateInterface i : TranslationFactory.CACHE.values()) {
                i._getHandler().setLanguage(loc);
            }
        }
        return true;
    }

    /**
     * @param lng
     * @return
     */
    public static Locale stringToLocale(final String lng) {
        try {
            if (Application.getJavaVersion() >= Application.JAVA17) { return Locale.forLanguageTag(lng.replace("_", "-")); }
        } catch (final Throwable e) {
        }
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
