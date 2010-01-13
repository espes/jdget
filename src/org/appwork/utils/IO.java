package org.appwork.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
}
