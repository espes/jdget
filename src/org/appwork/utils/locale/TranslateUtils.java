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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.parser.SourceParser;

/**
 * @author thomas
 * 
 */
public class TranslateUtils {
    /**
     * analyses the translation files, and overwrites them with the filtered
     * version.
     * 
     * @param sourceParser
     * @param clazz
     * @param path
     * @throws Exception
     */
    public static void checkTranslateFiles(final SourceParser sourceParser, final Class<?> clazz, final String path) throws Exception {
        final File file = new File(sourceParser.getSource(), path);
        if (!file.exists()) { throw new Exception("File " + file + " does not exist"); }
        sourceParser.setFilter(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return !new File(dir, name).equals(file);
            }
        });

        sourceParser.scan();
        final String source = IO.readFileToString(file);
        final Regex reg = new Regex(source, "(.*?\\{\\s*)\\w+\\(\".*?(?<!\\\\)\"\\)[\\,].*?\\w+\\(\".*?(?<!\\\\)\"\\)[\\;](.*)");
        final String pre = reg.getMatch(0);
        final String post = reg.getMatch(1);
        if (post == null) { throw new Exception("Translate Enum requires at least 2 entries"); }
        // final String pre = new Regex(source,
        // ".*?public enum \\w+ implements Translate \\{").getMatch(-1);
        final StringBuilder fin = new StringBuilder();
        fin.append(pre);

        for (final Field f : clazz.getDeclaredFields()) {
            if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                final HashMap<File, String> occures = sourceParser.findOccurancesOf(f);
                if (occures.size() == 0) {
                    System.out.println("Filtered: " + f);
                    continue;
                }

                fin.append("\r\n");
                String line = new Regex(source, f.getName() + "\\s*\\(\"[^\r^\n]*?(?<!\\\\)\"\\, \\d+\\)[\\,\\;]").getMatch(-1);
                if (line == null) {
                    line = new Regex(source, f.getName() + "\\s*\\(\"[^\r^\n]*?(?<!\\\\)\"\\)[\\,\\;]").getMatch(-1);
                }
                if (line.contains("autofilter")) {
                    line = line;
                }
                fin.append(line);

            }

        }
        fin.append("\r\n");
        fin.append(post);
        System.out.println(fin);
        file.delete();
        IO.writeStringToFile(file, fin.toString());

    }

    /**
     * @param def
     * @return
     */
    private static int countWildcards(final String def) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = def.indexOf("%s", lastIndex);

            if (lastIndex != -1) {
                lastIndex += 2;
                count++;
            }
        }
        return count;
    }

    /**
     * @param string
     * @param class1
     * @param class2
     * @param class3
     * @throws Exception
     */
    public static String createLocFile(final String lng, final Class<?>... classes) throws Exception {

        final StringBuilder sb = new StringBuilder();

        sb.append("\r\n####  Translation: " + lng + "\r\n");
        Loc.setLocale(lng);
        for (final Class<?> c : classes) {
            final StringBuilder untrans = new StringBuilder();
            final StringBuilder equals = new StringBuilder();
            // final String text = c.getMethod("list", new Class<?>[]
            // {}).invoke(null, new Object[] {}) + "";

            Translate[] values = (Translate[]) c.getMethod("values", new Class<?>[] {}).invoke(null, new Object[] {});

            values = values;
            sb.append("\r\n############################ " + c.getSimpleName() + " Entries: ");
            int max = 0;
            for (final Translate entry : values) {

                max = Math.max(TranslateUtils.getName(entry).length(), max);
            }
            for (final Translate entry : values) {
                final String def = entry.getDefaultTranslation();
                if (TranslateUtils.countWildcards(def) != entry.getWildCardCount()) {
                    //
                    throw new Exception("Wrong wildcard count in defaulttranslation: " + TranslateUtils.getName(entry) + "=" + entry.getDefaultTranslation() + " WCC: " + entry.getWildCardCount());
                }

                StringBuilder dest = sb;
                final String translated = Loc.L(c.getSimpleName() + ":::" + TranslateUtils.getName(entry), entry.getDefaultTranslation()).replace("\r", "\\r").replace("\n", "\\n");

                if (translated.equals(entry.getDefaultTranslation())) {
                    dest = equals;
                }
                if (translated == entry.getDefaultTranslation()) {
                    dest = untrans;
                }

                if (entry.getWildCardCount() > 0) {
                    dest.append("\r\n######");
                    dest.append(TranslateUtils.getName(entry));
                    dest.append("-wildcards: ");
                    dest.append(entry.getWildCardCount());
                }

                dest.append("\r\n" + c.getSimpleName() + ":::");
                dest.append(TranslateUtils.getName(entry));
                dest.append("      ");
                for (int i = TranslateUtils.getName(entry).length(); i < max; i++) {
                    dest.append(" ");
                }
                dest.append(" = ");
                dest.append(translated);

            }
            if (untrans.length() > 0) {
                sb.append("\r\n################" + c.getSimpleName() + " Untranslated: \r\n");
                sb.append(untrans);
            }
            if (equals.length() > 0) {
                sb.append("\r\n################" + c.getSimpleName() + " Equals default: \r\n");
                sb.append(equals);
            }

        }

        return sb.toString();

    }

    /**
     * @param entry
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    private static String getName(final Translate entry) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        // TODO Auto-generated method stub

        for (final Field f : entry.getClass().getDeclaredFields()) {
            if (f.isEnumConstant()) {
                final Object value = f.get(null);
                if (value == entry) { return f.getName(); }
            }
        }
        // For(Object o:entry.getClass().getEnumConstants()){
        return null;
    }
}
