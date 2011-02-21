package org.appwork.txtresource;

import java.lang.reflect.Proxy;
import java.util.HashMap;

public class TranslationFactory {

    private static final HashMap<String, TranslateInterface> CACHE = new HashMap<String, TranslateInterface>();

    /**
     * @param class1
     * @return
     */
    public static <T extends TranslateInterface> T create(final Class<T> class1) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * do not call this directly for each translationrequest. use a static cahe
     * instead!
     * 
     * @param class1
     * @return
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
                ret = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { class1 }, new TranslationHandler(class1, lookup));
                TranslationFactory.CACHE.put(id, ret);
            }
            return ret;
        }

    }

    public static void main(final String[] args) {

        final Translate t = TranslationFactory.create(Translate.class, "de", "en");

        System.out.println(t.getTestText());

        System.out.println(t.getOrderedText(1, 7, 23, 5));

        System.err.println(t.createFile());
    }

    private final String name;

    /**
     * @param string
     */
    public TranslationFactory(final String string) {
        name = string;
    }

}
