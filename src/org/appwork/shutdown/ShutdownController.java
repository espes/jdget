package org.appwork.shutdown;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.appwork.utils.logging.Log;

public class ShutdownController extends Thread {
    class ShutdownEventWrapper extends ShutdownEvent {

        private final Thread orgThread;

        /**
         * @param value
         */
        public ShutdownEventWrapper(final Thread value) {
            this.orgThread = value;
            // call "Nativ" hooks at the end.
            this.setHookPriority(Integer.MIN_VALUE);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ShutdownEventWrapper) { return this.orgThread == ((ShutdownEventWrapper) obj).orgThread; }
            return false;
        }

        @Override
        public int hashCode() {

            return this.orgThread.hashCode();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.appwork.shutdown.ShutdownEvent#run()
         */
        @Override
        public void run() {
            this.orgThread.run();
        }

        @Override
        public String toString() {
            return "ShutdownEventWrapper " + this.orgThread + " - " + this.orgThread.getClass().getName() + " Priority: " + this.getHookPriority();
        }

    }

    private static final ShutdownController INSTANCE = new ShutdownController();

    /**
     * get the only existing instance of ShutdownController. This is a singleton
     * 
     * @return
     */
    public static ShutdownController getInstance() {
        return ShutdownController.INSTANCE;
    }

    public static void main(final String[] args) {
        Log.L.setLevel(Level.ALL);
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 1;
            }

            @Override
            public void run() {
                Log.L.finest("DO " + this.getHookPriority());
            }

        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 3;
            }

            @Override
            public void run() {
                Log.L.finest("DO " + this.getHookPriority());

            }

        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 2;
            }

            @Override
            public void run() {
                Log.L.finest("DO " + this.getHookPriority());
            }

        });
    }

    private final LinkedList<ShutdownEvent>       hooks;
    private final ArrayList<ShutdownVetoListener> vetoListeners;

    private int                                   exitCode = 0;

    /**
     * Create a new instance of ShutdownController. This is a singleton class.
     * Access the only existing instance by using {@link #getInstance()}.
     */
    private ShutdownController() {
        super(ShutdownController.class.getSimpleName());

        this.hooks = new LinkedList<ShutdownEvent>();
        this.vetoListeners = new ArrayList<ShutdownVetoListener>();
        try {
            // first try to hook in the original hooks manager
            // to "disable" the original hook manager, we overwrite the actual
            // hook list with our own one, and redirect all registered hooks to
            // ShutdownController.
            // this may fail (reflektion). As a fallback we just add
            // Shutdowncontroller as a normal hook.

            final IdentityHashMap<Thread, Thread> hookDelegater = new IdentityHashMap<Thread, Thread>() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 8334628124340671103L;

                {
                    // SHutdowncontroller should be the only hook!!
                    super.put(ShutdownController.this, ShutdownController.this);
                }

                @Override
                public Thread put(final Thread key, final Thread value) {
                    final ShutdownEventWrapper hook = new ShutdownEventWrapper(value);

                    ShutdownController.this.addShutdownEvent(hook);
                    return null;
                }

                @Override
                public Thread remove(final Object key) {
                    ShutdownController.this.removeShutdownEvent(new ShutdownEventWrapper((Thread) key));
                    return (Thread) key;
                }
            };

            final Field field = Class.forName("java.lang.ApplicationShutdownHooks").getDeclaredField("hooks");
            field.setAccessible(true);
            final Map<Thread, Thread> hooks = (Map<Thread, Thread>) field.get(null);
            synchronized (hooks) {

                final Set<Thread> threads = hooks.keySet();

                for (final Thread hook : threads) {
                    this.addShutdownEvent(new ShutdownEventWrapper(hook));

                }
                field.set(null, hookDelegater);
            }

        } catch (final Throwable e) {
            Log.exception(e);
            Runtime.getRuntime().addShutdownHook(this);
        }

        // do not remove the Log call here. we have to be sure that Log.class is
        // already loaded
        Log.L.finest("Init ShutdownController");

    }

    public void addShutdownEvent(final ShutdownEvent event) {
        if (this.isAlive()) { throw new IllegalStateException("Cannot add hooks during shutdown"); }
        synchronized (this.hooks) {
            ShutdownEvent next;
            int i = 0;
            // add event sorted
            for (final Iterator<ShutdownEvent> it = this.hooks.iterator(); it.hasNext();) {
                next = it.next();
                if (next.getHookPriority() <= event.getHookPriority()) {
                    this.hooks.add(i, event);
                    return;
                }
                i++;
            }
            this.hooks.add(event);
        }

    }

    public void addShutdownVetoListener(final ShutdownVetoListener listener) {
        synchronized (this.vetoListeners) {
            this.vetoListeners.remove(listener);
            this.vetoListeners.add(listener);
        }
    }

    /**
     * @return
     */
    public ArrayList<ShutdownVetoException> collectVetos() {
        final ArrayList<ShutdownVetoException> vetos = new ArrayList<ShutdownVetoException>();
        synchronized (this.vetoListeners) {
            for (final ShutdownVetoListener v : this.vetoListeners) {
                try {
                    v.onShutdownRequest();
                } catch (final ShutdownVetoException e) {
                    vetos.add(e);
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return vetos;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * Same function as org.appwork.utils.Exceptions.getStackTrace(Throwable)<br>
     * <b>DO NOT REPLACE IT EITHER!</b> Exceptions.class my be unloaded. This
     * would cause Initialize Exceptions during shutdown.
     * 
     * @param thread
     * @return
     */
    private String getStackTrace(final Thread thread) {
        try {
            final StackTraceElement[] st = thread.getStackTrace();
            final StringBuilder sb = new StringBuilder("");
            for (final StackTraceElement element : st) {
                sb.append(element);
                sb.append("\r\n");
            }
            return sb.toString();
        } catch (final Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param instance2
     * @return
     */
    public boolean hasShutdownEvent(final ShutdownEvent instance2) {
        synchronized (this.hooks) {
            return this.hooks.contains(instance2);
        }
    }

    public void removeShutdownEvent(final ShutdownEvent event) {
        if (this.isAlive()) { throw new IllegalStateException("Cannot add hooks during shutdown"); }
        synchronized (this.hooks) {
            ShutdownEvent next;

            // add event sorted
            for (final Iterator<ShutdownEvent> it = this.hooks.iterator(); it.hasNext();) {
                next = it.next();
                if (next == event) {
                    it.remove();
                }

            }
        }
    }

    public void removeShutdownVetoListener(final ShutdownVetoListener listener) {
        synchronized (this.vetoListeners) {
            this.vetoListeners.remove(listener);
        }
    }

    /**
     * 
     */
    public void requestShutdown() {
        final ArrayList<ShutdownVetoException> vetos = this.collectVetos();
        if (vetos.size() == 0) {
            synchronized (this.vetoListeners) {
                for (final ShutdownVetoListener v : this.vetoListeners) {
                    try {
                        v.onShutdown();
                    } catch (final Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            final Thread th = new Thread("ShutdownThread") {
                @Override
                public void run() {
                    System.exit(ShutdownController.this.getExitCode());
                }
            };

            th.start();
            while (th.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    return;
                }
            }
            Log.L.finest("DONE");
        } else {
            synchronized (this.vetoListeners) {
                for (final ShutdownVetoListener v : this.vetoListeners) {
                    v.onShutdownVeto(vetos);
                }
            }
        }

    }

    @Override
    public void run() {
        /*
         * Attention. This runs in shutdownhook. make sure, that we do not have
         * to load previous unloaded classes here.
         */

        try {
            synchronized (this.hooks) {
                int i = 0;
                for (final ShutdownEvent e : this.hooks) {
                    try {
                        i++;
                        final long started = System.currentTimeMillis();

                        Log.L.finest("[" + i + "/" + this.hooks.size() + "|Priority: " + e.getHookPriority() + "]" + "ShutdownController: start item->" + e);
                        final Thread thread = new Thread(e);
                        thread.setName("ShutdownHook [" + i + "/" + this.hooks.size() + "|Priority: " + e.getHookPriority() + "]");
                        thread.start();
                        try {
                            thread.join(e.getMaxDuration());
                        } catch (final Throwable e1) {
                            e1.printStackTrace();

                        }
                        if (thread.isAlive()) {
                            Log.L.finest("[" + i + "/" + this.hooks.size() + "|Priority: " + e.getHookPriority() + "]" + "ShutdownController: " + e + "->is still running after " + e.getMaxDuration() + " ms");
                            Log.L.finest("[" + i + "/" + this.hooks.size() + "|Priority: " + e.getHookPriority() + "]" + "ShutdownController: " + e + "->StackTrace:\r\n" + this.getStackTrace(thread));
                        } else {
                            Log.L.finest("[" + i + "/" + this.hooks.size() + "|Priority: " + e.getHookPriority() + "]" + "ShutdownController: item ended after->" + (System.currentTimeMillis() - started));
                        }
                    } catch (final Throwable e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (final Throwable e1) {
            // do not use Log here. If Log.exception(e1); throws an exception,
            // we have to catch it here without the risk of another exception.

        }
    }

    /**
     * @param i
     */
    public void setExitCode(final int i) {
        this.exitCode = i;

    }
}
