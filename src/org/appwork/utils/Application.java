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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import org.appwork.utils.logging.Log;

/**
 * Application utils provide statis helper functions concerning the applications
 * System integration
 * 
 * @author $Author: unknown$
 * 
 */
public class Application {

    private static String APP_FOLDER = ".appwork";

    private static String ROOT;

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
        APP_FOLDER = newAppFolder;
    }

    /**
     * Returns the Path of appworkutils.jar
     * 
     * @return
     */
    public static String getRoot() {
        return getRoot(Application.class);
    }

    /**
     * Detects the applications home directory. it is either the pass of the
     * appworkutils.jar or HOME/
     */
    public static String getRoot(Class<?> rootOfClazz) {
        if (ROOT != null) return ROOT;
        if (isJared()) {
            // this is the jar file
            String loc;
            try {
                loc = URLDecoder.decode(rootOfClazz.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
            } catch (Exception e) {
                loc = rootOfClazz.getProtectionDomain().getCodeSource().getLocation().getFile();
                System.err.println("failed urldecoding Location: " + loc);
            }
            File appRoot = new File(loc);
            if (appRoot.isFile()) appRoot = appRoot.getParentFile();
            ROOT = appRoot.getAbsolutePath();
        } else {
            ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + APP_FOLDER + System.getProperty("file.separator");
        }
        return ROOT;
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

    /**
     * Adds a folder to the System classloader classpath this might fail if
     * there is a security manager
     * 
     * @param file
     * @throws IOException
     */
    public static void addFolderToClassPath(File file) throws IOException {
        try {
            // hack to add an url to the system classpath
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(Application.class.getClassLoader(), new Object[] { file.toURI().toURL() });

        } catch (Throwable t) {
            Log.exception(t);
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

}
