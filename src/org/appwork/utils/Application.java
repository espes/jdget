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
import java.net.MalformedURLException;
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
     * Adds a folder to the System classloader classpath this might fail if
     * there is a security manager
     * 
     * @param file
     * @throws IOException
     */
    public static void addFolderToClassPath(final File file) throws IOException {
        try {
            // hack to add an url to the system classpath
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(Application.class.getClassLoader(), new Object[] { file.toURI().toURL() });

        } catch (final Throwable t) {
            Log.exception(t);
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

    /**
     * Returns a ressourcefile relative to the instaldirectory
     * 
     * @param relative
     * @return
     */
    public static File getRessource(final String relative) {

        return new File(Application.getRoot(), relative);
    }

    /**
     * @param string
     * @return
     */
    public static URL getRessourceURL(final String relative) {
        final URL res = Application.class.getClassLoader().getResource(relative);
        if (res != null) {
            System.out.println("Found jar ressource " + res);

            return res;
        }

        try {
            return new File(Application.getRoot(), relative).toURI().toURL();
        } catch (final MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the Path of appworkutils.jar
     * 
     * @return
     */
    public static String getRoot() {
        return Application.getRoot(Application.class);
    }

    /**
     * Detects the applications home directory. it is either the pass of the
     * appworkutils.jar or HOME/
     */
    public static String getRoot(final Class<?> rootOfClazz) {
        if (Application.ROOT != null) { return Application.ROOT; }
        if (Application.isJared(rootOfClazz)) {
            // this is the jar file
            String loc;
            try {
                loc = URLDecoder.decode(rootOfClazz.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
            } catch (final Exception e) {
                loc = rootOfClazz.getProtectionDomain().getCodeSource().getLocation().getFile();
                System.err.println("failed urldecoding Location: " + loc);
            }
            File appRoot = new File(loc);
            if (appRoot.isFile()) {
                appRoot = appRoot.getParentFile();
            }
            Application.ROOT = appRoot.getAbsolutePath();
        } else {
            Application.ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + Application.APP_FOLDER + System.getProperty("file.separator");
        }
        return Application.ROOT;
    }

    /**
     * Detects if the Application runs out of a jar or not.
     * 
     * @param rootOfClazz
     * 
     * @return
     */
    public static boolean isJared(final Class<?> rootOfClazz) {
        final String name = rootOfClazz.getName().replaceAll("\\.", "/") + ".class";
        final URL caller = Thread.currentThread().getContextClassLoader().getResource(name);
        /*
         * caller is null in case the ressource is not found or not enough
         * rights, in that case we assume its not jared
         */
        if (caller == null) { return false; }
        return caller.toString().matches("jar\\:.*\\.jar\\!.*");
    }

    /**
     * sets current Application Folder and Jar ID. MUST BE SET at startup! Can
     * only be set once!
     * 
     * @param newAppFolder
     * @param newJar
     */
    public synchronized static void setApplication(final String newAppFolder) {
        Application.APP_FOLDER = newAppFolder;
    }

}
