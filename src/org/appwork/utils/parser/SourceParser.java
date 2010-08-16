package org.appwork.utils.parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.appwork.utils.IO;

public class SourceParser extends Object {

    private static final Pattern PATTERN_REMOVE_COMENTS1 = Pattern.compile("\\/\\*.*?\\*\\/", Pattern.DOTALL);

    private static final Pattern PATTERN_REMOVE_COMENTS2 = Pattern.compile("//.*");

    private HashMap<File, String[]> map;

    private final File sourceFolder;

    private FilenameFilter filter;

    /**
     * @param file
     * @param string
     * @param string
     * @throws IOException
     */
    public SourceParser(final File file) throws IOException {
        this.map = new HashMap<File, String[]>();
        this.sourceFolder = file;
        this.filter = null;

    }

    /**
     * @param f
     * @return
     */
    public HashMap<File, String> findOccurancesOf(final Field f) {
        final HashMap<File, String> found = new HashMap<File, String>();

        Entry<File, String[]> next;
        for (final Iterator<Entry<File, String[]>> it = this.map.entrySet().iterator(); it.hasNext();) {
            next = it.next();

            for (String statement : next.getValue()) {

                if (statement.contains(f.getName())) {
                    if (statement.contains("//") || statement.contains("/*")) {
                        statement = statement;
                    }
                    found.put(next.getKey(), statement);
                }
            }
        }
        return found;
    }

    /**
     * @return
     */
    public File getSource() {
        // TODO Auto-generated method stub
        return this.sourceFolder;
    }

    private void getSourceFiles(final File file) throws IOException {

        for (final File f : file.listFiles(new FilenameFilter() {

            @Override
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

    /**
     * @throws IOException
     * 
     */
    public void scan() throws IOException {
        this.map = new HashMap<File, String[]>();
        this.getSourceFiles(this.sourceFolder);
    }

    /**
     * @param filenameFilter
     */
    public void setFilter(final FilenameFilter filenameFilter) {
        this.filter = filenameFilter;

    }

}
