package org.appwork.utils.parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.appwork.utils.IO;

public class SourceParser extends Object {

    private static final Pattern PATTERN_REMOVE_COMENTS1 = Pattern.compile("\\/\\*.*?\\*\\/", Pattern.DOTALL);

    private static final Pattern PATTERN_REMOVE_COMENTS2 = Pattern.compile("//.*");

    private HashMap<File, String[]> map;

    private final File sourceFolder;

    private FilenameFilter filter;

    public SourceParser(final File file) throws IOException {
        this.map = new HashMap<File, String[]>();
        this.sourceFolder = file;
        this.filter = null;
    }

    public HashMap<File, String> findOccurancesOf(final Field f) {
        final HashMap<File, String> found = new HashMap<File, String>();

        for (Entry<File, String[]> next : this.map.entrySet()) {
            for (String statement : next.getValue()) {
                if (statement.contains(f.getName())) {
                    if (statement.contains("//") || statement.contains("/*")) {
                        // TODO: Old assignment made no sense
                        // statement = statement;
                    }
                    found.put(next.getKey(), statement);
                }
            }
        }
        return found;
    }

    public File getSource() {
        return this.sourceFolder;
    }

    private void getSourceFiles(final File file) throws IOException {
        for (final File f : file.listFiles(new FilenameFilter() {

            
            public boolean accept(final File dir, final String name) {
                return (SourceParser.this.filter == null || SourceParser.this.filter.accept(dir, name)) && (name.endsWith(".java") || new File(dir, name).isDirectory());
            }

        })) {
            if (f.isDirectory()) {
                this.getSourceFiles(f);
            } else {
                String statement = IO.readFileToString(f);
                statement = SourceParser.PATTERN_REMOVE_COMENTS1.matcher(statement).replaceAll("/*comment*/");
                statement = SourceParser.PATTERN_REMOVE_COMENTS2.matcher(statement).replaceAll("//comment");
                this.map.put(f, statement.split("[\\{\\}\\;]"));
            }
        }
    }

    public void scan() throws IOException {
        this.map = new HashMap<File, String[]>();
        this.getSourceFiles(this.sourceFolder);
    }

    public void setFilter(final FilenameFilter filenameFilter) {
        this.filter = filenameFilter;
    }

}
