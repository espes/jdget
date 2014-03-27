/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.appwork.utils.IO;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.ExtFileChooserDialog;
import org.appwork.utils.swing.dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.FileChooserType;

/**
 * The Eventsenderclass is the core of the Eventsystem. it can be used to design
 * new Eventbroadcaster Systems easily.
 * 
 * Guidelines:<br>
 * 1. CReate a new MyEventSender extends
 * org.appwork.utils.event.Eventsender<ListenerType, EventType> <br>
 * 2. Create MyListenerType extends java.util.EventListener<br>
 * 3. CReate MyEvent extends org.appwork.utils.event.SimpleEvent<CallerType,
 * ParameterType, TypeEnumType><br>
 * 
 * <br>
 * TypeEnumType is usually a intern enum which defines all available eventtypes
 * 
 * @author $Author: unknown$
 * 
 */

public abstract class Eventsender<ListenerType extends EventListener, EventType extends DefaultEvent> {
    /**
     * @param name
     * @param file
     * @throws IOException
     */
    private static void create(final String name, final File file) throws IOException {
        String pkg = "";
        System.out.println("");
        File p = file;
        do {
            if (pkg.length() > 0) {
                pkg = "." + pkg;
            }
            pkg = p.getName() + pkg;

        } while ((p = p.getParentFile()) != null && !p.getName().equals("src"));

        StringBuilder sb = new StringBuilder();
        final String senderName = name + "EventSender";
        final String eventName = name + "Event";
        final String listenerName = name + "Listener";

        sb.append("package " + pkg + ";\r\n\r\n");
        sb.append("import org.appwork.utils.event.Eventsender;\r\n\r\n");
        sb.append("public class " + senderName + " extends Eventsender<" + listenerName + ", " + eventName + "> {\r\n\r\n");
        sb.append("@Override\r\n");
        sb.append("protected void fireEvent(" + listenerName + " listener, " + eventName + " event) {\r\nswitch (event.getType()) {\r\n//fill\r\ndefault: System.out.println(\"Unhandled Event: \"+event); \r\n}\r\n}");
        sb.append("}");
        new File(file, senderName + ".java").delete();
        IO.writeStringToFile(new File(file, senderName + ".java"), sb.toString());
        sb = new StringBuilder();

        sb.append("package " + pkg + ";\r\n\r\n");
        sb.append("import java.util.EventListener;\r\n\r\n");
        sb.append("public interface " + listenerName + " extends EventListener {\r\n\r\n}");
        new File(file, listenerName + ".java").delete();
        IO.writeStringToFile(new File(file, listenerName + ".java"), sb.toString());

        sb = new StringBuilder();
        sb.append("package " + pkg + ";\r\n\r\n");
        sb.append("import org.appwork.utils.event.SimpleEvent;\r\n\r\n");
        sb.append("public class " + eventName + " extends SimpleEvent<Object, Object, " + eventName + ".Type> {\r\n\r\n");
        sb.append("public static enum Type{\r\n}\r\n");
        sb.append("public " + eventName + "(Object caller, Type type, Object... parameters) {\r\n");
        sb.append("super(caller, type, parameters);\r\n}\r\n");
        sb.append("}");
        new File(file, eventName + ".java").delete();
        IO.writeStringToFile(new File(file, eventName + ".java"), sb.toString());

    }

    public static void main(final String[] args) throws DialogClosedException, DialogCanceledException, IOException, URISyntaxException {
        final URL root = Thread.currentThread().getClass().getResource("/");
        final File rootFile = new File(root.toURI());
        final String name = Dialog.getInstance().showInputDialog("Enter Name");

        final ExtFileChooserDialog d = new ExtFileChooserDialog(0, "Choose folder", null, null);
        d.setStorageID("EventSenderCReater");
        d.setFileSelectionMode(FileChooserSelectionMode.DIRECTORIES_ONLY);

        d.setType(FileChooserType.OPEN_DIALOG);
        d.setMultiSelection(false);
        d.setPreSelection(rootFile.getParentFile().getParentFile());
        try {
            Dialog.I().showDialog(d);
        } catch (final DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Eventsender.create(name, d.getSelectedFile());
        System.exit(1);
    }

    /**
     * List of registered Eventlistener
     */

    transient volatile protected Set<EventSuppressor<EventType>>    eventSuppressors = new CopyOnWriteArraySet<EventSuppressor<EventType>>();
    transient volatile protected List<ListenerType>                strongListeners  = null;
    transient volatile protected List<WeakReference<ListenerType>> weakListener     = null;

    private final Object                                           LOCK             = new Object();

    /**
     * List of Listeners that are requested for removal
     * 
     */

    /**
     * Creates a new Eventsender Instance
     */
    public Eventsender() {
        this.strongListeners = new ArrayList<ListenerType>();
        this.weakListener = new ArrayList<WeakReference<ListenerType>>();
    }

    /**
     * Adds a list of listeners
     * 
     * @param listener
     */
    public void addAllListener(final java.util.List<ListenerType> listener) {
        this.addAllListener(listener, false);
    }

    public void addAllListener(final java.util.List<ListenerType> listener, final boolean weak) {
        for (final ListenerType l : listener) {
            this.addListener(l, weak);
        }
    }

    public void addEventSuppressor(final EventSuppressor<EventType> eventSuppressor) {
        if (eventSuppressor != null) {
            this.eventSuppressors.add(eventSuppressor);
        }
    }

    public void addListener(final ListenerType t) {
        this.addListener(t, false);
    }

    /**
     * Add a single Listener
     * 
     * @param listener
     */
    public void addListener(final ListenerType t, final boolean weak) {
        if (t == null) { return; }
        synchronized (this.LOCK) {
            boolean added = false;
            if (weak == false) {
                /* update strong listeners */
                final java.util.List<ListenerType> newStrongListener = new ArrayList<ListenerType>(this.strongListeners);
                if (!newStrongListener.contains(t)) {
                    newStrongListener.add(t);
                }
                this.strongListeners = newStrongListener;
            }
            /* update weak listeners */
            ListenerType l = null;
            final java.util.List<WeakReference<ListenerType>> newWeakListener = new ArrayList<WeakReference<ListenerType>>(this.weakListener.size());
            for (final WeakReference<ListenerType> listener : this.weakListener) {
                if ((l = listener.get()) == null) {
                    /* remove weak listener because it is gone */
                } else if (l == t) {
                    /* list already contains t, no need to add it again */
                    added = true;
                    newWeakListener.add(listener);
                } else {
                    newWeakListener.add(listener);
                }
            }
            if (added == false) {
                newWeakListener.add(new WeakReference<ListenerType>(t));
            }
            this.weakListener = newWeakListener;
        }
    }

    public void cleanup() {
        synchronized (this.LOCK) {
            final java.util.List<WeakReference<ListenerType>> newWeakListener = new ArrayList<WeakReference<ListenerType>>(this.weakListener.size());
            for (final WeakReference<ListenerType> listener : this.weakListener) {
                if (listener.get() == null) {
                    /* weak item is gone */
                    continue;
                } else {
                    newWeakListener.add(listener);
                }
            }
            this.weakListener = newWeakListener;
        }
    }

    public boolean containsListener(final ListenerType t) {
        if (t == null) { return false; }
        synchronized (this.LOCK) {
            for (final ListenerType tmp : this.strongListeners) {
                if (tmp == t) { return true; }
            }
            ListenerType l = null;
            for (final WeakReference<ListenerType> listener : this.weakListener) {
                if ((l = listener.get()) == null) {
                    /* weak item is gone */
                    continue;
                } else if (l == t) { return true; }
            }
            return false;
        }
    }

    final public void fireEvent(final EventType event) {
        if (event == null) { return; }
        for (final EventSuppressor<EventType> eventSuppressor : this.eventSuppressors) {
            if (eventSuppressor.suppressEvent(event)) { return; }
        }
        ListenerType t = null;
        boolean cleanup = false;
        final java.util.List<WeakReference<ListenerType>> listeners = this.weakListener;
        for (final WeakReference<ListenerType> listener : listeners) {
            t = listener.get();
            if (t == null) {
                cleanup = true;
                continue;
            }
            this.fireEvent(t, event);
        }
        if (cleanup && listeners.size() > 0) {
            this.cleanup();
        }
    }

    /**
     * Abstract fire Event Method.
     * 
     * @param listener
     * @param event
     */
    protected abstract void fireEvent(ListenerType listener, EventType event);

    public java.util.List<ListenerType> getListener() {
        final java.util.List<WeakReference<ListenerType>> listeners = this.weakListener;
        boolean cleanup = true;
        final java.util.List<ListenerType> ret = new ArrayList<ListenerType>(listeners.size());
        ListenerType t = null;
        for (final WeakReference<ListenerType> listener : listeners) {
            t = listener.get();
            if (t != null) {
                ret.add(t);
            } else {
                cleanup = true;
            }
        }
        if (cleanup && listeners.size() > 0) {
            this.cleanup();
        }
        return ret;
    }

    public boolean hasListener() {
        final java.util.List<WeakReference<ListenerType>> listeners = this.weakListener;
        for (final WeakReference<ListenerType> listener : listeners) {
            if (listener.get() != null) { return true; }
        }
        return false;
    }

    public void removeEventSuppressor(final EventSuppressor<EventType> eventSuppressor) {
        if (eventSuppressor != null) {
            this.eventSuppressors.remove(eventSuppressor);
        }
    }

    public void removeListener(final ListenerType t) {
        if (t == null) { return; }
        synchronized (this.LOCK) {
            ListenerType l = null;
            final java.util.List<WeakReference<ListenerType>> newWeakListener = new ArrayList<WeakReference<ListenerType>>(this.weakListener.size());
            final java.util.List<ListenerType> newStrongListener = new ArrayList<ListenerType>(this.strongListeners);
            for (final WeakReference<ListenerType> listener : this.weakListener) {
                if ((l = listener.get()) == null) {
                    /* weak item is gone */
                    continue;
                } else if (l != t) {
                    newWeakListener.add(listener);
                }
            }
            /* remove strong item */
            newStrongListener.remove(t);
            this.weakListener = newWeakListener;
            this.strongListeners = newStrongListener;
        }
    }
}
