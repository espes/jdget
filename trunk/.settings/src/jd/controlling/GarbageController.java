package jd.controlling;

import jd.nutils.Formatter;

//final, because the constructor calls Thread.start(),
//see http://findbugs.sourceforge.net/bugDescriptions.html#SC_START_IN_CTOR
public final class GarbageController implements Runnable {

    private static final GarbageController INSTANCE    = new GarbageController();

    private static long                    GCTimeout   = 10 * 60 * 1000;
    private static long                    GCFactor    = 30 * 1000;

    private static long                    gcRequested = 0;

    private Thread                         thread      = null;

    private boolean                        running     = false;

    public static GarbageController getInstance() {
        return INSTANCE;
    }

    private GarbageController() {
        thread = null;
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public static synchronized void requestGC() {
        gcRequested++;
    }

    public void run() {
        try {
            synchronized (this) {
                if (running) return;
                running = true;
            }
            long nextGC = 0;
            while (true) {
                synchronized (this) {
                    nextGC -= gcRequested * GCFactor;
                    gcRequested = 0;
                }
                try {
                    Thread.sleep(10000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                nextGC -= 10000;
                if (nextGC <= 0) {
                    final long before = Runtime.getRuntime().totalMemory();
                    Runtime.getRuntime().gc();
                    Runtime.getRuntime().runFinalization();
                    Runtime.getRuntime().gc();
                    final long now = Runtime.getRuntime().totalMemory();
                    JDLogger.getLogger().info("GCed: before: " + Formatter.formatReadable(before) + " now: " + Formatter.formatReadable(now) + " freed: " + Formatter.formatReadable(Math.max(0, before - now)));
                    synchronized (this) {
                        nextGC = GCTimeout;
                        gcRequested = 0;
                    }
                }
            }
        } finally {
            synchronized (this) {
                running = false;
            }
        }

    }
}
