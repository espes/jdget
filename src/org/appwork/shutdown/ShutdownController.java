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
                System.out.println("DO " + getHookPriority());
            }

        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 3;
            }

            @Override
            public void run() {
                System.out.println("DO " + getHookPriority());

            }

        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 2;
            }

            @Override
            public void run() {
                System.out.println("DO " + getHookPriority());
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

        hooks = new LinkedList<ShutdownEvent>();
        vetoListeners = new ArrayList<ShutdownVetoListener>();
        Runtime.getRuntime().addShutdownHook(this);
    }

    /**
     * @param restartViewUpdaterEvent
     */
    public void addShutdownEvent(final ShutdownEvent event) {
        if (isAlive()) { throw new IllegalStateException("Cannot add hooks during shutdown"); }
        synchronized (hooks) {
            ShutdownEvent next;
            int i = 0;
            // add event sorted
            for (final Iterator<ShutdownEvent> it = hooks.iterator(); it.hasNext();) {
                next = it.next();

                if (next.getHookPriority() <= event.getHookPriority()) {
                    hooks.add(i, event);
                    return;
                }
                i++;

            }
            hooks.add(event);

        }

    }

    public void addShutdownVetoListener(final ShutdownVetoListener listener) {
        synchronized (vetoListeners) {
            vetoListeners.remove(listener);
            vetoListeners.add(listener);
        }
    }

    /**
     * @return
     */
    public ArrayList<ShutdownVetoException> collectVetos() {
        final ArrayList<ShutdownVetoException> vetos = new ArrayList<ShutdownVetoException>();
        synchronized (vetoListeners) {
            for (final ShutdownVetoListener v : vetoListeners) {
                try {
                    v.onShutdownRequest();

                } catch (final ShutdownVetoException e) {
                    vetos.add(e);
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

    public void removeShutdownEvent(final ShutdownEvent event) {
        if (isAlive()) { throw new IllegalStateException("Cannot add hooks during shutdown"); }
        synchronized (hooks) {
            ShutdownEvent next;

            // add event sorted
            for (final Iterator<ShutdownEvent> it = hooks.iterator(); it.hasNext();) {
                next = it.next();
                if (next == event) {
                    it.remove();
                }

            }
        }
    }

    public void removeShutdownVetoListener(final ShutdownVetoListener listener) {
        synchronized (vetoListeners) {
            vetoListeners.remove(listener);

        }
    }

    /**
     * 
     */
    public void requestShutdown() {
        final ArrayList<ShutdownVetoException> vetos = collectVetos();
        if (vetos.size() == 0) {
            System.exit(0);
        } else {
            synchronized (vetoListeners) {
                for (final ShutdownVetoListener v : vetoListeners) {
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
            synchronized (hooks) {
                for (final ShutdownEvent e : hooks) {
                    try {
                        System.out.println("ShutdownController: start item" + e);
                        e.start();
                        try {
                            e.join(e.getMaxDuration());
                        } catch (final Throwable e1) {
                            e1.printStackTrace();

                        }
                        if (e.isAlive()) {
                            System.out.println("ShutdownController: " + e + " is still running after " + e.getMaxDuration() + " ms");
                            System.out.println("ShutdownController: " + e + " StackTrace:\r\n" + getStackTrace(e));
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
