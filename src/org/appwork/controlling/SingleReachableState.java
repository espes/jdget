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

import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public class SingleReachableState {

    private StateMachine stateMachine = null;
    private static State WAITING      = new State("WAITING");
    private static State REACHED      = new State("REACHED");
    static {
        SingleReachableState.WAITING.addChildren(SingleReachableState.REACHED);
    }
    private String       name;

    public SingleReachableState(final String name) {
        this.stateMachine = new StateMachine(new StateMachineInterface() {
            @Override
            public StateMachine getStateMachine() {
                return SingleReachableState.this.stateMachine;
            }

        }, SingleReachableState.WAITING, SingleReachableState.REACHED);
        this.name = name;
    }

    public void executeWhenReached(final Runnable run) {
        if (run == null) { return; }
        boolean runRunnable = true;
        if (this.stateMachine != null) {
            synchronized (this) {
                if (this.stateMachine == null) {
                    runRunnable = true;
                } else {
                    runRunnable = false;
                    this.stateMachine.executeOnceOnState(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                run.run();
                            } catch (final Throwable e) {
                                Log.exception(e);
                            }
                        }

                    }, SingleReachableState.REACHED);
                }
            }
        }
        if (runRunnable) {
            try {
                run.run();
            } catch (final Throwable e) {
                Log.exception(e);
            }
        }
    }

    public synchronized boolean isReached() {
        return this.stateMachine == null;
    }

    public void setReached() {
        if (this.stateMachine == null) { return; }
        synchronized (this) {
            if (this.stateMachine == null) { return; }
            this.stateMachine.setStatus(SingleReachableState.REACHED);
            this.stateMachine = null;
        }
    }

    @Override
    public String toString() {
        return "SingleReachableState: " + this.name + " reached:" + (this.stateMachine == null);
    }
}
