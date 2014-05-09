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

/**
 * @author daniel
 * 
 *         Helper NullObject for AtomicReference, because null can have multiple
 *         memory references (depends on jvm) so that
 *         AtomicReference.compareAndSet(null,xy) can return false even when it
 *         is null!
 */
public final class NullObject {

    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof NullObject) { return true; }
        if (obj == this) { return true; }
        if (obj == null) { return true; }
        return false;
    }
}
