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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;

import org.appwork.utils.IO;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.Dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.Dialog.FileChooserType;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

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

        final File[] sel = Dialog.getInstance().showFileChooser("EventSenderCReater", "Choose folder", FileChooserSelectionMode.DIRECTORIES_ONLY, null, false, FileChooserType.OPEN_DIALOG, rootFile.getParentFile().getParentFile());
        Eventsender.create(name, sel[0]);
        System.exit(1);
    }

    transient protected ArrayList<ListenerType>          addRequestedListeners    = null;
    /**
     * List of registered Eventlistener
     */
    // TODO: DO we really need ArrayLists here?
    transient volatile protected ArrayList<ListenerType> listeners                = null;
    private final Object                                 LOCK                     = new Object();
    private volatile long                                readR                    = 0;
    /**
     * List of Listeners that are requested for removal
     * 
     */
    // We use a removeList to avoid threating problems
    transient protected ArrayList<ListenerType>          removeRequestedListeners = null;

    private volatile long                                writeR                   = 0;

    /**
     * Creates a new Eventsender Instance
     */
    public Eventsender() {
        this.listeners = new ArrayList<ListenerType>();
        this.removeRequestedListeners = new ArrayList<ListenerType>();
        this.addRequestedListeners = new ArrayList<ListenerType>();
    }

    /**
     * Adds a list of listeners
     * 
     * @param listener
     */
    public void addAllListener(final ArrayList<ListenerType> listener) {
        for (final ListenerType l : listener) {
            this.addListener(l);
        }

    }

    /**
     * Add a single Listener
     * 
     * @param listener
     */
    public void addListener(final ListenerType t) {
        synchronized (this.LOCK) {
            /* decrease WriteCounter in case we remove the removeRequested */
            if (this.removeRequestedListeners.contains(t)) {
                this.removeRequestedListeners.remove(t);
                this.writeR--;
            }
            /*
             * increase WriteCounter in case we add addRequestedListeners and t
             * is not in current listeners list
             */
            if (!this.addRequestedListeners.contains(t) && !this.listeners.contains(t)) {
                this.addRequestedListeners.add(t);
                this.writeR++;
            }
        }
    }

    final public void fireEvent(final EventType event) {
        if (event == null) { return; }
        ArrayList<ListenerType> listeners;
        synchronized (this.LOCK) {
            if (this.writeR == this.readR) {
                /* nothing changed, we can use old pointer to listeners */
                if (this.listeners.size() == 0) { return; }
                listeners = this.listeners;
            } else {
                /* create new list with copy of old one */
                listeners = new ArrayList<ListenerType>(this.listeners);
                /* remove and add wished items */
                listeners.removeAll(this.removeRequestedListeners);
                this.removeRequestedListeners.clear();
                listeners.addAll(this.addRequestedListeners);
                this.addRequestedListeners.clear();
                /* update ReadCounter and pointer to listeners */
                this.readR = this.writeR;
                this.listeners = listeners;
                if (this.listeners.size() == 0) { return; }
            }
        }
        for (final ListenerType t : listeners) {
            // final long tt = System.currentTimeMillis();

            this.fireEvent(t, event);
            // System.out.println(t + " " + (System.currentTimeMillis() - tt));
        }
        synchronized (this.LOCK) {
            if (this.writeR != this.readR) {
                /* something changed, lets update the list */
                /* create new list with copy of old one */
                listeners = new ArrayList<ListenerType>(this.listeners);
                /* remove and add wished items */
                listeners.removeAll(this.removeRequestedListeners);
                this.removeRequestedListeners.clear();
                listeners.addAll(this.addRequestedListeners);
                this.addRequestedListeners.clear();
                /* update ReadCounter and pointer to listeners */
                this.readR = this.writeR;
                this.listeners = listeners;
            }
        }
    }

    /**
     * Fires an Event to all registered Listeners
     * 
     * @param event
     * @return
     */
    final public void fireEvent(final int id, final Object... parameters) {

        ArrayList<ListenerType> listeners;
        synchronized (this.LOCK) {
            if (this.writeR == this.readR) {
                /* nothing changed, we can use old pointer to listeners */
                if (this.listeners.size() == 0) { return; }
                listeners = this.listeners;
            } else {
                /* create new list with copy of old one */
                listeners = new ArrayList<ListenerType>(this.listeners);
                /* remove and add wished items */
                listeners.removeAll(this.removeRequestedListeners);
                this.removeRequestedListeners.clear();
                listeners.addAll(this.addRequestedListeners);
                this.addRequestedListeners.clear();
                /* update ReadCounter and pointer to listeners */
                this.readR = this.writeR;
                this.listeners = listeners;
                if (this.listeners.size() == 0) { return; }
            }
        }
        for (final ListenerType t : listeners) {
            this.fireEvent(t, id, parameters);
        }
        synchronized (this.LOCK) {
            if (this.writeR != this.readR) {
                /* something changed, lets update the list */
                /* create new list with copy of old one */
                listeners = new ArrayList<ListenerType>(this.listeners);
                /* remove and add wished items */
                listeners.removeAll(this.removeRequestedListeners);
                this.removeRequestedListeners.clear();
                listeners.addAll(this.addRequestedListeners);
                this.addRequestedListeners.clear();
                /* update ReadCounter and pointer to listeners */
                this.readR = this.writeR;
                this.listeners = listeners;
            }
        }
    }

    /**
     * Abstract fire Event Method.
     * 
     * @param listener
     * @param event
     */
    protected abstract void fireEvent(ListenerType listener, EventType event);

    /**
     * 
     * @param t
     * @param id
     * @param parameters
     */
    protected void fireEvent(final ListenerType listener, final int id, final Object... parameters) {
        throw new RuntimeException("Not implemented. Overwrite org.appwork.utils.event.Eventsender.fireEvent(T, int, Object...) to use this");

    }

    public ArrayList<ListenerType> getListener() {
        synchronized (this.LOCK) {
            return new ArrayList<ListenerType>(this.listeners);
        }
    }

    public boolean hasListener() {
        synchronized (this.LOCK) {
            return this.listeners.size() > 0;
        }
    }

    public void removeListener(final ListenerType t) {
        synchronized (this.LOCK) {
            /* decrease WriteCounter in case we remove the addRequest */
            if (this.addRequestedListeners.contains(t)) {
                this.addRequestedListeners.remove(t);
                this.writeR--;
            }
            /*
             * increase WriteCounter in case we add removeRequest and t is in
             * current listeners list
             */
            if (!this.removeRequestedListeners.contains(t) && this.listeners.contains(t)) {
                this.removeRequestedListeners.add(t);
                this.writeR++;
            }
        }
    }
}
