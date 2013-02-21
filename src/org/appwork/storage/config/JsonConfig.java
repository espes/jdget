/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.appwork.exceptions.WTFException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.handler.StorageHandler;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.Dialog;

/**
 * @author thomas
 * 
 */
public class JsonConfig {

    private static final HashMap<String, ConfigInterface> CACHE = new HashMap<String, ConfigInterface>();

    /**
     * @param <T>
     * @param class1
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final Class<T> configInterface) {
        ConfigInterface ret = JsonConfig.CACHE.get(configInterface.getName());
        if (ret == null) {
            /*
             * we first lock on Cache to access it and check for existence of
             * the configInterface
             */
            try {
                synchronized (JSonStorage.LOCK) {
                    ret = JsonConfig.CACHE.get(configInterface.getName());
                    if (ret == null) {
                        /*
                         * see GraphicalUserInterface, a static inside the
                         * configInterface itself
                         */
                        // a static referenze in the interface itself would
                        // bypass
                        // the
                        // cache and create to storagehandler. let's create a
                        // dummy
                        // proxy here. and check again afterwards
                        Proxy.newProxyInstance(configInterface.getClassLoader(), new Class<?>[] { configInterface }, new InvocationHandler() {

                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                                // TODO Auto-generated method stub
                                return null;
                            }
                        });
                        /* check if the cache now has this configInterface */
                        ret = JsonConfig.CACHE.get(configInterface.getName());
                    }

                    if (ret == null) {
                        /*
                         * WARNING: as the JSonConfig uses JSonStorage in
                         * Background we FIRST get JSonStorage Lock AND then
                         * JSonStorage Lock. This avoids deadlocks that could
                         * happen if we restore an Json Object which needs to
                         * create a JSonConfig during the restore procress
                         */

                        ret = JsonConfig.CACHE.get(configInterface.getName());
                        if (ret == null) {

                            ret = (T) Proxy.newProxyInstance(configInterface.getClassLoader(), new Class<?>[] { configInterface }, new StorageHandler<T>(Application.getResource("cfg/" + configInterface.getName()), configInterface));

                            JsonConfig.CACHE.put(configInterface.getName(), ret);
                        }
                    }
                }
            } catch (final RuntimeException e) {
                Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            }
        }
        return (T) ret;
    }

    /**
     * @param name
     * @param class1
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final File path, final Class<T> configInterface) {
        final String id = path.getAbsolutePath() + configInterface.getName();
        /*
         * WARNING: as the JSonConfig uses JSonStorage in Background we FIRST
         * get JSonStorage Lock AND then JSonStorage Lock. This avoids deadlocks
         * that could happen if we restore an Json Object which needs to create
         * a JSonConfig during the restore procress
         */try {
            synchronized (JSonStorage.LOCK) {

                ConfigInterface ret = JsonConfig.CACHE.get(id);
                if (ret == null) {
                    final ClassLoader cl = configInterface.getClassLoader();
                    ret = (T) Proxy.newProxyInstance(cl, new Class<?>[] { configInterface }, new StorageHandler<T>(path, configInterface));

                    JsonConfig.CACHE.put(id, ret);
                }
                return (T) ret;

            }
        } catch (final RuntimeException e) {
            Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }

    }

    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final String urlPath, final Class<T> configInterface) {

        final String id = urlPath + configInterface.getName();
        /*
         * WARNING: as the JSonConfig uses JSonStorage in Background we FIRST
         * get JSonStorage Lock AND then JSonStorage Lock. This avoids deadlocks
         * that could happen if we restore an Json Object which needs to create
         * a JSonConfig during the restore procress
         */try {
            synchronized (JSonStorage.LOCK) {

                ConfigInterface ret = JsonConfig.CACHE.get(id);
                if (ret == null) {

                    ret = (T) Proxy.newProxyInstance(configInterface.getClassLoader(), new Class<?>[] { configInterface }, new StorageHandler<T>(urlPath, configInterface));

                    JsonConfig.CACHE.put(id, ret);
                }
                return (T) ret;

            }
        } catch (final RuntimeException e) {
            Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        } catch (final URISyntaxException e) {
            Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
            throw new WTFException(e);
        }
    }

    public static HashMap<String, ConfigInterface> getCache() {
        return JsonConfig.CACHE;
    }

}
