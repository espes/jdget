/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.locale
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storage;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

/**
 * This class provides functions to return translated strings
 * 
 * @author Christian
 */
public class Loc {
    /**
     * The directory, where all localization files are located. A_ because this
     * the order is important.
     */
    public static final File                A_LOCALIZATION_DIR   = Application.getRessource("languages/");

    public static final Storage             CFG                  = JSonStorage.getStorage("Locale");
    /**
     * The HashMap which contains all hashcodes of the keys and their translated
     * values.
     * 
     * @see Loc#parseLocalization(RFSFile)
     */
    private static HashMap<Integer, String> DATA                 = null;

    private static String                   DEFAULT_LOCALE_CACHE;

    /**
     * The default localization file. This is the english language.
     */
    private static final File               DEFAULT_LOCALIZATION = new File(Loc.A_LOCALIZATION_DIR, Loc.getDefaultLocale() + ".loc");

    /**
     * The name of the default localization file. This is the english language.
     */
    private static final String             FALLBACK_LOCALE      = "en_GB";

    private static String                   locale;

    /**
     * The key (String) under which the saved localization-name is stored.
     */
    public static final String              PROPERTY_LOCALE      = "PROPERTY_LOCALE2";

    public static String _(final Translate t) {
        return t.s();
    }

    public static String _(final Translate t, final Object... parameter) {
        return t.s(parameter);
    }

    /**
     * @return
     */
    private static String getDefaultLocale() {
        if (Loc.DEFAULT_LOCALE_CACHE != null) { return Loc.DEFAULT_LOCALE_CACHE; }
        final String sys = System.getProperty("user.language").toLowerCase();
        final String cou = System.getProperty("user.country").toUpperCase();

        final String[] locs = Loc.getLocales();
        if (locs.length == 0) {
            Loc.DEFAULT_LOCALE_CACHE = Loc.FALLBACK_LOCALE;

        }
        if (Loc.DEFAULT_LOCALE_CACHE == null) {
            for (final String l : locs) {

                if (l.equals(sys + "_" + cou)) {
                    Loc.DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }
        if (Loc.DEFAULT_LOCALE_CACHE == null) {
            for (final String l : locs) {

                if (l.equals(sys)) {
                    Loc.DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }
        if (Loc.DEFAULT_LOCALE_CACHE == null) {
            for (final String l : locs) {
                if (l.startsWith(sys + "_")) {
                    Loc.DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }

        if (Loc.DEFAULT_LOCALE_CACHE == null) {
            for (final String l : locs) {
                if (l.equals(Loc.FALLBACK_LOCALE)) {
                    Loc.DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }
        if (Loc.DEFAULT_LOCALE_CACHE == null) {
            Loc.DEFAULT_LOCALE_CACHE = locs[0];
        }
        return Loc.DEFAULT_LOCALE_CACHE;
    }

    /**
     * Returns a localized regular expression for words that usualy ar present
     * in an error message
     * 
     * @return
     */
    public static String getErrorRegex() {
        return Loc.L("system.error", ".*(error|failed).*");
    }

    /**
     * @return
     */
    public static String getLocale() {

        return Loc.locale;
    }

    /**
     * @return
     */
    public static String[] getLocales() {
        final String[] files = Loc.A_LOCALIZATION_DIR.list(new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                return name.endsWith(".loc");

            }

        });
        if (files == null) { return new String[] {}; }
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace(".loc", "");
        }
        return files;
    }

    /**
     * Returns the translated value for the translation-key. If the current
     * language file doesn't contain the translated value, the default value
     * will be returned.
     * 
     * @param key
     *            key for the translation in the language file. the key should
     *            <b>always</b> have the following structure
     *            <i>PACKAGE_NAME_FROM_CALLER.CLASS_NAME_FROM_CALLER.key</i>
     * @param def
     *            default value which will be returned if there is no mapping
     *            for the key
     * @return translated value or the def parameter
     * @see Loc#LF(String, String, Object...)
     * @throws IllegalArgumentException
     *             if the key is null or is empty
     */
    public static String L(String key, final String def) {
        if ((key == null) || ((key = key.trim()).length() == 0)) { throw new IllegalArgumentException(); }
        if (Loc.DATA == null) {
            Log.L.warning("No parsed localization found! Loading now from saved localization file!");
            try {
                Loc.setLocale(Loc.CFG.get(Loc.PROPERTY_LOCALE, Loc.FALLBACK_LOCALE));
            } catch (final Exception e) {

                Log.L.severe("Error while loading the stored localization name!");
                Loc.setLocale(Loc.FALLBACK_LOCALE);
            }
            if (Loc.DATA == null) { return def == null ? "Error in Loc! No loaded data!" : def; }
        }

        final String loc = Loc.DATA.get(key.toLowerCase().hashCode());
        if (loc == null) {
            Loc.DATA.put(key.toLowerCase().hashCode(), def);
            return def;
        }
        return loc;
    }

    /**
     * Returns the translated value for the translation-key filled with the
     * parameters.
     * 
     * @param key
     *            key for the translation in the language file. the key should
     *            <b>always</b> have the following structure
     *            <i>PACKAGE_NAME_FROM_CALLER.CLASS_NAME_FROM_CALLER.key</i>
     * @param def
     *            default value which will be returned if there is no mapping
     *            for the key
     * @param args
     *            parameters which should be inserted in the translated string
     * @return translated value or the def parameter filled with the parameters
     * @see Loc#L(String, String)
     */
    public static String LF(final String key, final String def, final Object... args) {
        try {
            return String.format(Loc.L(key, def), args);
        } catch (final Exception e) {
            return "Error: " + key;
        }
    }

    /**
     * Creates a HashMap with the data obtained from the localization file. <br>
     * <b>Warning:</b> Overwrites any previously created HashMap
     * 
     * @param file
     *            {@link RFSFile} object to the localization file
     * @throws IllegalArgumentException
     *             if the parameter is null or doesn't exist
     * @see Loc#DATA
     */
    public static void parseLocalization(final File file) throws IllegalArgumentException {
        if ((file == null) || !file.exists()) { throw new IllegalArgumentException(); }

        if (Loc.DATA != null) {
            Log.L.finer("Previous HashMap will be overwritten!");
        }
        Loc.DATA = new HashMap<Integer, String>();

        BufferedReader reader = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            reader = new BufferedReader(isr = new InputStreamReader(fis = new FileInputStream(file), "UTF8"));

            String line;
            String key;
            String value;
            int split;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                if ((split = line.indexOf('=')) <= 0) {
                    continue;
                }

                key = line.substring(0, split).toLowerCase().trim();
                value = line.substring(split + 1).trim();
                value = value.replace("\\n", "\n").replace("\\r", "\r");

                Loc.DATA.put(key.hashCode(), value);
            }
        } catch (final Exception e) {
            org.appwork.utils.logging.Log.exception(e);
        } finally {
            try {
                reader.close();
            } catch (Throwable e) {
            }
            try {
                isr.close();
            } catch (Throwable e) {
            }
            try {
                fis.close();
            } catch (Throwable e) {
            }
        }
    }

    /**
     * Set-up this class by creating the HashMap for the key-string-pairs.
     * 
     * @param loc
     *            name of the localization file
     * @see Loc#parseLocalization(RFSFile)
     */
    public static void setLocale(String loc) {
        try {
            if (loc == null) {

                loc = Loc.CFG.get(Loc.PROPERTY_LOCALE, Loc.getDefaultLocale());
            }
            final File file = new File(Loc.A_LOCALIZATION_DIR, loc + ".loc");
            Loc.locale = loc;
            if ((file != null) && file.exists()) {

                final String[] locs = loc.split("_");
                if (locs.length == 1) {
                    Locale.setDefault(new Locale(locs[0]));
                } else {
                    Locale.setDefault(new Locale(locs[0], locs[1]));
                }
                Loc.CFG.put(Loc.PROPERTY_LOCALE, loc);
                Loc.parseLocalization(file);
            } else {
                Log.L.info("The language " + loc + " isn't available! Parsing default (" + Loc.FALLBACK_LOCALE + ".loc) one!");
                Loc.locale = Loc.getDefaultLocale();
                final String[] locs = Loc.locale.split("_");
                Locale.setDefault(new Locale(locs[0], locs[1]));
                Loc.parseLocalization(Loc.DEFAULT_LOCALIZATION);
            }
        } catch (final Exception e) {
            org.appwork.utils.logging.Log.exception(e);
        }
    }

}
