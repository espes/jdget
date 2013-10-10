/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.controlling
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.controlling;

import java.util.ArrayList;

import org.appwork.utils.NullsafeAtomicReference;
import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public class SingleReachableState {

    private final NullsafeAtomicReference<ArrayList<Runnable>> stateMachine;

    private final String                                       name;

    public SingleReachableState(final String name) {
        this.stateMachine = new NullsafeAtomicReference<ArrayList<Runnable>>(new ArrayList<Runnable>());
        this.name = name;
    }

    public void executeWhen(final Runnable reached, final Runnable notreached) {
        if (reached == null && notreached == null) { return; }
        while (true) {
            final ArrayList<Runnable> runnables = this.stateMachine.get();
            if (runnables == null) {
                this.run(reached);
                return;
            }
            if (reached != null) {
                final ArrayList<Runnable> newRunnables = new ArrayList<Runnable>(runnables);
                newRunnables.add(reached);
                if (this.stateMachine.compareAndSet(runnables, newRunnables)) {
                    this.run(notreached);
                    return;
                }
            } else {
                this.run(notreached);
                return;
            }
        }
    }

    public void executeWhenReached(final Runnable run) {
        this.executeWhen(run, null);
    }

    public boolean isReached() {
        return this.stateMachine.get() == null;
    }

    private void run(final Runnable run) {
        try {
            if (run != null) {
                run.run();
            }
        } catch (final Throwable e) {
            Log.exception(e);
        }
    }

    public void setReached() {
        final ArrayList<Runnable> runnables = this.stateMachine.getAndSet(null);
        if (runnables == null) { return; }
        for (final Runnable run : runnables) {
            this.run(run);
        }
    }

    @Override
    public String toString() {
        return "SingleReachableState: " + this.name + " reached: " + (this.stateMachine.get() != null);
    }
}
