/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Application utils provide statis helper functions concerning the applications
 * System integration
 * 
 * @author $Author: unknown$
 * 
 */
public class Application {

    private static String AppFolder = "appw";
    private static String Jar = "org/appwork";
    private static boolean appSet = false;

    private static String ROOT = null;

    /**
     * Detects if the Application runs out of a jar or not.
     * 
     * @return
     */
    public static boolean isJared() {
        String caller = (Thread.currentThread().getContextClassLoader().getResource(Jar) + "");
        return caller.matches("jar\\:.*\\.jar\\!.*");

    }

    /**
     * sets current Application Folder and Jar ID. MUST BE SET at startup! Can
     * only be set once!
     * 
     * @param newAppFolder
     * @param newJar
     */
    public synchronized static void setApplication(String newAppFolder, String newJar) {
        if (appSet) return;
        AppFolder = newAppFolder;
        Jar = newJar;
        appSet = true;
    }

    /**
     * Detects the applications home directory. it is either the pass of the
     * main.jar or HOME/.rs
     */
    public static String getRoot() {
        if (ROOT != null) return ROOT;
        if (isJared()) {
            File currentDir = null;
            isJared();
            URL ressource = Thread.currentThread().getContextClassLoader().getResource(Jar);
            System.out.println(ressource);
            String dir = ressource + "";
            dir = dir.split("\\.jar\\!")[0] + ".jar";
            dir = dir.substring(Math.max(dir.indexOf("file:"), 0));

            try {
                currentDir = new File(new URI(dir));

                if (currentDir.isFile()) {
                    currentDir = currentDir.getParentFile();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + AppFolder + System.getProperty("file.separator");
            }

            return ROOT = currentDir.getAbsolutePath();
        } else {
            return ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + AppFolder + System.getProperty("file.separator");

        }
    }

    /**
     * Returns a ressourcefile relative to the instaldirectory
     * 
     * @param relative
     * @return
     */
    public static File getRessource(String relative) {
        return new File(getRoot(), relative);
    }

}
