/**
 * 
 */
package org.appwork.utils.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.logging.Level;

import org.appwork.utils.Application;
import org.appwork.utils.Hash;
import org.appwork.utils.logging.Log;

import com.sun.nio.file.ExtendedWatchEventModifier;

/**
 * @author $Author: unknown$
 * 
 */
public abstract class J7FileObserver implements Runnable {

    public static void main(final String s[]) throws IOException, InterruptedException {

        final J7FileObserver o = new J7FileObserver("file.txt", null) {

            @Override
            public void onFound(final File file) {
                System.out.println("Found " + file);
            }
        };

        o.start();

        Thread.sleep(150000);
        o.stop();

    }

    private Thread              runner;
    private final String        filename;
    private final String        hash;
    private java.util.List<WatchKey> keys;

    /**
     * @param string
     * @param object
     */
    public J7FileObserver(final String name, final String hash) {
        filename = name;
        this.hash = hash;
        if (Application.getJavaVersion() < Application.JAVA17 /*
                                                     * ||
                                                     * !CrossSystem.isWindows()
                                                     */) { throw new IllegalStateException("This Class is Java 1.7 and Windows only"); }
    }

    public abstract void onFound(File file);

    @Override
    public void run() {
        WatchService watcher;
        keys = new ArrayList<WatchKey>();
        try {
            watcher = FileSystems.getDefault().newWatchService();

            for (final Path next : FileSystems.getDefault().getRootDirectories()) {
                try {
                    keys.add(next.register(watcher, new WatchEvent.Kind[] { java.nio.file.StandardWatchEventKinds.ENTRY_CREATE }, ExtendedWatchEventModifier.FILE_TREE));

                } catch (final Throwable e) {

                }
            }

            for (;;) {

                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (final InterruptedException x) {
                    return;
                }

                for (final WatchEvent<?> event : key.pollEvents()) {
                    final WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only for ENTRY_CREATE events,
                    // but an OVERFLOW event can occur regardless if events are
                    // lost or discarded.
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // The filename is the context of the event.
                    final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    final Path filename = ev.context();
                    final Path abp = ((Path) key.watchable()).resolve(filename);
                    // System.out.println(this.filename + " Created abp " +
                    // abp);
                    if (abp.getFileName().toString().equals(this.filename)) {
                        for (int i = 0; i < 5; i++) {

                            // avoid java.io.FileNotFoundException:
                            // C:\test\Bilder\1312445939619.tmp (Der Prozess
                            // kann nicht auf die Datei zugreifen, da sie
                            // von
                            // einem anderen Prozess verwendet wird)

                            final String localHash = Hash.getMD5(abp.toFile());
                            if (localHash == null && hash != null) {
                                try {
                                    Thread.sleep(200);
                                } catch (final InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                continue;
                            }
                            System.out.println(hash + " - " + localHash);
                            if (hash == null || hash.equals(localHash)) {
                                onFound(abp.toFile());
                                return;
                            }

                        }
                    }
                    if (!key.reset()) {
                        System.out.println("Key " + key + " is invalid" + key.watchable());
                    }

                }

            }
        } catch (final IOException e) {
            Log.exception(Level.WARNING, e);

        }

    }

    /**
     * 
     */
    public void start() {
        if (runner != null) { throw new IllegalStateException("Already running"); }
        runner = new Thread(this);
        runner.start();
    }

    /**
     * 
     */
    public void stop() {
        if (runner != null) {
            runner.interrupt();
            runner = null;
        }
        for (final WatchKey w : keys) {
            w.cancel();
        }

    }
}
