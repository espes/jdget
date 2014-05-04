/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.os.mime
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.os.mime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeLinux extends MimeDefault {
    @Override
    public String getMimeDescription(String mimeType) {
        if (super.getMimeDescriptionCache(mimeType) != null) { return super.getMimeDescriptionCache(mimeType); }

        File file = new File("/usr/share/mime/" + mimeType + ".xml");

        if (!file.exists()) { return "Unknown"; }

        String mime = "Unkown";

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;

            while ((line = in.readLine()) != null) {
                if (line.contains("<comment>")) {
                    Matcher m = Pattern.compile("<comment>(.*?)</comment>").matcher(line.trim());
                    m.find();
                    mime = m.group(1);
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            org.appwork.utils.logging.Log.exception(e);
        } catch (IOException e) {
            org.appwork.utils.logging.Log.exception(e);
        }

        super.saveMimeDescriptionCache(mimeType, mime);

        return mime;
    }
}