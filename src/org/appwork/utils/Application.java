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

/**
 * Application utils provide statis helper functions concerning the applications
 * System integration
 * 
 * @author $Author: unknown$
 * 
 */
public class Application {

    private static String APP_FOLDER = ".appwork";
    private static boolean APP_SET = false;

    private static String ROOT = null;

    /**
     * Detects if the Application runs out of a jar or not.
     * 
     * @return
     */
    public static boolean isJared() {
        String caller = (Thread.currentThread().getContextClassLoader().getResource("org/appwork") + "");
        return caller.matches("jar\\:.*\\.jar\\!.*");

    }

    /**
     * sets current Application Folder and Jar ID. MUST BE SET at startup! Can
     * only be set once!
     * 
     * @param newAppFolder
     * @param newJar
     */
    public synchronized static void setApplication(String newAppFolder) {
        if (APP_SET) return;
        APP_FOLDER = newAppFolder;
        APP_SET = true;
    }

    /**
     * Detects the applications home directory. it is either the pass of the
     * main.jar or HOME/
     */
    public static String getRoot() {
        if (ROOT != null) return ROOT;
        if (isJared()) {
            return ROOT = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getAbsolutePath();
        } else {
            return ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + APP_FOLDER + System.getProperty("file.separator");

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
