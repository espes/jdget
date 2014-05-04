/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Daniel Wilhelm
 * 
 */
public class WeakHashSet<E> extends AbstractSet<E> implements Set<E> {

    protected ReferenceQueue<Object>                          queue = new ReferenceQueue<Object>();
    protected HashMap<WeakHashSetElement, WeakHashSetElement> map   = new HashMap<WeakHashSetElement, WeakHashSetElement>();

    @Override
    public boolean contains(Object o) {
        return map.containsKey(WeakHashSetElement.create(o));
    }

    @Override
    public boolean add(E e) {
        cleanUp();
        WeakHashSetElement item = WeakHashSetElement.create(e, queue);
        return map.put(item, item) == null;
    }

    @SuppressWarnings("unchecked")
    public E getDuplicateOrAdd(E e) {
        WeakHashSetElement item = WeakHashSetElement.create(e);
        WeakHashSetElement exists = map.get(item);
        Object ret = null;
        if (exists != null && (ret = exists.get()) != null) {            
            return (E) ret;
        }
        cleanUp();
        map.put(item, item);
        return e;
    }

    @Override
    public boolean remove(Object o) {
        WeakHashSetElement removeItem = WeakHashSetElement.create(o);
        WeakHashSetElement removedItem = map.remove(removeItem);
        cleanUp();
        if (removedItem == null) return false;
        return removedItem.equals(removeItem);
    }

    private void cleanUp() {
        Object item = null;
        while ((item = queue.poll()) != null) {
            map.remove((WeakHashSetElement) item);
        }
    }

    static private class WeakHashSetElement extends WeakReference<Object> {

        private int weakHashSetElementHash = -1;

        private WeakHashSetElement(Object o) {
            super(o);
            weakHashSetElementHash = o.hashCode();
        }

        private WeakHashSetElement(Object o, ReferenceQueue<Object> q) {
            super(o, q);
            weakHashSetElementHash = o.hashCode();
        }

        private static WeakHashSetElement create(Object o) {
            return (o == null) ? null : new WeakHashSetElement(o);
        }

        private static WeakHashSetElement create(Object o, ReferenceQueue<Object> q) {
            return (o == null) ? null : new WeakHashSetElement(o, q);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WeakHashSetElement)) return false;
            Object t = this.get();
            Object u = ((WeakHashSetElement) o).get();
            if (t == u) return true;
            if ((t == null) || (u == null)) return false;
            return t.equals(u);
        }

        @Override
        public int hashCode() {
            return weakHashSetElementHash;
        }

    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<WeakHashSetElement> i = map.keySet().iterator();

        return new Iterator<E>() {
            public boolean hasNext() {
                return i.hasNext();
            }

            public E next() {
                return (E) ((WeakHashSetElement) i.next()).get();
            }

            public void remove() {
                i.remove();
            }
        };
    }

    @Override
    public int size() {
        return map.size();
    }

    public static void main(String[] args) {
        java.util.List<String> strong = new ArrayList<String>();
        WeakHashSet<String> test = new WeakHashSet<String>();
        {
            for (int i = 1; i < 1000000; i++) {
                String strongItem = "" + i;
                strong.add(strongItem);
                test.add(strongItem);
                System.out.println(test.size());
            }
        }

    }
}
