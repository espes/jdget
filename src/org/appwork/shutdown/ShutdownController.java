package org.appwork.shutdown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import org.appwork.utils.logging.Log;

public class ShutdownController extends Thread {
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
                System.out.println("DO " + this.getHookPriority());
            }

        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 3;
            }

            @Override
            public void run() {
                System.out.println("DO " + this.getHookPriority());

            }

        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 2;
            }

            @Override
            public void run() {
                System.out.println("DO " + this.getHookPriority());
            }

        });
    }

    private final LinkedList<ShutdownEvent>       hooks;
    private final ArrayList<ShutdownVetoListener> vetoListeners;

    /**
     * Create a new instance of ShutdownController. This is a singleton class.
     * Access the only existing instance by using {@link #getInstance()}.
     */
    private ShutdownController() {
        super(ShutdownController.class.getSimpleName());

        this.hooks = new LinkedList<ShutdownEvent>();
        this.vetoListeners = new ArrayList<ShutdownVetoListener>();
        Runtime.getRuntime().addShutdownHook(this);
    }

    /**
     * @param restartViewUpdaterEvent
     */
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
            System.exit(0);
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
         * to load previous unloaded classes here. For example avoid Log.class
         * here
         */
        try {
            synchronized (this.hooks) {
                for (final ShutdownEvent e : this.hooks) {
                    try {
                        System.out.println("ShutdownController: start item->" + e);
                        final Thread thread = new Thread(e);
                        thread.start();
                        try {
                            thread.join(e.getMaxDuration());
                        } catch (final Throwable e1) {
                            e1.printStackTrace();

                        }
                        if (thread.isAlive()) {
                            System.out.println("ShutdownController: " + e + "->is still running after " + e.getMaxDuration() + " ms");
                            System.out.println("ShutdownController: " + e + "->StackTrace:\r\n" + this.getStackTrace(thread));
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
}
