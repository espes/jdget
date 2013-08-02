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
 *         Nullsafe AtomicReference, because null can have multiple memory
 *         references (depends on jvm) so that
 *         AtomicReference.compareAndSet(null,xy) can return false even when it
 *         is null!
 * 
 */
public class NullsafeAtomicReference<V> {

    private static final NullObject       NULL      = new NullObject();

    private final AtomicReference<Object> reference = new AtomicReference<Object>();

    public NullsafeAtomicReference() {
        this(null);
    }

    public NullsafeAtomicReference(final V initialValue) {
        if (NullsafeAtomicReference.NULL.equals(initialValue)) {
            this.reference.set(NullsafeAtomicReference.NULL);
        } else {
            this.reference.set(initialValue);
        }
    }

    public final boolean compareAndSet(final V expect, final V update) {
        Object a = expect;
        Object b = update;
        if (NullsafeAtomicReference.NULL.equals(a)) {
            a = NullsafeAtomicReference.NULL;
        }
        if (NullsafeAtomicReference.NULL.equals(b)) {
            b = NullsafeAtomicReference.NULL;
        }
        return this.reference.compareAndSet(a, b);
    }

    public final V get() {
        final Object ret = this.reference.get();
        if (NullsafeAtomicReference.NULL.equals(ret)) { return null; }
        return (V) ret;
    }

    public final V getAndSet(final V newValue) {
        while (true) {
            final V x = this.get();
            if (this.compareAndSet(x, newValue)) { return x; }
        }
    }

    public final void set(final V newValue) {
        Object a = newValue;
        if (NullsafeAtomicReference.NULL.equals(a)) {
            a = NullsafeAtomicReference.NULL;
        }
        this.reference.set(a);
    }

}
