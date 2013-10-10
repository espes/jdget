package org.jdownloader.plugins.controller;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.appwork.utils.Application;
import org.appwork.utils.IO;

public class PluginClassLoader extends URLClassLoader {
    private static HashMap<String, Class<?>>                   helperClasses           = new HashMap<String, Class<?>>();
    private static WeakHashMap<PluginClassLoaderChild, String> sharedPluginClassLoader = new WeakHashMap<PluginClassLoader.PluginClassLoaderChild, String>();

    public static class PluginClassLoaderChild extends URLClassLoader {

        private boolean                 createDummyLibs          = true;
        private boolean                 jared                    = Application.isJared(PluginClassLoader.class);
        private final PluginClassLoader parent;
        private boolean                 checkStableCompatibility = false;
        private String                  pluginClass              = null;

        public boolean isCheckStableCompatibility() {
            return checkStableCompatibility;
        }

        public void setCheckStableCompatibility(boolean checkStableCompatibility) {
            this.checkStableCompatibility = checkStableCompatibility;
        }

        public PluginClassLoaderChild(PluginClassLoader parent) {
            super(new URL[] { Application.getRootUrlByClass(jd.SecondLevelLaunch.class, null) }, parent);
            this.parent = parent;
        }

        public boolean isUpdateRequired(String name) {
            if (!jared) return false;
            name = name.replace("/", ".");
            synchronized (DYNAMIC_LOADABLE_LOBRARIES) {
                Iterator<Entry<String, String>> it = DYNAMIC_LOADABLE_LOBRARIES.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, String> next = it.next();
                    String dynamicPackage = next.getKey();
                    String libFile = next.getValue();
                    if (name.startsWith(dynamicPackage)) {
                        /* dynamic Library in use */
                        /* check if the library is already available on disk */
                        File lib = Application.getResource("libs/" + libFile);
                        if (lib.exists() && lib.isFile() && lib.length() != 0) {
                            /* file already exists on disk, so we can use it */
                            it.remove();
                            break;
                        } else if (lib.exists() && lib.isFile() && lib.length() == 0) {
                            /* dummy library, we have to wait for update */
                            return true;
                        } else if (!lib.exists()) {
                            /* library file not existing, create a new one if wished, so the update system replaces it with correct one */
                            return true;
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                if (jared) {
                    synchronized (DYNAMIC_LOADABLE_LOBRARIES) {
                        Iterator<Entry<String, String>> it = DYNAMIC_LOADABLE_LOBRARIES.entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<String, String> next = it.next();
                            String dynamicPackage = next.getKey();
                            String libFile = next.getValue();
                            if (name.startsWith(dynamicPackage)) {
                                /* dynamic Library in use */
                                /* check if the library is already available on disk */
                                File lib = Application.getResource("libs/" + libFile);
                                if (lib.exists() && lib.isFile() && lib.length() != 0) {
                                    /* file already exists on disk, so we can use it */
                                    it.remove();
                                    break;
                                } else if (lib.exists() && lib.isFile() && lib.length() == 0) {
                                    /* dummy library, we have to wait for update */
                                    throw new UpdateRequiredClassNotFoundException(libFile);
                                } else if (!lib.exists()) {
                                    /*
                                     * library file not existing, create a new one if wished, so the update system replaces it with correct one
                                     */
                                    if (createDummyLibs) lib.createNewFile();
                                    throw new UpdateRequiredClassNotFoundException(libFile);
                                }
                                throw new ClassNotFoundException(name);
                            }
                        }
                    }
                }
                if (isCheckStableCompatibility() && name.equals(pluginClass) == false && !name.startsWith(pluginClass + "$")) {
                    boolean check = true;
                    if (check) check = !name.equals("jd.plugins.hoster.RTMPDownload");/* available in 09581 Stable */
                    if (check) check = !name.equals("org.appwork.utils.speedmeter.SpeedMeterInterface");/* available in 09581 Stable */
                    if (check) check = !name.equals("org.appwork.utils.net.throttledconnection.MeteredThrottledInputStream");/* available in 09581 Stable */
                    if (check) check = !name.equals("org.appwork.utils.net.throttledconnection.ThrottledConnection");/* available in 09581 Stable */
                    if (check) {
                        if (name.startsWith("org.appwork") || name.startsWith("jd.plugins.hoster") || name.startsWith("jd.plugins.decrypter")) {
                            System.out.println("Check for Stable Compatibility!!!: " + getPluginClass() + " wants to load " + name);
                        }
                    }
                }
                if (!name.startsWith("jd.plugins.hoster") && !name.startsWith("jd.plugins.decrypter")) { return super.loadClass(name); }
                if (name.startsWith("jd.plugins.hoster.RTMPDownload")) { return super.loadClass(name); }
                Class<?> c = null;
                boolean helperClass = name.endsWith("StringContainer");
                if (helperClass) {
                    synchronized (helperClasses) {
                        c = helperClasses.get(name);
                    }
                } else {
                    c = findLoadedClass(name);
                }
                if (c != null) {
                    // System.out.println("Class has already been loaded by this PluginClassLoaderChild");
                    return c;
                }
                synchronized (this) {
                    /*
                     * we have to synchronize this because concurrent defineClass for same class throws exception
                     */
                    if (helperClass) {
                        synchronized (helperClasses) {
                            c = helperClasses.get(name);
                        }
                    } else {
                        c = findLoadedClass(name);
                    }
                    if (c != null) return c;
                    URL myUrl = Application.getRessourceURL(name.replace(".", "/") + ".class");
                    if (myUrl == null) throw new ClassNotFoundException("Class does not exist(anymore): " + name);
                    byte[] data;
                    if (helperClass) {
                        synchronized (helperClasses) {
                            c = helperClasses.get(name);
                            if (c == null) {
                                data = IO.readURL(myUrl);
                                c = parent.defineClass(name, data, 0, data.length);
                                helperClasses.put(name, c);
                            }
                        }
                    } else {
                        data = IO.readURL(myUrl);
                        c = defineClass(name, data, 0, data.length);
                    }
                    return c;
                }
            } catch (Exception e) {
                if (e instanceof UpdateRequiredClassNotFoundException) throw (UpdateRequiredClassNotFoundException) e;
                if (e instanceof ClassNotFoundException) throw (ClassNotFoundException) e;
                throw new ClassNotFoundException(name, e);
            }

        }

        /**
         * @return the createDummyLibs
         */
        public boolean isCreateDummyLibs() {
            return createDummyLibs;
        }

        /**
         * @param createDummyLibs
         *            the createDummyLibs to set
         */
        public void setCreateDummyLibs(boolean createDummyLibs) {
            this.createDummyLibs = createDummyLibs;
        }

        /**
         * @return the pluginClass
         */
        public String getPluginClass() {
            return pluginClass;
        }

        /**
         * @param pluginClass
         *            the pluginClass to set
         */
        public void setPluginClass(String pluginClass) {
            this.pluginClass = pluginClass;
        }
    }

    // private ClassLoader parentClassLoader;
    //
    // @Override
    // protected synchronized Class<?> loadClass(String name, boolean resolve)
    // throws ClassNotFoundException {
    // try {
    // return super.loadClass(name, resolve);
    // } catch (ClassNotFoundException e) {
    // return parentClassLoader.loadClass(name);
    // }
    //
    // }
    //
    // @Override
    // public URL getResource(String name) {
    //
    // URL ret = super.getResource(name);
    // if (ret == null) ret = parentClassLoader.getResource(name);
    // return ret;
    //
    // }
    //
    // @Override
    // public Enumeration<URL> getResources(String name) throws IOException {
    //
    // Enumeration<URL> ret = super.getResources(name);
    // if (ret == null || !ret.hasMoreElements()) ret =
    // parentClassLoader.getResources(name);
    // return ret;
    // }
    private static final PluginClassLoader       INSTANCE                   = new PluginClassLoader();
    private static final HashMap<String, String> DYNAMIC_LOADABLE_LOBRARIES = new HashMap<String, String>();
    static {
        synchronized (DYNAMIC_LOADABLE_LOBRARIES) {
            DYNAMIC_LOADABLE_LOBRARIES.put("org.bouncycastle", "bcprov-jdk15on-147.jar");
        }
    }

    public static PluginClassLoader getInstance() {
        return INSTANCE;
    }

    private PluginClassLoader() {
        super(new URL[] { Application.getRootUrlByClass(jd.SecondLevelLaunch.class, null) }, PluginClassLoader.class.getClassLoader());

    }

    public PluginClassLoaderChild getChild() {
        return new PluginClassLoaderChild(this);
    }

    public synchronized PluginClassLoaderChild getSharedChild(String id) {
        if (id == null) return getChild();
        Iterator<Entry<PluginClassLoaderChild, String>> it = sharedPluginClassLoader.entrySet().iterator();
        while (it.hasNext()) {
            Entry<PluginClassLoaderChild, String> next = it.next();
            if (next.getValue().equals(id)) return next.getKey();
        }
        PluginClassLoaderChild ret = getChild();
        sharedPluginClassLoader.put(ret, id);
        return ret;
    }

    public static PluginClassLoaderChild getThreadPluginClassLoaderChild() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null && cl instanceof PluginClassLoaderChild) return (PluginClassLoaderChild) cl;
        return null;
    }

}
