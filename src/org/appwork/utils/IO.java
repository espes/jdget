package org.appwork.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

public class IO {
    public static String readFileToString(File file) throws IOException {

        final BufferedReader f;

        f = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;
        final StringBuilder ret = new StringBuilder();
        String sep = System.getProperty("line.separator");
        while ((line = f.readLine()) != null) {
            ret.append(line + sep);
        }
        f.close();
        return ret.toString();

    }

    /**
     * @param file
     * @param string
     */
    public static void writeStringToFile(File file, String string) throws IOException {
        if (file == null) { throw new IllegalArgumentException("File is null."); }
        if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
        file.createNewFile();
        if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
        if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }

        Writer output = new BufferedWriter(new FileWriter(file));
        try {

            output.write(string);
        } finally {
            output.close();
        }

    }
}
