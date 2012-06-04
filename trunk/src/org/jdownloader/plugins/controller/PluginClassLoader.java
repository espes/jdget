package org.jdownloader.plugins.controller;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;

public class PluginClassLoader extends URLClassLoader {

    public static class PluginClassLoaderChild extends URLClassLoader {

        private boolean createDummyLibs = true;
        private boolean jared           = Application.isJared(PluginClassLoader.class);

        public PluginClassLoaderChild(ClassLoader parent) {
            super(new URL[] { Application.getRootUrlByClass(jd.Launcher.class, null) }, parent);
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
                                    /* library file not existing, create a new one if wished, so the update system replaces it with correct one */
                                    if (createDummyLibs) lib.createNewFile();
                                    throw new UpdateRequiredClassNotFoundException(libFile);
                                }
                                throw new ClassNotFoundException(name);
                            }
                        }
                    }
                }
                if (!name.startsWith("jd.plugins.hoster") && !name.startsWith("jd.plugins.decrypter")) { return super.loadClass(name); }
                if (name.startsWith("jd.plugins.hoster.RTMPDownload")) { return super.loadClass(name); }
                Class<?> c = findLoadedClass(name);
                if (c != null) {
                    // System.out.println("Class has already been loaded by this PluginClassLoaderChild");
                    return c;
                }
                synchronized (this) {
                    /*
                     * we have to synchronize this because concurrent defineClass for same class throws exception
                     */
                    c = findLoadedClass(name);
                    if (c != null) { return c; }
                    URL myUrl = Application.getRessourceURL(name.replace(".", "/") + ".class");
                    byte[] data;
                    data = IO.readURL(myUrl);
                    return defineClass(name, data, 0, data.length);
                }
            } catch (Exception e) {
                if (e instanceof UpdateRequiredClassNotFoundException) throw (UpdateRequiredClassNotFoundException) e;
                Log.exception(e);
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
    private static final PluginClassLoader       INSTANCE                 = new PluginClassLoader();
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
        super(new URL[] { Application.getRootUrlByClass(jd.Launcher.class, null) }, PluginClassLoader.class.getClassLoader());

    }

    public PluginClassLoaderChild getChild() {
        return new PluginClassLoaderChild(this);
    }

}
