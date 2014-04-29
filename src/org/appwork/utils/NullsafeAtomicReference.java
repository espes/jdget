/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author daniel
 * 
 *         NullSafeAtomicReference
 * 
 *         AtomicReference which can differ between set/null/empty (tri-state)
 * 
 */
public class NullsafeAtomicReference<V> {

    private static final NullObject       NULL      = new NullObject();
    private static final Object           EMPTY     = new Object();
    private final AtomicReference<Object> reference = new AtomicReference<Object>(NullsafeAtomicReference.EMPTY);

    public NullsafeAtomicReference() {
    }

    public NullsafeAtomicReference(final V initialValue) {
        this.set(initialValue);
    }

    public final boolean compareAndSet(final V expect, final V update) {
        Object nullSafeUpdate = update;
        if (NullsafeAtomicReference.NULL.equals(nullSafeUpdate)) {
            nullSafeUpdate = NullsafeAtomicReference.NULL;
        }
        if (NullsafeAtomicReference.NULL.equals(expect)) {
            return this.reference.compareAndSet(NullsafeAtomicReference.NULL, nullSafeUpdate) || this.reference.compareAndSet(NullsafeAtomicReference.EMPTY, nullSafeUpdate);
        } else {
            return this.reference.compareAndSet(expect, nullSafeUpdate);
        }

    }

    public final V get() {
        final Object get = this.reference.get();
        if (NullsafeAtomicReference.NULL.equals(get) || NullsafeAtomicReference.EMPTY == get) { return null; }
        return (V) get;
    }

    public V getAndClear() {
        final Object get = this.reference.getAndSet(NullsafeAtomicReference.EMPTY);
        if (NullsafeAtomicReference.NULL.equals(get) || NullsafeAtomicReference.EMPTY == get) { return null; }
        return (V) get;
    }

    public final V getAndSet(final V newValue) {
        Object set = newValue;
        if (NullsafeAtomicReference.NULL.equals(set)) {
            set = NullsafeAtomicReference.NULL;
        }
        final Object get = this.reference.getAndSet(set);
        if (NullsafeAtomicReference.NULL.equals(get) || NullsafeAtomicReference.EMPTY == get) { return null; }
        return (V) get;
    }

    public boolean isValueSet() {
        return this.reference.get() != NullsafeAtomicReference.EMPTY;
    }

    public final void set(final V newValue) {
        if (NullsafeAtomicReference.NULL.equals(newValue)) {
            this.reference.set(NullsafeAtomicReference.NULL);
        } else {
            this.reference.set(newValue);
        }

    }

}
