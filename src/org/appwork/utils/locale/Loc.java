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
import java.io.InputStreamReader;
import java.util.HashMap;

import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;
import org.appwork.utils.storage.DatabaseInterface;

/**
 * This class provides functions to return translated strings
 * 
 * @author Christian
 */
public class Loc {

    private static DatabaseInterface DATABASE;

    /**
     * @param dATABASE
     *            the dATABASE to set
     */
    public static void setDatabase(DatabaseInterface db) {
        DATABASE = db;
    }

    /**
     * The key (String) under which the saved localization-name is stored.
     */
    public static final String PROPERTY_LOCALE = "PROPERTY_LOCALE";

    /**
     * The directory, where all localization files are located.
     */
    private static final File LOCALIZATION_DIR = Application.getRessource("languages/");

    /**
     * The name of the default localization file. This is the english language.
     */
    private static final String DEFAULE_LOCALIZATION_NAME = "en";

    /**
     * The default localization file. This is the english language.
     */
    private static final File DEFAULT_LOCALIZATION = new File(LOCALIZATION_DIR, DEFAULE_LOCALIZATION_NAME + ".loc");

    /**
     * The HashMap which contains all hashcodes of the keys and their translated
     * values.
     * 
     * @see Loc#parseLocalization(RFSFile)
     */
    private static HashMap<Integer, String> data = null;

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
    public static String L(String key, String def) {
        if (key == null || (key = key.trim()).length() == 0) throw new IllegalArgumentException();
        if (data == null) {
            Log.L.warning("No parsed localization found! Loading now from saved localization file!");
            try {
                Loc.setLocale(DATABASE.get(PROPERTY_LOCALE, DEFAULE_LOCALIZATION_NAME));
            } catch (Exception e) {
                Log.L.severe("Error while loading the stored localization name!");
                Loc.setLocale(DEFAULE_LOCALIZATION_NAME);
            }
            if (data == null) return "Error in Loc! No loaded data!";
        }

        String loc = data.get(key.toLowerCase().hashCode());
        if (loc == null) {
            data.put(key.toLowerCase().hashCode(), def);
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
    public static String LF(String key, String def, Object... args) {
        try {
            return String.format(Loc.L(key, def), args);
        } catch (Exception e) {
            return "Error: " + key;
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
        File file = new File(LOCALIZATION_DIR, loc + ".loc");
        if (file != null && file.exists()) {
            Loc.parseLocalization(file);
        } else {
            Log.L.info("The language " + loc + " isn't available! Parsing default (en.loc) one!");
            Loc.parseLocalization(DEFAULT_LOCALIZATION);
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
     * @see Loc#data
     */
    public static void parseLocalization(File file) throws IllegalArgumentException {
        if (file == null || !file.exists()) throw new IllegalArgumentException();

        if (data != null) Log.L.finer("Previous HashMap will be overwritten!");
        data = new HashMap<Integer, String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            String line;
            String key;
            String value;
            int split;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;

                if ((split = line.indexOf('=')) <= 0) continue;

                key = line.substring(0, split).toLowerCase().trim();
                value = line.substring(split + 1).trim();
                value = value.replace("\\n", "\n").replace("\\r", "\r");

                data.put(key.hashCode(), value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns a localized regular expression for words that usualy ar present
     * in an error message
     * 
     * @return
     */
    public static String getErrorRegex() {
        return L("system.error", ".*(error|failed).*");
    }

}
