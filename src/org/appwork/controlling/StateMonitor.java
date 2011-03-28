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

/**
 * @author daniel
 * 
 */
public class StateMonitor {

    private final StateMachine machine;

    public StateMonitor(final StateMachine machine) {
        this.machine = machine;
    }

    public boolean executeIfOnState(final Runnable run, final State state) {
        return this.machine.executeIfOnState(run, state);
    }

    public void executeOnceOnState(final Runnable run, final State state) {
        this.machine.executeOnceOnState(run, state);
    }

    public State getState() {
        return this.machine.getState();
    }

    public boolean hasPassed(final State... states) {
        return this.machine.hasPassed(states);
    }

    public boolean isFinal() {
        return this.machine.isFinal();
    }

    public boolean isStartState() {
        return this.machine.isStartState();
    }

    public boolean isState(final State... states) {
        return this.machine.isState(states);
    }
}
