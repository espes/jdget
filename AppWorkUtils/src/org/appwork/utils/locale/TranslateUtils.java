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
import java.util.regex.Pattern;

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
     * @return
     * @throws Exception
     */
    public static String checkTranslateFiles(final SourceParser sourceParser, final Class<?> clazz, final boolean write) throws Exception {
        final String path = clazz.getName().replaceAll("\\.", "/") + ".java";
        final File file = new File(sourceParser.getSource(), path);
        if (!file.exists()) { throw new Exception("File " + file + " does not exist"); }
        sourceParser.setFilter(new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                return !new File(dir, name).equals(file);
            }
        });

        sourceParser.scan();
        String source = IO.readFileToString(file);
        final Regex reg = new Regex(source, "(.*?\\{)");
        final String pre = reg.getMatch(0);
        final String post = new Regex(source, "(//\\s*ENDOFENUMS.*)").getMatch(0);
        source = source.replace(pre, "<pre>");
        if (post == null) { throw new Exception("Translate Enum must end with //ENDOFENUMS"); }
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
                if (line.contains("CHATPANEL_HISTORY_LASTWEEK")) {
                    System.out.println("fdsf");
                }
                String comment = new Regex(source, "\\/\\*(.*?)\\*\\/\\s*" + Pattern.quote(line)).getMatch(0);
                if (comment != null) {
                    if (comment.contains("/*")) {

                        comment = TranslateUtils.handleMultiComment(comment);
                    }
                    if (comment.contains("Kontakt hinzufügen")) {
                        System.out.println("fdsf");
                    }
                    fin.append("/*\r\n");
                    fin.append(comment.trim());
                    fin.append("\r\n*/\r\n");
                    final String full = new Regex(source, "(\\/\\*.*?\\*\\/\\s*" + Pattern.quote(line) + ")").getMatch(0);
                    source = source.replace(full, "---\r\n" + line);
                }

                fin.append(line);

            }

        }
        fin.append("");
        fin.append("\r\n");
        fin.append(post);
        System.out.println(fin);
        if (write) {
            file.delete();

            IO.writeStringToFile(file, fin.toString());
        }
        return fin.toString();

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
     * @param files
     * @param string
     * @param class1
     * @param class2
     * @param class3
     * @throws Exception
     */
    public static String createLocFile(final String lng, final File[] files, final Class<?>[] classes) throws Exception {

        final StringBuilder sb = new StringBuilder();

        sb.append("\r\n####  Translation: " + lng + "\r\n");
        Loc.setLocale(lng);

        for (int i = 0; i < classes.length; i++) {
            final Class<?> c = classes[i];
            final File file = new File(files[i], c.getName().replaceAll("\\.", "/") + ".java");
            String source = IO.readFileToString(file);
            source = new Regex(source, "public\\s+enum.*?implements\\s+Translate\\s+\\{(.*)").getMatch(0);
            final StringBuilder untrans = new StringBuilder();
            final StringBuilder equals = new StringBuilder();
            // final String text = c.getMethod("list", new Class<?>[]
            // {}).invoke(null, new Object[] {}) + "";

            final Translate[] values = (Translate[]) c.getMethod("values", new Class<?>[] {}).invoke(null, new Object[] {});

            sb.append("\r\n############################ " + c.getSimpleName() + " Entries: ");
            int max = 0;
            for (final Translate entry : values) {

                max = Math.max(TranslateUtils.getName(entry).length(), max);
            }
            for (final Translate entry : values) {
                final String def = entry.getDefaultTranslation();
                final String name = TranslateUtils.getName(entry);

                if (TranslateUtils.countWildcards(def) != entry.getWildCardCount()) {
                    //
                    throw new Exception("Wrong wildcard count in defaulttranslation: " + name + "=" + entry.getDefaultTranslation() + " WCC: " + entry.getWildCardCount());
                }

                StringBuilder dest = sb;

                String line = new Regex(source, name + "\\s*\\(\"[^\r^\n]*?(?<!\\\\)\"\\, \\d+\\)[\\,\\;]").getMatch(-1);
                System.out.println(name);

                if (line == null) {
                    line = new Regex(source, name + "\\s*\\(\"[^\r^\n]*?(?<!\\\\)\"\\)[\\,\\;]").getMatch(-1);
                }
                if (line == null) {

                throw new Exception("SYNTAX line. PLease recompile"); }
                String comment = new Regex(source, "\\/\\*(.*?)\\*\\/\\s*" + Pattern.quote(line)).getMatch(0);
                if (comment != null) {
                    comment = comment.replaceAll("\\s*\\*\\s*", " ");
                    comment = new Regex(comment, "###" + lng + ":([^\r^\n]+)").getMatch(0);
                    if (comment != null) {
                        comment = comment.split("###")[0].trim();
                    }
                    source = source.replace(new Regex(source, "(\\/\\*.*?\\*\\/\\s*" + Pattern.quote(line) + ")").getMatch(0), "---\r\n" + line);
                }
                final String translated = Loc.L(c.getSimpleName() + ":::" + name, comment != null ? comment : entry.getDefaultTranslation()).replace("\r", "\\r").replace("\n", "\\n");

                if (translated.equals(entry.getDefaultTranslation())) {
                    dest = equals;
                }
                if (translated == entry.getDefaultTranslation()) {
                    dest = untrans;
                }

                if (entry.getWildCardCount() > 0) {
                    dest.append("\r\n######");
                    dest.append(name);
                    dest.append("-wildcards: ");
                    dest.append(entry.getWildCardCount());
                }

                dest.append("\r\n" + c.getSimpleName() + ":::");

                dest.append(name);
                dest.append("      ");
                for (int ii = name.length(); ii < max; ii++) {
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

    /**
     * @param comment
     * @return
     */
    private static String handleMultiComment(String comment) {
        comment = new Regex(comment, ".*\\/\\*(.*)").getMatch(0);
        return comment;
    }
}
