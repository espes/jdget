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
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.logging.Log;
import org.appwork.utils.logging2.LogInterface;
import org.appwork.utils.os.CrossSystem;

/**
 * Application utils provide statis helper functions concerning the applications
 * System integration
 * 
 * @author $Author: unknown$
 * 
 */
public class Application {


    static {
        Application.redirectOutputStreams();
    }
    private static String       APP_FOLDER  = ".appwork";

    private static String       ROOT;

    private static long         javaVersion = 0;
    public static long          JAVA15      = 15000000;
    public static long          JAVA16      = 16000000;
    public static long          JAVA17      = 17000000;
    public static long          JAVA18      = 18000000;
    private static Boolean      JVM64BIT    = null;

    private static boolean      REDIRECTED  = false;

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
     * Adds a url to the classloader classpath this might fail if there is a
     * security manager
     * 
     * @param file
     * @throws IOException
     */
    public static void addUrlToClassPath(final URL url, final ClassLoader cl) throws IOException {
        try {
            // hack to add an url to the system classpath
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(cl, new Object[] { url });

        } catch (final Throwable t) {
            Log.exception(t);
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

    public static String getApplication() {
        return Application.APP_FOLDER;
    }

    /**
     * @return
     */
    public static File getApplicationRoot() {
        return Application.getRootByClass(Application.class, null);
    }

    /**
     * Returns the Path of appworkutils.jar
     * 
     * @return
     */
    public static String getHome() {
        return Application.getRoot(Application.class);
    }

    /**
     * @return
     */
    public static URL getHomeURL() {

        try {
            return new File(Application.getHome()).toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new WTFException(e);
        }
    }

    // returns the jar filename of clazz
    public static File getJarFile(final Class<?> clazz) {
        final String name = clazz.getName().replaceAll("\\.", "/") + ".class";
        final URL url = Application.getRessourceURL(name);
        final String prot = url.getProtocol();
        final String path = url.getPath();
        Log.L.info(url + "");
        if (!"jar".equals(prot)) { throw new WTFException("Works in Jared mode only"); }
        final int index = path.indexOf(".jar!");
        if (index < 0) { throw new WTFException("Works in Jared mode only"); }
        try {
            return new File(new URL(path.substring(0, index + 4)).toURI());
        } catch (final MalformedURLException e) {
            Log.exception(Level.WARNING, e);

        } catch (final URISyntaxException e) {
            Log.exception(Level.WARNING, e);

        }
        return null;
    }

    /**
     * @param object
     * @return
     */
    public static String getJarName(Class<?> clazz) {
        if (clazz == null) {
            clazz = Application.class;
        }
        final String name = clazz.getName().replaceAll("\\.", "/") + ".class";
        final String url = Application.getRessourceURL(name).toString();

        final int index = url.indexOf(".jar!");
        if (index < 0) { throw new IllegalStateException("No JarName Found"); }
        try {
            return new File(new URL(url.substring(4, index + 4)).toURI()).getName();
        } catch (final Exception e) {

        }
        throw new IllegalStateException("No JarName Found");

    }

    public static long getJavaVersion() {
        if (Application.javaVersion > 0) { return Application.javaVersion; }
        try {
            final String version = System.getProperty("java.version");
            String v = new Regex(version, "^(\\d+\\.\\d+\\.\\d+)").getMatch(0);
            final String u = new Regex(version, "^.*?_(\\d+)").getMatch(0);
            final String b = new Regex(version, "^.*?_b(\\d+)").getMatch(0);
            v = v.replaceAll("\\.", "");
            /* 170uubbb */
            /* eg 1.6 = 16000000 */
            long ret = Long.parseLong(v) * 100000;
            if (u != null) {
                /* append update number */
                ret = ret + Long.parseLong(u) * 1000;
            }
            if (b != null) {
                /* append build number */
                ret = ret + Long.parseLong(b);
            }
            Application.javaVersion = ret;
            return ret;
        } catch (final Exception e) {
            Log.exception(e);
            return -1;
        }
    }

    /**
     * @param class1
     * @return
     */
    public static String getPackagePath(final Class<?> class1) {
        // TODO Auto-generated method stub
        return class1.getPackage().getName().replace('.', '/') + "/";
    }

    /**
     * Returns a ressourcefile relative to the instaldirectory
     * 
     * @param relative
     * @return
     */
    public static File getResource(final String relative) {
        return new File(Application.getHome(), relative);
    }

    /**
     * returns the url for the resource. if The resource can be found in
     * classpath, it will be returned. otherwise the function will return the
     * fileurl to current wprkingdirectory
     * 
     * @param string
     * @return
     */
    public static URL getRessourceURL(final String relative) {

        return Application.getRessourceURL(relative, true);
    }

    /**
     * Returns the Resource url for relative.
     * 
     * NOTE:this function only returns URL's that really exists!
     * 
     * if preferClassPath is true:
     * 
     * we first check if there is a ressource available inside current
     * classpath, for example inside the jar itself. if no such URL exists we
     * check for file in local filesystem
     * 
     * if preferClassPath if false:
     * 
     * first check local filesystem, then inside classpath
     * 
     * 
     * 
     * @param string
     * @param b
     */
    public static URL getRessourceURL(final String relative, final boolean preferClasspath) {
        try {

            if (relative == null) { return null; }
            if (relative.startsWith("/") || relative.startsWith("\\")) { throw new WTFException("getRessourceURL only works with relative pathes."); }
            if (preferClasspath) {

                final URL res = Application.class.getClassLoader().getResource(relative);
                if (res != null) { return res; }
                final File file = new File(Application.getHome(), relative);
                if (!file.exists()) { return null; }
                return file.toURI().toURL();

            } else {
                final File file = new File(Application.getHome(), relative);
                if (file.exists()) { return file.toURI().toURL(); }

                final URL res = Application.class.getClassLoader().getResource(relative);
                if (res != null) { return res; }

            }
        } catch (final MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Detects the applications home directory. it is either the pass of the
     * appworkutils.jar or HOME/
     */
    public static String getRoot(final Class<?> rootOfClazz) {
        if (Application.ROOT != null) { return Application.ROOT; }
        final String key = "awuhome" + Application.APP_FOLDER;
        final String sysProp = System.getProperty(key);
        if (sysProp != null) {
            Application.ROOT = sysProp;
            return Application.ROOT;
        }
        if (Application.isJared(rootOfClazz)) {
            // this is the jar file
            URL loc;

            loc = rootOfClazz.getProtectionDomain().getCodeSource().getLocation();

            File appRoot;
            try {
                appRoot = new File(loc.toURI());

                if (appRoot.isFile()) {
                    appRoot = appRoot.getParentFile();
                }
                Application.ROOT = appRoot.getAbsolutePath();
                System.out.println("Application Root: " + Application.ROOT + " (jared) " + rootOfClazz);
            } catch (final URISyntaxException e) {
                Log.exception(e);
                Application.ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + Application.APP_FOLDER + System.getProperty("file.separator");
                System.out.println("Application Root: " + Application.ROOT + " (jared but error) " + rootOfClazz);
            }
        } else {
            Application.ROOT = System.getProperty("user.home") + System.getProperty("file.separator") + Application.APP_FOLDER + System.getProperty("file.separator");
            System.out.println("Application Root: " + Application.ROOT + " (DEV) " + rootOfClazz);
        }
        // do not use Log.L here. this might be null
        return Application.ROOT;
    }

    /**
     * @param class1
     * @param subPaths
     * @return
     */
    public static File getRootByClass(final Class<?> class1, final String subPaths) {
        // this is the jar file
        URL loc;

        loc = class1.getProtectionDomain().getCodeSource().getLocation();

        File appRoot;
        try {
            appRoot = new File(loc.toURI());

            if (appRoot.isFile()) {
                appRoot = appRoot.getParentFile();
            }
            if (subPaths != null) { return new File(appRoot, subPaths); }
            return appRoot;
        } catch (final URISyntaxException e) {
            Log.exception(e);
            return null;
        }
    }

    /**
     * @param class1
     * @param subPaths
     *            TODO
     * @return
     */
    public static URL getRootUrlByClass(final Class<?> class1, final String subPaths) {
        // TODO Auto-generated method stub
        try {
            return Application.getRootByClass(class1, subPaths).toURI().toURL();
        } catch (final MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static boolean is64BitJvm() {
        if (Application.JVM64BIT != null) { return Application.JVM64BIT; }
        boolean ret = false;
        String prop = System.getProperty("sun.arch.data.model");
        try {
            if (Integer.parseInt(prop) == 64) {
                ret = true;
            } else {
                ret = false;
            }
        } catch (final Throwable e) {
            prop = System.getProperty("os.arch");
            if (prop != null) {
                if ("i386".equals(prop)) {
                    ret = false;
                } else if ("x86".equals(prop)) {
                    ret = false;
                } else if ("sparc".equals(prop)) {
                    ret = false;
                } else if ("amd64".equals(prop)) {
                    ret = true;
                } else if ("ppc64".equals(prop)) {
                    ret = true;
                } else if ("amd_64".equals(prop)) {
                    ret = true;
                } else if ("x86_64".equals(prop)) {
                    ret = true;
                } else if ("sparcv9".equals(prop)) {
                    ret = true;
                } else {
                    ret = false;
                }
            }
        }
        Application.JVM64BIT = ret;
        return ret;
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
        final ClassLoader cll = Application.class.getClassLoader();
        if (cll == null) {
            Log.L.severe("getContextClassLoader() is null");
            return true;
        }
        // System.out.println(name);
        final URL caller = cll.getResource(name);

        // System.out.println(caller);
        /*
         * caller is null in case the ressource is not found or not enough
         * rights, in that case we assume its not jared
         */
        if (caller == null) { return false; }
        return caller.toString().matches("jar\\:.*\\.(jar|exe)\\!.*");
    }

    /**
     * checks current java version for known issues/bugs or unsupported ones
     * 
     * @param support15
     * @return
     */
    public static boolean isOutdatedJavaVersion(final boolean supportJAVA15) {
        final long java = Application.getJavaVersion();
        if (java < 16000000l && !CrossSystem.isMac()) {
            Log.L.warning("Java 1.6 should be available on your System, please upgrade!");
            /* this is no mac os, so please use java>=1.6 */
            return true;
        }
        if (java < 16000000l && !supportJAVA15) {
            Log.L.warning("Java 1.5 no longer supported!");
            /* we no longer support java 1.5 */
            return true;
        }
        if (java >= 16018000l && java < 16019000l) {
            Log.L.warning("Java 1.6 Update 18 has a serious bug in garbage collector!");
            /*
             * java 1.6 update 18 has a bug in garbage collector, causes java
             * crashes
             * 
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6847956
             */
            return true;
        }
        if (java >= 16010000l && java < 16011000l) {
            Log.L.warning("Java 1.6 Update 10 has a swing bug!");
            /*
             * 16010.26
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6657923
             */
            return true;
        }
        if (CrossSystem.isMac() && java >= Application.JAVA17 && java < 17006000l) {
            /*
             * leaking semaphores
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7166379
             */
            return true;
        }
        return false;
    }

    public static void main(final String[] args) {
        System.out.println("Java Version: " + Application.getJavaVersion());
        System.out.println("Java is 64bit: " + Application.is64BitJvm());
        System.out.println("OS is: " + CrossSystem.getOSString());
        System.out.println("OS is 64Bit: " + CrossSystem.is64BitOperatingSystem());
    }

    /**
     * @param logger
     */
    public static void printSystemProperties(final LogInterface logger) {
        final Properties p = System.getProperties();
        final Enumeration keys = p.keys();
        final StringBuilder sb = new StringBuilder();
        String key;
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            sb.append("SysProp: ").append(key).append(": ").append((String) p.get(key));

            logger.info(sb.toString());
            sb.setLength(0);
        }

        for (final Entry<String, String> e : System.getenv().entrySet()) {

            sb.append("SysEnv: ").append(e.getKey()).append(": ").append(e.getValue());
            logger.info(sb.toString());
            sb.setLength(0);
        }

    }

    /**
     * 
     */
    public static void redirectOutputStreams() {
        if (Application.REDIRECTED) { return; }
        if (Charset.defaultCharset() == Charset.forName("cp1252")) {
            Application.REDIRECTED = true;
            // workaround.
            // even 1252 is default codepage, windows console expects cp850
            // codepage input
            try {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "CP850"));
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "CP850"));
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * sets current Application Folder and Jar ID. MUST BE SET at startup! Can
     * only be set once!
     * 
     * @param newAppFolder
     * @param newJar
     */
    public synchronized static void setApplication(final String newAppFolder) {
        Application.ROOT = null;
        Application.APP_FOLDER = newAppFolder;
    }

}
