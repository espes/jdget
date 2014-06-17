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
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.appwork.exceptions.WTFException;
import org.appwork.storage.config.annotations.CustomStorageName;
import org.appwork.storage.config.handler.StorageHandler;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.Dialog;

/**
 * @author thomas
 * 
 */
public class JsonConfig {

    private static class LockObject {
        private final String        id;

        private final AtomicInteger lock           = new AtomicInteger(0);

        private StorageHandler      storageHandler = null;

        private LockObject(final String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public AtomicInteger getLock() {
            return this.lock;
        }

        public StorageHandler getStorageHandler() {
            return this.storageHandler;
        }

        public void setStorageHandler(final StorageHandler storageHandler) {
            this.storageHandler = storageHandler;
        }

    }

    private static final HashMap<String, ConfigInterface> CACHE = new HashMap<String, ConfigInterface>();

    private static final HashMap<String, LockObject>      LOCKS = new HashMap<String, LockObject>();

    /**
     * @param <T>
     * @param class1
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final Class<T> configInterface) {
        String path = configInterface.getName();
        CustomStorageName anno = configInterface.getAnnotation(CustomStorageName.class);
        if (anno != null) {
            path = anno.value();
        }
        synchronized (JsonConfig.CACHE) {
            final ConfigInterface ret = JsonConfig.CACHE.get(path);
            if (ret != null) { return (T) ret; }
        }
        final LockObject lock = JsonConfig.requestLock(path);
        synchronized (lock) {
            try {
                synchronized (JsonConfig.CACHE) {
                    final ConfigInterface ret = JsonConfig.CACHE.get(path);
                    if (ret != null) { return (T) ret; }
                }
                final ClassLoader cl = configInterface.getClassLoader();
                if (lock.getStorageHandler() == null) {
                    lock.setStorageHandler(new StorageHandler<T>(Application.getResource("cfg/" + path), configInterface));
                }
                final T ret = (T) Proxy.newProxyInstance(cl, new Class<?>[] { configInterface }, lock.getStorageHandler());
                synchronized (JsonConfig.CACHE) {
                    if (lock.getLock().get() == 1) {
                        JsonConfig.CACHE.put(path, ret);
                    }
                }
                return ret;
            } catch (final RuntimeException e) {
                e.printStackTrace();
                Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            } finally {
                JsonConfig.unLock(lock);
            }
        }
    }

    /**
     * @param name
     * @param class1
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final File path, final Class<T> configInterface) {
        final String id = path.getAbsolutePath() + configInterface.getName();
        synchronized (JsonConfig.CACHE) {
            final ConfigInterface ret = JsonConfig.CACHE.get(id);
            if (ret != null) { return (T) ret; }
        }
        final LockObject lock = JsonConfig.requestLock(id);
        synchronized (lock) {
            try {
                synchronized (JsonConfig.CACHE) {
                    final ConfigInterface ret = JsonConfig.CACHE.get(id);
                    if (ret != null) { return (T) ret; }
                }
                final ClassLoader cl = configInterface.getClassLoader();
                if (lock.getStorageHandler() == null) {
                    lock.setStorageHandler(new StorageHandler<T>(path, configInterface));
                }
                final T ret = (T) Proxy.newProxyInstance(cl, new Class<?>[] { configInterface }, lock.getStorageHandler());
                synchronized (JsonConfig.CACHE) {
                    if (lock.getLock().get() == 1) {
                        JsonConfig.CACHE.put(id, ret);
                    }
                }
                return ret;
            } catch (final RuntimeException e) {
                Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            } finally {
                JsonConfig.unLock(lock);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final String urlPath, final Class<T> configInterface) {
        final String id = urlPath + configInterface.getName();
        synchronized (JsonConfig.CACHE) {
            final ConfigInterface ret = JsonConfig.CACHE.get(id);
            if (ret != null) { return (T) ret; }
        }
        final LockObject lock = JsonConfig.requestLock(id);
        synchronized (lock) {
            try {
                synchronized (JsonConfig.CACHE) {
                    final ConfigInterface ret = JsonConfig.CACHE.get(id);
                    if (ret != null) { return (T) ret; }
                }
                final ClassLoader cl = configInterface.getClassLoader();
                if (lock.getStorageHandler() == null) {
                    lock.setStorageHandler(new StorageHandler<T>(urlPath, configInterface));
                }
                final T ret = (T) Proxy.newProxyInstance(cl, new Class<?>[] { configInterface }, lock.getStorageHandler());
                synchronized (JsonConfig.CACHE) {
                    if (lock.getLock().get() == 1) {
                        JsonConfig.CACHE.put(id, ret);
                    }
                }
                return ret;
            } catch (final RuntimeException e) {
                Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            } catch (final URISyntaxException e) {
                Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
                throw new WTFException(e);
            } finally {
                JsonConfig.unLock(lock);
            }
        }
    }

    public static HashMap<String, ConfigInterface> getCache() {
        return JsonConfig.CACHE;
    }

    private static synchronized LockObject requestLock(final String id) {
        LockObject lockObject = JsonConfig.LOCKS.get(id);
        if (lockObject == null) {
            lockObject = new LockObject(id);
            JsonConfig.LOCKS.put(id, lockObject);
        }
        lockObject.getLock().incrementAndGet();
        return lockObject;
    }

    private static synchronized void unLock(final LockObject lock) {
        final LockObject lockObject = JsonConfig.LOCKS.get(lock.getId());
        if (lockObject != null) {
            if (lockObject.getLock().decrementAndGet() == 0) {
                JsonConfig.LOCKS.remove(lock.getId());
            }
        }
    }

}
