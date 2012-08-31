package org.appwork.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtils {

    /**
     * @param <T>
     * @param name
     * @param object
     * @param class1
     * @return
     */
    public static <T> List<Class<? extends T>> getClassesInPackage(ClassLoader cl, String name, final Pattern pattern, Class<T> class1) {
        Enumeration<URL> found;
        List<T> ret = new ArrayList<T>();
        try {
            final String finalName = name.replace(".", "/");
            found = cl.getResources(finalName);

            while (found.hasMoreElements()) {

                URL url = found.nextElement();
           
                if (url.getProtocol().equalsIgnoreCase("jar")) {
                    String path = url.getPath();
                    File jarFile = new File(new URL(path.substring(0, path.lastIndexOf('!'))).toURI());
                    JarInputStream jis = null;
                    try {
                        jis = new JarInputStream(new FileInputStream(jarFile));
                        JarEntry e;

                        while ((e = jis.getNextJarEntry()) != null) {
                           
                            if (!e.getName().endsWith(".class")) continue;
                            if (!e.getName().startsWith(finalName)) continue;
                            // try {
                            if (pattern != null) {

                                Matcher matcher = pattern.matcher(e.getName());
                                if (!matcher.matches()) continue;

                            }

                            String classPath = e.getName().replace("/", ".");
                            classPath = classPath.substring(0, classPath.length() - 6);
                            try {
                                Class<?> clazz = cl.loadClass(classPath);
                         if(class1==clazz)continue;
                                if (class1 == null || class1.isAssignableFrom(clazz)) {
                                    ret.add((T) clazz);
                                }
                            } catch (Throwable ee) {

                            }

                        }
                    } finally {
                        try {
                            jis.close();
                        } catch (final Throwable e) {
                        }
                    }
                } else {
                    File path = new File(url.toURI());
                    int i = path.getAbsolutePath().replace("\\", "/").indexOf(finalName);
                    final File root = new File(path.getAbsolutePath().substring(0, i));
                    List<File> files = Files.getFiles(new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            if (!pathname.getName().endsWith(".class")) return false;
                            String rel = Files.getRelativePath(root, pathname);
                            if (pattern != null) {
                                Matcher matcher = pattern.matcher(rel);
                                if (!matcher.matches()) return false;
                            }

                            return true;
                        }
                    }, new File(url.toURI()));
                    
                    for(File classFile:files){
                        String classPath = Files.getRelativePath(root, classFile).replace("/", ".").replace("\\", ".");
                        classPath = classPath.substring(0, classPath.length() - 6);
                        try {
                            Class<?> clazz = cl.loadClass(classPath);
                            if(class1==clazz)continue;
                            if (class1 == null || class1.isAssignableFrom(clazz)) {
                                ret.add((T) clazz);
                            }
                        } catch (Throwable ee) {

                        }
                        
                    }
                    //
                }
            }
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        return (List<Class<? extends T>>) ret;
    }

}
