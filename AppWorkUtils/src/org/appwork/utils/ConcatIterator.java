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

import java.util.Iterator;

/**
 * @author daniel
 * 
 */
public class ConcatIterator<E> implements Iterator<E>, Iterable<E> {

    private final Iterator<E>[] iterators;
    private int                 iteratorIndex = 0;
    private E                   next          = null;
    private boolean             nextSet       = false;

    public ConcatIterator(final Iterator<E>... iterators) {
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        if (this.nextSet) { return true; }
        while (this.iteratorIndex < this.iterators.length) {
            if (this.iterators[this.iteratorIndex].hasNext()) {
                this.next = this.iterators[this.iteratorIndex].next();
                this.nextSet = true;
                return true;
            }
            this.iteratorIndex++;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return this;
    }

    @Override
    public E next() {
        if (this.nextSet) {
            final E ret = this.next;
            this.next = null;
            this.nextSet = false;
            return ret;
        }
        if (this.hasNext()) {
            final E ret = this.next;
            this.next = null;
            this.nextSet = false;
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
    }

}
